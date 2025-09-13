# Kafka Redis Test Project

Kafka와 Redis를 사용하는 Java Spring Boot 테스트 프로젝트입니다.

## 📋 프로젝트 개요

이 프로젝트는 **Apache Kafka**와 **Redis**를 연동하여 메시지 스트리밍과 캐싱 기능을 구현한 Spring Boot 애플리케이션입니다. 초보 개발자도 쉽게 이해하고 사용할 수 있도록 상세한 주석과 로깅이 포함되어 있습니다.

### 🎯 학습 목표
- Kafka 메시지 발행/구독 패턴 이해
- Redis 캐싱 전략 학습
- Spring Boot와 외부 시스템 연동 방법
- REST API 설계 및 구현
- 로깅 및 모니터링 기법

## ✨ 주요 기능

- **Kafka Producer**: 메시지를 Kafka 토픽으로 전송 (비동기 처리)
- **Kafka Consumer**: Kafka 토픽에서 메시지를 소비하고 처리
- **Redis Cache**: 데이터를 Redis에 캐싱 (만료 시간 설정 가능)
- **REST API**: Kafka 및 Redis 테스트를 위한 HTTP 엔드포인트
- **통합 테스트**: Kafka와 Redis를 동시에 사용하는 시나리오
- **상세한 로깅**: 모든 작업에 대한 디버그 로그 및 오류 추적

## 🛠️ 기술 스택

### Backend
- **Java 17**: 최신 LTS 버전의 Java
- **Spring Boot 3.2.0**: 마이크로서비스 개발을 위한 프레임워크
- **Spring Kafka**: Kafka 연동을 위한 Spring 모듈
- **Spring Data Redis**: Redis 연동을 위한 Spring 모듈

### Database & Message Queue
- **Apache Kafka**: 분산 스트리밍 플랫폼
- **Redis**: 인메모리 데이터 구조 저장소

### Build & Test
- **Maven**: 의존성 관리 및 빌드 도구
- **JUnit 5**: 단위 테스트 프레임워크
- **Lombok**: 보일러플레이트 코드 제거

### Development Tools
- **SLF4J + Logback**: 로깅 프레임워크
- **Jackson**: JSON 직렬화/역직렬화

## 📁 프로젝트 구조

```
src/
├── main/
│   ├── java/com/example/kafkaredis/
│   │   ├── config/                    # 설정 클래스들
│   │   │   ├── KafkaConfig.java      # Kafka 브로커 및 토픽 설정
│   │   │   └── RedisConfig.java      # Redis 연결 및 직렬화 설정
│   │   ├── controller/               # REST API 컨트롤러
│   │   │   └── TestController.java   # 테스트용 API 엔드포인트
│   │   ├── service/                  # 비즈니스 로직
│   │   │   ├── KafkaProducerService.java  # Kafka 메시지 발행
│   │   │   ├── KafkaConsumerService.java  # Kafka 메시지 구독
│   │   │   └── RedisService.java     # Redis 캐시 관리
│   │   └── KafkaRedisApplication.java # 메인 애플리케이션 클래스
│   └── resources/
│       └── application.yml           # 애플리케이션 설정
└── test/
    ├── java/                         # 테스트 코드
    │   └── com/example/kafkaredis/
    │       ├── KafkaRedisApplicationTest.java
    │       └── service/
    │           ├── KafkaProducerServiceTest.java
    │           └── RedisServiceTest.java
    └── resources/
        └── application-test.yml      # 테스트용 설정
```

### 📝 각 패키지 설명

- **config**: Spring Bean 설정 및 외부 시스템 연결 설정
- **controller**: HTTP 요청을 처리하는 REST API 엔드포인트
- **service**: 비즈니스 로직과 외부 시스템과의 상호작용
- **test**: 단위 테스트 및 통합 테스트 코드

## 🚀 실행 방법

### 1. 사전 요구사항

#### 필수 소프트웨어
- **Java 17** 이상
- **Maven 3.6** 이상
- **Docker** (Kafka, Redis 실행용)

#### Docker를 사용한 인프라 실행

```bash
# 1. Zookeeper 실행 (Kafka 의존성)
docker run -d --name zookeeper \
  -p 2181:2181 \
  zookeeper:3.7

# 2. Kafka 실행
docker run -d --name kafka \
  -p 9092:9092 \
  -e KAFKA_ZOOKEEPER_CONNECT=localhost:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  confluentinc/cp-kafka:latest

# 3. Redis 실행
docker run -d --name redis \
  -p 6379:6379 \
  redis:latest
```

