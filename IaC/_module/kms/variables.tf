variable "environment" {
  type        = string
  description = "Environment name"
}

variable "region_prefix" {
  type        = string
  description = "AWS region prefix"
}

variable "account_id" {
  type        = string
  description = "AWS account ID"
}

variable "key_user_arn" {
  type        = string
  description = "IAM user ARN for the KMS key"
}