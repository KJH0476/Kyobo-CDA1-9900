## (기존) network → iam → db → config → lb → app → web 순서로 생성
## (수정1) network → kms → iam → db → config → bastion(선택) → lb → lambda → ecs_cluster → ecs → web 순서로 생성
## (수정2) network → common → kms → iam → db → config → lb → bastion → lambda → ecs → Web
## (수정2) network, route53(호스트 영역 생성, common 에서 분리) → common → kms → iam → db → config → lb → bastion → lambda → ecs → Web

data "aws_caller_identity" "current_user" {}

module "network" {
  source = "../../../_module/network"

  region_prefix           = var.region_prefix
  environment             = var.environment
  cidr_numeral            = var.cidr_numeral
  availability_zones      = var.availability_zones
  cidr_numeral_public     = var.cidr_numeral_public
  cidr_numeral_private    = var.cidr_numeral_private
  cidr_numeral_private_db = var.cidr_numeral_private_db
  my_ip                   = var.my_ip
  auth_port               = var.auth_port
  user_port               = var.user_port
  noti_port               = var.noti_port
  search_port             = var.search_port
  resv_port               = var.resv_port
  cache_port              = var.cache_port
  rds_port                = var.rds_port
}

module "route53" {
  source = "../../../_module/route53"

  environment      = var.environment
  root_domain_name = var.root_domain_name
}

module "common" {
  source = "../../../_module/common"

  region_prefix         = var.region_prefix
  environment           = var.environment
  root_domain_name      = var.root_domain_name
  route53_zone_id        = module.route53.route53_hosted_zone_id
  ses_emails    = var.ses_emails

  depends_on = [module.route53]
}

module "kms" {
  source = "../../../_module/kms"

  region_prefix               = var.region_prefix
  environment                 = var.environment
  ecs_task_execution_role_arn = module.common.ecs_task_execution_role_arn
  key_user_arn                = data.aws_caller_identity.current_user.arn

  depends_on = [module.common]
}

module "iam" {
  source = "../../../_module/iam"

  aws_region                   = var.aws_region
  account_id                   = data.aws_caller_identity.current_user.account_id
  region_prefix                = var.region_prefix
  environment                  = var.environment
  ssm_prefix                   = var.ssm_prefix
  kms_key_id                   = module.kms.kms_key_id
  ecs_task_execution_role_name = module.common.ecs_task_execution_role_name
  opensearch_domain            = "${var.environment}-${var.region_prefix}-es-domain"
  dynamodb_table               = "${var.environment}-${var.region_prefix}-dynamodb-RestaurantTable"
  lambda_function_name         = "${var.environment}-${var.region_prefix}-migration-es-lambda"

  depends_on = [module.kms]
}

module "database" {
  source = "../../../_module/database"

  region_prefix                 = var.region_prefix
  environment                   = var.environment
  availability_zones            = var.availability_zones
  aws_region                    = var.aws_region
  account_id                    = data.aws_caller_identity.current_user.account_id
  subnet_ids                    = module.network.private_db_subnet_ids
  cache_instance_type           = var.cache_instance_type
  cache_security_group_ids      = [module.network.redis_sg_id]
  opensearch_instance_type      = var.opensearch_instance_type
  opensearch_username           = var.opensearch_username
  opensearch_password           = var.opensearch_password
  opensearch_security_group_ids = [module.network.opensearch_sg_id]
  opensearch_instance_count     = 2
  user_db_instance_type         = var.user_db_instance_type
  user_db_name                  = var.user_db_name
  user_db_username              = var.user_db_username
  user_db_password              = var.user_db_password
  user_db_security_group_ids    = [module.network.user_db_sg_id]
  resv_db_instance_type         = var.resv_db_instance_type
  resv_db_name                  = var.resv_db_name
  resv_db_username              = var.resv_db_username
  resv_db_password              = var.resv_db_password
  resv_db_security_group_ids    = [module.network.resv_db_sg_id]
  resv_db_instance_count        = 2

  depends_on = [module.network]
}

module "ssm" {
  source = "../../../_module/ssm"

  region_prefix = var.region_prefix
  environment   = var.environment
  kms_key_arn   = module.kms.kms_key_arn
  ssm_parameters = merge(
    var.ssm_parameters,
    {
      "USER_DATABASE_URL"             = "jdbc:postgresql://${module.database.user_db_endpoint}/${var.user_db_name}"
      "USER_DATABASE_USERNAME"        = var.user_db_username
      "USER_DATABASE_PASSWORD"        = var.user_db_password
      "RESERVATION_DATABASE_URL"      = "jdbc:postgresql://${module.database.resv_db_instance_endpoints[1]}:5432/${var.resv_db_name}"
      "RESERVATION_DATABASE_USERNAME" = var.resv_db_username
      "RESERVATION_DATABASE_PASSWORD" = var.resv_db_password
      "REDIS_HOST"                    = module.database.redis_primary_endpoint
      "REDIS_PORT"                    = var.cache_port,
      "OPENSEARCH_HOST"               = module.database.opensearch_domain_endpoint
      "OPENSEARCH_REGION"             = var.aws_region
    }
  )

