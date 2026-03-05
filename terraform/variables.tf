variable "aws_region" {

}
  default     = "/actuator/health"
  description = "Health check path for ALB"
  type        = string
variable "health_check_path" {

}
  default     = 8080
  description = "Container port for the application"
  type        = number
variable "container_port" {

}
  default     = 7
  description = "CloudWatch logs retention in days"
  type        = number
variable "logs_retention_in_days" {

}
  default     = 4
  description = "Maximum ECS task count for auto-scaling"
  type        = number
variable "ecs_max_capacity" {

}
  default     = 1
  description = "Minimum ECS task count for auto-scaling"
  type        = number
variable "ecs_min_capacity" {

}
  default     = 1
  description = "Desired number of ECS tasks"
  type        = number
variable "ecs_desired_count" {

}
  default     = "512"
  description = "ECS task memory (must be compatible with CPU)"
  type        = string
variable "ecs_memory" {

}
  default     = "256"
  description = "ECS task CPU (256, 512, 1024, 2048, 4096)"
  type        = string
variable "ecs_cpu" {

}
  default     = 5
  description = "DynamoDB write capacity units (only used when BillingMode is PROVISIONED)"
  type        = number
variable "dynamodb_write_capacity" {

}
  default     = 5
  description = "DynamoDB read capacity units (only used when BillingMode is PROVISIONED)"
  type        = number
variable "dynamodb_read_capacity" {

}
  description = "Route 53 Hosted Zone ID for the domain"
  type        = string
variable "hosted_zone_id" {

}
  description = "Custom domain name for CloudFront distribution (e.g., example.com)"
  type        = string
variable "domain_name" {

}
  sensitive   = true
  description = "Base64 encoded CloudFront public key (PEM format, without the header and footer)"
  type        = string
variable "cloudfront_encoded_public_key_value" {

}
  default     = "journal"
  description = "Name of the service"
  type        = string
variable "service_name" {

}
  default     = "us-east-1"
  description = "AWS region"
  type        = string
