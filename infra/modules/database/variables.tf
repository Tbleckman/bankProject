variable "project_name" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "private_subnet_ids" {
  type = list(string)
}

variable "rds_sg_id" {
  description = "Security group ID that allows inbound 5432 from the app tier"
  type        = string
}

variable "db_name" {
  type    = string
  default = "banking_system"
}

variable "db_username" {
  type    = string
  default = "postgres"
}

variable "instance_class" {
  description = "db.t4g.micro is Free Tier eligible - fine for a demo workload"
  type        = string
  default     = "db.t4g.micro"
}

variable "allocated_storage" {
  type    = number
  default = 20
}

variable "multi_az" {
  description = "Off by default to stay in Free Tier / keep cost down for a portfolio project"
  type        = bool
  default     = false
}

variable "skip_final_snapshot" {
  description = "true = easy to tear down for a demo project. Set false for anything real."
  type        = bool
  default     = true
}
