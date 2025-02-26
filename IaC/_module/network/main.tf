# ----- VPC 설정 ----- #
resource "aws_vpc" "main" {
  cidr_block           = "10.${var.cidr_numeral}.0.0/16"
  enable_dns_hostnames = true

  tags = {
    Name = "${var.environment}-${var.region_prefix}-vpc"
  }
}

resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name = "${var.environment}-${var.region_prefix}-igw"
  }
}

resource "aws_nat_gateway" "nat" {
  count         = length(var.availability_zones)
  allocation_id = element(aws_eip.nat.*.id, count.index)
  subnet_id     = element(aws_subnet.public.*.id, count.index)

  lifecycle {
    create_before_destroy = true
  }

  tags = {
    Name = "${var.environment}-${var.region_prefix}-nat-${count.index}"
  }
}

resource "aws_eip" "nat" {
  count  = length(var.availability_zones)
  domain = "vpc"

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_subnet" "public" {
  count  = length(var.availability_zones)
  vpc_id = aws_vpc.main.id

  cidr_block        = "10.${var.cidr_numeral}.${var.cidr_numeral_public[count.index]}.0/24"
  availability_zone = element(var.availability_zones, count.index)

  map_public_ip_on_launch = true

  tags = {
    Name    = "${var.environment}-${var.region_prefix}-public${count.index}"
    Network = "Public"
  }
}

resource "aws_subnet" "private" {
  count  = length(var.availability_zones)
  vpc_id = aws_vpc.main.id

  cidr_block        = "10.${var.cidr_numeral}.${var.cidr_numeral_private[count.index]}.0/24"
  availability_zone = element(var.availability_zones, count.index)

  tags = {
    Name    = "${var.environment}-${var.region_prefix}-private${count.index}"
    Network = "Private"
  }
}

resource "aws_subnet" "private_db" {
  count  = length(var.availability_zones)
  vpc_id = aws_vpc.main.id

  cidr_block        = "10.${var.cidr_numeral}.${var.cidr_numeral_private_db[count.index]}.0/24"
  availability_zone = element(var.availability_zones, count.index)

  tags = {
    Name    = "${var.environment}-${var.region_prefix}-db-private${count.index}"
    Network = "Private"
  }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.igw.id
  }

  tags = {
    Name    = "${var.environment}-${var.region_prefix}-public-rt"
    Network = "Public"
  }
}

resource "aws_route_table_association" "public" {
  count          = length(var.availability_zones)
  subnet_id      = element(aws_subnet.public.*.id, count.index)
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table" "private" {
  count  = length(var.availability_zones)
  vpc_id = aws_vpc.main.id

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = element(aws_nat_gateway.nat.*.id, count.index)
  }

  tags = {
    Name    = "${var.environment}-${var.region_prefix}-private${count.index}-rt"
    Network = "Private"
  }
}

resource "aws_route_table_association" "private" {
  count          = length(var.availability_zones)
  subnet_id      = element(aws_subnet.private.*.id, count.index)
  route_table_id = element(aws_route_table.private.*.id, count.index)
}

resource "aws_route_table" "private_db" {
  count  = length(var.availability_zones)
  vpc_id = aws_vpc.main.id

  tags = {
    Name    = "${var.environment}-${var.region_prefix}-privatedb${count.index}-rt"
    Network = "Private"
  }
}

resource "aws_route_table_association" "private_db" {
  count          = length(var.availability_zones)
  subnet_id      = element(aws_subnet.private_db.*.id, count.index)
  route_table_id = element(aws_route_table.private_db.*.id, count.index)
}

# ----- 보안그룹 설정 ----- #
resource "aws_security_group" "alb_sg" {
  name        = "${var.environment}-${var.region_prefix}-alb-sg"
  description = "ALB security group"

  vpc_id = aws_vpc.main.id

  ingress {
    from_port = 80
    to_port   = 80
    protocol  = "tcp"
    cidr_blocks = [
      "0.0.0.0/0"
    ]
  }

  ingress {
    from_port = 443
    to_port   = 443
    protocol  = "tcp"
    cidr_blocks = [
      "0.0.0.0/0"
    ]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.environment}-${var.region_prefix}-alb-sg"
  }
}

resource "aws_security_group" "ecs_auth_sg" {
  name        = "${var.environment}-${var.region_prefix}-ecs-auth-sg"
  description = "ECS authorization service security group"

  vpc_id = aws_vpc.main.id

  ingress {
    from_port = var.auth_port # 허용할 포트 범위의 시작 값
    to_port   = var.auth_port # 허용할 포트 범위의 끝 값
    protocol  = "tcp"
    security_groups = [
      aws_security_group.alb_sg.id
    ]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1" # 모든 프로토콜
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.environment}-${var.region_prefix}-ecs-auth-sg"
  }
}

