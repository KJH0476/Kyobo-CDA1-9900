variable "aws_region" {
  type        = string
  description = "AWS region"
}

variable "account_id" {
  type        = string
  description = "AWS account ID"
}

variable "region_prefix" {
  type        = string
  description = "The AWS region to deploy the VPC"
}

variable "environment" {
  type        = string
  description = "The environment of the VPC"
}

variable "ssm_prefix" {
  type        = string
  description = "SSM parameter prefix (e.g., 9900)"
}

variable "kms_key_id" {
  type        = string
  description = "KMS key ID"
}

variable "opensearch_domain" {
  type        = string
  description = "Opensearch domain name"
}

variable "dynamodb_table" {
  type        = string
  description = "DynamoDB table name"
}

variable "lambda_function_name" {
  type        = string
  description = "Lambda function name"
}

variable "ecs_task_execution_role_name" {
  type        = string
  description = "ECS task execution role name"
}