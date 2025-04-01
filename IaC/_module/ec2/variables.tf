variable "aws_region" {
  type        = string
  description = "AWS region"
}

variable "subnets" {
  type        = list(string)
  description = "List of subnet IDs"
}

variable "environment" {
  type        = string
  description = "Environment name"
}

variable "region_prefix" {
  type        = string
  description = "AWS region prefix"
}

variable "key_name" {
  type        = string
  default     = "my-ec2-key"
  description = "Name of the Bastion Instance(EC2) key pair"
}

variable "public_key_path" {
  type        = string
  description = "Path to the public key file"
}

variable "instance_type" {
  type        = string
  description = "Instance type for the Bastion Instance(EC2)"
}

variable "security_groups" {
  type        = list(string)
  description = "List of security group IDs for the Bastion Instance(EC2)"
}

variable "role" {
  type        = string
  description = "Role for the Instance(EC2)"
}