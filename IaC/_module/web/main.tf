resource "aws_s3_bucket" "hosting_bucket" {
  bucket = "team9900-${var.account_id}-hosting-bucket"
}

resource "aws_s3_bucket_public_access_block" "hosting_bucket_access_block" {
  bucket = aws_s3_bucket.hosting_bucket.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_cors_configuration" "hosting_bucket_cors" {
  bucket = aws_s3_bucket.hosting_bucket.id

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["GET", "HEAD"]
    allowed_origins = ["*"]
    expose_headers  = ["ETag"]
    max_age_seconds = 3000
  }
}

resource "aws_s3_bucket_versioning" "hosting_bucket_versioning" {
  bucket = aws_s3_bucket.hosting_bucket.id
  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_policy" "hosting_bucket_policy" {
  bucket = aws_s3_bucket.hosting_bucket.id
  policy = data.aws_iam_policy_document.iam_policy_document.json
}

data "aws_iam_policy_document" "iam_policy_document" {
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
    resources = ["${aws_s3_bucket.hosting_bucket.arn}/*"]
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
    domain_name              = aws_s3_bucket.hosting_bucket.bucket_regional_domain_name
    origin_id                = "team9900_origin"
    origin_access_control_id = aws_cloudfront_origin_access_control.team9900_hosting_OCI.id
  }

  enabled         = true
  is_ipv6_enabled = true
  comment         = "Cloudfront configuration for cdn"
  http_version    = "http2and3"
  aliases         = [var.domain_name]

  default_cache_behavior {
    allowed_methods       = ["GET", "HEAD", "OPTIONS"]
    cached_methods        = ["GET", "HEAD"]
    target_origin_id      = "team9900_origin"
    compress              = true
    viewer_protocol_policy = "redirect-to-https"
    min_ttl               = 0
    default_ttl           = 3600
    max_ttl               = 86400

    cache_policy_id = aws_cloudfront_cache_policy.custom_policy.id

    function_association {
      event_type   = "viewer-request"
      function_arn = aws_cloudfront_function.cloudfront_function.arn
    }
  }

  viewer_certificate {
    acm_certificate_arn      = var.certification_arn_ue1
    minimum_protocol_version = "TLSv1.1_2016"
    ssl_support_method       = "sni-only"
  }

  ordered_cache_behavior {
    path_pattern          = "*.gif"
    allowed_methods       = ["GET", "HEAD"]
    cached_methods        = ["GET", "HEAD"]
    target_origin_id      = "team9900_origin"
    compress              = false
    viewer_protocol_policy = "redirect-to-https"
    min_ttl               = 0
    default_ttl           = 3600
    max_ttl               = 3600

    cache_policy_id = aws_cloudfront_cache_policy.custom_policy.id
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

resource "aws_cloudfront_cache_policy" "custom_policy" {
  name        = "custom-cache-policy-with-country"
  default_ttl = 3600
  max_ttl     = 86400
  min_ttl     = 0

  parameters_in_cache_key_and_forwarded_to_origin {
    headers_config {
      header_behavior = "whitelist"
      headers {
        items = ["CloudFront-Viewer-Country"]
      }
    }

    cookies_config {
      cookie_behavior = "none"
    }

    query_strings_config {
      query_string_behavior = "none"
    }
  }
}

resource "aws_cloudfront_function" "cloudfront_function" {
  name    = "add_country_header_functions"
  runtime = "cloudfront-js-2.0"
  publish = true
  code    = file(var.cloudfront_function_path)
}