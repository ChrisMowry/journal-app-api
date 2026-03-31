variable "service_name" {
  type = string
}

variable "env" {
  type = string
}

variable "cloudfront_distribution_id" {
  type        = string
  description = "CloudFront distribution ID for bucket policy"
}

variable "aws_account_id" {
  type = string
}

