package com.exam.service.impl;

import com.exam.entity.Admin;
import com.exam.entity.User;
import com.exam.service.AdminService;
import com.exam.mapper.AdminMapper;
import com.exam.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminServiceImplTest {

    @Mock
    private AdminMapper adminMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getByUserId() {
        // Arrange
        Integer userId = 1;
        Admin expectedAdmin = new Admin();
        expectedAdmin.setAdminId(1);
        expectedAdmin.setUserId(userId);
        expectedAdmin.setName("Test Admin");

        when(adminMapper.selectByUserId(userId)).thenReturn(expectedAdmin);

        // Act
        Admin result = adminService.getByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedAdmin.getAdminId(), result.getAdminId());
        assertEquals(expectedAdmin.getUserId(), result.getUserId());
        assertEquals(expectedAdmin.getName(), result.getName());
        verify(adminMapper).selectByUserId(userId);
    }

    @Test
    void getByName() {
        // Arrange
        String name = "Test Admin";
        Admin expectedAdmin = new Admin();
        expectedAdmin.setAdminId(1);
        expectedAdmin.setName(name);

        when(adminMapper.selectByName(name)).thenReturn(expectedAdmin);

        // Act
        Admin result = adminService.getByName(name);

        // Assert
        assertNotNull(result);
        assertEquals(expectedAdmin.getAdminId(), result.getAdminId());
        assertEquals(expectedAdmin.getName(), result.getName());
        verify(adminMapper).selectByName(name);
    }

    @Test
    void updateOther() {
        // Arrange
        Integer adminId = 1;
        String other = "Updated note";
        when(adminMapper.updateOther(adminId, other)).thenReturn(1);

        // Act
        int result = adminService.updateOther(adminId, other);

        // Assert
        assertEquals(1, result);
        verify(adminMapper).updateOther(adminId, other);
    }

    @Test
    void batchUpdateOther() {
        // Arrange
        List<Integer> adminIds = Arrays.asList(1, 2, 3);
        String other = "Batch update note";
        when(adminMapper.batchUpdateOther(adminIds, other)).thenReturn(3);

        // Act
        int result = adminService.batchUpdateOther(adminIds, other);

        // Assert
        assertEquals(3, result);
        verify(adminMapper).batchUpdateOther(adminIds, other);
    }

    @Test
    void getAdminLogs() {
        // Arrange
        Integer adminId = 1;
        Date startTime = new Date();
        Date endTime = new Date();
        List<Map<String, Object>> expectedLogs = new ArrayList<>();
        Map<String, Object> log = new HashMap<>();
        log.put("logId", 1);
        log.put("operation", "Login");
        expectedLogs.add(log);

        when(adminMapper.selectAdminLogs(adminId, startTime, endTime)).thenReturn(expectedLogs);

        // Act
        List<Map<String, Object>> result = adminService.getAdminLogs(adminId, startTime, endTime);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(expectedLogs.size(), result.size());
        assertEquals(expectedLogs.get(0).get("logId"), result.get(0).get("logId"));
        verify(adminMapper).selectAdminLogs(adminId, startTime, endTime);
    }

    @Test
    void countSystemUsers() {
        // Arrange
        List<User> adminList = Arrays.asList(createUser(1), createUser(2)); // 2 admins
        List<User> teacherList = Arrays.asList(createUser(3), createUser(4), createUser(5)); // 3 teachers
        List<User> studentList = Arrays.asList(createUser(6), createUser(7), createUser(8), createUser(9)); // 4 students

        when(userMapper.selectByRole(0)).thenReturn(adminList);
        when(userMapper.selectByRole(1)).thenReturn(teacherList);
        when(userMapper.selectByRole(2)).thenReturn(studentList);

        // Act
        Map<String, Long> result = adminService.countSystemUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.get("adminCount"));
        assertEquals(3L, result.get("teacherCount"));
        assertEquals(4L, result.get("studentCount"));
        assertEquals(9L, result.get("totalUsers"));
        
        verify(userMapper).selectByRole(0);
        verify(userMapper).selectByRole(1);
        verify(userMapper).selectByRole(2);
    }

    @Test
    void getSystemResourceStats() {
        // Act
        Map<String, Object> result = adminService.getSystemResourceStats();

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("totalMemory"));
        assertTrue(result.containsKey("usedMemory"));
        assertTrue(result.containsKey("freeMemory"));
        assertTrue(result.containsKey("memoryUsagePercent"));
        assertTrue(result.containsKey("processors"));

        assertTrue((Long)result.get("totalMemory") > 0);
        assertTrue((Long)result.get("freeMemory") > 0);
        assertTrue((Integer)result.get("processors") > 0);
        assertTrue((Double)result.get("memoryUsagePercent") >= 0 && (Double)result.get("memoryUsagePercent") <= 100);
    }

    private User createUser(Integer userId) {
        User user = new User();
        user.setUserId(userId);
        user.setUsername("user" + userId);
        user.setRole(userId <= 2 ? 0 : userId <= 5 ? 1 : 2); // 1-2为管理员，3-5为教师，6-9为学生
        user.setStatus(true);
        return user;
    }
} 