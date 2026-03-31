terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  backend "s3" {
    bucket         = "journal-app-terraform-state"
    key            = "terraform.tfstate"
    region         = "us-west-2"
    encrypt        = true
    dynamodb_table = "journal-app-terraform-locks"
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      env = terraform.workspace
      service = var.service_name
      terraform   = "true"
    }
  }
}

locals {
  env = terraform.workspace
  is_production = terraform.workspace == "prd"
}

