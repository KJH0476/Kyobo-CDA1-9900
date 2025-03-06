resource "aws_route53_zone" "route53_hosted_zone" {
  name = var.root_domain_name

  tags = {
    Name = var.root_domain_name
    Environment = var.environment
  }
}
