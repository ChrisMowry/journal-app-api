# Docker & ECR Deployment Guide

This guide covers building and pushing your Spring Boot application to AWS ECR (Elastic Container Registry) and deploying it to Fargate.

## Quick Start

### Windows (PowerShell)

```powershell
# 1. Build and push to ECR
.\deploy-to-ecr.ps1 dev

# 2. Deploy to Fargate (optional - auto-updates when image is pushed)
.\deploy-to-ecs.ps1 dev
```

### Linux/Mac (Bash)

```bash
# 1. Build and push to ECR
./deploy-to-ecr.sh dev

# 2. Deploy to Fargate (optional - auto-updates when image is pushed)
./deploy-to-ecs.sh dev
```

## Prerequisites

Before running deployment scripts, ensure you have:

1. **AWS CLI** installed and configured
   ```bash
   aws --version
   aws sts get-caller-identity  # Verify credentials
   ```

2. **Docker** installed and running
   ```bash
   docker --version
   docker ps  # Verify Docker daemon is running
   ```

3. **Maven** (included as `mvnw` in this project)
   ```bash
   ./mvnw --version
   ```

4. **AWS Account** with:
   - ECR repository named `journal-app-api` (created by CloudFormation)
   - ECS cluster and service (created by CloudFormation)
   - Appropriate IAM permissions

## What Each Script Does

### deploy-to-ecr.ps1 / deploy-to-ecr.sh
Builds your Spring Boot application and pushes the Docker image to ECR:

1. ✓ Builds the JAR file: `./mvnw clean package -DskipTests`
2. ✓ Builds Docker image locally
3. ✓ Authenticates with ECR
4. ✓ Tags image with `latest` and timestamp
5. ✓ Pushes both tags to ECR
6. ✓ Verifies image in ECR

**Usage:**
```powershell
.\deploy-to-ecr.ps1 dev    # Deploy to dev environment
.\deploy-to-ecr.ps1 prd    # Deploy to production
```

**Output includes:**
- ECR image URI
- Timestamp tag for version tracking
- Next steps for deploying to Fargate

### deploy-to-ecs.ps1 / deploy-to-ecs.sh
Triggers a new deployment in ECS Fargate. The service will:
- Pull the latest image from ECR
- Stop old tasks
- Start new tasks with updated image
- Use health checks to validate rollout

**Usage:**
```powershell
.\deploy-to-ecs.ps1 dev    # Deploy to dev environment
.\deploy-to-ecs.ps1 prd    # Deploy to production
```

**Note:** The ECS service automatically pulls the `:latest` tag, so pushing to ECR usually triggers automatic updates if your service is configured correctly.

## Full Deployment Workflow

### First Time Setup

```powershell
# 1. Verify your CloudFormation stack created the resources
aws cloudformation describe-stack-resources `
  --stack-name journal-api-stack `
  --region us-east-1

# 2. Build and push Docker image
.\deploy-to-ecr.ps1 dev

# 3. (Optional) Manually trigger deployment
.\deploy-to-ecs.ps1 dev

# 4. Monitor deployment in AWS Console
# Open: https://console.aws.amazon.com/ecs/v2/clusters/journal-app-api-cluster-dev
```

### Subsequent Updates

```powershell
# Just run this - it builds and pushes everything
.\deploy-to-ecr.ps1 dev

# ECS service will automatically detect the new image and deploy it
# If automatic deployment isn't working, manually trigger:
.\deploy-to-ecs.ps1 dev
```

## Troubleshooting

### "AWS CLI not found" or "aws: not found"
- Install AWS CLI: https://aws.amazon.com/cli/
- On Windows: Use `aws` command in PowerShell/CMD after installation

### "Docker daemon is not running"
- Start Docker Desktop
- Verify with: `docker ps`

### "access denied when pulling" from ECR
Check your AWS credentials:
```powershell
aws sts get-caller-identity
```

If credentials are wrong, reconfigure:
```powershell
aws configure
```

### "repository name not found" in ECR
The CloudFormation template should have created `journal-app-api` repository. Verify:
```powershell
aws ecr describe-repositories `
  --repository-names journal-app-api `
  --region us-east-1
```

If missing, create it:
```powershell
aws ecr create-repository `
  --repository-name journal-app-api `
  --image-scanning-configuration scanOnPush=true
```

