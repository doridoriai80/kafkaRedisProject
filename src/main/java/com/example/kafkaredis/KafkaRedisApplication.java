package com.example.kafkaredis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Kafka와 Redis를 연동한 Spring Boot 애플리케이션의 메인 클래스
 * 
 * 이 애플리케이션은 다음과 같은 기능을 제공합니다:
 * - Kafka 메시지 발행 및 구독
 * - Redis 캐시 저장 및 조회
 * - REST API를 통한 테스트 엔드포인트 제공
 * 
 * @author 개발자
 * @version 1.0
 */
@EnableKafka  // Kafka 기능을 활성화하는 어노테이션
@SpringBootApplication  // Spring Boot 애플리케이션임을 나타내는 어노테이션
public class KafkaRedisApplication {

    /**
     * 로깅을 위한 Logger 인스턴스
     */
    private static final Logger log = LoggerFactory.getLogger(KafkaRedisApplication.class);

    /**
     * 애플리케이션의 진입점(Entry Point)
     * 
     * @param args 명령행 인수 (현재는 사용하지 않음)
     */
    public static void main(String[] args) {
        log.info("=== Kafka-Redis 애플리케이션 시작 ===");
        log.info("애플리케이션 초기화 중...");
        
        try {
            // Spring Boot 애플리케이션을 실행합니다
            SpringApplication.run(KafkaRedisApplication.class, args);
            log.info("=== Kafka-Redis 애플리케이션이 성공적으로 시작되었습니다 ===");
        } catch (Exception e) {
            log.error("애플리케이션 시작 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
}