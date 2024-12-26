package com.exam.controller.test;

import com.exam.entity.Student;
import com.exam.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test/redis")
public class TestRedisController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StudentService studentService;

    @GetMapping("/write")
    public Map<String, Object> testWrite() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 测试写入Redis
            redisTemplate.opsForValue().set("test_key", "Hello Redis!");
            result.put("success", true);
            result.put("message", "Successfully wrote to Redis");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to write to Redis: " + e.getMessage());
        }
        return result;
    }

    @GetMapping("/read")
    public Map<String, Object> testRead() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 测试读取Redis
            String value = (String) redisTemplate.opsForValue().get("test_key");
            result.put("success", true);
            result.put("value", value);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to read from Redis: " + e.getMessage());
        }
        return result;
    }

    @GetMapping("/test-cache/{id}")
    public Map<String, Object> testCache(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 第一次调用，会从数据库读取并缓存
            long startTime1 = System.currentTimeMillis();
            Student student1 = studentService.getById(id);
            long endTime1 = System.currentTimeMillis();

            // 第二次调用，应该从缓存读取
            long startTime2 = System.currentTimeMillis();
            Student student2 = studentService.getById(id);
            long endTime2 = System.currentTimeMillis();

            result.put("success", true);
            result.put("firstCallTime", endTime1 - startTime1 + "ms");
            result.put("secondCallTime", endTime2 - startTime2 + "ms");
            result.put("student", student2);
            result.put("message", "First call should be slower (DB), second call should be faster (Cache)");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error testing cache: " + e.getMessage());
        }
        return result;
    }

    @GetMapping("/cache-info")
    public Map<String, Object> getCacheInfo() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取所有的缓存keys
            result.put("keys", redisTemplate.keys("*"));
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to get cache info: " + e.getMessage());
        }
        return result;
    }
} 