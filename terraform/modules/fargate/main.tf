locals {
  task_name            = var.env == "prd" ? "${var.service_name}-task" : "${var.service_name}-task-${var.env}"
  container_name       = var.env == "prd" ? "${var.service_name}-container" : "${var.service_name}-container-${var.env}"
  cluster_name         = var.env == "prd" ? "${var.service_name}-cluster-${var.env}" : "${var.service_name}-cluster-${var.env}"
  service_name         = var.env == "prd" ? var.service_name : "${var.service_name}-${var.env}"
  scaling_policy_name  = var.env == "prd" ? "${var.service_name}-scaling-policy" : "${var.service_name}-scaling-policy-${var.env}"

  task_definition_parameters = {
    environment_name     = var.env == "prd" ? "prd" : "dev"
    admin_port           = var.task_admin_port
    task_port            = var.task_port
    aws_region           = var.aws_region
    task_name            = local.task_name
    image_name           = "${var.aws_account_id}.dkr.ecr.${var.aws_region}.amazonaws.com/${var.service_name}-${var.env}:latest"
    service_name         = var.service_name
    task_cpu_primary     = var.ecs_cpu
    task_memory_primary  = var.ecs_memory
    dynamodb_table_name  = var.dynamodb_table_name
    s3_bucket_name       = var.s3_bucket_name
    cognito_user_pool_id = var.cognito_user_pool_id
    cloudwatch_log_group = aws_cloudwatch_log_group.main.name
  }
}

# ----------------------
# Application Load Balancer
# ----------------------
resource "aws_lb" "main" {
  name_prefix = "${var.service_name}-${var.env}-alb-"
  internal    = false
  load_balancer_type = "application"
  security_groups    = [var.alb_security_group_id]
  subnets            = var.subnet_ids

  tags = {
    service = var.service_name,
    env     = var.env
  }
}

resource "aws_lb_target_group" "main" {
  name_prefix = "${var.service_name}-${var.env}-tg-"
  port        = var.task_port
  protocol    = "HTTP"
  vpc_id      = var.vpc_id
  target_type = "ip"

  health_check {
    healthy_threshold   = 2
    unhealthy_threshold = 3
    timeout             = 5
    interval            = 30
    port                = tostring(var.task_admin_port)
    path                = var.health_check_path
    matcher             = "200"
  }

  tags = {
    service = var.service_name,
    env     = var.env
  }
}

resource "aws_lb_listener" "main" {
  load_balancer_arn = aws_lb.main.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.main.arn
  }
}

# ----------------------
# CloudWatch Logs
# ----------------------
resource "aws_cloudwatch_log_group" "main" {
  name              = "/ecs/${var.service_name}-${var.env}"
  retention_in_days = var.logs_retention_in_days

  tags = {
    service = var.service_name,
    env     = var.env
  }
}

# ----------------------
# ECS Cluster
# ----------------------
resource "aws_ecs_cluster" "main" {
  name = local.cluster_name

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = {
    service = var.service_name,
    env     = var.env
  }
}

# ----------------------
# IAM Roles
# ----------------------
resource "aws_iam_role" "ecs_task_execution_role" {
  name_prefix = "${var.service_name}-${var.env}-ecs-task-exec-role-"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_role_policy" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role_policy" "ecs_task_execution_ecr" {
  name_prefix = "${var.service_name}-${var.env}-ecs-task-exec-ecr-"
  role        = aws_iam_role.ecs_task_execution_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage",
          "ecr:BatchCheckLayerAvailability"
        ]
        Resource = aws_ecr_repository.main.arn
      },
      {
        Effect = "Allow"
        Action = [
          "ecr:GetAuthorizationToken"
        ]
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_role_policy" "ecs_task_execution_logs" {
  name_prefix = "${var.service_name}-${var.env}-ecs-task-exec-logs-policy-"
  role        = aws_iam_role.ecs_task_execution_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ]
        Resource = "${aws_cloudwatch_log_group.main.arn}:*"
      }
    ]
  })
}

