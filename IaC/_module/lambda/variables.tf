variable "environment" {
  type        = string
  description = "Environment name"
}

variable "region_prefix" {
  type        = string
  description = "AWS region prefix"
}

variable "app_private_subnets" {
  type        = list(string)
  description = "List of APP private subnet IDs"
}

variable "lambda_security_groups" {
  type        = list(string)
  description = "List of security group IDs for the Lambda function"
}

variable "path_lambda_func_file" {
  type        = string
  description = "Path to the Lambda function file"
}

variable "dynamodb_stream_arn" {
  type        = string
  description = "ARN of the DynamoDB"
}

variable "lambda_role_arn" {
  type        = string
  description = "ARN of the Lambda role"
}
