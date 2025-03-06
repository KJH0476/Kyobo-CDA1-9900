variable "aws_region" {
  type        = string
  description = "The AWS region"
}

variable "region_prefix" {
  type        = string
  description = "The AWS region prefix"
}

variable "assume_role_arn" {
  type        = string
  default     = ""
  description = "The ARN of the role to assume"
}

variable "atlantis_user" {
  type        = string
  default     = "atlantis_user"
  description = "The name of the Atlantis user"
}

variable "environment" {
  type        = string
  description = "The name of the VPC"
}

variable "cidr_numeral" {
  type        = string
  description = "The VPC CIDR numeral (10.x.0.0/16)"
}

variable "availability_zones" {
  type        = list(string)
  description = "The availability zones for the VPC"
}

variable "cidr_numeral_public" {
  type = map(string)
  default = {
    "0" = "0"
    "1" = "16"
    "2" = "32"
  }
  description = "The VPC CIDR numeral for public subnet (10.x.0.0/16)"
}

variable "cidr_numeral_private" {
  type = map(string)
  default = {
    "0" = "80"
    "1" = "96"
    "2" = "112"
  }
  description = "The VPC CIDR numeral for private subnet (10.x.0.0/16)"
}

variable "cidr_numeral_private_db" {
  type = map(string)
  default = {
    "0" = "160"
    "1" = "176"
    "2" = "192"
  }
  description = "The VPC CIDR numeral for private DB subnet (10.x.0.0/16)"
}

variable "my_ip" {
  type        = string
  description = "My IP address"
}

variable "auth_port" {
  type        = number
  default     = 8000
  description = "The port number for the Authorization Service"
}

variable "user_port" {
  type        = number
  default     = 8081
  description = "The port number for the User Service"
}

variable "noti_port" {
  type        = number
  default     = 8082
  description = "The port number for the Notification Service"
}

variable "search_port" {
  type        = number
  default     = 8083
  description = "The port number for the Search Service"
}

variable "resv_port" {
  type        = number
  default     = 8084
  description = "The port number for the Reservation Service"
}

variable "cache_port" {
  type        = number
  default     = 6379
  description = "The port number for the Redis service"
}

variable "rds_port" {
  type        = number
  default     = 5432
  description = "The port number for the RDS (PostgreSQL) instance"
}

# lb 모듈 주입 변수
variable "service_port" {
  type        = number
  description = "The port number for the target group"

}

# common 모듈 주입 변수
variable "root_domain_name" {
  type        = string
  description = "The domain name for the Route 53 Hosted Zone"
}

variable "alb_record_name" {
  type        = string
  description = "The record name for the ALB"
}

# iam 모듈 주입 변수
variable "ssm_prefix" {
  type        = string
  description = "SSM parameter prefix"
}

# db 모듈 주입 변수
variable "cache_instance_type" {
  type        = string
  description = "The cache instance type"
}

variable "opensearch_instance_type" {
  type        = string
  description = "The OpenSearch instance type"
}

variable "opensearch_username" {
  type        = string
  description = "The OpenSearch username"
}

variable "opensearch_password" {
  type        = string
  description = "The OpenSearch password"
}

variable "user_db_instance_type" {
  type        = string
  description = "The user database instance type"
}

variable "user_db_name" {
  type        = string
  description = "The name of the user database"
}

variable "user_db_username" {
  type        = string
  description = "The username for the user database"
}

variable "user_db_password" {
  type        = string
  description = "The password for the user database"
}

variable "resv_db_instance_type" {
  type        = string
  description = "The reservation database instance type"
}

variable "resv_db_name" {
  type        = string
  description = "The name of the reservation database"
}

variable "resv_db_username" {
  type        = string
  description = "The username for the reservation database"
}

variable "resv_db_password" {
  type        = string
  description = "The password for the reservation database"
}

# config 모듈 주입 변수
variable "ses_emails" {
  type        = list(string)
  description = "The list of SES email addresses"
}

variable "ssm_parameters" {
  type        = map(string)
  description = "The parameter store parameters"
}

# bastion 모듈 주입 변수
variable "bastion_instance_type" {
  type        = string
  description = "The bastion instance type"
}

variable "public_key_path" {
  type        = string
  description = "The path to the public key for bastion(ec2)"
}

variable "services" {
  type = map(object({
    container_name        = string
    service_name          = string
    container_port        = number
    ecs_cpu               = number
    ecs_memory            = number
    enable_alb            = bool
    family                = string
    host_port             = number
    image_uri             = string
    log_index             = string
    log_port              = number
    port_name             = string
    dns_name              = string
    discovery_name        = string
    service_desired_count = number
  }))
  description = "Defining ECS and service settings for each service except for authorization service"
}

variable "authorization_service_config" {
  type = object({
    container_name        = string
    service_name          = string
    container_port        = number
    ecs_cpu               = number
    ecs_memory            = number
    enable_alb            = bool
    family                = string
    host_port             = number
    image_uri             = string
    log_index             = string
    log_port              = number
    port_name             = string
    dns_name              = string
    discovery_name        = string
    service_desired_count = number
  })
  description = "Defining ECS and service settings for authorization service"
}
