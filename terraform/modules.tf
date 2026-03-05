data "aws_caller_identity" "current" {}

module "vpc" {
  source = "./modules/vpc"

  service_name = var.service_name
  environment  = local.env
}

module "dynamodb" {
  source = "./modules/dynamodb"

  service_name   = var.service_name
  environment    = local.env
  read_capacity  = var.dynamodb_read_capacity
  write_capacity = var.dynamodb_write_capacity
}

module "s3" {
  source = "./modules/s3"

  service_name              = var.service_name
  environment               = local.env
  cloudfront_distribution_id = module.cloudfront.distribution_id
  aws_account_id            = data.aws_caller_identity.current.account_id

  depends_on = [module.cloudfront]
}

module "cognito" {
  source = "./modules/cognito"

  service_name = var.service_name
  environment  = local.env
}

module "cloudfront" {
  source = "./modules/cloudfront"

  service_name                          = var.service_name
  environment                           = local.env
  s3_bucket_regional_domain_name        = aws_s3_bucket.temp_bucket.regional_domain_name
  s3_bucket_arn                         = aws_s3_bucket.temp_bucket.arn
  cloudfront_encoded_public_key_value   = var.cloudfront_encoded_public_key_value
  domain_name                           = var.domain_name
  hosted_zone_id                        = var.hosted_zone_id

  depends_on = [aws_s3_bucket.temp_bucket]
}

module "fargate" {
  source = "./modules/fargate"

  service_name              = var.service_name
  environment               = local.env
  vpc_id                    = module.vpc.vpc_id
  subnet_ids                = [module.vpc.subnet_id, module.vpc.subnet2_id]
  security_group_ids        = [module.vpc.ecs_security_group_id]
  alb_security_group_id     = module.vpc.alb_security_group_id
  ecs_cpu                   = var.ecs_cpu
  ecs_memory                = var.ecs_memory
  ecs_desired_count         = var.ecs_desired_count
  ecs_min_capacity          = var.ecs_min_capacity
  ecs_max_capacity          = var.ecs_max_capacity
  container_port            = var.container_port
  health_check_path         = var.health_check_path
  logs_retention_in_days    = var.logs_retention_in_days
  dynamodb_table_name       = module.dynamodb.table_name
  s3_bucket_name            = module.s3.bucket_name
  cognito_user_pool_id      = module.cognito.user_pool_id
  aws_region                = var.aws_region
  aws_account_id            = data.aws_caller_identity.current.account_id
}

# Temporary bucket for CloudFront dependency
resource "aws_s3_bucket" "temp_bucket" {
  bucket = "${var.service_name}-temp-cf-${local.env}-${data.aws_caller_identity.current.account_id}"

  tags = {
    Name = "Temporary bucket for CloudFront"
  }
}

