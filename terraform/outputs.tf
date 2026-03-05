output "dynamodb_table_name" {
  value       = module.dynamodb.table_name
  description = "Name of the DynamoDB table"
}

output "s3_bucket_name" {
  value       = module.s3.bucket_name
  description = "Name of the S3 bucket for photos"
}

output "cognito_user_pool_id" {
  value       = module.cognito.user_pool_id
  description = "Cognito User Pool ID"
}

output "cognito_user_pool_client_id" {
  value       = module.cognito.user_pool_client_id
  description = "Cognito User Pool Client ID"
  sensitive   = true
}

output "cloudfront_distribution_id" {
  value       = module.cloudfront.distribution_id
  description = "CloudFront distribution ID"
}

output "cloudfront_domain_name" {
  value       = module.cloudfront.distribution_domain_name
  description = "CloudFront distribution domain name"
}

output "alb_dns_name" {
  value       = module.fargate.alb_dns_name
  description = "ALB DNS name"
}

output "ecs_cluster_name" {
  value       = module.fargate.ecs_cluster_name
  description = "ECS cluster name"
}

output "ecs_service_name" {
  value       = module.fargate.ecs_service_name
  description = "ECS service name"
}

output "ecr_repository_url" {
  value       = module.fargate.ecr_repository_url
  description = "ECR repository URL"
}

output "vpc_id" {
  value       = module.vpc.vpc_id
  description = "VPC ID"
}

