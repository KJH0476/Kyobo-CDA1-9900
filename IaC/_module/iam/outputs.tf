output "ecs_task_execution_role_arn" {
  value       = aws_iam_role.ecs_task_execution.arn
  description = "ARN of the ECS Task Execution Role"
}

output "notification_task_role_arn" {
  value       = aws_iam_role.notification_task.arn
  description = "ARN of the Notification Task Role"
}

output "ecs_task_role_arn" {
  value       = aws_iam_role.ecs_task.arn
  description = "ARN of the ECS Task Role"
}

output "bastion_es_role_arn" {
  value       = aws_iam_role.bastion_es.arn
  description = "ARN of the Bastion Instance Role"
}

output "lambda_role_arn" {
  value       = aws_iam_role.lambda_role.arn
  description = "ARN of the Lambda Role"
}