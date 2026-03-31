output "vpc_id" {
  value = aws_vpc.ecs_task_vpc.id
}

output "subnet_id" {
  value = aws_subnet.ecs_task_subnet.id
}

output "subnet2_id" {
  value = aws_subnet.ecs_task_subnet2.id
}

output "ecs_security_group_id" {
  value = aws_security_group.ecs_security_group.id
}

output "alb_security_group_id" {
  value = aws_security_group.alb_security_group.id
}
