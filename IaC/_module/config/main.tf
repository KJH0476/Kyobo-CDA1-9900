resource "aws_kms_key" "secret_key" {
  enable_key_rotation     = true
  deletion_window_in_days = 20
  description             = "This key is used to encrypt secrets"
}

resource "aws_kms_key_policy" "secret_key_policy" {
  key_id = aws_kms_key.secret_key.id
  policy = jsonencode({
    Version = "2012-10-17"
    Id      = "${var.environment}-${var.region_prefix}-key-policy"
    Statement = [
      {
        Sid    = "Enable IAM User Permissions"
        Effect = "Allow"
        Principal = {
          AWS = var.key_user_arn
        }
        Action   = "kms:*"
        Resource = "*"
      },
      {
        Sid    = "Allow Parameter Store to use the key"
        Effect = "Allow"
        Principal = {
          Service = "ssm.amazonaws.com"
        }
        Action = [
          "kms:Encrypt",
          "kms:Decrypt",
          "kms:ReEncrypt*",
          "kms:GenerateDataKey*",
          "kms:DescribeKey"
        ]
        Resource = "*"
      }
    ]
  })
}

resource "aws_ssm_parameter" "secret_parameters" {
  for_each = var.ssm_parameters

  name   = "/team9900/${each.key}"
  type   = "SecureString"
  value  = each.value
  key_id = aws_kms_key.secret_key.arn

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

