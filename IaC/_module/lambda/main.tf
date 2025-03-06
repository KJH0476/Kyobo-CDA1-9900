# Lambda 함수 생성
resource "aws_lambda_function" "lambda_function" {
  function_name = "${var.environment}-${var.region_prefix}-migration-es-lambda"
  handler       = "lambda_function.lambda_handler"
  runtime       = "python3.12"

  role = var.lambda_role_arn

  filename         = var.path_lambda_func_file # 배포 패키지(.zip) 경로
  source_code_hash = filebase64sha256(var.path_lambda_func_file)

  vpc_config {
    subnet_ids         = var.app_private_subnets
    security_group_ids = var.lambda_security_groups
  }
}

# Lambda 트리거로 DynamoDB Streams 설정
resource "aws_lambda_event_source_mapping" "dynamodb_streams_mapping" {
  event_source_arn  = var.dynamodb_stream_arn
  function_name     = aws_lambda_function.lambda_function.arn
  starting_position = "LATEST"
  batch_size        = 10
}