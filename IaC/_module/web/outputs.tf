output "s3_bucket_name" {
  value       = aws_s3_bucket.team9900_hosting.bucket
  description = "Name of the S3 hosting bucket"
}

output "s3_bucket_arn" {
  value       = aws_s3_bucket.team9900_hosting.arn
  description = "ARN of the S3 hosting bucket"
}

output "cloudfront_distribution_id" {
  value       = aws_cloudfront_distribution.team9900_cdn_distribution.id
  description = "ID of the CloudFront distribution"
}

output "cloudfront_domain_name" {
  value       = aws_cloudfront_distribution.team9900_cdn_distribution.domain_name
  description = "Domain name of the CloudFront distribution"
}