resource "aws_iam_role" "ecs_task_role" {
  name_prefix = "${var.service_name}-${var.env}-ecs-task-role-"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy" "ecs_task_dynamodb" {
  name_prefix = "${var.service_name}-${var.env}-ecs-task-dynamodb-policy-"
  role        = aws_iam_role.ecs_task_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "dynamodb:GetItem",
          "dynamodb:PutItem",
          "dynamodb:UpdateItem",
          "dynamodb:DeleteItem",
          "dynamodb:Query",
          "dynamodb:Scan"
        ]
        Resource = "arn:aws:dynamodb:${var.aws_region}:${var.aws_account_id}:table/${var.dynamodb_table_name}"
      }
    ]
  })
}

resource "aws_iam_role_policy" "ecs_task_s3" {
  name_prefix = "${var.service_name}-${var.env}-ecs-task-s3-policy-"
  role        = aws_iam_role.ecs_task_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject",
          "s3:DeleteObject"
        ]
        Resource = "arn:aws:s3:::${var.s3_bucket_name}/*"
      }
    ]
  })
}

resource "aws_iam_role_policy" "ecs_task_cognito" {
  name_prefix = "${var.service_name}-${var.env}-ecs-task-cognito-policy-"
  role        = aws_iam_role.ecs_task_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "cognito-idp:GetUser",
          "cognito-idp:AdminGetUser"
        ]
        Resource = "arn:aws:cognito-idp:${var.aws_region}:${var.aws_account_id}:userpool/*"
      }
    ]
  })
}

# ----------------------
# ECS Task Definition
# ----------------------
resource "aws_ecs_task_definition" "main" {
  family                   = local.task_name
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.ecs_cpu
  memory                   = var.ecs_memory
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn
  task_role_arn            = aws_iam_role.ecs_task_role.arn

  container_definitions = templatefile("task_definition.json.tpl", local.task_definition_parameters)

  tags = {
    service = var.service_name,
    env     = var.env
  }
}

# ----------------------
# ECS Service
# ----------------------
resource "aws_ecs_service" "main" {
  name            = local.service_name
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.main.arn
  desired_count   = var.ecs_desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = var.subnet_ids
    security_groups  = var.security_group_ids
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.main.arn
    container_name   = local.container_name
    container_port   = var.task_port
  }

  deployment_circuit_breaker {
    enable   = false
    rollback = false
  }

  deployment_configuration {
    maximum_percent            = 200
    minimum_healthy_percent    = 100
  }

  depends_on = [aws_lb_listener.main]

  tags = {
    service = var.service_name,
    env     = var.env
  }
}

# ----------------------
# Auto Scaling
# ----------------------
resource "aws_appautoscaling_target" "ecs_target" {
  max_capacity       = var.ecs_max_capacity
  min_capacity       = var.ecs_min_capacity
  resource_id        = "service/${aws_ecs_cluster.main.name}/${aws_ecs_service.main.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}

resource "aws_appautoscaling_policy" "ecs_policy" {
  name               = local.scaling_policy_name
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.ecs_target.resource_id
  scalable_dimension = aws_appautoscaling_target.ecs_target.scalable_dimension
  service_namespace  = aws_appautoscaling_target.ecs_target.service_namespace

  target_tracking_scaling_policy_configuration {
    target_value = 70.0

    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }

    scale_out_cooldown = 60
    scale_in_cooldown  = 300
  }
}

# ----------------------
# ECR Repository
# ----------------------
resource "aws_ecr_repository" "main" {
  name                 = var.env == "prd" ? "${var.service_name}-ecr-repo" : "${var.service_name}-ecr-repo-${var.env}"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    service = var.service_name,
    env     = var.env
  }
}

resource "aws_ecr_lifecycle_policy" "main" {
  repository = aws_ecr_repository.main.name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Expire untagged images after 7 days"
        selection = {
          tagStatus     = "untagged"
          countType     = "sinceImagePushed"
          countUnit     = "days"
          countNumber   = 7
        }
        action = {
          type = "expire"
        }
      }
    ]
  })
}