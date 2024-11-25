#!/bin/bash

# 변수 선언
ACCOUNT_ID=324037290341  # 본인의 AWS 계정 ID로 변경
REGION=ap-northeast-2    # 본인의 리전으로 변경
REPO_NAME=repo           # 본인의 리포지토리 이름으로 변경
REPO_PREFIX="${ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com/${REPO_NAME}"

# ECR 로그인
aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin ${ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com

# Authorization Service 빌드 및 푸시
docker build -t ${REPO_PREFIX}/authorization-service:latest --platform linux/amd64 ./services/AuthorizationService
docker push ${REPO_PREFIX}/authorization-service:latest

# User Service 빌드 및 푸시
docker build -t ${REPO_PREFIX}/user-service:latest --platform linux/amd64 ./services/UserService
docker push ${REPO_PREFIX}/user-service:latest

# Notification Service 빌드 및 푸시
docker build -t ${REPO_PREFIX}/notification-service:latest --platform linux/amd64 ./services/NotificationService
docker push ${REPO_PREFIX}/notification-service:latest

# Search Service 빌드 및 푸시
docker build -t ${REPO_PREFIX}/search-service:latest --platform linux/amd64 ./services/SearchService
docker push ${REpo_PREFIX}/search-service:latest

# Reservation Service 빌드 및 푸시
docker build -t ${REPO_PREFIX}/reservation-service:latest --platform linux/amd64 ./services/ReservationService
docker push ${REPO_PREFIX}/reservation-service:latest