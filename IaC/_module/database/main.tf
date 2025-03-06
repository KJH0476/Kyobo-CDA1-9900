resource "aws_db_subnet_group" "db_subnet_group" {
  name       = "${var.environment}-${var.region_prefix}-db-subnet-group"
  subnet_ids = var.subnet_ids

  tags = {
    Name = "${var.environment}-${var.region_prefix}-db-subnet-group"
  }
}

resource "aws_elasticache_subnet_group" "cache_subnet_group" {
  name       = "${var.environment}-${var.region_prefix}-cache-subnet-group"
  subnet_ids = var.subnet_ids

  tags = {
    Name = "${var.environment}-${var.region_prefix}-cache-subnet-group"
  }
}

resource "aws_db_instance" "user_db" {
  identifier             = "${var.environment}-${var.region_prefix}-user-db-instance"
  allocated_storage      = 20                        # 할당할 스토리지 (GB 단위)
  storage_type           = "gp2"                     # 스토리지 타입 (gp2, io1 등)
  engine                 = "postgres"                # 데이터베이스 엔진 (예: mysql, postgres, mariadb 등)
  engine_version         = "16.4"                    # 엔진 버전
  instance_class         = var.user_db_instance_type # 인스턴스 클래스 (용량, 성능에 따라 선택)
  db_name                = var.user_db_name          # 생성할 데이터베이스 이름
  username               = var.user_db_username      # DB 사용자명 (변수 또는 민감 정보를 저장하는 방법 사용 권장)
  password               = var.user_db_password      # DB 비밀번호 (Terraform의 sensitive 변수나 별도 secret 관리 방식 권장)
  db_subnet_group_name   = aws_db_subnet_group.db_subnet_group.name
  vpc_security_group_ids = var.user_db_security_group_ids # RDS에 적용할 보안 그룹 ID 리스트
  multi_az               = true                           # 다중 가용 영역 사용 여부
  skip_final_snapshot    = true                           # 삭제 시 최종 스냅샷 생성 여부, true로 설정 시 스냅샷 생성 안함
  publicly_accessible    = false                          # 퍼블릭 접근 허용 여부 (보안상 false 권장)

  tags = {
    Name = "user-db-instance"
  }
}

resource "aws_rds_cluster" "resv_db_cluster" {
  cluster_identifier     = "${var.environment}-${var.region_prefix}-aurora-resvdb-cluster"
  engine                 = "aurora-postgresql"
  engine_version         = "16.4"
  availability_zones     = var.availability_zones
  database_name          = var.resv_db_name
  master_username        = var.resv_db_username
  master_password        = var.resv_db_password
  db_subnet_group_name   = aws_db_subnet_group.db_subnet_group.name
  skip_final_snapshot    = true
  vpc_security_group_ids = var.resv_db_security_group_ids

  lifecycle {
    ignore_changes = [cluster_identifier, availability_zones]
  }

  tags = {
    Name = "${var.environment}-${var.region_prefix}-aurora-resvdb-cluster"
  }
}

resource "aws_rds_cluster_instance" "resv_db_cluster_instances" {
  count               = var.resv_db_instance_count # 생성할 인스턴스 개수
  identifier          = "${var.environment}-${var.region_prefix}-aurora-resvdb-instance${count.index}"
  cluster_identifier  = aws_rds_cluster.resv_db_cluster.id
  instance_class      = var.resv_db_instance_type # 인스턴스 타입 설정
  engine              = aws_rds_cluster.resv_db_cluster.engine
  engine_version      = aws_rds_cluster.resv_db_cluster.engine_version
  publicly_accessible = false

  lifecycle {
    ignore_changes = [cluster_identifier]
  }

  tags = {
    Name = "${var.environment}-${var.region_prefix}-aurora-resvdb-instance${count.index}"
  }
}

