output "route53_hosted_zone_id" {
  value       = aws_route53_zone.route53_hosted_zone.zone_id
  description = "Route53 Hosted Zone ID"
}
