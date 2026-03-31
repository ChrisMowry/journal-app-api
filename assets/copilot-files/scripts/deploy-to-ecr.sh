#!/bin/bash

set -e

# Configuration
REGION="us-east-1"
SERVICE_NAME="journal-app-api"
ENVIRONMENT="${1:-dev}"  # Default to dev, or pass 'prd' as argument

# Validate environment
if [[ ! "$ENVIRONMENT" =~ ^(dev|prd)$ ]]; then
  echo "Invalid environment: $ENVIRONMENT"
  echo "Usage: ./deploy-to-ecr.sh [dev|prd]"
  exit 1
fi

# Get AWS Account ID
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
ECR_REPO="$ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/$SERVICE_NAME"

echo "================================================"
echo "Building and Pushing Docker Image to ECR"
echo "================================================"
echo "Service: $SERVICE_NAME"
echo "Environment: $ENVIRONMENT"
echo "ECR Repository: $ECR_REPO"
echo "Account ID: $ACCOUNT_ID"
echo ""

# Step 1: Build JAR
echo "[1/6] Building Spring Boot application..."
./mvnw clean package -DskipTests

# Step 2: Build Docker Image
echo "[2/6] Building Docker image locally..."
docker build -t $SERVICE_NAME:latest .

# Step 3: Login to ECR
echo "[3/6] Logging in to ECR..."
aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $ECR_REPO

# Step 4: Tag image for ECR
echo "[4/6] Tagging image for ECR..."
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
docker tag $SERVICE_NAME:latest $ECR_REPO:latest
docker tag $SERVICE_NAME:latest $ECR_REPO:$TIMESTAMP

# Step 5: Push to ECR
echo "[5/6] Pushing image to ECR..."
docker push $ECR_REPO:latest
docker push $ECR_REPO:$TIMESTAMP

# Step 6: Verify
echo "[6/6] Verifying image in ECR..."
aws ecr describe-images \
  --repository-name $SERVICE_NAME \
  --region $REGION \
  --output table

echo ""
echo "================================================"
echo "✓ Successfully pushed to ECR!"
echo "================================================"
echo "Image: $ECR_REPO:latest"
echo "Timestamp Tag: $ECR_REPO:$TIMESTAMP"
echo "Environment: $ENVIRONMENT"
echo ""
echo "Next steps:"
echo "1. Deploy to Fargate:"
echo "   aws ecs update-service --cluster $SERVICE_NAME-cluster-$ENVIRONMENT --service $SERVICE_NAME-service-$ENVIRONMENT --force-new-deployment --region $REGION"
echo "2. Monitor deployment in AWS Console"
echo "3. Check logs in CloudWatch: /ecs/$SERVICE_NAME-$ENVIRONMENT"
echo ""

