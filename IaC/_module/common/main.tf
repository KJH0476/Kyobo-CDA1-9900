# ECS Cluster
resource "aws_ecs_cluster" "ecs_cluster" {
  name = "${var.environment}-${var.region_prefix}-ecs-cluster"
}

# ECR
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

# ECS Namespace
resource "aws_service_discovery_http_namespace" "ecs_service_connect_namespace" {
  name        = "${var.environment}-${var.region_prefix}-ecs-namespace"
  description = "HTTP namespace for ECS Service Connect"
}

# ECS Task Execution Role
data "aws_iam_policy_document" "ecs_task_execution_assume_role" {
  statement {
    effect    = "Allow"
    actions   = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "ecs_task_execution" {
  name               = "${var.environment}-${var.region_prefix}-ecs-exec-role"
  assume_role_policy = data.aws_iam_policy_document.ecs_task_execution_assume_role.json
}

# Certificate Manager
resource "aws_acm_certificate" "acm_certificate" {
  domain_name       = var.root_domain_name
  validation_method = "DNS"

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_route53_record" "cert_validation" {
  for_each = {
    for dvo in aws_acm_certificate.acm_certificate.domain_validation_options : dvo.domain_name => dvo
  }
  zone_id = var.route53_zone_id
  name    = each.value.resource_record_name
  type    = each.value.resource_record_type
  ttl     = 300
  records = [each.value.resource_record_value]
}

# SES
resource "aws_ses_email_identity" "email_identities" {
  count = length(var.ses_emails)
  email = var.ses_emails[count.index]
}