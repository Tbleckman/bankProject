output "vpc_id" {
  value = module.networking.vpc_id
}

output "db_address" {
  value = module.database.address
}

output "db_credentials_secret_arn" {
  value = module.database.credentials_secret_arn
}
