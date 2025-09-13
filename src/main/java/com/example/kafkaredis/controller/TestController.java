package com.example.kafkaredis.controller;

import com.example.kafkaredis.service.KafkaProducerService;
import com.example.kafkaredis.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka와 Redis 기능을 테스트하기 위한 REST API 컨트롤러
 * 
 * 이 컨트롤러는 다음과 같은 기능을 제공합니다:
 * - Kafka 메시지 발행 테스트
 * - Redis 데이터 저장 및 조회 테스트
 * - Kafka와 Redis 통합 테스트
 * - 애플리케이션 상태 확인
 * 
 * 모든 API는 /api/test 경로 하위에 위치합니다.
 * 
 * @author 개발자
 * @version 1.0
 */
@RestController  // REST API 컨트롤러임을 나타내는 어노테이션
@RequestMapping("/api/test")  // 기본 경로 설정
public class TestController {

    /**
     * 로깅을 위한 Logger 인스턴스
     */
    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    /**
     * Kafka 메시지 발행을 위한 서비스
     */
    private final KafkaProducerService kafkaProducerService;
    
    /**
     * Redis 캐시 관리를 위한 서비스
     */
    private final RedisService redisService;

    /**
     * 생성자 주입을 통한 의존성 주입
     */
    public TestController(KafkaProducerService kafkaProducerService, RedisService redisService) {
        this.kafkaProducerService = kafkaProducerService;
        this.redisService = redisService;
    }

    /**
     * Kafka 토픽에 메시지를 발행합니다.
     * 
     * 이 API는 Kafka 메시지 발행 기능을 테스트하기 위해 사용됩니다.
     * 키가 제공되면 해당 키로 메시지를 발행하고, 그렇지 않으면 키 없이 발행합니다.
     * 
     * @param topic 메시지를 발행할 토픽 이름
     * @param key 메시지 키 (선택사항, 파티션 분산에 사용)
     * @param message 발행할 메시지 내용
     * @return 발행 결과 메시지
     */
    @PostMapping("/kafka/send")
    public ResponseEntity<String> sendKafkaMessage(@RequestParam String topic,
                                                  @RequestParam(required = false) String key,
                                                  @RequestBody String message) {
        log.info("=== Kafka 메시지 발행 API 호출 ===");
        log.info("요청 정보 - 토픽: {}, 키: {}, 메시지 길이: {}", 
                topic, key, message != null ? message.length() : 0);
        
        try {
            // 키가 제공된 경우와 그렇지 않은 경우를 구분하여 처리
            if (key != null && !key.trim().isEmpty()) {
                log.debug("키와 함께 메시지 발행 - 토픽: {}, 키: {}", topic, key);
                kafkaProducerService.sendMessage(topic, key, message);
            } else {
                log.debug("키 없이 메시지 발행 - 토픽: {}", topic);
                kafkaProducerService.sendMessage(topic, message);
            }
            
            String responseMessage = "Message sent to Kafka topic: " + topic;
            log.info("Kafka 메시지 발행 성공 - 토픽: {}", topic);
            return ResponseEntity.ok(responseMessage);
            
        } catch (Exception e) {
            log.error("Kafka 메시지 발행 실패 - 토픽: {}, 오류: {}", topic, e.getMessage(), e);
            String errorMessage = "Error sending message: " + e.getMessage();
            return ResponseEntity.internalServerError().body(errorMessage);
        }
    }

    /**
     * Redis에 문자열 값을 저장합니다.
     * 
     * 이 API는 Redis 데이터 저장 기능을 테스트하기 위해 사용됩니다.
     * 
     * @param key 저장할 키
     * @param value 저장할 값
     * @return 저장 결과 메시지
     */
    @PostMapping("/redis/set")
    public ResponseEntity<String> setRedisValue(@RequestParam String key,
                                               @RequestBody String value) {
        log.info("=== Redis 값 저장 API 호출 ===");
        log.info("요청 정보 - 키: {}, 값 길이: {}", key, value != null ? value.length() : 0);
        
        try {
            redisService.setString(key, value);
            String responseMessage = "Value set in Redis for key: " + key;
            log.info("Redis 값 저장 성공 - 키: {}", key);
            return ResponseEntity.ok(responseMessage);
            
        } catch (Exception e) {
            log.error("Redis 값 저장 실패 - 키: {}, 오류: {}", key, e.getMessage(), e);
            String errorMessage = "Error setting value: " + e.getMessage();
            return ResponseEntity.internalServerError().body(errorMessage);
        }
    }

