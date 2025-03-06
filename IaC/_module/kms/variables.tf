variable "environment" {
  type        = string
  description = "Environment name"
}

variable "region_prefix" {
  type        = string
  description = "AWS region prefix"
}

variable "ecs_task_execution_role_arn" {
  type        = string
  description = "IAM role ARN for ECS task execution"
}

variable "key_user_arn" {
  type        = string
  description = "IAM user ARN for the KMS key"
}
