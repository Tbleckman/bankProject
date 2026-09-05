variable "project_name" {
  type = string
}

variable "aws_region" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "public_subnet_ids" {
  type = list(string)
}

variable "private_subnet_ids" {
  type = list(string)
}

variable "alb_sg_id" {
  type = string
}

variable "ecs_app_sg_id" {
  type = string
}

variable "app_image" {
  description = "Full ECR image URI for the Spring Boot app, e.g. <acct>.dkr.ecr.<region>.amazonaws.com/bankproject/app:sha-abc123"
  type        = string
}

variable "risk_image" {
  description = "Full ECR image URI for the FastAPI risk service"
  type        = string
}

variable "db_address" {
  type = string
}

variable "db_port" {
  type    = string
  default = "5432"
}

variable "db_name" {
  type = string
}

variable "db_credentials_secret_arn" {
  type = string
}

variable "task_cpu" {
  description = "Total CPU units for the task (app + risk-service containers share this)"
  type        = string
  default     = "512"
}

variable "task_memory" {
  type    = string
  default = "1024"
}

variable "desired_count" {
  type    = number
  default = 1
}

variable "log_retention_days" {
  type    = number
  default = 14
}