#### Docker Compose 사용 (권장)

프로젝트에 포함된 `docker-compose.yml` 파일을 사용하여 한 번에 실행:

```bash
# 모든 서비스 실행 (백그라운드)
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 서비스 상태 확인
docker-compose ps

# 서비스 중지
docker-compose down

# 데이터까지 삭제 (주의!)
docker-compose down -v
```

**포함된 서비스:**
- **Zookeeper** (포트 2181): Kafka 메타데이터 관리
- **Kafka** (포트 9092): 메시지 스트리밍 플랫폼
- **Redis** (포트 6379): 인메모리 데이터 저장소
- **Kafka UI** (포트 8080): Kafka 관리 웹 인터페이스 (선택사항)

> **⚠️ 주의**: Kafka UI가 포트 8080을 사용하므로, Spring Boot 애플리케이션은 포트 8081에서 실행됩니다.  
> **참고**: Kafka UI는 http://localhost:8080 에서 접근 가능하며, Kafka 토픽과 메시지를 시각적으로 확인할 수 있습니다.

### 3. 서비스 상태 확인

Docker Compose로 실행한 후 각 서비스가 정상적으로 동작하는지 확인하는 방법:

#### 📊 전체 서비스 상태 확인
```bash
# 모든 컨테이너 상태 확인
docker-compose ps

# 예상 결과:
# NAME                    STATUS
# kafka-redis-kafka       Up (healthy)
# kafka-redis-redis       Up (healthy)  
# kafka-redis-zookeeper   Up
# kafka-redis-ui          Up
```

#### 🔍 개별 서비스 상태 확인

**Zookeeper 확인:**
```bash
# Zookeeper 연결 테스트
docker exec kafka-redis-zookeeper zkCli.sh -server localhost:2181 <<< "ls /"

# 또는 간단한 상태 확인 (포트 연결 테스트)
docker exec kafka-redis-zookeeper bash -c "nc -z localhost 2181 && echo 'Zookeeper port 2181 is open'"

# Zookeeper 로그 확인
docker exec kafka-redis-zookeeper bash -c "tail -5 /datalog/zookeeper.out"
```

**Kafka 확인:**
```bash
# Kafka 토픽 목록 확인 (연결 테스트)
docker exec kafka-redis-kafka kafka-topics --bootstrap-server localhost:9092 --list

# Kafka 브로커 정보 확인
docker exec kafka-redis-kafka kafka-broker-api-versions --bootstrap-server localhost:9092

# 테스트 토픽 생성 및 삭제
docker exec kafka-redis-kafka kafka-topics --bootstrap-server localhost:9092 --create --topic test-connection --partitions 1 --replication-factor 1
docker exec kafka-redis-kafka kafka-topics --bootstrap-server localhost:9092 --delete --topic test-connection
```

**Redis 확인:**
```bash
# Redis 연결 테스트
docker exec kafka-redis-redis redis-cli ping
# 정상이면 "PONG" 응답

# Redis 정보 확인
docker exec kafka-redis-redis redis-cli info server

# 간단한 데이터 저장/조회 테스트
docker exec kafka-redis-redis redis-cli set test-key "Hello Redis"
docker exec kafka-redis-redis redis-cli get test-key
docker exec kafka-redis-redis redis-cli del test-key

# 예상 결과:
# PONG
# OK
# Hello Redis
# 1
```

**Kafka UI 확인:**
```bash
# Kafka UI 접근 확인
curl -f http://localhost:8080/actuator/health || echo "Kafka UI 접근 불가"
# 정상이면 {"status":"UP"} 응답

# 브라우저에서 http://localhost:8080 접속하여 웹 인터페이스 확인
```

#### 🚨 문제 해결

**서비스가 시작되지 않는 경우:**
```bash
# 특정 서비스 로그 확인
docker-compose logs kafka
docker-compose logs redis
docker-compose logs zookeeper
docker-compose logs kafka-ui

# 모든 서비스 재시작
docker-compose restart

# 완전히 재시작 (데이터 유지)
docker-compose down && docker-compose up -d

# 데이터까지 삭제하고 재시작
docker-compose down -v && docker-compose up -d
```

**포트 충돌 확인:**
```bash
# 포트 사용 현황 확인
lsof -i :2181  # Zookeeper
lsof -i :6379  # Redis  
lsof -i :8080  # Kafka UI
lsof -i :9092  # Kafka
```

