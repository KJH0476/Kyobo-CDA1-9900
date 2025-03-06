resource "aws_s3_bucket" "team9900_hosting" {
  bucket = "${var.account_id}-hosting-bucket"
}

resource "aws_s3_bucket_public_access_block" "team9900_hosting" {
  bucket = aws_s3_bucket.team9900_hosting.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_cors_configuration" "team9900_hosting" {
  bucket = aws_s3_bucket.team9900_hosting.id

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["GET", "HEAD"]
    allowed_origins = ["*"]
    expose_headers  = ["ETag"]
    max_age_seconds = 3000
  }
}

resource "aws_s3_bucket_versioning" "team9900_hosting" {
  bucket = aws_s3_bucket.team9900_hosting.id
  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_policy" "team9900_hosting" {
  bucket = aws_s3_bucket.team9900_hosting.id
  policy = data.aws_iam_policy_document.contents_devart.json
}

data "aws_iam_policy_document" "contents_devart" {
  statement {
    principals {
      type        = "Service"
      identifiers = ["cloudfront.amazonaws.com"]
    }

    condition {
      test     = "StringEquals"
      variable = "AWS:SourceArn"
      values = [
        aws_cloudfront_distribution.team9900_cdn_distribution.arn,
      ]
    }

    actions   = ["s3:GetObject"]
    resources = ["${aws_s3_bucket.team9900_hosting.arn}/*"]
  }
}

resource "aws_cloudfront_origin_access_control" "team9900_hosting_OCI" {
  name                              = "team9900_hosting_OCI"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

# Cloudfront Distribution
resource "aws_cloudfront_distribution" "team9900_cdn_distribution" {
  origin {
    domain_name              = aws_s3_bucket.team9900_hosting.bucket_regional_domain_name
    origin_id                = "team9900_origin"
    origin_access_control_id = aws_cloudfront_origin_access_control.team9900_hosting_OCI.id
  }

  enabled         = true
  is_ipv6_enabled = true
  comment         = "Cloudfront configuration for cdn"
  http_version    = "http2and3"
  aliases = [var.domain_name]

  default_cache_behavior {
    allowed_methods  = ["GET", "HEAD", "OPTIONS"]
    cached_methods   = ["GET", "HEAD"]
    target_origin_id = "devart_origin"
    compress         = true

    forwarded_values {
      query_string = false

      cookies {
        forward = "all"
      }
    }

    viewer_protocol_policy = "redirect-to-https"
    min_ttl                = 0
    default_ttl            = 3600
    max_ttl                = 86400
  }

  viewer_certificate {
    acm_certificate_arn      = aws_acm_certificate.acm_certificate.arn
    minimum_protocol_version = "TLSv1.1_2016"
    ssl_support_method       = "sni-only"
  }

  ordered_cache_behavior {
    path_pattern = "*.gif"

    allowed_methods  = ["GET", "HEAD"]
    cached_methods   = ["GET", "HEAD"]
    target_origin_id = "devart_origin"
    compress         = false

    viewer_protocol_policy = "redirect-to-https"
    min_ttl                = 0
    default_ttl            = 3600
    max_ttl                = 3600

    forwarded_values {
      query_string            = true
      query_string_cache_keys = ["d"]

      cookies {
        forward = "all"
      }
    }
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  custom_error_response {
    error_caching_min_ttl = 5
    error_code            = 404
    response_code         = 404
    response_page_path    = "/404.html"
  }

  custom_error_response {
    error_caching_min_ttl = 5
    error_code            = 500
    response_code         = 500
    response_page_path    = "/500.html"
  }

  custom_error_response {
    error_caching_min_ttl = 5
    error_code            = 502
    response_code         = 502
    response_page_path    = "/500.html"
  }

  tags = {
    Name = var.domain_name
  }
}

# us-east-1 리전에서 만들어야함
resource "aws_acm_certificate" "acm_certificate" {
  domain_name               = var.domain_name
  validation_method         = "DNS"

  lifecycle {
    create_before_destroy = true
  }
}