# ✅ Region Migration Checklist

## Current Status: Configuration Updated ✅

Your Terraform configuration is now set to use **us-west-2**.

---

## Pre-Deployment Checklist

### 1. Choose Your Scenario

- [ ] **Scenario A**: Fresh start (no existing infrastructure)
- [ ] **Scenario B**: Migrating from us-east-1 (have existing state)
- [ ] **Scenario C**: Need to change bucket name (name conflict)

### 2. Backend Setup (Required for All Scenarios)

#### Scenario A: Fresh Start

```powershell
cd terraform

# Create S3 bucket
aws s3api create-bucket `
  --bucket journal-app-terraform-state `
  --region us-west-2 `
  --create-bucket-configuration LocationConstraint=us-west-2

# Enable versioning
aws s3api put-bucket-versioning `
  --bucket journal-app-terraform-state `
  --versioning-configuration Status=Enabled `
  --region us-west-2

# Enable encryption
aws s3api put-bucket-encryption `
  --bucket journal-app-terraform-state `
  --region us-west-2 `
  --server-side-encryption-configuration '{
    "Rules": [{
      "ApplyServerSideEncryptionByDefault": {"SSEAlgorithm": "AES256"},
      "BucketKeyEnabled": true
    }]
  }'

# Block public access
aws s3api put-public-access-block `
  --bucket journal-app-terraform-state `
  --region us-west-2 `
  --public-access-block-configuration '{
    "BlockPublicAcls": true,
    "IgnorePublicAcls": true,
    "BlockPublicPolicy": true,
    "RestrictPublicBuckets": true
  }'

# Create DynamoDB table
aws dynamodb create-table `
  --table-name terraform-locks `
  --region us-west-2 `
  --attribute-definitions AttributeName=LockID,AttributeType=S `
  --key-schema AttributeName=LockID,KeyType=HASH `
  --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5
```

- [ ] S3 bucket created
- [ ] Versioning enabled
- [ ] Encryption enabled
- [ ] Public access blocked
- [ ] DynamoDB table created

#### Scenario B: Automated Migration

```powershell
cd terraform
./migrate-state-region.ps1
```

- [ ] Migration script executed
- [ ] State files copied
- [ ] Terraform reinitialized
- [ ] Old resources cleaned up (optional)

### 3. Configure Variables

Update these files with your actual values:

#### `terraform/environments/dev/dev.tfvars`

```hcl
aws_region                          = "us-west-2"
service_name                        = "journal"
cloudfront_encoded_public_key_value = "YOUR_BASE64_ENCODED_KEY"
domain_name                         = "dev.yourdomain.com"
hosted_zone_id                      = "Z1XXXXXXXXXXXX"
```

- [ ] CloudFront public key added
- [ ] Domain name configured
- [ ] Hosted Zone ID added

#### `terraform/environments/prd/prd.tfvars`

```hcl
aws_region                          = "us-west-2"
service_name                        = "journal"
cloudfront_encoded_public_key_value = "YOUR_BASE64_ENCODED_KEY"
domain_name                         = "yourdomain.com"
hosted_zone_id                      = "Z1XXXXXXXXXXXX"
```

- [ ] CloudFront public key added
- [ ] Domain name configured
- [ ] Hosted Zone ID added

### 4. Initialize Terraform

```powershell
cd terraform
terraform init
```

- [ ] Terraform initialized successfully
- [ ] No errors reported

### 5. Create Workspaces

```powershell
terraform workspace new dev
terraform workspace new prd
terraform workspace list  # Verify both exist
```

- [ ] Dev workspace created
- [ ] Prd workspace created
- [ ] Can list workspaces

### 6. Validate Configuration

```powershell
# Validate syntax
terraform validate

# Format code
terraform fmt -recursive
```

- [ ] Configuration validated
- [ ] Code formatted

### 7. Test Dev Environment

```powershell
terraform workspace select dev
terraform plan -var-file="environments/dev/dev.tfvars"
```

- [ ] Workspace selected
- [ ] Plan executed without errors
- [ ] Resources look correct

### 8. Test Prd Environment

```powershell
terraform workspace select prd
terraform plan -var-file="environments/prd/prd.tfvars"
```

- [ ] Workspace selected
- [ ] Plan executed without errors
- [ ] Resources look correct

---

## Deployment Checklist

### Dev Deployment

```powershell
./deploy.ps1 dev apply
```

- [ ] Resources created successfully
- [ ] ALB healthy
- [ ] ECS service running
- [ ] CloudFront distribution active

### Production Deployment

```powershell
./deploy.ps1 prd apply
```

- [ ] Resources created successfully
- [ ] ALB healthy
- [ ] ECS service running
- [ ] CloudFront distribution active

---

## Post-Deployment Verification

### Check Outputs

```powershell
./deploy.ps1 dev output
```

Expected outputs:
- [ ] dynamodb_table_name
- [ ] s3_bucket_name
- [ ] cognito_user_pool_id
- [ ] cloudfront_distribution_id
- [ ] cloudfront_domain_name
- [ ] alb_dns_name
- [ ] ecs_cluster_name
- [ ] ecr_repository_url

### Verify Resources in AWS Console

- [ ] VPC created in us-west-2
- [ ] DynamoDB table exists
- [ ] S3 bucket exists
- [ ] Cognito User Pool exists
- [ ] CloudFront distribution deployed
- [ ] ECS Cluster running
- [ ] ALB active and healthy
- [ ] ECR repository created

### Check Application Health

```powershell
# Get ALB DNS from outputs
$albDns = terraform output -raw alb_dns_name

# Test health endpoint
curl "http://$albDns/actuator/health"
```

- [ ] Health check responds
- [ ] Status: UP
- [ ] Application accessible

---

## Troubleshooting

### Issue: "Bucket does not exist"
**Solution**: Run the S3 bucket creation commands from Step 2

### Issue: "Error acquiring state lock"
**Solution**: Check DynamoDB table exists and is accessible in us-west-2

### Issue: "No valid credential sources found"
**Solution**: Configure AWS credentials
```powershell
aws configure
# or
$env:AWS_PROFILE = "your-profile"
```

### Issue: "ACM certificate validation timeout"
**Solution**: Verify Route 53 hosted zone ID is correct and domain exists

### Issue: "ECS task failing health checks"
**Solution**: Check CloudWatch Logs at `/ecs/journal-{environment}`

---

## Quick Reference

### Region Switched From → To
- ❌ us-east-1 (old)
- ✅ us-west-2 (new)

### Key Files Updated
- ✅ terraform/main.tf
- ✅ terraform/variables.tf
- ✅ terraform/environments/dev/dev.tfvars
- ✅ terraform/environments/prd/prd.tfvars
- ✅ terraform/TERRAFORM_GUIDE.md

### Helper Scripts
- `deploy.ps1` - Main deployment script
- `migrate-state-region.ps1` - State migration script

### Documentation
- `TERRAFORM_GUIDE.md` - Complete usage guide
- `REGION_MIGRATION.md` - Migration details
- `MIGRATION_SUMMARY.md` - Quick overview
- `CHECKLIST.md` - This file

---

## Summary

✅ Configuration updated to us-west-2  
⏳ Backend setup needed (run commands above)  
⏳ Variables configuration needed  
⏳ Terraform initialization needed  
⏳ Ready to deploy  

**Next Step**: Complete Section 2 (Backend Setup) above.

