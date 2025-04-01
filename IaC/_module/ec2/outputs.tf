output "instance_id" {
  value       = aws_instance.instance.id
  description = "Bastion Host Instance ID"
}

output "public_ip" {
  value       = aws_instance.instance.public_ip
  description = "Bastion Host Public IP Address"
}