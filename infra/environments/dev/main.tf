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

module "ecr" {
  source = "../../modules/ecr"

  project_name = var.project_name
  repo_names   = ["app", "risk-service"]
}

module "compute" {
  source = "../../modules/compute"

  project_name       = var.project_name
  aws_region         = var.aws_region
  vpc_id             = module.networking.vpc_id
  public_subnet_ids  = module.networking.public_subnet_ids
  private_subnet_ids = module.networking.private_subnet_ids
  alb_sg_id          = module.networking.alb_sg_id
  ecs_app_sg_id      = module.networking.ecs_app_sg_id

  app_image  = "${module.ecr.repository_urls["app"]}:${var.image_tag}"
  risk_image = "${module.ecr.repository_urls["risk-service"]}:${var.image_tag}"

  db_address                = module.database.address
  db_name                   = module.database.db_name
  db_credentials_secret_arn = module.database.credentials_secret_arn
}

module "cicd" {
  source = "../../modules/cicd"

  project_name = var.project_name
  github_org   = var.github_org
  github_repo  = var.github_repo

  ecr_repository_arns     = values(module.ecr.repository_arns)
  ecs_cluster_arn          = module.compute.cluster_arn
  ecs_service_arn          = module.compute.service_arn
  task_execution_role_arn = module.compute.task_execution_role_arn
  task_role_arn            = module.compute.task_role_arn
  create_oidc_provider    = var.create_oidc_provider
}
