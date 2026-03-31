# PowerShell script to build and push Docker image to ECR

Write-Host ""
Write-Host "3. Check logs in CloudWatch: /ecs/$ServiceName-$Environment" -ForegroundColor White
Write-Host "2. Monitor deployment in AWS Console" -ForegroundColor White
Write-Host "   aws ecs update-service --cluster $ServiceName-cluster-$Environment --service $ServiceName-service-$Environment --force-new-deployment --region $Region" -ForegroundColor Gray
Write-Host "1. Deploy to Fargate:" -ForegroundColor White
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host ""
Write-Host "Environment: $Environment" -ForegroundColor Green
Write-Host "Timestamp Tag: $ECRRepo`:$Timestamp" -ForegroundColor Green
Write-Host "Image: $ECRRepo`:latest" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green
Write-Host "✓ Successfully pushed to ECR!" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green
Write-Host ""

  --output table
  --region $Region `
  --repository-name $ServiceName `
& aws ecr describe-images `
Write-Host "[6/6] Verifying image in ECR..." -ForegroundColor Yellow
# Step 6: Verify

}
  exit $LASTEXITCODE
  Write-Host "❌ Failed to push timestamp tag!" -ForegroundColor Red
if ($LASTEXITCODE -ne 0) {
& docker push "$ECRRepo`:$Timestamp"
Write-Host "Pushing timestamp tag..." -ForegroundColor Gray

}
  exit $LASTEXITCODE
  Write-Host "❌ Failed to push latest tag!" -ForegroundColor Red
if ($LASTEXITCODE -ne 0) {
& docker push "$ECRRepo`:latest"
Write-Host "Pushing latest..." -ForegroundColor Gray
Write-Host "[5/6] Pushing image to ECR..." -ForegroundColor Yellow
# Step 5: Push to ECR

& docker tag "$ServiceName`:latest" "$ECRRepo`:$Timestamp"
& docker tag "$ServiceName`:latest" "$ECRRepo`:latest"
$Timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
Write-Host "[4/6] Tagging image for ECR..." -ForegroundColor Yellow
# Step 4: Tag image for ECR

}
  exit $LASTEXITCODE
  Write-Host "❌ Docker ECR login failed!" -ForegroundColor Red
if ($LASTEXITCODE -ne 0) {
$LoginToken | docker login --username AWS --password-stdin "$ECRRepo"

}
  exit $LASTEXITCODE
  Write-Host "Check your AWS credentials and permissions" -ForegroundColor Red
  Write-Host "❌ Failed to get ECR login token!" -ForegroundColor Red
if ($LASTEXITCODE -ne 0) {
$LoginToken = aws ecr get-login-password --region $Region
Write-Host "[3/6] Logging in to ECR..." -ForegroundColor Yellow
# Step 3: Login to ECR

}
  exit $LASTEXITCODE
  Write-Host "❌ Docker build failed!" -ForegroundColor Red
if ($LASTEXITCODE -ne 0) {
& docker build -t "$ServiceName`:latest" .
Write-Host "[2/6] Building Docker image locally..." -ForegroundColor Yellow
# Step 2: Build Docker Image

}
  exit $LASTEXITCODE
  Write-Host "❌ Maven build failed!" -ForegroundColor Red
if ($LASTEXITCODE -ne 0) {
& ./mvnw clean package -DskipTests
Write-Host "[1/6] Building Spring Boot application..." -ForegroundColor Yellow
# Step 1: Build JAR

Write-Host ""
Write-Host "Account ID: $AccountId" -ForegroundColor White
Write-Host "ECR Repository: $ECRRepo" -ForegroundColor White
Write-Host "Environment: $Environment" -ForegroundColor White
Write-Host "Service: $ServiceName" -ForegroundColor White
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Building and Pushing Docker Image to ECR" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan

$ECRRepo = "$AccountId.dkr.ecr.$Region.amazonaws.com/$ServiceName"
$AccountId = aws sts get-caller-identity --query Account --output text
Write-Host "Getting AWS Account ID..." -ForegroundColor Gray
# Get AWS Account ID

}
  exit 1
  Write-Host "Usage: .\deploy-to-ecr.ps1 [dev|prd]" -ForegroundColor Yellow
  Write-Host "Invalid environment: $Environment" -ForegroundColor Red
if ($Environment -notmatch "^(dev|prd)$") {
# Validate environment

$Environment = if ($args.Count -gt 0) { $args[0] } else { "dev" }
$ServiceName = "journal-app-api"
$Region = "us-east-1"
# Configuration

