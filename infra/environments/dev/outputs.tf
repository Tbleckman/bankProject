output "vpc_id" {
  value = module.networking.vpc_id
}

output "db_address" {
  value = module.database.address
}

output "db_credentials_secret_arn" {
  value = module.database.credentials_secret_arn
}

output "ecr_repository_urls" {
  value = module.ecr.repository_urls
}

output "alb_url" {
  value = "http://${module.compute.alb_dns_name}"
}

output "github_deploy_role_arn" {
  value = module.cicd.github_deploy_role_arn
}
