package com.example.kafkaredis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka 메시지 발행을 담당하는 서비스 클래스
 * 
 * 이 클래스는 다음과 같은 기능을 제공합니다:
 * - 객체를 JSON으로 변환하여 Kafka에 발행
 * - 키와 함께 메시지 발행 (파티션 분산 처리)
 * - 문자열 메시지 직접 발행
 * - 비동기 발행 결과 처리 및 로깅
 * 
 * @author 개발자
 * @version 1.0
 */
@Service  // Spring 서비스 컴포넌트임을 나타내는 어노테이션
public class KafkaProducerService {

    /**
     * 로깅을 위한 Logger 인스턴스
     */
    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    /**
     * Kafka 메시지 발행을 위한 템플릿
     * String 타입의 키와 값을 사용합니다.
     */
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    /**
     * 객체를 JSON으로 변환하기 위한 매퍼
     */
    private final ObjectMapper objectMapper;

    /**
     * 생성자 주입을 통한 의존성 주입
     */
    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 키와 함께 객체를 Kafka 토픽에 발행합니다.
     * 
     * 이 메서드는 객체를 JSON 문자열로 변환한 후 Kafka에 발행합니다.
     * 키를 사용하여 특정 파티션에 메시지를 전송할 수 있습니다.
     * 
     * @param topic 발행할 토픽 이름
     * @param key 메시지 키 (파티션 분산에 사용, null 가능)
     * @param message 발행할 객체
     */
    public void sendMessage(String topic, String key, Object message) {
        log.info("Kafka 메시지 발행 시작 - 토픽: {}, 키: {}, 메시지 타입: {}", 
                topic, key, message.getClass().getSimpleName());
        
        try {
            // 객체를 JSON 문자열로 변환
            String jsonMessage = objectMapper.writeValueAsString(message);
            log.debug("메시지 JSON 변환 완료: {}", jsonMessage);
            
            // Kafka에 메시지 발행 (비동기)
            CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send(topic, key, jsonMessage);
            
            // 발행 결과 처리 (비동기 콜백)
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    // 발행 성공
                    log.info("메시지 발행 성공 - 토픽: {}, 파티션: {}, 오프셋: {}, 메시지: {}", 
                            topic, 
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            jsonMessage);
                } else {
                    // 발행 실패
                    log.error("메시지 발행 실패 - 토픽: {}, 메시지: {}, 오류: {}", 
                            topic, jsonMessage, ex.getMessage(), ex);
                }
            });
            
        } catch (JsonProcessingException e) {
            log.error("메시지 JSON 변환 실패 - 메시지: {}, 오류: {}", 
                    message, e.getMessage(), e);
        } catch (Exception e) {
            log.error("메시지 발행 중 예상치 못한 오류 발생 - 토픽: {}, 오류: {}", 
                    topic, e.getMessage(), e);
        }
    }

    /**
     * 키 없이 객체를 Kafka 토픽에 발행합니다.
     * 
     * 키가 null인 경우 Kafka가 자동으로 파티션을 선택합니다.
     * 
     * @param topic 발행할 토픽 이름
     * @param message 발행할 객체
     */
    public void sendMessage(String topic, Object message) {
        log.debug("키 없이 메시지 발행 - 토픽: {}", topic);
        sendMessage(topic, null, message);
    }

    /**
     * 문자열 메시지를 직접 Kafka 토픽에 발행합니다.
     * 
     * 이미 문자열 형태의 메시지를 발행할 때 사용합니다.
     * JSON 변환 과정을 거치지 않으므로 성능상 유리합니다.
     * 
     * @param topic 발행할 토픽 이름
     * @param message 발행할 문자열 메시지
     */
    public void sendStringMessage(String topic, String message) {
        log.info("문자열 메시지 발행 시작 - 토픽: {}, 메시지 길이: {}", 
                topic, message != null ? message.length() : 0);
        
        try {
            // 문자열 메시지를 직접 Kafka에 발행
            CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send(topic, message);
            
            // 발행 결과 처리 (비동기 콜백)
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    // 발행 성공
                    log.info("문자열 메시지 발행 성공 - 토픽: {}, 파티션: {}, 오프셋: {}, 메시지: {}", 
                            topic, 
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            message);
                } else {
                    // 발행 실패
                    log.error("문자열 메시지 발행 실패 - 토픽: {}, 메시지: {}, 오류: {}", 
                            topic, message, ex.getMessage(), ex);
                }
            });
            
        } catch (Exception e) {
            log.error("문자열 메시지 발행 중 예상치 못한 오류 발생 - 토픽: {}, 오류: {}", 
                    topic, e.getMessage(), e);
        }
    }
}