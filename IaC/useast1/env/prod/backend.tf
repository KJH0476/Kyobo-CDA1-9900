terraform {
  required_version = ">= 1.5.7"

  backend "s3" {
    bucket         = "team9900-terraform-tfstate"
    key            = "apnorthe2/dev/terraform.tfstate"
    region         = "ap-northeast-2"
    encrypt        = true
    dynamodb_table = "team9900-terraform-lock"
  }
}