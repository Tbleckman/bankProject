variable "project_name" {
  type = string
}

variable "github_org" {
  description = "GitHub org/user that owns the repo, e.g. Tbleckman"
  type        = string
}

variable "github_repo" {
  description = "Repo name only, e.g. bankProject"
  type        = string
}

variable "allowed_branch" {
  description = "Only this branch can assume the deploy role via OIDC (protects against a PR from a fork deploying to your AWS account)"
  type        = string
  default     = "main"
}

variable "ecr_repository_arns" {
  type = list(string)
}

variable "ecs_cluster_arn" {
  type = string
}

variable "ecs_service_arn" {
  type = string
}

variable "task_execution_role_arn" {
  type = string
}

variable "task_role_arn" {
  type = string
}

variable "create_oidc_provider" {
  description = "false if token.actions.githubusercontent.com is already registered in this AWS account (e.g. from aws-terraform-demo) - an account can only have one per issuer URL"
  type        = bool
  default     = true
}
