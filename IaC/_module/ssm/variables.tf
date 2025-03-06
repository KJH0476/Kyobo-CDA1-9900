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

variable "kms_key_arn" {
  type        = string
  description = "KMS key ARN for SSM parameters"
}