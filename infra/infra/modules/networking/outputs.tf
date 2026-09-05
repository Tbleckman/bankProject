output "vpc_id" {
  value = aws_vpc.this.id
}

output "public_subnet_ids" {
  value = aws_subnet.public[*].id
}

output "private_subnet_ids" {
  value = aws_subnet.private[*].id
}

output "alb_sg_id" {
  value = aws_security_group.alb.id
}

output "ecs_app_sg_id" {
  value = aws_security_group.ecs_app.id
}

output "ecs_risk_sg_id" {
  value = aws_security_group.ecs_risk.id
}

output "rds_sg_id" {
  value = aws_security_group.rds.id
}
