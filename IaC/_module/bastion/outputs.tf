output "bastion_instance_id" {
  value       = aws_instance.bastion_instance.id
  description = "Bastion Host Instance ID"
}

output "bastion_public_ip" {
  value       = aws_instance.bastion_instance.public_ip
  description = "Bastion Host Public IP Address"
}