resource "aws_elasticache_replication_group" "redis_cluster" {
  replication_group_id        = "${var.environment}-${var.region_prefix}-redis-cluster" # 고유 식별자 (소문자, 숫자, 하이픈만 사용)
  preferred_cache_cluster_azs = var.availability_zones
  multi_az_enabled            = true
  engine                      = "redis"
  engine_version              = "7.1"                   # 사용하고자 하는 Redis 버전
  node_type                   = var.cache_instance_type # 캐시 노드 인스턴스 타입 (필요에 따라 조정)
  port                        = var.cache_port          # 캐시 포트
  num_cache_clusters          = 2                       # 마스터 노드 + 복제본 수 (총 2개의 노드)
  automatic_failover_enabled  = true                    # 자동 장애 조치 활성화
  subnet_group_name           = aws_elasticache_subnet_group.cache_subnet_group.name
  security_group_ids          = var.cache_security_group_ids # 실제 보안 그룹 ID로 변경
  transit_encryption_enabled  = true                         # 전송 암호화 활성화
  description                 = "Redis replication group"

  tags = {
    Name = "${var.environment}-${var.region_prefix}-redis-cluster"
  }
}

resource "aws_opensearch_domain" "opensearch_domain" {
  domain_name    = "${var.environment}-${var.region_prefix}-opensearch-domain" # 도메인 이름 (소문자, 숫자, 하이픈만 사용)
  engine_version = "Elasticsearch_7.10"                                        # Elasticsearch 엔진 버전 지정

  cluster_config {
    instance_type          = var.opensearch_instance_type  # 인스턴스 타입 (필요에 따라 변경)
    instance_count         = var.opensearch_instance_count # 최소 2개 이상의 노드 권장 (HA 구성)
    zone_awareness_enabled = true

    # 여러 가용영역에 걸쳐 배포할 경우 zone_awareness_config를 추가할 수 있음
    zone_awareness_config {
      availability_zone_count = 2
    }
  }
  # EBS 볼륨 설정
  ebs_options {
    ebs_enabled = true
    volume_size = 10    # 볼륨 크기 (GiB)
    volume_type = "gp2" # 스토리지 타입
  }
  # VPC 내에 배치하기 위한 설정
  vpc_options {
    subnet_ids         = var.subnet_ids
    security_group_ids = var.opensearch_security_group_ids
  }

  # 세분화된 제어정책 설정
  access_policies = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Effect" : "Allow",
        "Principal" : {
          "AWS" : "*"
        },
        "Action" : "es:*",
        "Resource" : "arn:aws:es:${var.aws_region != "" ? var.aws_region : "ap-northeast-2"}:${var.account_id}:domain/${var.environment}-${var.region_prefix}-opensearch-domain/*"
      }
    ]
  })
  # Advanced Security Options (세분화된 접근 제어)
  # 내부 사용자 데이터베이스를 활성화하고 마스터 사용자를 지정하여 OpenSearch Dashboards 및 API에 대해 세부적인 권한 제어가 가능
  advanced_security_options {
    enabled                        = true
    internal_user_database_enabled = true

    master_user_options {
      master_user_name     = var.opensearch_username
      master_user_password = var.opensearch_password
    }
  }
  # 노드 간 암호화 설정, Advanced Security Options를 활성화한 경우 필수
  node_to_node_encryption {
    enabled = true
  }

  encrypt_at_rest {
    enabled = true
  }

  domain_endpoint_options {
    enforce_https       = true
    tls_security_policy = "Policy-Min-TLS-1-2-2019-07"
  }

  tags = {
    Name = "${var.environment}-${var.region_prefix}-opensearch-domain"
  }
}

# DynamoDB 테이블 생성
resource "aws_dynamodb_table" "restaurant_table" {
  name         = "${var.environment}-${var.region_prefix}-dynamodb-RestaurantTable"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "restaurant_id"

  attribute {
    name = "restaurant_id"
    type = "S"
  }

  attribute {
    name = "region"
    type = "S"
  }

  global_secondary_index {
    name            = "RegionIndex"
    hash_key        = "region"
    projection_type = "ALL"
  }

  # DynamoDB Streams 설정
  stream_enabled   = true
  stream_view_type = "NEW_IMAGE"
}