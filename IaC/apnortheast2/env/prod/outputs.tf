output "network_vpc_id" {
  value       = module.network.vpc_id
  description = "VPC ID from the network module."
}

output "network_public_subnet_ids" {
  value       = module.network.public_subnet_ids
  description = "Public subnet IDs from the network module."
}

output "network_private_subnet_ids" {
  value       = module.network.private_subnet_ids
  description = "Private subnet IDs from the network module."
}

output "route53_hosted_zone_id" {
  value       = module.route53.route53_hosted_zone_id
  description = "Route53 Hosted Zone ID from the common module."
}

output "common_ecs_cluster_id" {
  value       = module.common.ecs_cluster_id
  description = "ECS Cluster ID from the common module."
}

output "common_ecs_cluster_namespace" {
  value       = module.common.ecs_cluster_namespace
  description = "ECS Service Discovery HTTP Namespace from the common module."
}

output "common_ecs_task_execution_role_arn" {
  value       = module.common.ecs_task_execution_role_arn
  description = "ECS Task Execution Role ARN from the common module."
}

output "common_ecs_task_execution_role_name" {
  value       = module.common.ecs_task_execution_role_name
  description = "ECS Task Execution Role Name from the common module."
}

output "kms_key_id" {
  value       = module.kms.kms_key_id
  description = "KMS Key ID from the KMS module."
}

output "kms_key_arn" {
  value       = module.kms.kms_key_arn
  description = "KMS Key ARN from the KMS module."
}

output "iam_lambda_role_arn" {
  value       = module.iam.lambda_role_arn
  description = "Lambda Role ARN from the IAM module."
}

output "iam_ecs_task_execution_role_arn" {
  value       = module.common.ecs_task_execution_role_arn
  description = "ECS Task Execution Role ARN from the IAM module."
}

output "iam_ecs_task_role_arn" {
  value       = module.iam.ecs_task_role_arn
  description = "ECS Task Role ARN from the IAM module."
}

output "iam_notification_task_role_arn" {
  value       = module.iam.notification_task_role_arn
  description = "Notification Task Role ARN from the IAM module."
}

output "database_user_db_endpoint" {
  value       = module.database.user_db_endpoint
  description = "User Database endpoint from the Database module."
}

output "database_resv_db_instance_endpoints" {
  value       = module.database.resv_db_instance_endpoints
  description = "Reservation Database endpoints from the Database module."
}

output "database_redis_primary_endpoint" {
  value       = module.database.redis_primary_endpoint
  description = "Redis Primary endpoint from the Database module."
}

output "database_opensearch_domain_endpoint" {
  value       = module.database.opensearch_domain_endpoint
  description = "OpenSearch Domain endpoint from the Database module."
}

output "database_restaurant_table_stream_arn" {
  value       = module.database.restaurant_table_stream_arn
  description = "Restaurant Table Stream ARN from the Database module."
}

output "config_ssm_parameter_arns" {
  value       = module.ssm.ssm_parameter_arns
  description = "Map of SSM Parameter ARNs from the Config module."
}

output "config_acm_certificate_arn" {
  value       = module.common.acm_certificate_arn
  description = "ACM Certificate ARN from the Config module."
}

output "load_balancer_dns_name" {
  value       = module.load_balancer.app_external_lb_dns_name
  description = "Application Load Balancer DNS Name from the Load Balancer module."
}

output "load_balancer_app_external_tg_arn" {
  value       = module.load_balancer.app_external_tg_arn
  description = "Application Load Balancer Target Group ARN from the Load Balancer module."
}

output "lambda_function_arn" {
  value       = module.lambda.lambda_function_arn
  description = "Lambda Function ARN from the Lambda module."
}

output "ecs_services" {
  value       = { for key, svc in module.ecs : key => svc.ecs_service_name }
  description = "Map of ECS Service Names from the ECS module."
}

output "ecs_auth_service_name" {
  value       = module.ecs_auth_service.ecs_service_name
  description = "ECS Auth Service Name from the ECS Auth Service module."
}