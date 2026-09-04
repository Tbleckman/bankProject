# Generate + store the master password in Secrets Manager rather than a
# Terraform variable, so it never lands in a .tfvars file or state-viewer
# plaintext output. ECS reads it at task-start via secrets (see compute module).

resource "random_password" "db" {
  length  = 24
  special = false # avoid chars that need URL-encoding in a JDBC connection string
}

resource "aws_secretsmanager_secret" "db_credentials" {
  name = "${var.project_name}/rds/credentials"
}

resource "aws_secretsmanager_secret_version" "db_credentials" {
  secret_id = aws_secretsmanager_secret.db_credentials.id
  secret_string = jsonencode({
    username = var.db_username
    password = random_password.db.result
  })
}

resource "aws_db_subnet_group" "this" {
  name       = "${var.project_name}-db-subnet-group"
  subnet_ids = var.private_subnet_ids

  tags = {
    Name = "${var.project_name}-db-subnet-group"
  }
}

resource "aws_db_instance" "this" {
  identifier     = "${var.project_name}-db"
  engine         = "postgres"
  engine_version = "16"

  instance_class    = var.instance_class
  allocated_storage = var.allocated_storage
  storage_type      = "gp3"
  storage_encrypted = true

  db_name  = var.db_name
  username = var.db_username
  password = random_password.db.result

  db_subnet_group_name   = aws_db_subnet_group.this.name
  vpc_security_group_ids = [var.rds_sg_id]
  publicly_accessible    = false

  multi_az                = var.multi_az
  backup_retention_period = 7
  skip_final_snapshot     = var.skip_final_snapshot
  final_snapshot_identifier = var.skip_final_snapshot ? null : "${var.project_name}-db-final-snapshot"

  # Deletion protection off by default for a demo project you'll want to
  # tear down/rebuild; flip on before this ever holds real data.
  deletion_protection = false

  tags = {
    Name = "${var.project_name}-db"
  }
}
