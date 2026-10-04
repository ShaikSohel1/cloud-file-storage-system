provider "aws" {
  region = var.aws_region
}

# VPC Configuration
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "5.5.0"

  name = "cloudstorage-enterprise-vpc"
  cidr = "10.0.0.0/16"

  azs             = ["${var.aws_region}a", "${var.aws_region}b", "${var.aws_region}c"]
  private_subnets = ["10.0.1.0/24", "10.0.2.0/24", "10.0.3.0/24"]
  public_subnets  = ["10.0.101.0/24", "10.0.102.0/24", "10.0.103.0/24"]

  enable_nat_gateway = true
  single_nat_gateway = true

  tags = {
    Environment = "Production"
    Project     = "CloudStorage"
  }
}

# EKS Cluster for Microservices
module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "20.2.1"

  cluster_name    = "cloudstorage-prod-cluster"
  cluster_version = "1.29"

  vpc_id                   = module.vpc.vpc_id
  subnet_ids               = module.vpc.private_subnets
  cluster_endpoint_public_access = true

  eks_managed_node_groups = {
    general = {
      desired_size = 3
      min_size     = 3
      max_size     = 10
      instance_types = ["t3.large"]
    }
  }
}

# RDS PostgreSQL Database (Supabase Alternative for Core Data)
module "db" {
  source  = "terraform-aws-modules/rds/aws"
  version = "6.4.0"

  identifier = "cloudstorage-prod-db"
  engine     = "postgres"
  engine_version = "16.1"
  instance_class = "db.m5.xlarge"
  allocated_storage = 100

  db_name  = "cloudstorage"
  username = "dbadmin"
  port     = 5432

  vpc_security_group_ids = [module.vpc.default_security_group_id]
  subnet_ids             = module.vpc.private_subnets
}

# ElastiCache Redis Cluster
resource "aws_elasticache_cluster" "redis" {
  cluster_id           = "cloudstorage-redis"
  engine               = "redis"
  node_type            = "cache.t4g.micro"
  num_cache_nodes      = 1
  parameter_group_name = "default.redis7"
  engine_version       = "7.1"
  port                 = 6379
  subnet_group_name    = aws_elasticache_subnet_group.redis.name
}

resource "aws_elasticache_subnet_group" "redis" {
  name       = "cloudstorage-redis-subnet"
  subnet_ids = module.vpc.private_subnets
}
