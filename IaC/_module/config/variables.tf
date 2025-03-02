variable "environment" {
  type        = string
  description = "Environment name"
}

variable "region_prefix" {
  type        = string
  description = "AWS region prefix"
}

variable "ssm_parameters" {
  description = "Map of SSM parameters (name and value) to create"
  type        = map(string)
}

variable "ses_emails" {
  type = list(string)
  default = [
    "sender1@example.com",
    "sender2@example.com",
    "sender3@example.com"
  ]
  description = "List of SES emails to create"
}

variable "account_id" {
  type        = string
  description = "AWS account ID"
}

variable "key_user_arn" {
  type        = string
  description = "IAM user ARN for the KMS key"
}

variable "domain_name" {
  type        = string
  description = "Domain name for the ACM certificate"
}