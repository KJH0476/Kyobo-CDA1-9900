terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.17.0"
    }
  }
}

provider "aws" {
  region  = "ap-northeast-2"
}

# S3 bucket for backend
resource "aws_s3_bucket" "tfstate" {
  bucket = "${var.team_name}-terraform-tfstate"
  force_destroy = true
}

# DynamoDB for terraform state lock
resource "aws_dynamodb_table" "terraform_state_lock" {
  name           = "${var.team_name}-terraform-lock"
  hash_key       = "LockID"
  billing_mode   = "PAY_PER_REQUEST"

  attribute {
    name = "LockID"
    type = "S"
  }
}

variable "team_name" {
  type    = string
  default = "team9900"
}
