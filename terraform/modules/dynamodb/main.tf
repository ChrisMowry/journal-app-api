resource "aws_dynamodb_table" "journal_app_data" {
  name             = local.table_name
  billing_mode     = "PAY_PER_REQUEST"
  hash_key         = "pk"
  range_key        = "sk"

  attribute {
    name = "pk"
    type = "S"
  }

  attribute {
    name = "sk"
    type = "S"
  }

  tags = {
    Name = local.table_name
  }
}

locals {
  table_name = var.environment == "prd" ? "${var.service_name}-data" : "${var.service_name}-data-${var.environment}"
}

