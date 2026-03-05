# Dev Environment Configuration
aws_region   = "us-east-1"
service_name = "journal"

# CloudFront Configuration
cloudfront_encoded_public_key_value = "YOUR_BASE64_ENCODED_PUBLIC_KEY_HERE"
domain_name                         = "dev.example.com"
hosted_zone_id                      = "YOUR_HOSTED_ZONE_ID_HERE"

# ECS Configuration
ecs_cpu            = "256"
ecs_memory         = "512"
ecs_desired_count  = 1
ecs_min_capacity   = 1
ecs_max_capacity   = 4

# CloudWatch
logs_retention_in_days = 7

