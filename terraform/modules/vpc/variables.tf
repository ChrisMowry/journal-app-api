variable "vpc_cidr" {
  type        = string
  description = "CIDR block for the VPC"
  default     = "10.0.0.0/16"
}

variable "subnet_cidr" {
  type        = string
  description = "CIDR block for the subnet"
  default     = "10.0.1.0/24"
}

variable "subnet2_cidr" {
  type        = string
  description = "CIDR block for the second subnet (for high availability)"
  default     = "10.0.2.0/24"
}

variable "service_name" {
  type = string
}

variable "environment" {
  type = string
}

