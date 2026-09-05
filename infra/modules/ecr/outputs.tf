output "repository_urls" {
  description = "map of repo_name => full ECR URL, e.g. app => 123456789012.dkr.ecr.us-east-1.amazonaws.com/bankproject/app"
  value       = { for name, repo in aws_ecr_repository.this : name => repo.repository_url }
}

output "repository_arns" {
  value = { for name, repo in aws_ecr_repository.this : name => repo.arn }
}
