# State Bucket Region Migration Guide

## Summary

The Terraform state has been configured to use **us-west-2** region instead of us-east-1.

## What Changed

✅ **main.tf**: Backend configured for us-west-2  
✅ **variables.tf**: Default region set to us-west-2  
✅ **dev.tfvars**: Region set to us-west-2  
✅ **prd.tfvars**: Region set to us-west-2  
✅ **TERRAFORM_GUIDE.md**: Setup instructions updated  

## Migration Options

### Option 1: Fresh Start (No Existing State)

If you haven't deployed any infrastructure yet:

```powershell
# Just create the backend resources in us-west-2
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
  --attribute-definitions AttributeName=LockID,AttributeType=S `
  --key-schema AttributeName=LockID,KeyType=HASH `
  --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 `
  --region us-west-2

# Initialize Terraform
terraform init
```

### Option 2: Migrate Existing State (Automated)

If you have existing state in us-east-1 and want to migrate:

```powershell
cd terraform
./migrate-state-region.ps1
```

This script will:
1. Create new S3 bucket and DynamoDB table in us-west-2
2. Copy existing state files from us-east-1 to us-west-2
3. Reinitialize Terraform with new backend
4. Optionally delete old resources in us-east-1

### Option 3: Manual Migration

```powershell
# 1. Create new resources in us-west-2 (see Option 1)

# 2. Copy state files
aws s3 sync `
  s3://journal-app-terraform-state/ `
  s3://journal-app-terraform-state/ `
  --source-region us-east-1 `
  --region us-west-2

# 3. Reinitialize Terraform
cd terraform
terraform init -reconfigure

# 4. Verify workspaces
terraform workspace list

# 5. Test
terraform workspace select dev
terraform plan

# 6. Clean up old resources (optional)
aws s3 rb s3://journal-app-terraform-state --force --region us-east-1
aws dynamodb delete-table --table-name terraform-locks --region us-east-1
```

## Verification

After migration, verify everything works:

```powershell
cd terraform

# List workspaces
terraform workspace list

# Switch to dev
terraform workspace select dev

# Run plan (should show no changes if migrated)
terraform plan -var-file="environments/dev/dev.tfvars"

# Check state location
aws s3 ls s3://journal-app-terraform-state/ --region us-west-2
```

## Important Notes

1. **Bucket Names**: S3 bucket names are globally unique. If `journal-app-terraform-state` already exists in us-east-1 with a different account, you'll need to choose a different name or ensure you're migrating from your own bucket.

2. **State Locking**: The DynamoDB table name `terraform-locks` is region-specific, not global.

3. **No Downtime**: This migration doesn't affect your deployed infrastructure—only where Terraform stores its state files.

4. **Workspace Isolation**: All workspaces (dev, prd) will be migrated together since they share the same state bucket.

5. **Rollback**: If something goes wrong, you can revert by:
   - Changing `main.tf` backend region back to us-east-1
   - Running `terraform init -reconfigure`

## Troubleshooting

### Error: "Error loading state: NoSuchBucket"
- The bucket doesn't exist in us-west-2 yet
- Run the bucket creation commands from Option 1

### Error: "Error acquiring state lock"
- Another Terraform operation is in progress
- Wait for it to complete or check DynamoDB for stuck locks

### Error: "Access Denied"
- Verify your AWS credentials have permissions for us-west-2
- Ensure IAM policies allow operations in the new region

## Why us-west-2?

Common reasons to use us-west-2:
- Lower latency for West Coast users
- Compliance requirements
- Primary region for your application infrastructure
- Cost optimization
- Disaster recovery strategy

## Next Steps

1. ✅ Choose migration option (1, 2, or 3)
2. ✅ Create backend resources in us-west-2
3. ✅ Migrate state (if applicable)
4. ✅ Run `terraform init`
5. ✅ Verify with `terraform plan`
6. ✅ Continue with normal Terraform operations

