package com.example.kafkaredis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Kafka 메시지 구독을 담당하는 서비스 클래스
 * 
 * 이 클래스는 다음과 같은 기능을 제공합니다:
 * - test-topic에서 일반 메시지 구독 및 처리
 * - user-events 토픽에서 사용자 이벤트 구독 및 Redis 캐싱
 * - 메시지 처리 결과에 따른 수동 커밋 처리
 * - 오류 발생 시 상세한 로깅
 * 
 * @author 개발자
 * @version 1.0
 */
@Service  // Spring 서비스 컴포넌트임을 나타내는 어노테이션
public class KafkaConsumerService {

    /**
     * 로깅을 위한 Logger 인스턴스
     */
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    /**
     * JSON 역직렬화를 위한 매퍼
     */
    private final ObjectMapper objectMapper;
    
    /**
     * Redis 캐싱을 위한 서비스
     */
    private final RedisService redisService;

    /**
     * 생성자 주입을 통한 의존성 주입
     */
    public KafkaConsumerService(ObjectMapper objectMapper, RedisService redisService) {
        this.objectMapper = objectMapper;
        this.redisService = redisService;
    }

    /**
     * test-topic에서 메시지를 구독하고 처리합니다.
     * 
     * 이 메서드는 test-topic에서 발행된 메시지를 수신하여 처리합니다.
     * 메시지 처리 후 수동으로 커밋을 수행하여 메시지 처리 완료를 보장합니다.
     * 
     * @param message 수신된 메시지 내용
     * @param topic 메시지가 수신된 토픽 이름
     * @param partition 메시지가 수신된 파티션 번호
     * @param offset 메시지의 오프셋 값
     * @param acknowledgment 수동 커밋을 위한 acknowledgment 객체
     */
    @KafkaListener(topics = "test-topic", groupId = "test-group")
    public void consumeTestMessage(@Payload String message,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                 @Header(KafkaHeaders.OFFSET) long offset,
                                 Acknowledgment acknowledgment) {
        log.info("=== 테스트 메시지 수신 시작 ===");
        log.info("메시지 수신 정보 - 토픽: {}, 파티션: {}, 오프셋: {}", 
                topic, partition, offset);
        log.debug("수신된 메시지 내용: {}", message);
        
        try {
            // 메시지 처리 시작
            log.debug("메시지 처리 시작...");
            processMessage(message);
            log.debug("메시지 처리 완료");
            
            // 메시지 처리 성공 시 커밋 수행
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.debug("메시지 커밋 완료 - 오프셋: {}", offset);
            } else {
                log.warn("Acknowledgment가 null입니다. 자동 커밋이 사용될 수 있습니다.");
            }
            
            log.info("=== 테스트 메시지 처리 완료 ===");
            
        } catch (Exception e) {
            log.error("테스트 메시지 처리 중 오류 발생 - 메시지: {}, 오류: {}", 
                    message, e.getMessage(), e);
            // 오류 발생 시 커밋하지 않음 (메시지 재처리를 위해)
            log.warn("메시지 처리 실패로 인해 커밋하지 않습니다. 메시지가 재처리될 수 있습니다.");
        }
    }

    /**
     * user-events 토픽에서 사용자 이벤트 메시지를 구독하고 Redis에 캐싱합니다.
     * 
     * 이 메서드는 사용자 관련 이벤트를 수신하여 Redis에 캐싱합니다.
     * 사용자 ID를 키로 하여 24시간 동안 이벤트 데이터를 저장합니다.
     * 
     * @param message 수신된 사용자 이벤트 메시지
     * @param key 메시지 키 (사용자 ID)
     * @param topic 메시지가 수신된 토픽 이름
     * @param acknowledgment 수동 커밋을 위한 acknowledgment 객체
     */
    @KafkaListener(topics = "user-events", groupId = "user-group")
    public void consumeUserEvent(@Payload String message,
                               @Header(KafkaHeaders.RECEIVED_KEY) String key,
                               @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                               Acknowledgment acknowledgment) {
        log.info("=== 사용자 이벤트 수신 시작 ===");
        log.info("사용자 이벤트 수신 정보 - 키(사용자ID): {}, 토픽: {}", key, topic);
        log.debug("수신된 이벤트 메시지: {}", message);
        
        try {
            // Redis에 사용자 이벤트 캐싱
            log.debug("Redis에 사용자 이벤트 캐싱 시작 - 사용자ID: {}", key);
            redisService.cacheUserEvent(key, message);
            log.debug("Redis 캐싱 완료 - 사용자ID: {}", key);
            
            // 메시지 처리 성공 시 커밋 수행
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.debug("사용자 이벤트 메시지 커밋 완료 - 사용자ID: {}", key);
            } else {
                log.warn("Acknowledgment가 null입니다. 자동 커밋이 사용될 수 있습니다.");
            }
            
            log.info("=== 사용자 이벤트 처리 완료 - 사용자ID: {} ===", key);
            
        } catch (Exception e) {
            log.error("사용자 이벤트 처리 중 오류 발생 - 사용자ID: {}, 메시지: {}, 오류: {}", 
                    key, message, e.getMessage(), e);
            // 오류 발생 시 커밋하지 않음 (메시지 재처리를 위해)
            log.warn("사용자 이벤트 처리 실패로 인해 커밋하지 않습니다. 메시지가 재처리될 수 있습니다.");
        }
    }

    /**
     * 수신된 메시지를 처리하는 내부 메서드
     * 
     * 현재는 단순히 로깅만 수행하지만, 실제 비즈니스 로직을 추가할 수 있습니다.
     * 예: 데이터베이스 저장, 외부 API 호출, 다른 서비스로 전달 등
     * 
     * @param message 처리할 메시지 내용
     */
    private void processMessage(String message) {
        log.debug("메시지 처리 로직 실행 - 메시지: {}", message);
        
        // TODO: 실제 비즈니스 로직 구현
        // 예시:
        // - 메시지 파싱 및 검증
        // - 데이터베이스 저장
        // - 외부 API 호출
        // - 다른 서비스로 메시지 전달
        
        log.debug("메시지 처리 로직 완료");
    }
}