### Maven build fails
```powershell
# Clean build
./mvnw clean

# Full build with tests
./mvnw clean package

# Skip tests (faster)
./mvnw clean package -DskipTests
```

### Docker build fails
```powershell
# Check Dockerfile exists
cat Dockerfile

# Build with verbose output
docker build -t journal-app-api:latest . --progress=plain

# Clean Docker cache if needed
docker system prune
```

### ECS deployment stuck
Check logs in CloudWatch:
```powershell
aws logs tail /ecs/journal-app-api-dev --follow
```

View deployment status:
```powershell
aws ecs describe-services `
  --cluster journal-app-api-cluster-dev `
  --services journal-app-api-service-dev `
  --region us-east-1
```

## Manual Step-by-Step (if scripts don't work)

### Step 1: Build the JAR
```powershell
./mvnw clean package -DskipTests
```

### Step 2: Get your AWS Account ID
```powershell
$ACCOUNT_ID = aws sts get-caller-identity --query Account --output text
```

### Step 3: Login to ECR
```powershell
$TOKEN = aws ecr get-login-password --region us-east-1
$TOKEN | docker login --username AWS --password-stdin "$ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com"
```

### Step 4: Build Docker image
```powershell
docker build -t journal-app-api:latest .
```

### Step 5: Tag for ECR
```powershell
docker tag journal-app-api:latest "$ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/journal-app-api:latest"
```

### Step 6: Push to ECR
```powershell
docker push "$ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/journal-app-api:latest"
```

### Step 7: Verify
```powershell
aws ecr describe-images --repository-name journal-app-api --region us-east-1
```

## Environment Variables (passed to ECS tasks)

Your Docker container receives these environment variables from ECS Task Definition:

```
AWS_REGION = us-east-1
SPRING_PROFILES_ACTIVE = dev (or prd)
DYNAMODB_TABLE_NAME = journal-app-api-data-dev
S3_BUCKET_NAME = journal-app-api-photos-dev
COGNITO_USER_POOL_ID = <dynamically set>
```

Update your Spring Boot `application.yml` or `application-dev.yml` to use these:

```yaml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

aws:
  region: ${AWS_REGION:us-east-1}
  
dynamodb:
  table-name: ${DYNAMODB_TABLE_NAME:journal-app-api-data-dev}
  
s3:
  bucket-name: ${S3_BUCKET_NAME:journal-app-api-photos-dev}
  
cognito:
  user-pool-id: ${COGNITO_USER_POOL_ID}
```

## CloudWatch Logs

Your application logs are sent to CloudWatch automatically.

View logs:
```powershell
# Stream logs in real-time
aws logs tail /ecs/journal-app-api-dev --follow

# View specific time range
aws logs filter-log-events `
  --log-group-name /ecs/journal-app-api-dev `
  --start-time (Get-Date).AddHours(-1).Ticks
```

## Monitoring

### Check ECS Service Status
```powershell
aws ecs describe-services `
  --cluster journal-app-api-cluster-dev `
  --services journal-app-api-service-dev `
  --region us-east-1 `
  --output table
```

### View Task Logs
```powershell
aws ecs list-tasks `
  --cluster journal-app-api-cluster-dev `
  --region us-east-1
```

### Check ALB Health
```powershell
aws elbv2 describe-target-health `
  --target-group-arn <your-target-group-arn> `
  --region us-east-1
```

## ECR Lifecycle Policy

Your CloudFormation template includes a lifecycle policy that:
- Automatically deletes untagged images after 7 days
- Keeps `:latest` and timestamp-tagged images
- Scans images for vulnerabilities on push

## Best Practices

1. **Use semantic versioning**: Tag releases as `v1.0.0`, `v1.1.0`, etc.
2. **Keep `:latest` updated**: Always deploy `:latest` for automatic rolling updates
3. **Monitor logs**: Check CloudWatch for errors before issues escalate
4. **Test locally first**: Run `docker run -p 8080:8080 journal-app-api:latest` to test
5. **Environment-specific configs**: Use Spring profiles (dev/prd) for different settings
6. **Health checks**: ECS uses `/actuator/health` endpoint to validate deployments

## Additional Resources

- [AWS ECR Documentation](https://docs.aws.amazon.com/ecr/latest/userguide/)
- [AWS ECS Fargate](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/launch_types.html)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [Spring Boot in Docker](https://spring.io/guides/gs/spring-boot-docker/)

