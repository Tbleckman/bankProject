#!/usr/bin/env bash
# Run this ONCE after `terraform apply` in environments/dev has created the
# ECR repos, but before you expect the ECS service to go healthy.
#
# Why this needs to exist: the ECS task definition references an image tag
# (var.image_tag, default "initial") that has never been pushed anywhere the
# first time you apply. Terraform apply still succeeds - it's just creating
# a task definition, not pulling the image - but the ECS service will sit
# there failing to start tasks until an image with that exact tag exists in
# both ECR repos. This script closes that gap manually, once. After this,
# CI/CD (.github/workflows/deploy.yml) takes over and pushes a new
# git-SHA-tagged image (and a matching task def revision) on every push to
# main, so you won't need to run this again.
#
# Usage:
#   ./infra/bootstrap/push-initial-images.sh

set -euo pipefail

PROJECT_NAME="bankproject"
AWS_REGION="us-east-1"
IMAGE_TAG="initial" # must match var.image_tag in environments/dev

ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
REGISTRY="${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

echo "Logging in to ECR ($REGISTRY)..."
aws ecr get-login-password --region "$AWS_REGION" \
  | docker login --username AWS --password-stdin "$REGISTRY"

echo "Building + pushing app image..."
docker build -t "$REGISTRY/$PROJECT_NAME/app:$IMAGE_TAG" .
docker push "$REGISTRY/$PROJECT_NAME/app:$IMAGE_TAG"

echo "Building + pushing risk-service image..."
docker build -t "$REGISTRY/$PROJECT_NAME/risk-service:$IMAGE_TAG" ./risk_service
docker push "$REGISTRY/$PROJECT_NAME/risk-service:$IMAGE_TAG"

echo "Done. The ECS service should reach steady state within a couple minutes."
echo "Check with: aws ecs describe-services --cluster ${PROJECT_NAME}-cluster --services ${PROJECT_NAME}-service --region $AWS_REGION"
