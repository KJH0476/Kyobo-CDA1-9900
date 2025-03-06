variable "region_prefix" {
  type        = string
  description = "The AWS region to deploy the VPC"
}

variable "environment" {
  type        = string
  description = "The environment of the VPC"
}

variable "vpc_id" {
  type        = string
  description = "ID of the VPC"
}

variable "public_subnets" {
  type        = list(string)
  description = "List of public subnet IDs"
}

variable "acm_external_ssl_certificate_arn" {
  type        = string
  description = "ARN of the ACM SSL certificate for the external load balancer"
}

variable "lb_security_groups" {
  type        = list(string)
  description = "ID of the security group for the load balancer"
}

variable "healthcheck_port" {
  type        = number
  description = "Port to use for the health check"
}

variable "service_port" {
  type        = number
  description = "Port to use for the tg service"
}

variable "slow_start_time" {
  type        = number
  default     = 30
  description = "Time in seconds to wait before starting to route traffic to a new target"
}

variable "deregistration_delay_time" {
  type        = number
  default     = 300
  description = "Time in seconds to wait before deregistering a target"
}

variable "route53_hosted_zone_id" {
  type        = string
  description = "ID of the Route53 hosted zone"
}

variable "alb_record_name" {
  type        = string
  description = "The record name of the ALB"
}