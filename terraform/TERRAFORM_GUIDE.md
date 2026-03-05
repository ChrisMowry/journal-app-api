# Terraform Migration Guide

## Overview

This Terraform configuration replicates the CloudFormation template for the Journal App API. It uses **Terraform Workspaces** to manage multiple environments (dev, prd).

## Directory Structure

```
terraform/
├── main.tf                 # Provider and backend configuration
├── variables.tf            # Global variables
├── outputs.tf              # Global outputs
├── modules.tf              # Module declarations
├── modules/
│   ├── vpc/               # VPC, subnets, security groups
│   ├── dynamodb/          # DynamoDB table
│   ├── s3/                # S3 bucket with policies
│   ├── cognito/           # Cognito user pool and client
│   ├── cloudfront/        # CloudFront distribution with ACM cert
│   └── fargate/           # ECS Fargate, ALB, ECR
└── environments/
    ├── dev/
    │   └── dev.tfvars     # Dev environment variables
    └── prd/
        └── prd.tfvars     # Production environment variables
```

## Prerequisites

1. **Terraform**: >= 1.0
2. **AWS CLI**: Configured with appropriate credentials
3. **S3 Bucket for State**: Create an S3 bucket for Terraform state (see Backend Setup below)
4. **Domain in Route 53**: Your custom domain must be registered in AWS Route 53
5. **CloudFront Public Key**: Base64-encoded public key (from assets/)

## Backend Setup (One-time)

Before running Terraform, set up S3 backend for state management:

```powershell
# Create S3 bucket for Terraform state
aws s3 mb s3://journal-app-terraform-state --region us-east-1

# Create DynamoDB table for state locking
aws dynamodb create-table `
  --table-name terraform-locks `
  --attribute-definitions AttributeName=LockID,AttributeType=S `
  --key-schema AttributeName=LockID,KeyType=HASH `
  --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 `
  --region us-east-1

# Enable versioning on S3 bucket
aws s3api put-bucket-versioning `
  --bucket journal-app-terraform-state `
  --versioning-configuration Status=Enabled `
  --region us-east-1

# Enable encryption on S3 bucket
aws s3api put-bucket-encryption `
  --bucket journal-app-terraform-state `
  --server-side-encryption-configuration '{
    "Rules": [{
      "ApplyServerSideEncryptionByDefault": {"SSEAlgorithm": "AES256"}
    }]
  }' `
  --region us-east-1
```

## Initialization

Initialize Terraform in the terraform directory:

```powershell
cd terraform
terraform init
```

## Workspace Setup

Terraform workspaces allow you to maintain separate state for dev and prd environments:

```powershell
# Create workspaces
terraform workspace new dev
terraform workspace new prd

# List workspaces
terraform workspace list

# Switch to dev workspace
terraform workspace select dev
```

## Configuration

Update the tfvars files with your values:

### Dev Environment: `environments/dev/dev.tfvars`
```hcl
aws_region                          = "us-east-1"
service_name                        = "journal"
cloudfront_encoded_public_key_value = "YOUR_BASE64_ENCODED_KEY"
domain_name                         = "dev.example.com"
hosted_zone_id                      = "Z1XXXXXXXXXXXX"
ecs_cpu                             = "256"
ecs_memory                          = "512"
ecs_desired_count                   = 1
ecs_min_capacity                    = 1
ecs_max_capacity                    = 4
logs_retention_in_days              = 7
```

### Production Environment: `environments/prd/prd.tfvars`
```hcl
aws_region                          = "us-east-1"
service_name                        = "journal"
cloudfront_encoded_public_key_value = "YOUR_BASE64_ENCODED_KEY"
domain_name                         = "example.com"
hosted_zone_id                      = "Z1XXXXXXXXXXXX"
ecs_cpu                             = "512"
ecs_memory                          = "1024"
ecs_desired_count                   = 2
ecs_min_capacity                    = 2
ecs_max_capacity                    = 4
logs_retention_in_days              = 30
```

## Obtaining the CloudFront Public Key

To encode your CloudFront public key:

```powershell
# Extract the public key content (without header/footer)
$keyContent = Get-Content -Path "assets/cloudfront_public_key.pem" | `
  Select-Object -Skip 1 -SkipLast 1 | `
  Join-String -Separator "`n"

