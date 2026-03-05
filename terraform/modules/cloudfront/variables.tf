variable "service_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "s3_bucket_regional_domain_name" {
  type = string
}

variable "s3_bucket_arn" {
  type = string
}

variable "cloudfront_encoded_public_key_value" {
  type      = string
  sensitive = true
}

variable "domain_name" {
  type = string
}

variable "hosted_zone_id" {
  type = string
}

