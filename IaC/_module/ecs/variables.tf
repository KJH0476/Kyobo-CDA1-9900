variable "aws_region" {
  type        = string
  description = "AWS region"
}

variable "environment" {
  type        = string
  description = "Environment name"
}

variable "region_prefix" {
  type        = string
  description = "AWS region prefix"
}

variable "family" {
  type        = string
  description = "Family name of the ECS Task Definition"
}

variable "ecs_cluster_id" {
  type = string
  description = "ECS cluster ID"
}

variable "container_name" {
  type        = string
  description = "Name of the container"
}

variable "image_uri" {
  type        = string
  description = "URI of the Container image"
}

variable "port_name" {
  type        = string
  description = "Name of the port"
}

variable "container_port" {
  type        = number
  description = "Port of the container"
}

variable "host_port" {
  type        = number
  description = "Port of the host"
}

variable "ecs_cpu" {
  type        = number
  description = "CPU units for the ECS Task"
}

variable "ecs_memory" {
  type        = number
  description = "Memory for the ECS Task"
}

variable "service_secrets" {
  type        = map(string)
  description = "Map of secrets to be passed to the ECS Task"
}

variable "execution_role_arn" {
  type        = string
  description = "ARN of the ECS Task Execution Role"
}

variable "task_role_arn" {
  type        = string
  description = "ARN of the ECS Task Role"
}

variable "log_port" {
  type        = number
  description = "Port for the log server"
}

variable "log_host" {
  type        = string
  description = "Host for the log server"
}

variable "log_index" {
  type        = string
  description = "Index for the log server"
}

variable "service_name" {
  type        = string
  description = "Name of the ECS Service"
}

variable "service_desired_count" {
  type        = number
  description = "Desired count of the ECS Service"
}

variable "discovery_name" {
  type        = string
  description = "Name of the discovery service"
}

variable "enable_alb" {
  type        = bool
  description = "Enable Load Balancer"
}

variable "namespace" {
  type        = string
  description = "Namespace for the ECS Service"
}

variable "dns_name" {
  type        = string
  description = "DNS name for the ECS Service"
}

variable "lb_target_group_arn" {
  type        = string
  default     = ""
  description = "ARN of the Target Group"
}


variable "app_private_subnets" {
  type        = list(string)
  description = "List of APP private subnet IDs"
}

variable "assign_public_ip" {
  type        = bool
  default     = false
  description = "Assign public IP to the ECS Service"
}

variable "service_security_groups" {
  type        = list(string)
  description = "Service security group IDs"
}

variable "service_depends_on" {
  type        = list(any)
  default     = []
  description = "List of resources that the service depends on"
}