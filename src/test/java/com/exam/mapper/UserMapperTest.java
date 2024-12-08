package com.exam.mapper;

import com.exam.entity.User;
import com.exam.util.TestDataGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class UserMapperTest extends BaseMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void testSelectByUsername() {
        // 准备测试数据
        String username = TestDataGenerator.generateUsername();
        User user = new User();
        user.setUsername(username);
        user.setPassword(TestDataGenerator.generatePassword());
        user.setRole(1);
        user.setStatus(true);
        user.setSex(true);
        user.setPhone(TestDataGenerator.generatePhone());
        user.setEmail(TestDataGenerator.generateEmail());
        
        // 执行插入
        userMapper.insert(user);
        
        // 执行测试
        User found = userMapper.selectByUsername(username);
        
        // 验证结果
        assertNotNull(found);
        assertEquals(username, found.getUsername());
        assertEquals(user.getRole(), found.getRole());
    }

    @Test
    void testSelectByRole() {
        // 准备测试数据
        User teacher1 = createUser(1); // 教师
        User teacher2 = createUser(1); // 教师
        User student = createUser(2);  // 学生
        
        userMapper.insert(teacher1);
        userMapper.insert(teacher2);
        userMapper.insert(student);
        
        // 执行测试
        List<User> teachers = userMapper.selectByRole(1);
        
        // 验证结果
        assertNotNull(teachers);
        assertTrue(teachers.size() >= 2);
        teachers.forEach(teacher -> assertEquals(1, teacher.getRole()));
    }

    @Test
    void testUpdateStatus() {
        // 准备测试数据
        User user = createUser(1);
        userMapper.insert(user);
        
        // 执行测试
        boolean newStatus = false;
        userMapper.updateStatus(user.getUserId(), newStatus);
        
        // 验证结果
        User updated = userMapper.selectByUsername(user.getUsername());
        assertNotNull(updated);
        assertEquals(newStatus, updated.getStatus());
    }

    @Test
    void testBatchUpdateStatus() {
        // 准备测试数据
        User user1 = createUser(1);
        User user2 = createUser(1);
        userMapper.insert(user1);
        userMapper.insert(user2);
        
        List<Integer> userIds = Arrays.asList(user1.getUserId(), user2.getUserId());
        
        // 执行测试
        boolean newStatus = false;
        userMapper.batchUpdateStatus(userIds, newStatus);
        
        // 验证结果
        User updated1 = userMapper.selectByUsername(user1.getUsername());
        User updated2 = userMapper.selectByUsername(user2.getUsername());
        assertNotNull(updated1);
        assertNotNull(updated2);
        assertEquals(newStatus, updated1.getStatus());
        assertEquals(newStatus, updated2.getStatus());
    }

    @Test
    void testUpdatePassword() {
        // 准备测试数据
        User user = createUser(1);
        userMapper.insert(user);
        
        // 执行测试
        String newPassword = TestDataGenerator.generatePassword();
        userMapper.updatePassword(user.getUserId(), newPassword);
        
        // 验证结果
        User updated = userMapper.selectByUsername(user.getUsername());
        assertNotNull(updated);
        assertEquals(newPassword, updated.getPassword());
    }

    @Test
    void testUpdateContact() {
        // 准备测试数据
        User user = createUser(1);
        userMapper.insert(user);
        
        // 执行测试
        String newPhone = TestDataGenerator.generatePhone();
        String newEmail = TestDataGenerator.generateEmail();
        userMapper.updateContact(user.getUserId(), newPhone, newEmail);
        
        // 验证结果
        User updated = userMapper.selectByUsername(user.getUsername());
        assertNotNull(updated);
        assertEquals(newPhone, updated.getPhone());
        assertEquals(newEmail, updated.getEmail());
    }

    // 辅助方法
    private User createUser(int role) {
        User user = new User();
        user.setUsername(TestDataGenerator.generateUsername());
        user.setPassword(TestDataGenerator.generatePassword());
        user.setRole(role);
        user.setStatus(true);
        user.setSex(true);
        user.setPhone(TestDataGenerator.generatePhone());
        user.setEmail(TestDataGenerator.generateEmail());
        return user;
    }
} 