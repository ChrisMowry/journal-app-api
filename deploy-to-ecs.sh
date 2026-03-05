#!/bin/bash

set -e

# Configuration
REGION="us-east-1"
SERVICE_NAME="journal-app-api"
ENVIRONMENT="${1:-dev}"  # Default to dev, or pass 'prd' as argument

# Validate environment
if [[ ! "$ENVIRONMENT" =~ ^(dev|prd)$ ]]; then
  echo "Invalid environment: $ENVIRONMENT"
  echo "Usage: ./deploy-to-ecs.sh [dev|prd]"
  exit 1
fi

CLUSTER_NAME="$SERVICE_NAME-cluster-$ENVIRONMENT"
SERVICE_FULL_NAME="$SERVICE_NAME-service-$ENVIRONMENT"

echo "================================================"
echo "Deploying to ECS Fargate"
echo "================================================"
echo "Service: $SERVICE_NAME"
echo "Environment: $ENVIRONMENT"
echo "Cluster: $CLUSTER_NAME"
echo "ECS Service: $SERVICE_FULL_NAME"
echo "Region: $REGION"
echo ""

# Get current service info
echo "Retrieving current service configuration..."
aws ecs describe-services \
  --cluster "$CLUSTER_NAME" \
  --services "$SERVICE_FULL_NAME" \
  --region "$REGION" \
  --query 'services[0]' \
  --output table

echo ""
echo "Triggering new deployment..."
aws ecs update-service \
  --cluster "$CLUSTER_NAME" \
  --service "$SERVICE_FULL_NAME" \
  --force-new-deployment \
  --region "$REGION"

echo ""
echo "================================================"
echo "✓ Deployment triggered!"
echo "================================================"
echo "Cluster: $CLUSTER_NAME"
echo "Service: $SERVICE_FULL_NAME"
echo ""
echo "Monitor deployment progress:"
echo "1. AWS Console: https://console.aws.amazon.com/ecs/v2/clusters"
echo "2. CloudWatch Logs: /ecs/$SERVICE_NAME-$ENVIRONMENT"
echo "3. CLI:"
echo "   aws ecs describe-services --cluster $CLUSTER_NAME --services $SERVICE_FULL_NAME --region $REGION --output table"
echo ""
echo "Estimated time: 2-5 minutes"
echo ""

