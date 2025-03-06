output "kms_key_id" {
  value       = aws_kms_key.secret_key.id
  description = "The ID of the KMS key used for encrypting secrets"
}

output "kms_key_arn" {
  value       = aws_kms_key.secret_key.arn
  description = "The ARN of the KMS key used for encrypting secrets"
}
