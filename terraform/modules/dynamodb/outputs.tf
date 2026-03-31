output "table_name" {
  value = aws_dynamodb_table.journal_app_data.arn
  description = "The ARN of the DynamoDB table created for the journal app data."
}

output "table_arn" {
  value = aws_dynamodb_table.journal_app_data.name
  description = "The name of the DynamoDB table created for the journal app data."
}