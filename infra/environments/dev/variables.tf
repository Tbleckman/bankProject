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
