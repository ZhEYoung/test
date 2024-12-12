package com.exam.service;

import com.exam.entity.Log;
import com.exam.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LogService测试类
 */
@SpringBootTest
@Transactional
public class LogServiceTest {

    @Autowired
    private LogService logService;
    
    @Autowired
    private UserService userService;

    private Log testLog;
    private User testUser;
    private final String testUsername = "testUser";
    private final String testPassword = "testPass123";
    private final String testEmail = "test@example.com";
    private final String testPhone = "13800138000";

    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = new User();
        testUser.setUsername(testUsername + System.currentTimeMillis() % 100); // 确保用户名唯一且不超过长度限制
        testUser.setPassword(testPassword);
        testUser.setRole(User.ROLE_STUDENT); // 设置为学生角色
        testUser.setStatus(true);
        testUser.setSex(true);
        testUser.setPhone(testPhone);
        testUser.setEmail(testEmail);
        testUser.setCreatedTime(new Date());
        
        // 确保用户创建成功
        int result = userService.register(testUser);
        assertTrue(result > 0, "用户创建失败");
        assertNotNull(testUser.getUserId(), "用户ID不能为空");
        
        // 验证用户是否存在
        User savedUser = userService.getByUsername(testUser.getUsername());
        assertNotNull(savedUser, "无法查询到已保存的用户");
        
        // 初始化测试日志数据
        testLog = new Log();
        testLog.setUserId(savedUser.getUserId()); // 使用保存后的用户ID
        testLog.setActionType(3); // LOGIN
        testLog.setActionDescription("测试登录");
        testLog.setCreatedTime(new Date());
        testLog.setObjectType("USER");
        testLog.setIpAddress("127.0.0.1");
        testLog.setDeviceInfo("Chrome/Windows");
        testLog.setStatus("SUCCESS");
        
