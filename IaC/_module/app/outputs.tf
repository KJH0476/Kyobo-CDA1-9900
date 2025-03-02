output "bastion_instance_id" {
  value       = aws_instance.bastion_instance.id
  description = "Bastion Host Instance ID"
}

output "bastion_public_ip" {
  value       = aws_instance.bastion_instance.public_ip
  description = "Bastion Host Public IP Address"
}

output "ecs_cluster_id" {
  value       = aws_ecs_cluster.ecs_cluster.id
  description = "ECS cluster ID"
}

output "ecs_cluster_name" {
  value       = aws_ecs_cluster.ecs_cluster.name
  description = "ECS cluster name"
}

output "ecs_task_definition_arn" {
  value       = aws_ecs_task_definition.ecs_task_definition.arn
  description = "ECS task definition arn"
}

output "ecs_service_name" {
  value       = aws_ecs_service.ecs_service.name
  description = "ECS service name"
}

output "lambda_function_arn" {
  value       = aws_lambda_function.lambda_function.arn
  description = "Lambda function ARN"
}

output "lambda_function_name" {
  value       = aws_lambda_function.lambda_function.function_name
  description = "Lambda function name"
}

output "lambda_exec_role_arn" {
  value       = aws_iam_role.lambda_exec_role.arn
  description = "Lambda execution role ARN"
}