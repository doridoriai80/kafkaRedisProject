package com.example.kafkaredis.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka 설정을 담당하는 Configuration 클래스
 * 
 * 이 클래스는 다음과 같은 역할을 합니다:
 * - Kafka Admin 클라이언트 설정
 * - 필요한 토픽들을 자동으로 생성
 * - Kafka 브로커 연결 정보 관리
 * 
 * @author 개발자
 * @version 1.0
 */
@Configuration  // Spring 설정 클래스임을 나타내는 어노테이션
public class KafkaConfig {

    /**
     * 로깅을 위한 Logger 인스턴스
     */
    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);

    /**
     * application.yml에서 설정된 Kafka 브로커 서버 주소
     * 예: localhost:9092
     */
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Kafka Admin 클라이언트를 생성하는 Bean
     * 
     * Kafka Admin은 토픽 생성, 삭제, 조회 등의 관리 작업을 수행합니다.
     * 
     * @return KafkaAdmin 인스턴스
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        log.info("Kafka Admin 클라이언트 초기화 중...");
        log.debug("Kafka 브로커 서버: {}", bootstrapServers);
        
        // Kafka Admin 설정을 위한 Map 생성
        Map<String, Object> configs = new HashMap<>();
        // 브로커 서버 주소 설정
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        KafkaAdmin kafkaAdmin = new KafkaAdmin(configs);
        log.info("Kafka Admin 클라이언트가 성공적으로 초기화되었습니다");
        
        return kafkaAdmin;
    }

    /**
     * 테스트용 토픽을 생성하는 Bean
     * 
     * 이 토픽은 일반적인 메시지 테스트에 사용됩니다.
     * - 파티션 수: 3개 (메시지 분산 처리)
     * - 복제 팩터: 1개 (단일 브로커 환경용)
     * 
     * @return NewTopic 인스턴스
     */
    @Bean
    public NewTopic testTopic() {
        log.info("테스트 토픽 'test-topic' 생성 중... (파티션: 3, 복제팩터: 1)");
        return new NewTopic("test-topic", 3, (short) 1);
    }

    /**
     * 사용자 이벤트용 토픽을 생성하는 Bean
     * 
     * 이 토픽은 사용자 관련 이벤트 메시지를 처리합니다.
     * - 파티션 수: 3개 (사용자 ID 기반 분산 처리)
     * - 복제 팩터: 1개 (단일 브로커 환경용)
     * 
     * @return NewTopic 인스턴스
     */
    @Bean
    public NewTopic userEventsTopic() {
        log.info("사용자 이벤트 토픽 'user-events' 생성 중... (파티션: 3, 복제팩터: 1)");
        return new NewTopic("user-events", 3, (short) 1);
    }
}