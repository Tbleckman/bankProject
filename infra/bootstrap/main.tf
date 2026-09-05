# This is a SEPARATE root module from environments/dev, on purpose.
# You can't put an S3 backend config inside the same state it's meant to
# manage - the bucket has to exist before anything can point its backend
# at it. Apply this once, by hand, with local state. After that you never
# touch it again; environments/dev's backend block points here.
#
# Usage:
#   cd infra/bootstrap
#   terraform init
#   terraform apply -var="account_id=<your account id>"
#   # then copy the bucket/table names into environments/dev/providers.tf's
#   # backend "s3" block and run `terraform init -migrate-state` there

terraform {
  required_version = ">= 1.7"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "project_name" {
  type    = string
  default = "bankproject"
}

provider "aws" {
  region = var.aws_region
}

data "aws_caller_identity" "current" {}

resource "aws_s3_bucket" "tfstate" {
  # bucket names are globally unique across all of AWS, so the account id
  # gets baked in
  bucket = "${var.project_name}-tfstate-${data.aws_caller_identity.current.account_id}"
}

resource "aws_s3_bucket_versioning" "tfstate" {
  bucket = aws_s3_bucket.tfstate.id
  versioning_configuration {
    status = "Enabled" # lets you roll back to a prior state file if something corrupts it
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "tfstate" {
  bucket = aws_s3_bucket.tfstate.id
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_public_access_block" "tfstate" {
  bucket                  = aws_s3_bucket.tfstate.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_dynamodb_table" "tf_locks" {
  name         = "${var.project_name}-tf-locks"
  billing_mode = "PAY_PER_REQUEST" # locking traffic is tiny and bursty - no reason to provision fixed capacity
  hash_key     = "LockID"

  attribute {
    name = "LockID"
    type = "S"
  }
}

output "bucket_name" {
  value = aws_s3_bucket.tfstate.id
}

output "dynamodb_table_name" {
  value = aws_dynamodb_table.tf_locks.name
}
