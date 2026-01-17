# ===============================
# Runtime stage only
# ===============================
FROM eclipse-temurin:17-jre
WORKDIR /app

# Jenkins에서 이미 빌드된 jar만 복사
COPY build/libs/*.jar app.jar

ENV TZ=Asia/Seoul
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
