locals {
  table_name = var.env == "prd" ? "${var.service_name}-data" : "${var.service_name}-data-${var.env}"
}

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
    service = var.service_name,
    env     = var.env
  }
}