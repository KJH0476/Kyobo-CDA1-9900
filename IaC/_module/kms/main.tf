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
      },
      {
        "Sid": "AllowECSTaskExecutionRoleDecrypt",
        "Effect": "Allow",
        "Principal": {
          "AWS": "arn:aws:iam::${var.account_id}:role/dev-apn2-ecs-exec-role"
        },
        "Action": "kms:Decrypt",
        "Resource": "*"
      }
    ]
  })
}