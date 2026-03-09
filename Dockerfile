FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Gradle 커맨드 최적화를 위해 wrapper 관련 파일만 우선 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# 프로젝트 소스코드 복사 전 의존성 다운로드 캐싱
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

# 전체 소스 복사 및 jar 빌드 (테스트 스킵)
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# -- 런타임 이미지 --
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 서울 타임존 설정
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    apk del tzdata

# Builder 단계에서 만든 jar 파일만 가져오기
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
