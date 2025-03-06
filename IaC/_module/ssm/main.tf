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
