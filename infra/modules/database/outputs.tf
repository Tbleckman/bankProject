output "endpoint" {
  description = "host:port"
  value       = aws_db_instance.this.endpoint
}

output "address" {
  description = "host only (no port) - matches what DatabaseConfig.java expects for DB_HOST"
  value       = aws_db_instance.this.address
}

output "port" {
  value = aws_db_instance.this.port
}

output "db_name" {
  value = aws_db_instance.this.db_name
}

output "credentials_secret_arn" {
  description = "Secrets Manager ARN - ECS task definition reads DB_USER/DB_PASSWORD from here"
  value       = aws_secretsmanager_secret.db_credentials.arn
}