  depends_on = [module.database, module.kms]
}

module "load_balancer" {
  source = "../../../_module/lb"

  acm_external_ssl_certificate_arn = module.common.acm_certificate_arn
  region_prefix                    = var.region_prefix
  environment                      = var.environment
  healthcheck_port                 = var.auth_port
  lb_security_groups               = [module.network.alb_sg_id]
  public_subnets                   = module.network.public_subnet_ids
  service_port                     = var.service_port
  vpc_id                           = module.network.vpc_id
  alb_record_name                  = var.alb_record_name
  route53_hosted_zone_id           = module.route53.route53_hosted_zone_id

  depends_on = [module.network, module.common]
}

module "bastion" {
  source = "../../../_module/ec2"

  aws_region      = var.aws_region
  environment     = var.environment
  instance_type   = var.bastion_instance_type
  public_key_path = var.public_key_path
  subnets         = module.network.public_subnet_ids
  security_groups = [module.network.bastion_sg_id]
  region_prefix   = var.region_prefix
  role            = "bastion"

  depends_on = [module.network]
}

module "lambda" {
  source = "../../../_module/lambda"

  region_prefix          = var.region_prefix
  lambda_role_arn        = module.iam.lambda_role_arn
  environment            = var.environment
  app_private_subnets    = module.network.private_subnet_ids
  lambda_security_groups = [module.network.lambda_sg_id]
  path_lambda_func_file  = "lambda_functions/lambda_function.zip"
  dynamodb_stream_arn    = module.database.restaurant_table_stream_arn

  depends_on = [module.network, module.database]
}

module "ecs" {
  for_each = var.services
  source   = "../../../_module/ecs"

  region_prefix       = var.region_prefix
  environment         = var.environment
  app_private_subnets = module.network.private_subnet_ids
  aws_region          = var.aws_region

  container_name          = each.value.container_name
  container_port          = each.value.container_port
  dns_name                = each.value.dns_name
  ecs_cluster_id          = module.common.ecs_cluster_id
  ecs_cpu                 = each.value.ecs_cpu
  ecs_memory              = each.value.ecs_memory
  enable_alb              = each.value.enable_alb
  service_secrets         = local.service_secrets[each.key]
  execution_role_arn      = module.common.ecs_task_execution_role_arn
  family                  = each.value.family
  host_port               = each.value.host_port
  image_uri               = each.value.image_uri
  log_host                = module.database.opensearch_domain_endpoint
  log_index               = each.value.log_index
  log_port                = each.value.log_port
  namespace               = module.common.ecs_cluster_namespace
  port_name               = each.value.port_name
  service_name            = each.value.service_name
  discovery_name          = each.value.discovery_name
  service_desired_count   = each.value.service_desired_count
  service_security_groups = [local.service_security_groups[each.key]]
  task_role_arn           = local.service_task_roles[each.key]
  lb_target_group_arn     = module.load_balancer.app_external_tg_arn

  depends_on = [module.common, module.ssm, module.load_balancer]
}

# 서비스 커넥트 설정을 위해 authorization-service 를 가장 마지막에 생성
module "ecs_auth_service" {
  source = "../../../_module/ecs"

  region_prefix       = var.region_prefix
  environment         = var.environment
  app_private_subnets = module.network.private_subnet_ids
  aws_region          = var.aws_region

  container_name          = var.authorization_service_config.container_name
  container_port          = var.authorization_service_config.container_port
  dns_name                = var.authorization_service_config.dns_name
  ecs_cluster_id          = module.common.ecs_cluster_id
  ecs_cpu                 = var.authorization_service_config.ecs_cpu
  ecs_memory              = var.authorization_service_config.ecs_memory
  enable_alb              = var.authorization_service_config.enable_alb
  service_secrets         = local.service_secrets["authorization_service"]
  execution_role_arn      = module.common.ecs_task_execution_role_arn
  family                  = var.authorization_service_config.family
  host_port               = var.authorization_service_config.host_port
  image_uri               = var.authorization_service_config.image_uri
  log_host                = module.database.opensearch_domain_endpoint
  log_index               = var.authorization_service_config.log_index
  log_port                = var.authorization_service_config.log_port
  namespace               = module.common.ecs_cluster_namespace
  port_name               = var.authorization_service_config.port_name
  service_name            = var.authorization_service_config.service_name
  discovery_name          = var.authorization_service_config.discovery_name
  service_desired_count   = var.authorization_service_config.service_desired_count
  service_security_groups = [local.service_security_groups["authorization_service"]]
  task_role_arn           = local.service_task_roles["authorization_service"]
  lb_target_group_arn     = module.load_balancer.app_external_tg_arn

