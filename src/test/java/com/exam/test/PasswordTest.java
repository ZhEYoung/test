package com.exam.test;

import cn.hutool.crypto.SecureUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PasswordTest {
    
    @Test
    public void testPasswordHash() {
        String password = "123456";
        String hash = SecureUtil.sha256(password);
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        
        // 验证data.sql中的密码哈希值是否正确
        String expectedHash = "d0970714757783e6cf17b26fb8e2298f2c9d6f8efb6a3255f84cf1f86448f62b";
        assertEquals(expectedHash, hash, "密码哈希值不匹配");
    }
} 