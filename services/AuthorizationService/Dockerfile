# 빌드
FROM gradle:7.5.1-jdk17 AS build

WORKDIR /app

COPY build.gradle settings.gradle ./

RUN gradle dependencies --no-daemon

COPY . .

RUN gradle build -x test --no-daemon

RUN rm -rf /app/.gradle /app/build/tmp /app/build/classes /app/build/resources

# 실행
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "app.jar"]