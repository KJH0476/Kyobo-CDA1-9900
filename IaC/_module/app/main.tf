data "aws_ami" "amazon_linux_2" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["amzn2-ami-hvm-*-x86_64-gp2"]
  }
}

resource "aws_key_pair" "bastion_key_pair" {
  key_name   = "${var.environment}-${var.region_prefix}-${var.key_name}"
  public_key = file(var.public_key_path)

  tags = {
    Name = "${var.environment}-${var.region_prefix}-${var.key_name}"
  }
}

resource "aws_instance" "bastion_instance" {
  ami                    = data.aws_ami.amazon_linux_2.id
  instance_type          = var.instance_type
  key_name               = aws_key_pair.bastion_key_pair.key_name
  subnet_id              = var.public_subnets[0]
  vpc_security_group_ids = var.bastion_security_groups

  tags = {
    Name = "${var.environment}-${var.region_prefix}-bastion-instance"
  }
}

# ---------------------------- ECS ---------------------------- #

resource "aws_ecs_cluster" "ecs_cluster" {
  name = "${var.environment}-${var.region_prefix}-ecs-cluster"
}

# ECS Task Definition 생성 -> module로 분리
# 메인 코드에서 각 태스크별 설정을 간단한 데이터 구조(tfvars 또는 locals)로 정의한 후, for_each를 사용해 모듈을 호출
resource "aws_ecs_task_definition" "ecs_task_definition" {
  family                   = var.family
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.ecs_cpu
  memory                   = var.ecs_memory
  execution_role_arn       = var.execution_role_arn
  task_role_arn            = var.task_role_arn

  container_definitions = templatefile("${path.module}/task_definitions.tpl.json", {
    container_name       = var.container_name
    image_uri            = var.image_uri
    port_name            = var.port_name
    container_port       = var.container_port
    host_port            = var.host_port
    auth_service_secrets = var.service_secrets
    log_aws_region       = var.aws_region
    log_port             = var.log_port
    log_host             = var.log_host
    log_index            = var.log_index
  })
}

# ECS Service 생성
# authorization-service가 가장 마지막에 실행되게 하기 위해 해당 서비스 이외의 서비스만 메인 코드에서 foreach로 호출
# 그 후 authorization-service를 생성 -> depends_on을 사용해 명시적 의존성 추가
resource "aws_ecs_service" "ecs_service" {
  name            = "${var.environment}-${var.region_prefix}-${var.service_name}"
  cluster         = aws_ecs_cluster.ecs_cluster.id
  task_definition = aws_ecs_task_definition.ecs_task_definition.arn
  desired_count   = var.service_desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = var.app_private_subnets
    assign_public_ip = var.assign_public_ip
    security_groups  = var.service_security_groups
  }

  dynamic "load_balancer" {
    for_each = var.enable_alb ? [1] : []
    content {
      target_group_arn = var.lb_target_group_arn
      container_name   = var.container_name
      container_port   = var.container_port
    }
  }

  service_connect_configuration {
    enabled   = true
    namespace = var.namespace
    service {
      discovery_name = var.service_name
      client_alias {
        dns_name = var.dns_name
        port     = var.container_port
      }

      dynamic "port_mapping" {
        for_each = var.enable_server ? [1] : []
        content {
          port     = var.container_port
          protocol = "tcp"
        }
      }
    }
  }
}

# ---------------------------- Lambda ---------------------------- #
data "aws_iam_policy_document" "assume_role" {
  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role" "lambda_exec_role" {
  name               = "iam_for_lambda"
  assume_role_policy = data.aws_iam_policy_document.assume_role.json
}

resource "aws_iam_policy_attachment" "lambda_vpc_access" {
  name       = "lambda_vpc_access_attach"
  roles      = [aws_iam_role.lambda_exec_role.name]
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole"
}

# Lambda 함수 생성
resource "aws_lambda_function" "lambda_function" {
  function_name = "${var.environment}-${var.region_prefix}-migration-es-lambda"
  handler       = "lambda_function.lambda_handler" # zip 파일 내 진입점 (예: lambda_function.py 내 lambda_handler 함수)
  runtime       = "python3.12"

  role = aws_iam_role.lambda_exec_role.arn

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