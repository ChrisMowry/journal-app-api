output "bucket_name" {
  value = aws_s3_bucket.photo_bucket.arn
}

output "bucket_arn" {
  value = aws_s3_bucket.photo_bucket.id
}