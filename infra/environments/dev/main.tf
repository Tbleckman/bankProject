module "networking" {
  source = "../../modules/networking"

  project_name         = var.project_name
  azs                  = var.azs
  public_subnet_cidrs  = ["10.20.0.0/24", "10.20.1.0/24"]
  private_subnet_cidrs = ["10.20.10.0/24", "10.20.11.0/24"]
}

module "database" {
  source = "../../modules/database"

  project_name       = var.project_name
  vpc_id             = module.networking.vpc_id
  private_subnet_ids = module.networking.private_subnet_ids
  rds_sg_id          = module.networking.rds_sg_id
}

# --- ECR / ECS / GitHub Actions OIDC modules get wired in here next ---
