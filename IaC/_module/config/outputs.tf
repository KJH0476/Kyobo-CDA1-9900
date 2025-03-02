output "kms_key_id" {
  value       = aws_kms_key.secret_key.id
  description = "The ID of the KMS key used for encrypting secrets"
}

output "kms_key_arn" {
  value       = aws_kms_key.secret_key.arn
  description = "The ARN of the KMS key used for encrypting secrets"
}

output "ssm_parameter_arns" {
  value = {
    for key, param in aws_ssm_parameter.secret_parameters : key => param.arn
  }
  description = "Map of SSM parameter keys to their ARNs for decryption usage"
}

output "acm_certificate_arn" {
  value       = aws_acm_certificate.acm_certificate.arn
  description = "ARN of the ACM certificate"
}