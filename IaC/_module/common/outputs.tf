output "ecs_cluster_id" {
  value       = aws_ecs_cluster.ecs_cluster.id
  description = "ECS cluster ID"
}

output "ecs_cluster_name" {
  value       = aws_ecs_cluster.ecs_cluster.name
  description = "ECS cluster name"
}

output "ecs_cluster_namespace" {
  value       = aws_service_discovery_http_namespace.ecs_service_connect_namespace.name
  description = "ECS cluster ARN"
}

output "ecs_task_execution_role_arn" {
  value = aws_iam_role.ecs_task_execution.arn
}

output "ecs_task_execution_role_name" {
  value = aws_iam_role.ecs_task_execution.name
}

output "ecr_repository_url" {
  value       = aws_ecr_repository.ecr_repositories.repository_url
  description = "ECR repository URL"
}

output "acm_certificate_arn" {
  value       = aws_acm_certificate.acm_certificate.arn
  description = "ACM certificate ARN"
}