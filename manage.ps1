# PowerShell utility script for common ECR and ECS operations

param(
    [Parameter(Position = 0, Mandatory = $true)]
    [ValidateSet("status", "logs", "images", "push-latest", "deploy", "restart", "scale")]
    [string]$Command,

    [Parameter(Position = 1)]
    [ValidateSet("dev", "prd")]
    [string]$Environment = "dev"
)

# Configuration
$Region = "us-east-1"
$ServiceName = "journal-app-api"
$ClusterName = "$ServiceName-cluster-$Environment"
$ServiceFullName = "$ServiceName-service-$Environment"

function Show-Help {
    Write-Host "ECR/ECS Utility Script" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Usage: .\manage.ps1 <command> [environment]" -ForegroundColor White
    Write-Host ""
    Write-Host "Commands:" -ForegroundColor Cyan
    Write-Host "  status          - Show ECS service status" -ForegroundColor Gray
    Write-Host "  logs            - Tail ECS logs (real-time)" -ForegroundColor Gray
    Write-Host "  images          - List ECR images" -ForegroundColor Gray
    Write-Host "  push-latest     - Push current Docker image to ECR" -ForegroundColor Gray
    Write-Host "  deploy          - Trigger ECS deployment" -ForegroundColor Gray
    Write-Host "  restart         - Full restart (stop and start)" -ForegroundColor Gray
    Write-Host "  scale           - Scale number of tasks" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Environment:" -ForegroundColor Cyan
    Write-Host "  dev (default)   - Development environment" -ForegroundColor Gray
    Write-Host "  prd             - Production environment" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Examples:" -ForegroundColor Cyan
    Write-Host "  .\manage.ps1 status" -ForegroundColor Gray
    Write-Host "  .\manage.ps1 status prd" -ForegroundColor Gray
    Write-Host "  .\manage.ps1 logs dev --follow" -ForegroundColor Gray
    Write-Host "  .\manage.ps1 push-latest dev" -ForegroundColor Gray
    Write-Host ""
}

function Show-Status {
    Write-Host "ECS Service Status ($Environment)" -ForegroundColor Cyan
    Write-Host ""

    $service = aws ecs describe-services `
        --cluster "$ClusterName" `
        --services "$ServiceFullName" `
        --region "$Region" `
        --query 'services[0]' `
        --output json | ConvertFrom-Json

    Write-Host "Service Name: $ServiceFullName" -ForegroundColor White
    Write-Host "Cluster: $ClusterName" -ForegroundColor White
    Write-Host "Status: $($service.status)" -ForegroundColor $(if ($service.status -eq 'ACTIVE') { 'Green' } else { 'Yellow' })
    Write-Host "Desired Count: $($service.desiredCount)" -ForegroundColor White
    Write-Host "Running Count: $($service.runningCount)" -ForegroundColor $(if ($service.runningCount -eq $service.desiredCount) { 'Green' } else { 'Yellow' })
    Write-Host "Task Definition: $($service.taskDefinition.Split('/')[-1])" -ForegroundColor Gray
    Write-Host ""

    if ($service.deployments) {
        Write-Host "Deployments:" -ForegroundColor Cyan
        foreach ($deployment in $service.deployments) {
            Write-Host "  Status: $($deployment.status) | Running: $($deployment.runningCount) | Pending: $($deployment.pendingCount)" -ForegroundColor Gray
        }
    }

    Write-Host ""
    Write-Host "Load Balancer:" -ForegroundColor Cyan
    if ($service.loadBalancers) {
        foreach ($lb in $service.loadBalancers) {
            Write-Host "  Container: $($lb.containerName):$($lb.containerPort)" -ForegroundColor Gray
            Write-Host "  Target Group: $($lb.targetGroupArn.Split('/')[-1])" -ForegroundColor Gray
        }
    }

    Write-Host ""
}

function Show-Logs {
    param([string[]]$AdditionalArgs)

    Write-Host "CloudWatch Logs ($Environment)" -ForegroundColor Cyan
    Write-Host "Log Group: /ecs/$ServiceName-$Environment" -ForegroundColor Gray
    Write-Host ""

    $cmd = @('logs', 'tail', "/ecs/$ServiceName-$Environment")
    if ($AdditionalArgs) {
        $cmd += $AdditionalArgs
    } else {
        $cmd += '--follow'
    }

    & aws @cmd
}

function Show-Images {
    Write-Host "ECR Images" -ForegroundColor Cyan
    Write-Host ""

    $images = aws ecr describe-images `
        --repository-name "$ServiceName" `
        --region "$Region" `
        --query 'sort_by(imageDetails, &imagePushedAt) | [-10:]' `
        --output json | ConvertFrom-Json

    Write-Host "Latest 10 images:" -ForegroundColor Gray
    Write-Host ""

    foreach ($img in $images) {
        $tags = if ($img.imageTags) { ($img.imageTags -join ', ') } else { 'untagged' }
        $date = $img.imagePushedAt -replace 'T', ' ' -replace 'Z', ''
        Write-Host "$date | $($img.imageId.imageDigest.Substring(0, 12)) | $tags" -ForegroundColor Gray
    }

    Write-Host ""
    Write-Host "Total images: $($images.Count)" -ForegroundColor Gray
}

function Push-Latest {
    Write-Host "Pushing latest Docker image to ECR ($Environment)" -ForegroundColor Cyan
    Write-Host ""

    $AccountId = aws sts get-caller-identity --query Account --output text
    $ECRRepo = "$AccountId.dkr.ecr.$Region.amazonaws.com/$ServiceName"

    Write-Host "Building application..." -ForegroundColor Yellow
    & ./mvnw clean package -DskipTests -q

    Write-Host "Building Docker image..." -ForegroundColor Yellow
    & docker build -t "$ServiceName`:latest" . -q

    Write-Host "Logging into ECR..." -ForegroundColor Yellow
    $token = aws ecr get-login-password --region $Region
    $token | docker login --username AWS --password-stdin "$ECRRepo" | Out-Null

    Write-Host "Tagging image..." -ForegroundColor Yellow
    & docker tag "$ServiceName`:latest" "$ECRRepo`:latest"

    Write-Host "Pushing to ECR..." -ForegroundColor Yellow
    & docker push "$ECRRepo`:latest"

    Write-Host ""
    Write-Host "✓ Pushed successfully!" -ForegroundColor Green
    Write-Host "Image: $ECRRepo`:latest" -ForegroundColor Green
}

