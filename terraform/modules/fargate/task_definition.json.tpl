[
  {
      "name": "${task_name}",
      "image": "${task_image}",
      "essential": true,
      "cpu": ${task_cpu_primary},
      "memory": ${task_memory_primary},
      "environment": [
        {
          "name"  = "AWS_REGION"
          "value" = "${aws_region}"
        },
        {
          "name": "ENV",
          "value": "${environment_name}"
        },
        {
          "name"  = "SPRING_PROFILES_ACTIVE"
          "value" = "${environment_name}"
        },
        {
          "name"  = "DYNAMODB_TABLE_NAME"
          "value" = "${dynamodb_table_name}"
        },
        {
          "name"  = "S3_BUCKET_NAME"
          "value" = "${s3_bucket_name}"
        },
        {
          "name"  = "COGNITO_USER_POOL_ID"
          "value" = "${cognito_user_pool_id}"
        }
      ],
    "portMappings": [
      {
        "containerPort": ${task_port},
        "hostPort": ${task_port},
        "protocol": "tcp"
      },
      {
        "containerPort": ${admin_port},
        "hostPort": ${admin_port},
        "protocol": "tcp"
      }
    ],
    "logConfiguration" : {
      "logDriver" : "awslogs"
      "options" : {
        "awslogs-group": "${cloudwatch_log_group}"
        "awslogs-region" : "${aws_region}"
        "awslogs-stream-prefix" = "ecs"
      }
    }
  }
]
