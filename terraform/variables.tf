variable "aws_region" {
  type        = string
  description = "AWS region"
  default     = "us-west-2"
}

variable "service_name" {
  type        = string
  description = "Name of the service"
  default     = "journal-app"
}

variable "cloudfront_encoded_public_key_value" {
  type        = string
  description = "Base64 encoded CloudFront public key (PEM format, without the header and footer)"
  sensitive   = true
}

variable "domain_name" {
  type        = string
  description = "Custom domain name for CloudFront distribution (e.g., example.com)"
}

variable "hosted_zone_id" {
  type        = string
  description = "Route 53 Hosted Zone ID for the domain"
}

variable "ecs_cpu" {
  type        = string
  description = "ECS task CPU (256, 512, 1024, 2048, 4096)"
  default     = "256"
}

variable "ecs_memory" {
  type        = string
  description = "ECS task memory (must be compatible with CPU)"
  default     = "512"
}

variable "ecs_desired_count" {
  type        = number
  description = "Desired number of ECS tasks"
  default     = 1
}

variable "ecs_min_capacity" {
  type        = number
  description = "Minimum ECS task count for auto-scaling"
  default     = 1
}

variable "ecs_max_capacity" {
  type        = number
  description = "Maximum ECS task count for auto-scaling"
  default     = 4
}

variable "logs_retention_in_days" {
  type        = number
  description = "CloudWatch logs retention in days"
  default     = 7
}

variable "container_port" {
  type        = number
  description = "Container port for the application"
  default     = 8080
}

variable "task_admin_port" {
  type        = number
  description = "Admin/management port for health checks"
  default     = 8081
}

variable "health_check_path" {
  type        = string
  description = "Health check path for ALB"
  default     = "/actuator/health"
}
