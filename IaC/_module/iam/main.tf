data "aws_iam_policy_document" "ecs_task_execution_assume_role" {
  statement {
    effect    = "Allow"
    actions   = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "ecs_task_execution" {
  name               = "${var.environment}-${var.region_prefix}-ecs-exec-role"
  assume_role_policy = data.aws_iam_policy_document.ecs_task_execution_assume_role.json
}

data "aws_iam_policy_document" "ecs_exec_policy_doc" {
  statement {
    effect    = "Allow"
    actions   = [
      "ssm:GetParameter",
      "ssm:GetParameters",
      "ssm:GetParametersByPath",
      "ssm:DescribeParameters"
    ]
    resources = [
      "arn:aws:ssm:${var.aws_region}:${var.account_id}:parameter/${var.ssm_prefix}/*"
    ]
  }
}

resource "aws_iam_role_policy" "ecs_exec_policy" {
  name   = "${var.environment}-${var.region_prefix}-ecs-exec-policy"
  role   = aws_iam_role.ecs_task_execution.id
  policy = data.aws_iam_policy_document.ecs_exec_policy_doc.json
}

resource "aws_iam_role_policy_attachment" "ecs_exec_attachment" {
  role       = aws_iam_role.ecs_task_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

data "aws_iam_policy_document" "notification_task_assume_role" {
  statement {
    effect    = "Allow"
    actions   = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "notification_task" {
  name               = "${var.environment}-${var.region_prefix}-noti-task-role"
  assume_role_policy = data.aws_iam_policy_document.notification_task_assume_role.json
}

data "aws_iam_policy_document" "notification_policy_doc" {
  statement {
    effect    = "Allow"
    actions   = [
      "ses:SendEmail",
      "ses:SendRawEmail"
    ]
    resources = ["*"]
  }
  statement {
    effect    = "Allow"
    actions   = [
      "es:ESHttpPost",
      "es:ESHttpPut",
      "es:ESHttpGet"
    ]
    resources = [
      "arn:aws:es:${var.aws_region}:${var.account_id}:domain/${var.opensearch_domain}/*"
    ]
  }
}

resource "aws_iam_role_policy" "notification_policy" {
  name   = "${var.environment}-${var.region_prefix}-noti-task-policy"
  role   = aws_iam_role.notification_task.id
  policy = data.aws_iam_policy_document.notification_policy_doc.json
}

data "aws_iam_policy_document" "ecs_task_assume_role" {
  statement {
    effect    = "Allow"
    actions   = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "ecs_task" {
  name               = "${var.environment}-${var.region_prefix}-ecs-task-role"
  assume_role_policy = data.aws_iam_policy_document.ecs_task_assume_role.json
}

data "aws_iam_policy_document" "es_policy_doc" {
  statement {
    effect    = "Allow"
    actions   = [
      "es:ESHttpPost",
      "es:ESHttpPut",
      "es:ESHttpGet"
    ]
    resources = [
      "arn:aws:es:${var.aws_region}:${var.account_id}:domain/${var.opensearch_domain}/*"
    ]
  }
}

resource "aws_iam_role_policy" "search_es_get_policy" {
  name   = "${var.environment}-${var.region_prefix}-es-get-policy"
  role   = aws_iam_role.ecs_task.id
  policy = data.aws_iam_policy_document.es_policy_doc.json
}

data "aws_iam_policy_document" "bastion_es_assume_role" {
  statement {
    effect    = "Allow"
    actions   = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "bastion_es" {
  name               = "${var.environment}-${var.region_prefix}-bastion-es-role"
  assume_role_policy = data.aws_iam_policy_document.bastion_es_assume_role.json
}

data "aws_iam_policy_document" "bastion_es_policy_doc" {
  statement {
    effect    = "Allow"
    actions   = [
      "es:ESHttpGet",
      "es:ESHttpPost",
      "es:ESHttpPut",
      "es:ESHttpDelete"
    ]
    resources = [
      "arn:aws:es:${var.aws_region}:${var.account_id}:domain/${var.opensearch_domain}/*",
      "arn:aws:es:${var.aws_region}:${var.account_id}:domain/*"
    ]
  }
}

resource "aws_iam_role_policy" "bastion_es_policy" {
  name   = "${var.environment}-${var.region_prefix}-bastion-es-policy"
  role   = aws_iam_role.bastion_es.id
  policy = data.aws_iam_policy_document.bastion_es_policy_doc.json
}

data "aws_iam_policy_document" "lambda_assume_role" {
  statement {
    effect    = "Allow"
    actions   = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "lambda_role" {
  name               = "${var.environment}-${var.region_prefix}-lambda-role"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume_role.json
}

data "aws_iam_policy_document" "stream_watch_trigger_policy_doc" {
  statement {
    effect    = "Allow"
    actions   = [
      "dynamodb:GetRecords",
      "dynamodb:GetShardIterator",
      "dynamodb:DescribeStream",
      "dynamodb:ListStreams"
    ]
    resources = [
      "arn:aws:dynamodb:${var.aws_region}:${var.account_id}:table/${var.dynamodb_table}/stream/*"
    ]
  }
  statement {
    effect    = "Allow"
    actions   = ["lambda:InvokeFunction"]
    resources = [
      "arn:aws:lambda:${var.aws_region}:${var.account_id}:function:${var.lambda_function_name}"
    ]
  }
}

resource "aws_iam_role_policy" "stream_watch_trigger_policy" {
  name   = "${var.environment}-${var.region_prefix}-stream-wathch-policy"
  role   = aws_iam_role.lambda_role.id
  policy = data.aws_iam_policy_document.stream_watch_trigger_policy_doc.json
}

data "aws_iam_policy_document" "es_access_policy_doc" {
  statement {
    effect    = "Allow"
    actions   = [
      "es:ESHttpGet",
      "es:ESHttpPost",
      "es:ESHttpPut",
      "es:ESHttpDelete"
    ]
    resources = [
      "arn:aws:es:${var.aws_region}:${var.account_id}:domain/${var.opensearch_domain}/*"
    ]
  }
}

resource "aws_iam_role_policy" "opensearch_access_policy" {
  name   = "${var.environment}-${var.region_prefix}-lambda-es-policy"
  role   = aws_iam_role.lambda_role.id
  policy = data.aws_iam_policy_document.es_access_policy_doc.json
}