package com.example.kafkaredis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis 캐시 관리를 담당하는 서비스 클래스
 * 
 * 이 클래스는 다음과 같은 기능을 제공합니다:
 * - 문자열 데이터 저장 및 조회
 * - 객체를 JSON으로 변환하여 저장 및 조회
 * - 만료 시간이 있는 데이터 저장
 * - 키 존재 여부 확인 및 삭제
 * - 사용자 이벤트 전용 캐싱 기능
 * 
 * @author 개발자
 * @version 1.0
 */
@Service  // Spring 서비스 컴포넌트임을 나타내는 어노테이션
public class RedisService {

    /**
     * 로깅을 위한 Logger 인스턴스
     */
    private static final Logger log = LoggerFactory.getLogger(RedisService.class);

    /**
     * Redis와의 모든 상호작용을 담당하는 템플릿
     * String 타입의 키와 값을 사용합니다.
     */
    private final RedisTemplate<String, String> redisTemplate;
    
    /**
     * 객체를 JSON으로 변환하기 위한 매퍼
     */
    private final ObjectMapper objectMapper;

    /**
     * 생성자 주입을 통한 의존성 주입
     */
    public RedisService(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 문자열 값을 Redis에 저장합니다.
     * 
     * 만료 시간이 없는 영구 저장입니다.
     * 
     * @param key 저장할 키
     * @param value 저장할 문자열 값
     */
    public void setString(String key, String value) {
        log.debug("Redis에 문자열 저장 시작 - 키: {}, 값 길이: {}", 
                key, value != null ? value.length() : 0);
        
        try {
            redisTemplate.opsForValue().set(key, value);
            log.debug("Redis에 문자열 저장 완료 - 키: {}, 값: {}", key, value);
        } catch (Exception e) {
            log.error("Redis에 문자열 저장 실패 - 키: {}, 오류: {}", key, e.getMessage(), e);
        }
    }

    /**
     * 만료 시간과 함께 문자열 값을 Redis에 저장합니다.
     * 
     * 지정된 시간 후에 자동으로 삭제됩니다.
     * 
     * @param key 저장할 키
     * @param value 저장할 문자열 값
     * @param expiration 만료 시간
     */
    public void setString(String key, String value, Duration expiration) {
        log.debug("Redis에 만료시간이 있는 문자열 저장 시작 - 키: {}, 값 길이: {}, 만료시간: {}", 
                key, value != null ? value.length() : 0, expiration);
        
        try {
            redisTemplate.opsForValue().set(key, value, expiration);
            log.debug("Redis에 만료시간이 있는 문자열 저장 완료 - 키: {}, 값: {}, 만료시간: {}", 
                    key, value, expiration);
        } catch (Exception e) {
            log.error("Redis에 만료시간이 있는 문자열 저장 실패 - 키: {}, 만료시간: {}, 오류: {}", 
                    key, expiration, e.getMessage(), e);
        }
    }

    /**
     * Redis에서 문자열 값을 조회합니다.
     * 
     * @param key 조회할 키
     * @return 저장된 문자열 값, 키가 존재하지 않으면 null
     */
    public String getString(String key) {
        log.debug("Redis에서 문자열 조회 시작 - 키: {}", key);
        
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.debug("Redis에서 문자열 조회 성공 - 키: {}, 값: {}", key, value);
            } else {
                log.debug("Redis에서 문자열 조회 결과 없음 - 키: {}", key);
            }
            return value;
        } catch (Exception e) {
            log.error("Redis에서 문자열 조회 실패 - 키: {}, 오류: {}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 객체를 JSON으로 변환하여 Redis에 저장합니다.
     * 
     * 만료 시간이 없는 영구 저장입니다.
     * 
     * @param <T> 저장할 객체의 타입
     * @param key 저장할 키
     * @param object 저장할 객체
     * @param clazz 객체의 클래스 타입
     */
    public <T> void setObject(String key, T object, Class<T> clazz) {
        log.debug("Redis에 객체 저장 시작 - 키: {}, 객체 타입: {}", key, clazz.getSimpleName());
        
        try {
            // 객체를 JSON 문자열로 변환
            String jsonValue = objectMapper.writeValueAsString(object);
            log.debug("객체 JSON 변환 완료 - 키: {}, JSON 길이: {}", key, jsonValue.length());
            
            // Redis에 저장
            redisTemplate.opsForValue().set(key, jsonValue);
            log.debug("Redis에 객체 저장 완료 - 키: {}, 객체 타입: {}", key, clazz.getSimpleName());
            
        } catch (JsonProcessingException e) {
            log.error("객체 JSON 변환 실패 - 키: {}, 객체 타입: {}, 오류: {}", 
                    key, clazz.getSimpleName(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("Redis에 객체 저장 실패 - 키: {}, 객체 타입: {}, 오류: {}", 
                    key, clazz.getSimpleName(), e.getMessage(), e);
        }
    }

    /**
     * 만료 시간과 함께 객체를 JSON으로 변환하여 Redis에 저장합니다.
     * 
     * 지정된 시간 후에 자동으로 삭제됩니다.
     * 
     * @param <T> 저장할 객체의 타입
     * @param key 저장할 키
     * @param object 저장할 객체
     * @param clazz 객체의 클래스 타입
     * @param expiration 만료 시간
     */
    public <T> void setObject(String key, T object, Class<T> clazz, Duration expiration) {
        log.debug("Redis에 만료시간이 있는 객체 저장 시작 - 키: {}, 객체 타입: {}, 만료시간: {}", 
                key, clazz.getSimpleName(), expiration);
        
        try {
            // 객체를 JSON 문자열로 변환
            String jsonValue = objectMapper.writeValueAsString(object);
            log.debug("객체 JSON 변환 완료 - 키: {}, JSON 길이: {}", key, jsonValue.length());
            
            // Redis에 만료시간과 함께 저장
            redisTemplate.opsForValue().set(key, jsonValue, expiration);
            log.debug("Redis에 만료시간이 있는 객체 저장 완료 - 키: {}, 객체 타입: {}, 만료시간: {}", 
                    key, clazz.getSimpleName(), expiration);
            
        } catch (JsonProcessingException e) {
            log.error("객체 JSON 변환 실패 - 키: {}, 객체 타입: {}, 오류: {}", 
                    key, clazz.getSimpleName(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("Redis에 만료시간이 있는 객체 저장 실패 - 키: {}, 객체 타입: {}, 만료시간: {}, 오류: {}", 
                    key, clazz.getSimpleName(), expiration, e.getMessage(), e);
        }
    }

    /**
     * Redis에서 JSON 문자열을 조회하여 객체로 변환합니다.
     * 
     * @param <T> 조회할 객체의 타입
     * @param key 조회할 키
     * @param clazz 객체의 클래스 타입
     * @return 변환된 객체, 키가 존재하지 않으면 null
     */
    public <T> T getObject(String key, Class<T> clazz) {
        log.debug("Redis에서 객체 조회 시작 - 키: {}, 객체 타입: {}", key, clazz.getSimpleName());
        
        try {
            // Redis에서 JSON 문자열 조회
            String jsonValue = redisTemplate.opsForValue().get(key);
            if (jsonValue != null) {
                log.debug("Redis에서 JSON 조회 성공 - 키: {}, JSON 길이: {}", key, jsonValue.length());
                
                // JSON을 객체로 변환
                T object = objectMapper.readValue(jsonValue, clazz);
                log.debug("JSON 객체 변환 완료 - 키: {}, 객체 타입: {}", key, clazz.getSimpleName());
                return object;
            } else {
                log.debug("Redis에서 객체 조회 결과 없음 - 키: {}", key);
                return null;
            }
        } catch (Exception e) {
            log.error("Redis에서 객체 조회 실패 - 키: {}, 객체 타입: {}, 오류: {}", 
                    key, clazz.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Redis에서 키의 존재 여부를 확인합니다.
     * 
     * @param key 확인할 키
     * @return 키가 존재하면 true, 그렇지 않으면 false
     */
    public boolean exists(String key) {
        log.debug("Redis 키 존재 여부 확인 - 키: {}", key);
        
        try {
            Boolean exists = redisTemplate.hasKey(key);
            boolean result = Boolean.TRUE.equals(exists);
            log.debug("Redis 키 존재 여부 확인 결과 - 키: {}, 존재: {}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Redis 키 존재 여부 확인 실패 - 키: {}, 오류: {}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Redis에서 키와 해당 값을 삭제합니다.
     * 
     * @param key 삭제할 키
     */
    public void delete(String key) {
        log.debug("Redis 키 삭제 시작 - 키: {}", key);
        
        try {
            redisTemplate.delete(key);
            log.debug("Redis 키 삭제 완료 - 키: {}", key);
        } catch (Exception e) {
            log.error("Redis 키 삭제 실패 - 키: {}, 오류: {}", key, e.getMessage(), e);
        }
    }

    /**
     * 기존 키에 만료 시간을 설정합니다.
     * 
     * @param key 만료 시간을 설정할 키
     * @param expiration 만료 시간
     */
    public void setExpiration(String key, Duration expiration) {
        log.debug("Redis 키 만료시간 설정 시작 - 키: {}, 만료시간: {}", key, expiration);
        
        try {
            redisTemplate.expire(key, expiration);
            log.debug("Redis 키 만료시간 설정 완료 - 키: {}, 만료시간: {}", key, expiration);
        } catch (Exception e) {
            log.error("Redis 키 만료시간 설정 실패 - 키: {}, 만료시간: {}, 오류: {}", 
                    key, expiration, e.getMessage(), e);
        }
    }

    /**
     * 사용자 이벤트를 Redis에 캐싱합니다.
     * 
     * 사용자 ID를 기반으로 한 고유한 키를 생성하고, 24시간 동안 저장합니다.
     * 이 메서드는 Kafka에서 수신한 사용자 이벤트를 캐싱하는 데 사용됩니다.
     * 
     * @param userId 사용자 ID
     * @param eventData 이벤트 데이터 (JSON 문자열)
     */
    public void cacheUserEvent(String userId, String eventData) {
        String cacheKey = "user:event:" + userId;
        log.info("사용자 이벤트 캐싱 시작 - 사용자ID: {}, 캐시키: {}", userId, cacheKey);
        
        // 24시간 동안 캐싱
        setString(cacheKey, eventData, Duration.ofHours(24));
        log.info("사용자 이벤트 캐싱 완료 - 사용자ID: {}, 캐시키: {}", userId, cacheKey);
    }

    /**
     * Redis에서 사용자 이벤트를 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 캐싱된 이벤트 데이터, 없으면 null
     */
    public String getUserEvent(String userId) {
        String cacheKey = "user:event:" + userId;
        log.debug("사용자 이벤트 조회 시작 - 사용자ID: {}, 캐시키: {}", userId, cacheKey);
        
        String eventData = getString(cacheKey);
        if (eventData != null) {
            log.debug("사용자 이벤트 조회 성공 - 사용자ID: {}", userId);
        } else {
            log.debug("사용자 이벤트 조회 결과 없음 - 사용자ID: {}", userId);
        }
        
        return eventData;
    }
}