resource "aws_security_group" "ecs_user_sg" {
  name        = "${var.environment}-${var.region_prefix}-ecs-user-sg"
  description = "ECS member service security group"

  vpc_id = aws_vpc.main.id

  ingress {
    from_port = var.user_port
    to_port   = var.user_port
    protocol  = "tcp"
    security_groups = [
      aws_security_group.ecs_auth_sg.id
    ]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.environment}-${var.region_prefix}-ecs-user-sg"
  }
}

resource "aws_security_group" "ecs_noti_sg" {
  name        = "${var.environment}-${var.region_prefix}-ecs-noti-sg"
  description = "ECS notification service security group"

  vpc_id = aws_vpc.main.id

  ingress {
    from_port = var.noti_port
    to_port   = var.noti_port
    protocol  = "tcp"
    security_groups = [
      aws_security_group.ecs_auth_sg.id,
      aws_security_group.ecs_resv_sg.id
    ]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.environment}-${var.region_prefix}-ecs-noti-sg"
  }
}

resource "aws_security_group" "ecs_search_sg" {
  name        = "${var.environment}-${var.region_prefix}-ecs-search-sg"
  description = "ECS search service security group"

  vpc_id = aws_vpc.main.id

  ingress {
    from_port = var.search_port
    to_port   = var.search_port
    protocol  = "tcp"
    security_groups = [
      aws_security_group.ecs_auth_sg.id
    ]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.environment}-${var.region_prefix}-ecs-search-sg"
  }
}

resource "aws_security_group" "ecs_resv_sg" {
  name        = "${var.environment}-${var.region_prefix}-ecs-resv-sg"
  description = "ECS reservation service security group"

  vpc_id = aws_vpc.main.id

  ingress {
    from_port = var.resv_port
    to_port   = var.resv_port
    protocol  = "tcp"
    security_groups = [
      aws_security_group.ecs_auth_sg.id
    ]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.environment}-${var.region_prefix}-ecs-resv-sg"
  }
}

resource "aws_security_group" "redis_sg" {
  name        = "${var.environment}-${var.region_prefix}-redis-sg"
  description = "ElastiCache(Redis) security group"

  vpc_id = aws_vpc.main.id

  ingress {
    from_port = var.cache_port
    to_port   = var.cache_port
    protocol  = "tcp"
    security_groups = [
      aws_security_group.ecs_auth_sg.id,
      aws_security_group.ecs_user_sg.id
    ]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.environment}-${var.region_prefix}-redis-sg"
  }
}

resource "aws_security_group" "user_db_sg" {
  name        = "${var.environment}-${var.region_prefix}-userdb-sg"
  description = "RDS security group for USER DB"

  vpc_id = aws_vpc.main.id

  ingress {
    from_port = var.rds_port
    to_port   = var.rds_port
    protocol  = "tcp"
    security_groups = [
      aws_security_group.ecs_user_sg.id,
      aws_security_group.bastion_sg.id
    ]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.environment}-${var.region_prefix}-userdb-sg"
  }
}

resource "aws_security_group" "resv_db_sg" {
  name        = "${var.environment}-${var.region_prefix}-resvdb-sg"
  description = "RDS security group for RESERVATION DB"

  vpc_id = aws_vpc.main.id

  ingress {
    from_port = var.rds_port
    to_port   = var.rds_port
    protocol  = "tcp"
    security_groups = [
      aws_security_group.ecs_resv_sg.id,
      aws_security_group.bastion_sg.id
    ]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.environment}-${var.region_prefix}-resvdb-sg"
  }
}

resource "aws_security_group" "bastion_sg" {
  name        = "${var.environment}-${var.region_prefix}-bastion-sg"
  description = "Bastion host security group"

  vpc_id = aws_vpc.main.id

  ingress {
    from_port = 22
    to_port   = 22
    protocol  = "tcp"
    cidr_blocks = [
      var.my_ip # 내 IP, 변경 필요
    ]
  }

  ingress {
    from_port = 9200
    to_port   = 9200
    protocol  = "tcp"
    cidr_blocks = [
      var.my_ip # 내 IP, 변경 필요
    ]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.environment}-${var.region_prefix}-bastion-sg"
  }
}

resource "aws_security_group" "opensearch_sg" {
  name        = "${var.environment}-${var.region_prefix}-opensearch-sg"
  description = "OpenSearch security group"

  vpc_id = aws_vpc.main.id

  ingress {
    from_port = 443
    to_port   = 443
    protocol  = "tcp"
    security_groups = [
      aws_security_group.ecs_auth_sg.id,
      aws_security_group.ecs_user_sg.id,
      aws_security_group.ecs_noti_sg.id,
      aws_security_group.ecs_search_sg.id,
      aws_security_group.ecs_resv_sg.id,
      aws_security_group.bastion_sg.id,
      aws_security_group.lambda_sg.id
    ]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.environment}-${var.region_prefix}-opensearch-sg"
  }
}

resource "aws_security_group" "lambda_sg" {
  name        = "${var.environment}-${var.region_prefix}-lambda-sg"
  description = "Lambda security group"

  vpc_id = aws_vpc.main.id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.environment}-${var.region_prefix}-lambda-sg"
  }
}