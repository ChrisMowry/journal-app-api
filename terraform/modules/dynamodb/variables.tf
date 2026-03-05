variable "service_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "read_capacity" {
  type        = number
  description = "Read capacity units"
  default     = 5
}

variable "write_capacity" {
  type        = number
  description = "Write capacity units"
  default     = 5
}