### 2. 애플리케이션 실행

```bash
# 프로젝트 클론 후
cd kafkaRedisProject

# 의존성 다운로드 및 컴파일
mvn clean compile

# 애플리케이션 실행 (포트 8081에서 실행)
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

> **참고**: Docker Compose를 사용하는 경우 Kafka UI가 포트 8080을 사용하므로, Spring Boot 애플리케이션은 포트 8081에서 실행됩니다.

### 3. 테스트 실행

```bash
# 단위 테스트 실행
mvn test

# 특정 테스트 클래스 실행
mvn test -Dtest=RedisServiceTest
```

### 4. 애플리케이션 확인

애플리케이션이 정상적으로 실행되면 다음 로그를 확인할 수 있습니다:

```
=== Kafka-Redis 애플리케이션 시작 ===
Kafka Admin 클라이언트 초기화 중...
Redis 템플릿 초기화 중...
=== Kafka-Redis 애플리케이션이 성공적으로 시작되었습니다 ===
```

## 🔌 API 엔드포인트

### 📤 Kafka 테스트

| 메서드 | 엔드포인트 | 설명 | 매개변수 |
|--------|------------|------|----------|
| `POST` | `/api/test/kafka/send` | Kafka 토픽에 메시지 전송 | `topic` (필수), `key` (선택) |
| `POST` | `/api/test/integration/test` | Kafka + Redis 통합 테스트 | `userId` (필수) |

### 💾 Redis 테스트

| 메서드 | 엔드포인트 | 설명 | 매개변수 |
|--------|------------|------|----------|
| `POST` | `/api/test/redis/set` | Redis에 값 저장 | `key` (필수) |
| `GET` | `/api/test/redis/get` | Redis에서 값 조회 | `key` (필수) |

### 🏥 시스템

| 메서드 | 엔드포인트 | 설명 | 응답 |
|--------|------------|------|------|
| `GET` | `/api/test/health` | 애플리케이션 상태 확인 | `{"status": "UP", "service": "kafka-redis-test"}` |

## 📖 사용 예제

### 1. 🏥 헬스 체크

애플리케이션이 정상적으로 실행되고 있는지 확인:

```bash
curl -X GET "http://localhost:8081/api/test/health"
```

**응답 예시:**
```json
{
  "status": "UP",
  "service": "kafka-redis-test",
  "timestamp": "1703123456789"
}
```

### 2. 📤 Kafka 메시지 전송

#### 키와 함께 메시지 전송
```bash
curl -X POST "http://localhost:8081/api/test/kafka/send?topic=test-topic&key=user123" \
  -H "Content-Type: application/json" \
  -d "Hello Kafka! This is a test message."
```

#### 키 없이 메시지 전송
```bash
curl -X POST "http://localhost:8081/api/test/kafka/send?topic=test-topic" \
  -H "Content-Type: application/json" \
  -d "Hello Kafka! This is a test message without key."
```

**응답 예시:**
```
Message sent to Kafka topic: test-topic
```

### 3. 💾 Redis 값 저장/조회

#### 값 저장
```bash
curl -X POST "http://localhost:8081/api/test/redis/set?key=mykey" \
  -H "Content-Type: application/json" \
  -d "Hello Redis! This is cached data."
```

**응답 예시:**
```
Value set in Redis for key: mykey
```

#### 값 조회
```bash
curl -X GET "http://localhost:8081/api/test/redis/get?key=mykey"
```

**응답 예시:**
```
Hello Redis! This is cached data.
```

### 4. 🔄 통합 테스트

Kafka와 Redis를 동시에 사용하는 통합 테스트:

```bash
curl -X POST "http://localhost:8081/api/test/integration/test?userId=user123" \
  -H "Content-Type: application/json" \
  -d "Integration test data for user 123"
```

**응답 예시:**
```json
{
  "status": "success",
  "message": "Data sent to Kafka and stored in Redis",
  "userId": "user123",
  "redisKey": "test:user:user123"
}
```

### 5. 📊 로그 확인

애플리케이션 실행 중 다음과 같은 로그를 확인할 수 있습니다:

```
=== Kafka 메시지 발행 API 호출 ===
요청 정보 - 토픽: test-topic, 키: user123, 메시지 길이: 25
Kafka 메시지 발행 시작 - 토픽: test-topic, 키: user123, 메시지 타입: String
메시지 JSON 변환 완료: "Hello Kafka! This is a test message."
메시지 발행 성공 - 토픽: test-topic, 파티션: 0, 오프셋: 1, 메시지: "Hello Kafka! This is a test message."
```

## ⚙️ 설정

### application.yml 주요 설정

```yaml
server:
  port: 8081  # Kafka UI와 포트 충돌 방지

spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: test-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer

  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2000ms

logging:
  level:
    com.example.kafkaredis: DEBUG
    org.springframework.kafka: INFO
```

### 🔧 설정 변경 가이드

| 설정 항목 | 설명 | 기본값 | 변경 방법 |
|-----------|------|--------|-----------|
| `server.port` | 애플리케이션 포트 | 8081 | `application.yml` 수정 |
| `spring.kafka.bootstrap-servers` | Kafka 브로커 주소 | localhost:9092 | Kafka 서버 주소 변경 |
| `spring.data.redis.host` | Redis 호스트 | localhost | Redis 서버 주소 변경 |
| `spring.data.redis.port` | Redis 포트 | 6379 | Redis 포트 변경 |
| `logging.level` | 로깅 레벨 | INFO | DEBUG, WARN, ERROR 등 |

## 📋 토픽 구성

애플리케이션 시작 시 자동으로 생성되는 Kafka 토픽:

| 토픽명 | 파티션 수 | 복제 팩터 | 용도 |
|--------|-----------|-----------|------|
| `test-topic` | 3 | 1 | 일반 테스트용 메시지 |
| `user-events` | 3 | 1 | 사용자 이벤트 메시지 |

### 토픽 생성 확인

Kafka가 실행 중일 때 토픽 목록을 확인할 수 있습니다:

```bash
# Docker로 실행한 Kafka 컨테이너에 접속
docker exec -it kafka bash

# 토픽 목록 확인
kafka-topics --bootstrap-server localhost:9092 --list

# 특정 토픽 상세 정보 확인
kafka-topics --bootstrap-server localhost:9092 --describe --topic test-topic
```

## 🐛 문제 해결

### 자주 발생하는 문제들

#### 1. Kafka 연결 실패
```
Connection refused: localhost:9092
```
**해결 방법:**
- Kafka가 정상적으로 실행되고 있는지 확인
- Docker 컨테이너 상태 확인: `docker ps`
- 포트 9092가 사용 가능한지 확인

#### 2. Redis 연결 실패
```
Connection refused: localhost:6379
```
**해결 방법:**
- Redis가 정상적으로 실행되고 있는지 확인
- Docker 컨테이너 상태 확인: `docker ps`
- 포트 6379가 사용 가능한지 확인

#### 3. Lombok 관련 컴파일 오류
```
cannot find symbol: variable log
```
**해결 방법:**
- IDE에서 Lombok 플러그인 설치 및 활성화
- Annotation Processing 활성화
- 프로젝트 재빌드: `mvn clean compile`

#### 4. 포트 충돌
```
Port 8081 was already in use
```
**해결 방법:**
- `application.yml`에서 `server.port` 변경
- 기존 프로세스 종료: `lsof -ti:8081 | xargs kill -9`
- Docker Compose 사용 시 Kafka UI와 포트 충돌 확인

### 로그 레벨 조정

문제 진단을 위해 로그 레벨을 조정할 수 있습니다:

```yaml
logging:
  level:
    com.example.kafkaredis: DEBUG  # 애플리케이션 로그
    org.springframework.kafka: DEBUG  # Kafka 관련 로그
    org.springframework.data.redis: DEBUG  # Redis 관련 로그
    org.apache.kafka: WARN  # Kafka 클라이언트 로그
```

## 📚 추가 학습 자료

### Kafka 관련
- [Apache Kafka 공식 문서](https://kafka.apache.org/documentation/)
- [Spring Kafka Reference](https://docs.spring.io/spring-kafka/docs/current/reference/html/)

### Redis 관련
- [Redis 공식 문서](https://redis.io/documentation)
- [Spring Data Redis Reference](https://docs.spring.io/spring-data/redis/docs/current/reference/html/)

### Spring Boot 관련
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring Boot Guides](https://spring.io/guides)

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 `LICENSE` 파일을 참조하세요.

## 👥 작성자

- **개발자** - *초기 작업* - [GitHub](https://github.com/yourusername)

## 🙏 감사의 말

- Spring Boot 팀
- Apache Kafka 팀
- Redis 팀
- 모든 오픈소스 기여자들