# Base64 encode it
$base64Key = [Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes($keyContent))
Write-Output $base64Key
```

## Deployment

### Plan and Apply for Dev

```powershell
# Switch to dev workspace
terraform workspace select dev

# Plan
terraform plan -var-file="environments/dev/dev.tfvars"

# Apply
terraform apply -var-file="environments/dev/dev.tfvars"

# Output values
terraform output
```

### Plan and Apply for Production

```powershell
# Switch to prd workspace
terraform workspace select prd

# Plan
terraform plan -var-file="environments/prd/prd.tfvars"

# Apply (with auto-approval for CI/CD)
terraform apply -var-file="environments/prd/prd.tfvars"

# Output values
terraform output
```

## Resource Overview

### VPC Module
- VPC with CIDR 10.0.0.0/16
- 2 public subnets for high availability
- Internet Gateway
- Security groups for ALB and ECS

### DynamoDB Module
- PAY_PER_REQUEST billing mode
- Partition key: `pk` (String)
- Sort key: `sk` (String)

### S3 Module
- Private bucket with public access blocked
- CloudFront Origin Access Control (OAC)
- Bucket policy for CloudFront access

### Cognito Module
- User Pool with email-based authentication
- Password policy requirements
- User Pool Client with refresh token support
- MFA disabled by default

### CloudFront Module
- HTTPS-only distribution (redirect from HTTP)
- ACM certificate with DNS validation
- S3 origin with OAC
- Signed URL support via Key Groups
- Route 53 DNS records (apex and www)

### Fargate Module
- ECS Cluster with Container Insights
- Application Load Balancer
- Fargate Launch Type
- Auto-scaling (70% CPU target)
- ECR Repository with lifecycle policies
- CloudWatch Logs integration
- IAM roles for tasks and execution

## Useful Commands

```powershell
# Switch workspaces
terraform workspace select dev
terraform workspace select prd

# View current state
terraform show

# Validate configuration
terraform validate

# Format configuration
terraform fmt -recursive

# Destroy resources
terraform destroy -var-file="environments/dev/dev.tfvars"

# View specific outputs
terraform output alb_dns_name
terraform output ecs_cluster_name
```

## Important Notes

1. **State Management**: Terraform state is stored in S3 with DynamoDB locking enabled
2. **IAM Permissions**: Ensure your AWS credentials have permissions for all services
3. **ACM Validation**: Route 53 must validate the ACM certificate (automatic)
4. **ECR Image**: Push your Docker image to ECR before ECS can pull it
5. **Health Checks**: ALB checks `/actuator/health` endpoint (default for Spring Boot)

## Troubleshooting

### S3 Bucket Name Already Exists
S3 bucket names are globally unique. Update the bucket names in dev/prd tfvars if conflicts occur.

### ACM Certificate Validation Failed
Ensure your Route 53 hosted zone ID is correct and the domain is properly configured.

### ECS Task Not Starting
Check CloudWatch Logs in `/ecs/journal-{environment}` for application errors.

### ALB Health Checks Failing
Verify the Spring Boot application is running on port 8080 and `/actuator/health` is responding.

## Migration from CloudFormation

This Terraform configuration provides equivalent functionality:

| CloudFormation | Terraform Module |
|---|---|
| DynamoDB::Table | dynamodb/main.tf |
| Cognito::UserPool | cognito/main.tf |
| S3::Bucket | s3/main.tf |
| CloudFront::Distribution | cloudfront/main.tf |
| ECS::Cluster | fargate/main.tf |
| ECS::Service | fargate/main.tf |
| ALB | fargate/main.tf |
| ECR::Repository | fargate/main.tf |
| EC2::VPC | vpc/main.tf |

## Next Steps

1. Update tfvars files with your configuration
2. Run `terraform init`
3. Create workspaces: `terraform workspace new dev && terraform workspace new prd`
4. Plan and apply infrastructure
5. Push Docker image to ECR
6. Monitor ECS service for successful deployments

