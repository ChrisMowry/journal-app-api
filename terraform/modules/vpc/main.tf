resource "aws_ec2_vpc" "ecs_task_vpc" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name = "${var.service_name}-vpc-${var.environment}"
  }
}

resource "aws_internet_gateway" "main" {
  vpc_id = aws_ec2_vpc.ecs_task_vpc.id

  tags = {
    Name = "${var.service_name}-igw-${var.environment}"
  }
}

resource "aws_ec2_subnet" "ecs_task_subnet" {
  vpc_id                  = aws_ec2_vpc.ecs_task_vpc.id
  cidr_block              = var.subnet_cidr
  availability_zone       = data.aws_availability_zones.available.names[0]
  map_public_ip_on_launch = true

  tags = {
    Name = "${var.service_name}-subnet-1-${var.environment}"
  }
}

resource "aws_ec2_subnet" "ecs_task_subnet2" {
  vpc_id                  = aws_ec2_vpc.ecs_task_vpc.id
  cidr_block              = var.subnet2_cidr
  availability_zone       = data.aws_availability_zones.available.names[1]
  map_public_ip_on_launch = true

  tags = {
    Name = "${var.service_name}-subnet-2-${var.environment}"
  }
}

resource "aws_route_table" "main" {
  vpc_id = aws_ec2_vpc.ecs_task_vpc.id

  route {
    cidr_block      = "0.0.0.0/0"
    gateway_id      = aws_internet_gateway.main.id
  }

  tags = {
    Name = "${var.service_name}-rt-${var.environment}"
  }
}

resource "aws_route_table_association" "subnet1" {
  subnet_id      = aws_ec2_subnet.ecs_task_subnet.id
  route_table_id = aws_route_table.main.id
}

resource "aws_route_table_association" "subnet2" {
  subnet_id      = aws_ec2_subnet.ecs_task_subnet2.id
  route_table_id = aws_route_table.main.id
}

resource "aws_security_group" "ecs_security_group" {
  name_prefix = "${var.service_name}-ecs-sg-"
  description = "Security group for ECS tasks"
  vpc_id      = aws_ec2_vpc.ecs_task_vpc.id

  ingress {
    from_port       = 8080
    to_port         = 8080
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
    Name = "${var.service_name}-ecs-sg-${var.environment}"
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_security_group" "alb_security_group" {
  name_prefix = "${var.service_name}-alb-sg-"
  description = "Security group for ALB"
  vpc_id      = aws_ec2_vpc.ecs_task_vpc.id

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
    Name = "${var.service_name}-alb-sg-${var.environment}"
  }

  lifecycle {
    create_before_destroy = true
  }
}

data "aws_availability_zones" "available" {
  state = "available"
}

