FROM openjdk:17-jdk-slim

WORKDIR /app

# 의존성 관리를 위해 Gradle Wrapper 복사
COPY gradle gradle
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .

# 의존성 다운로드 (캐싱 최적화)
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src src

# 애플리케이션 빌드
RUN ./gradlew build --no-daemon -x test

# 로그 디렉터리 생성
RUN mkdir -p logs/responses

# 애플리케이션 실행
EXPOSE 8080

CMD ["java", "-jar", "build/libs/krx-api-explorer-1.0.0.jar"]