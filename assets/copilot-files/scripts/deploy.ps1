#!/usr/bin/env pwsh
<#
.SYNOPSIS
Terraform deployment script for Journal App API
.DESCRIPTION
Simplified script to manage Terraform workspaces and deployments
.PARAMETER Environment
Environment to deploy (dev or prd)
.PARAMETER Action
Action to perform (init, plan, apply, destroy)
.PARAMETER AutoApprove
Skip approval prompt for apply/destroy
#>

param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('dev', 'prd')]
    [string]$Environment,

    [Parameter(Mandatory = $true)]
    [ValidateSet('init', 'plan', 'apply', 'destroy', 'output')]
    [string]$Action,

    [switch]$AutoApprove
)

# Get script directory
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$TerraformDir = Join-Path $ScriptDir "terraform"
$TfvarsFile = Join-Path $TerraformDir "environments" $Environment "$Environment.tfvars"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Journal App Terraform Deployment" -ForegroundColor Cyan
Write-Host "Environment: $Environment" -ForegroundColor Yellow
Write-Host "Action: $Action" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan

# Change to terraform directory
Set-Location $TerraformDir

try {
    switch ($Action) {
        'init' {
            Write-Host "Initializing Terraform..." -ForegroundColor Green
            terraform init

            Write-Host "Creating workspace: $Environment" -ForegroundColor Green
            $workspaces = terraform workspace list | ForEach-Object { $_.Trim() }
            if ($workspaces -notcontains $Environment) {
                terraform workspace new $Environment
            }
            terraform workspace select $Environment
            Write-Host "Workspace '$Environment' is now active" -ForegroundColor Green
        }

        'plan' {
            Write-Host "Selecting workspace: $Environment" -ForegroundColor Green
            terraform workspace select $Environment

            Write-Host "Planning Terraform configuration..." -ForegroundColor Green
            terraform plan -var-file=$TfvarsFile -out=tfplan
        }

        'apply' {
            Write-Host "Selecting workspace: $Environment" -ForegroundColor Green
            terraform workspace select $Environment

            if (-not (Test-Path "tfplan")) {
                Write-Host "tfplan not found. Running plan first..." -ForegroundColor Yellow
                terraform plan -var-file=$TfvarsFile -out=tfplan
            }

            if ($AutoApprove) {
                Write-Host "Applying Terraform configuration (auto-approved)..." -ForegroundColor Green
                terraform apply -auto-approve tfplan
            }
            else {
                Write-Host "Applying Terraform configuration..." -ForegroundColor Green
                terraform apply tfplan
            }

            # Clean up tfplan
            Remove-Item -Force tfplan -ErrorAction SilentlyContinue

            Write-Host "Deployment complete!" -ForegroundColor Green
            Write-Host "Run './manage.ps1 $Environment output' to view outputs" -ForegroundColor Cyan
        }

        'destroy' {
            Write-Host "Selecting workspace: $Environment" -ForegroundColor Green
            terraform workspace select $Environment

            Write-Host "WARNING: This will destroy all resources in $Environment!" -ForegroundColor Red

            if ($AutoApprove) {
                Write-Host "Destroying resources (auto-approved)..." -ForegroundColor Red
                terraform destroy -auto-approve -var-file=$TfvarsFile
            }
            else {
                Write-Host "Destroying resources..." -ForegroundColor Red
                terraform destroy -var-file=$TfvarsFile
            }
        }

        'output' {
            Write-Host "Selecting workspace: $Environment" -ForegroundColor Green
            terraform workspace select $Environment

            Write-Host "Terraform Outputs:" -ForegroundColor Green
            terraform output
        }
    }
}
catch {
    Write-Host "Error: $_" -ForegroundColor Red
    exit 1
}
finally {
    Set-Location $ScriptDir
}

