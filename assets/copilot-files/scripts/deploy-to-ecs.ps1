# PowerShell script to deploy to ECS Fargate

# Configuration
$Region = "us-east-1"
$ServiceName = "journal-app-api"
$Environment = if ($args.Count -gt 0) { $args[0] } else { "dev" }

# Validate environment
if ($Environment -notmatch "^(dev|prd)$") {
  Write-Host "Invalid environment: $Environment" -ForegroundColor Red
  Write-Host "Usage: .\deploy-to-ecs.ps1 [dev|prd]" -ForegroundColor Yellow
  exit 1
}

$ClusterName = "$ServiceName-cluster-$Environment"
$ServiceFullName = "$ServiceName-service-$Environment"

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Deploying to ECS Fargate" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Service: $ServiceName" -ForegroundColor White
Write-Host "Environment: $Environment" -ForegroundColor White
Write-Host "Cluster: $ClusterName" -ForegroundColor White
Write-Host "ECS Service: $ServiceFullName" -ForegroundColor White
Write-Host "Region: $Region" -ForegroundColor White
Write-Host ""

# Get current service info
Write-Host "Retrieving current service configuration..." -ForegroundColor Yellow
& aws ecs describe-services `
  --cluster "$ClusterName" `
  --services "$ServiceFullName" `
  --region "$Region" `
  --query 'services[0]' `
  --output table

Write-Host ""
Write-Host "Triggering new deployment..." -ForegroundColor Yellow
& aws ecs update-service `
  --cluster "$ClusterName" `
  --service "$ServiceFullName" `
  --force-new-deployment `
  --region "$Region"

if ($LASTEXITCODE -ne 0) {
  Write-Host "❌ Deployment failed!" -ForegroundColor Red
  exit $LASTEXITCODE
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Green
Write-Host "✓ Deployment triggered!" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green
Write-Host "Cluster: $ClusterName" -ForegroundColor Green
Write-Host "Service: $ServiceFullName" -ForegroundColor Green
Write-Host ""
Write-Host "Monitor deployment progress:" -ForegroundColor Cyan
Write-Host "1. AWS Console: https://console.aws.amazon.com/ecs/v2/clusters" -ForegroundColor Gray
Write-Host "2. CloudWatch Logs: /ecs/$ServiceName-$Environment" -ForegroundColor Gray
Write-Host "3. CLI:" -ForegroundColor Gray
Write-Host "   aws ecs describe-services --cluster $ClusterName --services $ServiceFullName --region $Region --output table" -ForegroundColor Gray
Write-Host ""
Write-Host "Estimated time: 2-5 minutes" -ForegroundColor White
Write-Host ""

