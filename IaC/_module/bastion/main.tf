data "aws_ami" "amazon_linux_2" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["amzn2-ami-hvm-*-x86_64-gp2"]
  }
}

resource "aws_key_pair" "bastion_key_pair" {
  key_name   = "${var.environment}-${var.region_prefix}-${var.key_name}"
  public_key = file(var.public_key_path)

  tags = {
    Name = "${var.environment}-${var.region_prefix}-${var.key_name}"
  }
}

resource "aws_instance" "bastion_instance" {
  ami                    = data.aws_ami.amazon_linux_2.id
  instance_type          = var.instance_type
  key_name               = aws_key_pair.bastion_key_pair.key_name
  subnet_id              = var.public_subnets[0]
  vpc_security_group_ids = var.bastion_security_groups

  lifecycle {
    ignore_changes = [ami]
  }

  tags = {
    Name = "${var.environment}-${var.region_prefix}-bastion-instance"
  }
}