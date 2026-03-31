locals {
  bucket_name = var.env == "prd" ? "${var.service_name}-photos" : "${var.service_name}-photos-${var.env}"
}

resource "aws_s3_bucket" "photo_bucket" {
  bucket = local.bucket_name

  tags = {
    service = var.service_name,
    env = var.env
  }
}

resource "aws_s3_bucket_public_access_block" "photo_bucket" {
  bucket = aws_s3_bucket.photo_bucket.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_policy" "photo_bucket_policy" {
  bucket = aws_s3_bucket.photo_bucket.id

  policy = templatefile( "s3-resource-policy.tpl", {
    photo_bucket_arn = aws_s3_bucket.photo_bucket.arn
    aws_account_id = var.aws_account_id
    cloudfront_distribution_id = var.cloudfront_distribution_id
  })

  tags = {
    service = var.service_name,
    env = var.env
  }

  depends_on = [aws_s3_bucket_public_access_block.photo_bucket]
}