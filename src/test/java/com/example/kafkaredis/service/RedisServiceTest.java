package com.example.kafkaredis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisService redisService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testSetString() {
        String key = "test-key";
        String value = "test-value";

        redisService.setString(key, value);

        verify(valueOperations).set(key, value);
    }

    @Test
    void testSetStringWithExpiration() {
        String key = "test-key";
        String value = "test-value";
        Duration expiration = Duration.ofMinutes(10);

        redisService.setString(key, value, expiration);

        verify(valueOperations).set(key, value, expiration);
    }

    @Test
    void testGetString() {
        String key = "test-key";
        String expectedValue = "test-value";

        when(valueOperations.get(key)).thenReturn(expectedValue);

        String actualValue = redisService.getString(key);

        assertEquals(expectedValue, actualValue);
        verify(valueOperations).get(key);
    }

    @Test
    void testExists() {
        String key = "test-key";

        when(redisTemplate.hasKey(key)).thenReturn(true);

        boolean exists = redisService.exists(key);

        assertTrue(exists);
        verify(redisTemplate).hasKey(key);
    }

    @Test
    void testDelete() {
        String key = "test-key";

        redisService.delete(key);

        verify(redisTemplate).delete(key);
    }

    @Test
    void testCacheUserEvent() {
        String userId = "user123";
        String eventData = "event data";

        redisService.cacheUserEvent(userId, eventData);

        verify(valueOperations).set(eq("user:event:" + userId), eq(eventData), any(Duration.class));
    }

    @Test
    void testGetUserEvent() {
        String userId = "user123";
        String expectedEventData = "event data";

        when(valueOperations.get("user:event:" + userId)).thenReturn(expectedEventData);

        String actualEventData = redisService.getUserEvent(userId);

        assertEquals(expectedEventData, actualEventData);
        verify(valueOperations).get("user:event:" + userId);
    }
}