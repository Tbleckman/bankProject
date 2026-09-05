variable "project_name" {
  type = string
}

variable "repo_names" {
  description = "Short names for each ECR repo, e.g. [\"app\", \"risk-service\"]"
  type        = list(string)
}

variable "max_image_count" {
  description = "How many tagged images to keep per repo before the lifecycle policy expires the oldest"
  type        = number
  default     = 10
}
