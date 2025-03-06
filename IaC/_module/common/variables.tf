variable "environment" {
  type        = string
  description = "Environment name"
}

variable "region_prefix" {
  type        = string
  description = "AWS region prefix"
}

variable "root_domain_name" {
  type        = string
  description = "Domain name"
}

variable "route53_zone_id" {
  type        = string
  description = "Route53 zone ID"
}

variable "ses_emails" {
  type = list(string)
  description = "List of SES emails to create"
}