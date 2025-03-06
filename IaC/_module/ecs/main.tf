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

  container_definitions = templatefile("${path.module}/task_definition.tpl.json", {
    container_name  = var.container_name
    image_uri       = var.image_uri
    port_name       = var.port_name
    container_port  = var.container_port
    host_port       = var.host_port
    service_secrets = jsonencode([
      for key, value in var.service_secrets : {
        name      = key
        valueFrom = value
      }
    ])
    aws_region      = var.aws_region
    log_port        = var.log_port
    log_host        = var.log_host
    log_index       = var.log_index
  })
}

# ECS Service 생성
resource "aws_ecs_service" "ecs_service" {
  name            = "${var.environment}-${var.region_prefix}-${var.service_name}"
  cluster         = var.ecs_cluster_id
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
      client_alias {
        dns_name = var.dns_name
        port     = var.container_port
      }
      discovery_name = var.discovery_name
      port_name      = var.port_name
    }
  }

  depends_on = [aws_ecs_task_definition.ecs_task_definition]
}