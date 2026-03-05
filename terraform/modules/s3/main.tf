resource "aws_s3_bucket" "photo_bucket" {
  bucket = local.bucket_name

  tags = {
    Name = local.bucket_name
  }
}

resource "aws_s3_bucket_versioning" "photo_bucket" {
  bucket = aws_s3_bucket.photo_bucket.id
  versioning_configuration {
    status = "Enabled"
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

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "cloudfront.amazonaws.com"
        }
        Action   = "s3:GetObject"
        Resource = "${aws_s3_bucket.photo_bucket.arn}/*"
        Condition = {
          StringEquals = {
            "AWS:SourceArn" = "arn:aws:cloudfront::${var.aws_account_id}:distribution/${var.cloudfront_distribution_id}"
          }
        }
      }
    ]
  })

  depends_on = [aws_s3_bucket_public_access_block.photo_bucket]
}

locals {
  bucket_name = var.environment == "prd" ? "${var.service_name}-photos" : "${var.service_name}-photos-${var.environment}"
}

