# User DB (RDS Instance)
output "user_db_endpoint" {
  value       = aws_db_instance.user_db.endpoint
  description = "Endpoint address of the user RDS instance"
}

output "user_db_id" {
  value       = aws_db_instance.user_db.id
  description = "Database name of the user DB instance"
}

# Aurora Cluster (Reservation DB)
output "resv_db_cluster_endpoint" {
  value       = aws_rds_cluster.resv_db_cluster.endpoint
  description = "Endpoint address of the Aurora DB cluster (writer endpoint)"
}

output "resv_db_cluster_reader_endpoint" {
  value       = aws_rds_cluster.resv_db_cluster.reader_endpoint
  description = "Reader endpoint for the Aurora DB cluster"
}

output "resv_db_cluster_id" {
  value       = aws_rds_cluster.resv_db_cluster.id
  description = "Cluster identifier of the Aurora DB cluster"
}

output "resv_db_instance_endpoints" {
  value = [
    for instance in aws_rds_cluster_instance.resv_db_cluster_instances : instance.endpoint
  ]
  description = "List of endpoints for each Aurora DB cluster instance"
}

# ElastiCache (Redis)
output "redis_primary_endpoint" {
  value       = aws_elasticache_replication_group.redis_cluster.primary_endpoint_address
  description = "Primary endpoint address of the Redis replication group"
}

output "redis_configuration_endpoint" {
  value       = aws_elasticache_replication_group.redis_cluster.configuration_endpoint_address
  description = "Configuration endpoint address of the Redis replication group (if applicable)"
}

# OpenSearch
output "opensearch_domain_name" {
  value       = aws_opensearch_domain.opensearch_domain.domain_name
  description = "The name of the OpenSearch domain"
}

output "opensearch_domain_endpoint" {
  value       = aws_opensearch_domain.opensearch_domain.endpoint
  description = "OpenSearch domain endpoint"
}

# DynamoDB
output "restaurant_table_name" {
  value       = aws_dynamodb_table.restaurant_table.name
  description = "DynamoDB Table name for restaurant items"
}

output "restaurant_table_arn" {
  value       = aws_dynamodb_table.restaurant_table.arn
  description = "ARN of the DynamoDB Restaurant table"
}

output "restaurant_table_stream_arn" {
  value       = aws_dynamodb_table.restaurant_table.stream_arn
  description = "DynamoDB Stream ARN (useful for Lambda triggers)"
}