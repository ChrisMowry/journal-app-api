locals{
  ecs_security_group_name = var.env == "prd" ? "${var.service_name}-ecs-sg" : "${var.service_name}-ecs-sg-${var.env}"
  alb_security_group_name = var.env == "prd" ? "${var.service_name}-alb-sg" : "${var.service_name}-alb-sg-${var.env}"
  vpc_name = var.env == "prd" ? "${var.service_name}-vpc" : "${var.service_name}-vpc-${var.env}"
}

resource "aws_vpc" "ecs_task_vpc" {
  name                 = local.vpc_name
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    service = var.service_name,
    env = var.env
  }
}

resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.ecs_task_vpc.id

  tags = {
    service = var.service_name,
    env = var.env
  }
}

resource "aws_subnet" "ecs_task_subnet" {
  vpc_id                  = aws_vpc.ecs_task_vpc.id
  cidr_block              = var.subnet_cidr
  availability_zone       = data.aws_availability_zones.available.names[0]
  map_public_ip_on_launch = true

  tags = {
    service = var.service_name,
    env = var.env
  }
}

resource "aws_subnet" "ecs_task_subnet2" {
  vpc_id                  = aws_vpc.ecs_task_vpc.id
  cidr_block              = var.subnet2_cidr
  availability_zone       = data.aws_availability_zones.available.names[1]
  map_public_ip_on_launch = true

  tags = {
    service = var.service_name,
    env = var.env
  }
}

resource "aws_route_table" "main" {
  vpc_id = aws_vpc.ecs_task_vpc.id

  route {
    cidr_block      = "0.0.0.0/0"
    gateway_id      = aws_internet_gateway.main.id
  }

  tags = {
    service = var.service_name,
    env = var.env
  }
}

resource "aws_route_table_association" "subnet1" {
  subnet_id      = aws_subnet.ecs_task_subnet.id
  route_table_id = aws_route_table.main.id
}

resource "aws_route_table_association" "subnet2" {
  subnet_id      = aws_subnet.ecs_task_subnet2.id
  route_table_id = aws_route_table.main.id
}

resource "aws_security_group" "ecs_security_group" {
  name = local.ecs_security_group_name
  description = "Security group for ECS tasks"
  vpc_id      = aws_vpc.ecs_task_vpc.id

  ingress {
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb_security_group.id]
  }

  ingress {
    from_port       = 8081
    to_port         = 8081
    protocol        = "tcp"
    security_groups = [aws_security_group.alb_security_group.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    service = var.service_name,
    env = var.env
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_security_group" "alb_security_group" {
  name        = local.alb_security_group_name
  description = "Security group for ALB"
  vpc_id      = aws_vpc.ecs_task_vpc.id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    service = var.service_name,
    env = var.env
  }

  lifecycle {
    create_before_destroy = true
  }
}

data "aws_availability_zones" "available" {
  state = "available"
}

