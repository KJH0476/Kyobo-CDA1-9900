#!/bin/bash

# Authorization Service 빌드 및 푸시
docker build -t hyeok1234565/authorization-service --platform linux/amd64 ./services/AuthorizationService
docker push hyeok1234565/authorization-service

# User Service 빌드 및 푸시
docker build -t hyeok1234565/user-service --platform linux/amd64 ./services/UserService
docker push hyeok1234565/user-service

# Notification Service 빌드 및 푸시
docker build -t hyeok1234565/notification-service --platform linux/amd64 ./services/NotificationService
docker push hyeok1234565/notification-service

# Search Service 빌드 및 푸시
docker build -t hyeok1234565/search-service --platform linux/amd64 ./services/SearchService
docker push hyeok1234565/search-service

# Reservation Service 빌드 및 푸시
docker build -t hyeok1234565/reservation-service --platform linux/amd64 ./services/ReservationService
docker push hyeok1234565/reservation-service