  depends_on = [module.ecs]
}

# 로컬 변수 정의
locals {
  service_security_groups = {
    authorization_service = module.network.ecs_auth_sg_id
    user_service          = module.network.ecs_user_sg_id
    search_service        = module.network.ecs_search_sg_id
    notification_service  = module.network.ecs_noti_sg_id
    reservation_service   = module.network.ecs_resv_sg_id
  }

  service_task_roles = {
    authorization_service = module.iam.ecs_task_role_arn
    user_service          = module.iam.ecs_task_role_arn
    search_service        = module.iam.ecs_task_role_arn
    notification_service  = module.iam.notification_task_role_arn
    reservation_service   = module.iam.ecs_task_role_arn
  }

  service_secrets = {
    authorization_service = {
      JWT_SECRET_KEY          = module.ssm.ssm_parameter_arns["JWT_SECRET_KEY"]
      JWT_ACCESS_EXPIRE_TIME  = module.ssm.ssm_parameter_arns["JWT_ACCESS_EXPIRE_TIME"]
      JWT_REFRESH_EXPIRE_TIME = module.ssm.ssm_parameter_arns["JWT_REFRESH_EXPIRE_TIME"]
      REDIS_HOST              = module.ssm.ssm_parameter_arns["REDIS_HOST"]
      REDIS_PORT              = module.ssm.ssm_parameter_arns["REDIS_PORT"]
      USER_SERVICE_URI        = module.ssm.ssm_parameter_arns["USER_SERVICE_URI"]
      SEARCH_SERVICE_URI      = module.ssm.ssm_parameter_arns["SEARCH_SERVICE_URI"]
      RESERVATION_SERVICE_URI = module.ssm.ssm_parameter_arns["RESERVATION_SERVICE_URI"]
    }
    user_service = {
      JWT_SECRET_KEY          = module.ssm.ssm_parameter_arns["JWT_SECRET_KEY"]
      JWT_SIGNUP_SECRET_KEY   = module.ssm.ssm_parameter_arns["JWT_SIGNUP_SECRET_KEY"]
      JWT_ACCESS_EXPIRE_TIME  = module.ssm.ssm_parameter_arns["JWT_ACCESS_EXPIRE_TIME"]
      JWT_REFRESH_EXPIRE_TIME = module.ssm.ssm_parameter_arns["JWT_REFRESH_EXPIRE_TIME"]
      USER_DATABASE_URL       = module.ssm.ssm_parameter_arns["USER_DATABASE_URL"]
      USER_DATABASE_USERNAME  = module.ssm.ssm_parameter_arns["USER_DATABASE_USERNAME"]
      USER_DATABASE_PASSWORD  = module.ssm.ssm_parameter_arns["USER_DATABASE_PASSWORD"]
      REDIS_HOST              = module.ssm.ssm_parameter_arns["REDIS_HOST"]
      REDIS_PORT              = module.ssm.ssm_parameter_arns["REDIS_PORT"]
    }
    notification_service = {
      AWS_SES_SENDER = module.ssm.ssm_parameter_arns["AWS_SES_SENDER"]
    }
    search_service = {
      OPENSEARCH_INDEX  = module.ssm.ssm_parameter_arns["OPENSEARCH_INDEX"]
      OPENSEARCH_HOST   = module.ssm.ssm_parameter_arns["OPENSEARCH_HOST"]
      OPENSEARCH_REGION = module.ssm.ssm_parameter_arns["OPENSEARCH_REGION"]
    }
    reservation_service = {
      NOTIFICATION_SERVICE_URI      = module.ssm.ssm_parameter_arns["NOTIFICATION_SERVICE_URI"]
      RESERVATION_DATABASE_URL      = module.ssm.ssm_parameter_arns["RESERVATION_DATABASE_URL"]
      RESERVATION_DATABASE_USERNAME = module.ssm.ssm_parameter_arns["RESERVATION_DATABASE_USERNAME"]
      RESERVATION_DATABASE_PASSWORD = module.ssm.ssm_parameter_arns["RESERVATION_DATABASE_PASSWORD"]
    }
  }
}