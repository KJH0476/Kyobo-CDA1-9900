output "vpc_id" {
  description = "ID of the created VPC"
  value       = aws_vpc.main.id
}

output "vpc_cidr_block" {
  description = "CIDR block of the VPC"
  value       = aws_vpc.main.cidr_block
}

output "nat_gateway_ids" {
  description = "List of NAT Gateway IDs"
  value       = aws_nat_gateway.nat[*].id
}

output "public_subnet_ids" {
  description = "List of Public Subnet IDs"
  value       = aws_subnet.public[*].id
}

output "private_subnet_ids" {
  description = "List of Private Subnet IDs"
  value       = aws_subnet.private[*].id
}

output "private_db_subnet_ids" {
  description = "List of Private DB Subnet IDs"
  value       = aws_subnet.private_db[*].id
}

output "alb_sg_id" {
  description = "ID of the ALB Security Group"
  value       = aws_security_group.alb_sg.id
}

output "ecs_auth_sg_id" {
  description = "ID of the ECS Auth Service Security Group"
  value       = aws_security_group.ecs_auth_sg.id
}

output "ecs_user_sg_id" {
  description = "ID of the ECS User Service Security Group"
  value       = aws_security_group.ecs_user_sg.id
}

output "ecs_noti_sg_id" {
  description = "ID of the ECS Notification Service Security Group"
  value       = aws_security_group.ecs_noti_sg.id
}

output "ecs_search_sg_id" {
  description = "ID of the ECS Search Service Security Group"
  value       = aws_security_group.ecs_search_sg.id
}

output "ecs_resv_sg_id" {
  description = "ID of the ECS Reservation Service Security Group"
  value       = aws_security_group.ecs_resv_sg.id
}

output "redis_sg_id" {
  description = "ID of the Redis Security Group"
  value       = aws_security_group.redis_sg.id
}

output "user_db_sg_id" {
  description = "ID of the RDS Security Group for USER DB"
  value       = aws_security_group.user_db_sg.id
}

output "resv_db_sg_id" {
  description = "ID of the RDS Security Group for RESERVATION DB"
  value       = aws_security_group.resv_db_sg.id
}

output "bastion_sg_id" {
  description = "ID of the Bastion Host Security Group"
  value       = aws_security_group.bastion_sg.id
}

output "opensearch_sg_id" {
  description = "ID of the OpenSearch Security Group"
  value       = aws_security_group.opensearch_sg.id
}

output "lambda_sg_id" {
  description = "ID of the Lambda Security Group"
  value       = aws_security_group.lambda_sg.id
}