function Deploy-Service {
    Write-Host "Triggering deployment ($Environment)" -ForegroundColor Cyan
    Write-Host ""

    Write-Host "Updating service..." -ForegroundColor Yellow
    & aws ecs update-service `
        --cluster "$ClusterName" `
        --service "$ServiceFullName" `
        --force-new-deployment `
        --region "$Region" | Out-Null

    Write-Host ""
    Write-Host "✓ Deployment triggered!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Monitoring deployment (Ctrl+C to stop):" -ForegroundColor Cyan
    Write-Host ""

    # Show status every 5 seconds for 2 minutes
    for ($i = 0; $i -lt 24; $i++) {
        Show-Status
        if ($i -lt 23) {
            Start-Sleep -Seconds 5
        }
    }
}

function Restart-Service {
    Write-Host "Restarting service ($Environment)" -ForegroundColor Yellow
    Write-Host ""

    Write-Host "Setting desired count to 0..." -ForegroundColor Yellow
    aws ecs update-service `
        --cluster "$ClusterName" `
        --service "$ServiceFullName" `
        --desired-count 0 `
        --region "$Region" | Out-Null

    Write-Host "Waiting for tasks to stop..." -ForegroundColor Yellow
    Start-Sleep -Seconds 10

    $desiredCount = if ($Environment -eq 'prd') { 2 } else { 1 }
    Write-Host "Setting desired count to $desiredCount..." -ForegroundColor Yellow
    aws ecs update-service `
        --cluster "$ClusterName" `
        --service "$ServiceFullName" `
        --desired-count $desiredCount `
        --region "$Region" | Out-Null

    Write-Host ""
    Write-Host "✓ Service restarted!" -ForegroundColor Green
}

function Scale-Service {
    param([int]$DesiredCount)

    if (-not $DesiredCount -or $DesiredCount -lt 1 -or $DesiredCount -gt 4) {
        Write-Host "Enter desired number of tasks (1-4): " -ForegroundColor Cyan -NoNewline
        [int]$DesiredCount = Read-Host
    }

    if ($DesiredCount -lt 1 -or $DesiredCount -gt 4) {
        Write-Host "Invalid count. Must be between 1 and 4." -ForegroundColor Red
        return
    }

    Write-Host "Scaling to $DesiredCount tasks..." -ForegroundColor Yellow
    aws ecs update-service `
        --cluster "$ClusterName" `
        --service "$ServiceFullName" `
        --desired-count $DesiredCount `
        --region "$Region" | Out-Null

    Write-Host ""
    Write-Host "✓ Service scaled to $DesiredCount tasks!" -ForegroundColor Green
}

# Main execution
switch ($Command) {
    "status" {
        Show-Status
    }
    "logs" {
        Show-Logs
    }
    "images" {
        Show-Images
    }
    "push-latest" {
        Push-Latest
    }
    "deploy" {
        Deploy-Service
    }
    "restart" {
        Restart-Service
    }
    "scale" {
        Scale-Service
    }
    default {
        Show-Help
    }
}

