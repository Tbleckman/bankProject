output "github_deploy_role_arn" {
  description = "Put this in the GitHub Actions workflow's role-to-assume field"
  value       = aws_iam_role.github_deploy.arn
}