    /**
     * Redis에서 문자열 값을 조회합니다.
     * 
     * 이 API는 Redis 데이터 조회 기능을 테스트하기 위해 사용됩니다.
     * 
     * @param key 조회할 키
     * @return 저장된 값 또는 404 Not Found
     */
    @GetMapping("/redis/get")
    public ResponseEntity<String> getRedisValue(@RequestParam String key) {
        log.info("=== Redis 값 조회 API 호출 ===");
        log.info("요청 정보 - 키: {}", key);
        
        try {
            String value = redisService.getString(key);
            if (value != null) {
                log.info("Redis 값 조회 성공 - 키: {}, 값 길이: {}", key, value.length());
                return ResponseEntity.ok(value);
            } else {
                log.info("Redis 값 조회 결과 없음 - 키: {}", key);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Redis 값 조회 실패 - 키: {}, 오류: {}", key, e.getMessage(), e);
            String errorMessage = "Error getting value: " + e.getMessage();
            return ResponseEntity.internalServerError().body(errorMessage);
        }
    }

    /**
     * Kafka와 Redis 통합 테스트를 수행합니다.
     * 
     * 이 API는 Kafka 메시지 발행과 Redis 데이터 저장을 동시에 테스트합니다.
     * 사용자 이벤트를 user-events 토픽에 발행하고, 동시에 Redis에도 저장합니다.
     * 
     * @param userId 사용자 ID
     * @param data 저장할 데이터
     * @return 통합 테스트 결과
     */
    @PostMapping("/integration/test")
    public ResponseEntity<Map<String, Object>> integrationTest(@RequestParam String userId,
                                                              @RequestBody String data) {
        log.info("=== 통합 테스트 API 호출 ===");
        log.info("요청 정보 - 사용자ID: {}, 데이터 길이: {}", userId, data != null ? data.length() : 0);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. Kafka에 사용자 이벤트 발행
            log.debug("Kafka에 사용자 이벤트 발행 시작 - 사용자ID: {}", userId);
            kafkaProducerService.sendMessage("user-events", userId, data);
            log.debug("Kafka에 사용자 이벤트 발행 완료 - 사용자ID: {}", userId);
            
            // 2. Redis에 테스트 데이터 저장
            String redisKey = "test:user:" + userId;
            log.debug("Redis에 테스트 데이터 저장 시작 - 키: {}", redisKey);
            redisService.setString(redisKey, data);
            log.debug("Redis에 테스트 데이터 저장 완료 - 키: {}", redisKey);
            
            // 성공 응답 구성
            result.put("status", "success");
            result.put("message", "Data sent to Kafka and stored in Redis");
            result.put("userId", userId);
            result.put("redisKey", redisKey);
            
            log.info("통합 테스트 성공 - 사용자ID: {}", userId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("통합 테스트 실패 - 사용자ID: {}, 오류: {}", userId, e.getMessage(), e);
            
            // 실패 응답 구성
            result.put("status", "error");
            result.put("message", e.getMessage());
            result.put("userId", userId);
            
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 애플리케이션의 상태를 확인합니다.
     * 
     * 이 API는 애플리케이션이 정상적으로 동작하고 있는지 확인하기 위해 사용됩니다.
     * 
     * @return 애플리케이션 상태 정보
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        log.debug("=== 헬스 체크 API 호출 ===");
        
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "kafka-redis-test");
        status.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        log.debug("헬스 체크 완료 - 상태: UP");
        return ResponseEntity.ok(status);
    }
}