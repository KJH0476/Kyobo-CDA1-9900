output "app_external_lb_arn" {
  value       = aws_lb.app_external_lb.arn
  description = "ARN of the external Application Load Balancer"
}

output "app_external_lb_name" {
  value       = aws_lb.app_external_lb.name
  description = "Name of the external Application Load Balancer"
}

output "app_external_lb_dns_name" {
  value       = aws_lb.app_external_lb.dns_name
  description = "DNS name of the external ALB (useful for Route53 alias or direct access)"
}

output "app_external_lb_zone_id" {
  value       = aws_lb.app_external_lb.zone_id
  description = "Hosted zone ID of the external ALB (for Route53 alias record)"
}

output "app_external_tg_arn" {
  value       = aws_lb_target_group.app_external_tg.arn
  description = "ARN of the external ALB Target Group"
}

output "app_external_tg_name" {
  value       = aws_lb_target_group.app_external_tg.name
  description = "Name of the external ALB Target Group"
}

output "app_external_listener_80_arn" {
  value       = aws_lb_listener.external_80.arn
  description = "ARN of the external ALB Listener on port 80 (HTTP)"
}

output "app_external_listener_443_arn" {
  value       = aws_lb_listener.external_443.arn
  description = "ARN of the external ALB Listener on port 443 (HTTPS)"
}