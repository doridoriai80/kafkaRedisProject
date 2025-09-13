package com.example.kafkaredis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 설정을 담당하는 Configuration 클래스
 * 
 * 이 클래스는 다음과 같은 역할을 합니다:
 * - Redis 연결 템플릿 설정
 * - JSON 직렬화/역직렬화를 위한 ObjectMapper 설정
 * - Redis 데이터 타입별 시리얼라이저 설정
 * 
 * @author 개발자
 * @version 1.0
 */
@Configuration  // Spring 설정 클래스임을 나타내는 어노테이션
public class RedisConfig {

    /**
     * 로깅을 위한 Logger 인스턴스
     */
    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    /**
     * Redis 템플릿을 생성하는 Bean
     * 
     * RedisTemplate은 Redis와의 모든 상호작용을 담당합니다.
     * String 타입의 키와 값을 사용하도록 설정되어 있습니다.
     * 
     * @param connectionFactory Redis 연결 팩토리 (Spring Boot가 자동으로 주입)
     * @return RedisTemplate 인스턴스
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        log.info("Redis 템플릿 초기화 중...");
        
        // Redis 템플릿 인스턴스 생성
        RedisTemplate<String, String> template = new RedisTemplate<>();
        
        // Redis 연결 팩토리 설정
        template.setConnectionFactory(connectionFactory);
        log.debug("Redis 연결 팩토리가 설정되었습니다");
        
        // 모든 데이터 타입에 대해 String 시리얼라이저 사용
        // 키와 값 모두 String으로 직렬화/역직렬화
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        log.debug("String 시리얼라이저가 모든 데이터 타입에 설정되었습니다");
        
        // 설정 완료 후 초기화
        template.afterPropertiesSet();
        log.info("Redis 템플릿이 성공적으로 초기화되었습니다");
        
        return template;
    }

    /**
     * JSON 직렬화/역직렬화를 위한 ObjectMapper Bean
     * 
     * 이 ObjectMapper는 다음과 같은 기능을 제공합니다:
     * - Java 8 시간 API (LocalDateTime, LocalDate 등) 지원
     * - 날짜를 타임스탬프가 아닌 ISO-8601 형식으로 직렬화
     * 
     * @return ObjectMapper 인스턴스
     */
    @Bean
    public ObjectMapper objectMapper() {
        log.info("ObjectMapper 초기화 중...");
        
        ObjectMapper mapper = new ObjectMapper();
        
        // Java 8 시간 API 모듈 등록 (LocalDateTime, LocalDate 등 지원)
        mapper.registerModule(new JavaTimeModule());
        log.debug("JavaTimeModule이 등록되었습니다");
        
        // 날짜를 타임스탬프가 아닌 ISO-8601 형식으로 직렬화
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        log.debug("날짜 직렬화가 ISO-8601 형식으로 설정되었습니다");
        
        log.info("ObjectMapper가 성공적으로 초기화되었습니다");
        return mapper;
    }
}