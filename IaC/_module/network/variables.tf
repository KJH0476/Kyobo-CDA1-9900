variable "region_prefix" {
  type        = string
  description = "The AWS region to deploy the VPC"
}

variable "environment" {
  type        = string
  description = "The environment of the VPC"
}

variable "cidr_numeral" {
  type        = string
  description = "The VPC CIDR numeral (10.x.0.0/16)"
}

variable "availability_zones" {
  type        = list(string)
  description = "A comma-delimited list of availability zones for the VPC."
}

variable "cidr_numeral_public" {
  type = map(string)
  default = {
    "0" = "0"
    "1" = "16"
    "2" = "32"
  }
}

variable "cidr_numeral_private" {
  type = map(string)
  default = {
    "0" = "80"
    "1" = "96"
    "2" = "112"
  }
}

variable "cidr_numeral_private_db" {
  type = map(string)
  default = {
    "0" = "160"
    "1" = "176"
    "2" = "192"
  }
}

variable "my_ip" {
  type        = string
  description = "The IP address of the user"
  default     = "14.52.206.207/32"
}

variable "auth_port" {
  type        = number
  description = "The port number for the Authorization Service"
  default     = 8000
}

variable "user_port" {
  type        = number
  description = "The port number for the User Service"
  default     = 8081
}

variable "noti_port" {
  type        = number
  description = "The port number for the Notification Service"
  default     = 8082
}

variable "search_port" {
  type        = number
  description = "The port number for the Search Service"
  default     = 8083
}

variable "resv_port" {
  type        = number
  description = "The port number for the Reservation Service"
  default     = 8084
}

variable "cache_port" {
  type        = number
  description = "The port number for the Elasticache Redis instance"
  default     = 6379
}

variable "rds_port" {
  type        = number
  description = "The port number for the RDS(PostgreSQL) instance"
  default     = 5432
}
