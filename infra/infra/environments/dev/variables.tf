variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "project_name" {
  type    = string
  default = "bankproject"
}

variable "azs" {
  type    = list(string)
  default = ["us-east-1a", "us-east-1b"]
}

variable "image_tag" {
  description = "Tag to deploy for both containers, e.g. a git SHA pushed by CI. There's a bootstrap order problem: first 'terraform apply' will create a task def pointing at a tag that doesn't exist in ECR yet - that's fine, apply still succeeds, the ECS service just won't reach steady state until CI pushes a matching image. Build+push once manually the first time, or run apply after the first CI run."
  type        = string
  default     = "initial"
}

variable "github_org" {
  type    = string
  default = "Tbleckman"
}

variable "github_repo" {
  type    = string
  default = "bankProject"
}

variable "create_oidc_provider" {
  description = "Set to false if aws-terraform-demo (or anything else in this account) already registered the token.actions.githubusercontent.com OIDC provider - AWS only allows one per issuer URL per account."
  type        = bool
  default     = true
}
