resource "aws_lb" "app_external_lb" {
  name               = "${var.environment}-${var.region_prefix}-app-ext-lb"
  subnets            = var.public_subnets
  internal           = false
  security_groups    = var.lb_security_groups
  load_balancer_type = "application"

  tags = {
    Name = "${var.environment}-${var.region_prefix}-app-ext-lb"
  }
}

resource "aws_lb_target_group" "app_external_tg" {
  name                 = "${var.environment}-${var.region_prefix}-app-ext-tg"
  port                 = var.service_port
  protocol             = "HTTP"
  vpc_id               = var.vpc_id
  target_type          = "ip"
  slow_start           = var.slow_start_time
  deregistration_delay = var.deregistration_delay_time

  health_check {
    interval            = 15
    port                = var.healthcheck_port
    path                = "/health"
    timeout             = 3
    healthy_threshold   = 3
    unhealthy_threshold = 2
    matcher             = "200"
  }

  tags = {
    Name = "${var.environment}-${var.region_prefix}-app-ext-tg"
  }
}

resource "aws_lb_listener" "external_443" {
  load_balancer_arn = aws_lb.app_external_lb.arn
  port              = "443"
  protocol          = "HTTPS"

  certificate_arn = var.acm_external_ssl_certificate_arn

  default_action {
    target_group_arn = aws_lb_target_group.app_external_tg.arn
    type             = "forward"
  }
}

resource "aws_lb_listener" "external_80" {
  load_balancer_arn = aws_lb.app_external_lb.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type = "redirect"

    redirect {
      port     = "443"
      protocol = "HTTPS"
      status_code = "HTTP_301"
    }
  }
}

resource "aws_route53_record" "alb_record" {
  zone_id = var.route53_hosted_zone_id
  name    = var.alb_record_name
  type    = "A"

  alias {
    name                   = aws_lb.app_external_lb.dns_name
    zone_id                = aws_lb.app_external_lb.zone_id
    evaluate_target_health = true
  }
}