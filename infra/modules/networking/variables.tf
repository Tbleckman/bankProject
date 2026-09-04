variable "project_name" {
  description = "Name prefix used for all networking resources"
  type        = string
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.20.0.0/16"
}

variable "azs" {
  description = "Availability zones to spread subnets across"
  type        = list(string)
}

variable "public_subnet_cidrs" {
  description = "CIDR blocks for public subnets (ALB lives here)"
  type        = list(string)
}

variable "private_subnet_cidrs" {
  description = "CIDR blocks for private subnets (ECS tasks + RDS live here)"
  type        = list(string)
}

variable "single_nat_gateway" {
  description = "Use one NAT gateway for all AZs instead of one per AZ (cheaper, less HA - fine for a portfolio project)"
  type        = bool
  default     = true
}
