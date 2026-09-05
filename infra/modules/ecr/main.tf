resource "aws_ecr_repository" "this" {
  for_each = toset(var.repo_names)

  name = "${var.project_name}/${each.value}"

  image_tag_mutability = "IMMUTABLE" # CI pushes a new tag per build (e.g. git SHA); prevents silently overwriting a tag already deployed

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Name = "${var.project_name}-${each.value}"
  }
}

# Untagged layers (leftover from failed/superseded builds) expire fast;
# tagged images are capped at max_image_count so ECR storage doesn't grow
# unbounded while you're iterating.
resource "aws_ecr_lifecycle_policy" "this" {
  for_each   = aws_ecr_repository.this
  repository = each.value.name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Expire untagged images after 1 day"
        selection = {
          tagStatus   = "untagged"
          countType   = "sinceImagePushed"
          countUnit   = "days"
          countNumber = 1
        }
        action = { type = "expire" }
      },
      {
        rulePriority = 2
        description  = "Keep only the last ${var.max_image_count} tagged images"
        selection = {
          tagStatus     = "tagged"
          tagPrefixList = ["v", "sha-", "latest"]
          countType     = "imageCountMoreThan"
          countNumber   = var.max_image_count
        }
        action = { type = "expire" }
      }
    ]
  })
}
