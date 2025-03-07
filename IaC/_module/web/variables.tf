variable "account_id" {
  type        = string
  description = "AWS account ID"
}

variable "certification_arn_ue1" {
  type        = string
  description = "Certificate ARN for us-east-1"
}

variable "domain_name" {
  type        = string
  description = "Domain name"
}

variable "zone_id" {
  type        = string
  description = "Route53 zone ID"
}

variable "record_type" {
  type        = string
  description = "Route53 Record type"
}

variable "cloudfront_function_path" {
  type        = string
  description = "Path to the CloudFront function"
}