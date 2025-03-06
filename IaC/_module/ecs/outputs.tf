output "ecs_task_definition_arn" {
  value       = aws_ecs_task_definition.ecs_task_definition.arn
  description = "ECS task definition arn"
}

output "ecs_service_name" {
  value       = aws_ecs_service.ecs_service.name
  description = "ECS service name"
}