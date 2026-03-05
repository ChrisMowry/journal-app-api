variable "service_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "subnet_ids" {
  type = list(string)
}

variable "security_group_ids" {
  type = list(string)
}

variable "alb_security_group_id" {
  type = string
}

variable "ecs_cpu" {
  type    = string
  default = "256"
}

variable "ecs_memory" {
  type    = string
  default = "512"
}

variable "ecs_desired_count" {
  type    = number
  default = 1
}

variable "ecs_min_capacity" {
  type    = number
  default = 1
}

variable "ecs_max_capacity" {
  type    = number
  default = 4
}

variable "container_port" {
  type    = number
  default = 8080
}

variable "health_check_path" {
  type    = string
  default = "/actuator/health"
}

variable "logs_retention_in_days" {
  type    = number
  default = 7
}

variable "dynamodb_table_name" {
  type = string
}

variable "s3_bucket_name" {
  type = string
}

variable "cognito_user_pool_id" {
  type = string
}

variable "aws_region" {
  type = string
}

variable "aws_account_id" {
  type = string
}

