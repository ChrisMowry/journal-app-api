resource "aws_cognito_user_pool" "main" {
  name = local.user_pool_name

  username_attributes      = ["email"]
  auto_verified_attributes = ["email"]

  password_policy {
    minimum_length    = 8
    require_uppercase = true
    require_lowercase = true
    require_numbers   = true
    require_symbols   = false
  }

  account_recovery_setting {
    recovery_mechanism {
      name       = "verified_email"
      priority   = 1
    }
  }

  mfa_configuration = "OFF"

  schema {
    name                = "email"
    attribute_data_type = "String"
    required            = true
    mutable             = true
  }

  tags = {
    Name = local.user_pool_name
  }
}

resource "aws_cognito_user_pool_client" "main" {
  name            = local.user_pool_client_name
  user_pool_id    = aws_cognito_user_pool.main.id
  generate_secret = false

  explicit_auth_flows = [
    "ADMIN_NO_SRP_AUTH",
    "USER_PASSWORD_AUTH",
    "ALLOW_REFRESH_TOKEN_AUTH"
  ]

  refresh_token_validity       = 30
  enable_token_revocation      = true
  prevent_user_existence_errors = "ENABLED"
}

locals {
  user_pool_name        = var.environment == "prd" ? "${var.service_name}-user-pool" : "${var.service_name}-user-pool-${var.environment}"
  user_pool_client_name = var.environment == "prd" ? "${var.service_name}-user-pool-client" : "${var.service_name}-user-pool-client-${var.environment}"
}

