terraform {
  required_version = ">= 1.7"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.6"
    }
  }

  # State locking: bucket + table created once via infra/bootstrap (separate
  # root module, has to use local state itself - see that dir's comments).
  # Bucket/table names follow a fixed pattern from bootstrap/main.tf, but
  # backend blocks can't reference variables, so they're spelled out here.
  # If you renamed project_name away from "bankproject", update these to match.
  backend "s3" {
    bucket         = "bankproject-tfstate-<ACCOUNT_ID>" # replace <ACCOUNT_ID> after running bootstrap
    key            = "dev/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "bankproject-tf-locks"
    encrypt        = true
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = var.project_name
      Environment = "dev"
      ManagedBy   = "terraform"
    }
  }
}
