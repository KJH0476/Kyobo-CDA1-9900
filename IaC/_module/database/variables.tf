variable "aws_region" {
  type        = string
  description = "The AWS region"
}

variable "availability_zones" {
  type        = list(string)
  description = "List of availability zones"
}

variable "ssm_parameters" {
  description = "Map of SSM parameters (name and value) to create"
  type        = map(string)
  default = {
    "secret_variable1" = "superSecretValue1"
    "secret_variable2" = "superSecretValue2"
    "secret_variable3" = "superSecretValue3"
  }
}

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

variable "subnet_ids" {
  type        = list(string)
  description = "List of DB and Cache subnets"
}

variable "user_db_instance_type" {
  type        = string
  description = "Instance type for the user database"
}

variable "user_db_name" {
  type        = string
  description = "Name for the user database"
}

variable "user_db_username" {
  type        = string
  description = "Username for the user database"
}

variable "user_db_password" {
  type        = string
  description = "Password for the user database"
}

variable "resv_db_instance_type" {
  type        = string
  description = "Instance type for the reservation database"
}

variable "resv_db_name" {
  type        = string
  description = "Name for the reservation database"
}

variable "resv_db_username" {
  type        = string
  description = "Username for the reservation database"
}

variable "resv_db_password" {
  type        = string
  description = "Password for the reservation database"
}

variable "resv_db_instance_count" {
  type        = number
  default     = 2
  description = "Reservation DB, Number of instances to launch"
}

variable "user_db_security_group_ids" {
  type        = list(string)
  description = "List of User DB security group IDs"
}

variable "resv_db_security_group_ids" {
  type        = list(string)
  description = "List of Reservation DB security group IDs"
}

variable "cache_port" {
  type        = number
  default     = 6379
  description = "Port for the cache"
}

variable "cache_security_group_ids" {
  type        = list(string)
  description = "List of Cache security group IDs"
}

variable "cache_instance_type" {
  type        = string
  description = "Instance type for the cache"
}

variable "opensearch_security_group_ids" {
  type        = list(string)
  description = "List of OpenSearch security group IDs"
}

variable "opensearch_instance_type" {
  type        = string
  description = "Instance type for the OpenSearch"
}

variable "opensearch_instance_count" {
  type        = number
  default     = 2
  description = "OpenSearch, Number of instances to launch"
}

variable "opensearch_username" {
  type        = string
  description = "Username for the OpenSearch"
}

variable "opensearch_password" {
  type        = string
  description = "Password for the OpenSearch"
}