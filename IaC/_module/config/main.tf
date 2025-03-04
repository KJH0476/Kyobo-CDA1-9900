resource "aws_ssm_parameter" "secret_parameters" {
  for_each = var.ssm_parameters

  name   = "/team9900/${each.key}"
  type   = "SecureString"
  value  = each.value
  key_id = var.kms_key_arn

  tags = {
    environment = var.environment
  }
}

resource "aws_ses_email_identity" "email_identities" {
  count = length(var.ses_emails)
  email = var.ses_emails[count.index]
}

resource "aws_ecr_repository" "ecr_repositories" {
  name                 = "${var.environment}-${var.region_prefix}-ecr-repo"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Name = "${var.environment}-${var.region_prefix}-ecr-repo"
  }
}

resource "aws_acm_certificate" "acm_certificate" {
  domain_name       = var.domain_name
  validation_method = "DNS"

  lifecycle {
    create_before_destroy = true
  }
}