        // 验证日志数据的完整性
        assertNotNull(testLog.getUserId(), "日志用户ID不能为空");
        assertNotNull(testLog.getCreatedTime(), "日志时间不能为空");
    }

    @Test
    void testInsert() {
        // 验证用户ID存在
        assertNotNull(testUser.getUserId(), "用户ID不能为空");
        
        int result = logService.insert(testLog);
        assertEquals(1, result);
        assertNotNull(testLog.getLogId());
        
        // 验证插入后的日志记录
        Log savedLog = logService.getById(testLog.getLogId());
        assertNotNull(savedLog);
        assertEquals(testUser.getUserId(), savedLog.getUserId());
    }

    @Test
    void testSelectById() {
        logService.insert(testLog);
        Log found = logService.getById(testLog.getLogId());
        assertNotNull(found);
        assertEquals(testLog.getUserId(), found.getUserId());
        assertEquals(testLog.getActionType(), found.getActionType());
    }

    @Test
    void testUpdateById() {
        logService.insert(testLog);
        testLog.setActionDescription("更新的测试描述");
        int result = logService.updateById(testLog);
        assertEquals(1, result);
        Log updated = logService.getById(testLog.getLogId());
        assertEquals("更新的测试描述", updated.getActionDescription());
    }

    @Test
    void testDeleteById() {
        logService.insert(testLog);
        int result = logService.deleteById(testLog.getLogId());
        assertEquals(1, result);
        Log deleted = logService.getById(testLog.getLogId());
        assertNull(deleted);
    }

    @Test
    void testSelectPage() {
        // 插入多条测试数据
        for (int i = 0; i < 15; i++) {
            Log log = new Log();
            log.setUserId(testUser.getUserId());  // 使用测试用户ID
            log.setActionType(3);
            log.setActionDescription("测试数据" + i);
            log.setCreatedTime(new Date());
            log.setObjectType("USER");
            log.setIpAddress("127.0.0.1");
            log.setDeviceInfo("Chrome/Windows");
            log.setStatus("SUCCESS");
            logService.insert(log);
        }

        List<Log> page1 = logService.getPage(1, 10);
        assertEquals(10, page1.size());

        List<Log> page2 = logService.getPage(2, 10);
        assertEquals(5, page2.size());
    }

    @Test
    void testSelectByCondition() {
        // 先插入测试数据
        logService.insert(testLog);
        
        // 构建查询条件
        Map<String, Object> condition = new HashMap<>();
        condition.put("userId", testUser.getUserId());
        condition.put("actionType", testLog.getActionType()); // 使用Integer类型
        condition.put("objectType", testLog.getObjectType());
        condition.put("status", testLog.getStatus());
        
        // 测试条件查询
        List<Log> logs = logService.getByCondition(condition);
        assertFalse(logs.isEmpty());
        Log foundLog = logs.get(0);
        assertEquals(testUser.getUserId(), foundLog.getUserId());
        assertEquals(testLog.getActionType(), foundLog.getActionType());
        assertEquals(testLog.getObjectType(), foundLog.getObjectType());
        assertEquals(testLog.getStatus(), foundLog.getStatus());
    }

    @Test
    void testLogLogin() {
        int result = logService.logLogin(testUser.getUserId(), "192.168.1.1", "Chrome/Windows");
        assertEquals(1, result);
        
        List<Log> logs = logService.getByUserId(testUser.getUserId());
        assertFalse(logs.isEmpty());
        Log lastLog = logs.get(logs.size() - 1);
        assertEquals(3, lastLog.getActionType()); // LOGIN type
    }

    @Test
    void testLogOperation() {
        int result = logService.logOperation(testUser.getUserId(), 0, "新增用户", "192.168.1.1");
        assertEquals(1, result);
        
        List<Log> logs = logService.getByUserId(testUser.getUserId());
        assertFalse(logs.isEmpty());
        Log lastLog = logs.get(logs.size() - 1);
        assertEquals(0, lastLog.getActionType()); // INSERT type
    }

    @Test
    void testGetByTimeRange() {
        logService.insert(testLog);
        
        Date startTime = new Date(System.currentTimeMillis() - 3600000); // 1小时前
        Date endTime = new Date(System.currentTimeMillis() + 3600000);   // 1小时后
        
        List<Log> logs = logService.getByTimeRange(startTime, endTime);
        assertFalse(logs.isEmpty());
    }

    @Test
    void testCountByOperationType() {
        logService.logLogin(testUser.getUserId(), "127.0.0.1", "Chrome");
        logService.logOperation(testUser.getUserId(), 0, "新增", "127.0.0.1");
        logService.logOperation(testUser.getUserId(), 1, "更新", "127.0.0.1");
        
        List<Map<String, Object>> stats = logService.countByOperationType();
        assertFalse(stats.isEmpty());
    }

    @Test
    void testCleanExpiredLogs() {
        logService.insert(testLog);
        int result = logService.cleanExpiredLogs(30);
        assertTrue(result >= 0);
    }

    @Test
    void testExportOperationLogs() {
        logService.insert(testLog);
        
        Date startTime = new Date(System.currentTimeMillis() - 86400000); // 1天前
        Date endTime = new Date();
        
        Map<String, Object> exportData = logService.exportOperationLogs(startTime, endTime);
        assertNotNull(exportData);
        assertFalse(exportData.isEmpty());
    }

    @Test
    void testGetSuspiciousLogs() {
        // 1. 插入多次失败的登录尝试
        for (int i = 0; i < 3; i++) {
            Log loginLog = new Log();
            loginLog.setUserId(testUser.getUserId());
            loginLog.setActionType(3); // LOGIN
            loginLog.setActionDescription("登录失败");
            loginLog.setCreatedTime(new Date(System.currentTimeMillis() - i * 1000)); // 每次间隔1秒
            loginLog.setObjectType("USER");
            loginLog.setIpAddress("192.168.1." + i); // 不同IP
            loginLog.setDeviceInfo("Chrome/Windows");
            loginLog.setStatus("FAILED");
            logService.insert(loginLog);
        }

        // 2. 短时间内的大量删除操作
        for (int i = 0; i < 5; i++) {
            Log deleteLog = new Log();
            deleteLog.setUserId(testUser.getUserId());
            deleteLog.setActionType(2); // DELETE
            deleteLog.setActionDescription("批量删除数据");
            deleteLog.setCreatedTime(new Date(System.currentTimeMillis() - i * 500)); // 每次间隔0.5秒
            deleteLog.setObjectType("USER");
            deleteLog.setIpAddress("10.0.0.1");
            deleteLog.setDeviceInfo("Chrome/Windows");
            deleteLog.setStatus("SUCCESS");
            logService.insert(deleteLog);
        }

        // 3. 异常时间的操作
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 2); // 凌晨2点
        Log nightLog = new Log();
        nightLog.setUserId(testUser.getUserId());
        nightLog.setActionType(1); // UPDATE
        nightLog.setActionDescription("修改系统配置");
        nightLog.setCreatedTime(cal.getTime());
        nightLog.setObjectType("SYSTEM");
        nightLog.setIpAddress("8.8.8.8"); // 异常IP
        nightLog.setDeviceInfo("Unknown Device");
        nightLog.setStatus("SUCCESS");
        logService.insert(nightLog);

        // 4. 敏感操作
        Log sensitiveLog = new Log();
        sensitiveLog.setUserId(testUser.getUserId());
        sensitiveLog.setActionType(4); // ADMIN
        sensitiveLog.setActionDescription("修改用户权限");
        sensitiveLog.setCreatedTime(new Date());
        sensitiveLog.setObjectType("PERMISSION");
        sensitiveLog.setIpAddress("172.16.0.1");
        sensitiveLog.setDeviceInfo("Chrome/Windows");
        sensitiveLog.setStatus("SUCCESS");
        logService.insert(sensitiveLog);
        
        List<Log> suspiciousLogs = logService.getSuspiciousLogs();
        assertFalse(suspiciousLogs.isEmpty(), "应该检测到可疑的操作日志");
        assertTrue(suspiciousLogs.size() >= 2, "应该至少检测到2条可疑的操作日志");
    }
} 