package com.exam.service;

import com.exam.entity.Admin;
import com.exam.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AdminServiceTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;

    private Admin testAdmin;
    private User testUser;

    @BeforeEach
    public void setUp() {
        // 创建测试用户
        testUser = new User();
        testUser.setUsername("testadmin");
        testUser.setPassword("password");
        testUser.setRole(0); // 假设0是管理员角色
        testUser.setStatus(true);
        testUser.setEmail("admin@test.com");
        userService.insert(testUser);
        assertNotNull(testUser.getUserId(), "User ID should not be null after insert");

        // 创建测试管理员
        testAdmin = new Admin();
        testAdmin.setUserId(testUser.getUserId());
        testAdmin.setName("Test Admin");
        testAdmin.setOther("Test Admin Note");
    }

    @Test
    public void testBasicOperations() {
        // 测试插入
        int result = adminService.insert(testAdmin);
        assertEquals(1, result);
        assertNotNull(testAdmin.getAdminId());

        // 测试查询
        Admin queryAdmin = adminService.getById(testAdmin.getAdminId());
        assertNotNull(queryAdmin);
        assertEquals(testAdmin.getName(), queryAdmin.getName());
        assertEquals(testAdmin.getUserId(), queryAdmin.getUserId());
        assertEquals(testAdmin.getOther(), queryAdmin.getOther());

        // 测试更新
        String newName = "Updated Admin";
        testAdmin.setName(newName);
        result = adminService.updateById(testAdmin);
        assertEquals(1, result);
        
        queryAdmin = adminService.getById(testAdmin.getAdminId());
        assertEquals(newName, queryAdmin.getName());

        // 测试删除
        result = adminService.deleteById(testAdmin.getAdminId());
        assertEquals(1, result);
        
        queryAdmin = adminService.getById(testAdmin.getAdminId());
        assertNull(queryAdmin);
    }

    @Test
    public void testListOperations() {
        // 插入测试数据
        adminService.insert(testAdmin);

        // 测试查询所有
        List<Admin> admins = adminService.getAll();
        assertFalse(admins.isEmpty());
        assertTrue(admins.stream().anyMatch(a -> a.getAdminId().equals(testAdmin.getAdminId())));

        // 测试分页查询
        List<Admin> pagedAdmins = adminService.getPage(1, 10);
        assertNotNull(pagedAdmins);
        assertFalse(pagedAdmins.isEmpty());

        // 测试条件查询
        Map<String, Object> condition = new HashMap<>();
        condition.put("name", testAdmin.getName());
        List<Admin> conditionAdmins = adminService.getByCondition(condition);
        assertFalse(conditionAdmins.isEmpty());
        assertEquals(testAdmin.getName(), conditionAdmins.get(0).getName());
    }

    @Test
    public void testSpecialOperations() {
        // 插入测试数据
        adminService.insert(testAdmin);

        // 测试根据用户ID查询
        Admin adminByUserId = adminService.getByUserId(testUser.getUserId());
        assertNotNull(adminByUserId);
        assertEquals(testAdmin.getName(), adminByUserId.getName());

        // 测试根据管理员姓名查询
        Admin adminByName = adminService.getByName(testAdmin.getName());
        assertNotNull(adminByName);
        assertEquals(testAdmin.getUserId(), adminByName.getUserId());

        // 测试更新备注信息
        String newNote = "Updated Note";
        int result = adminService.updateOther(testAdmin.getAdminId(), newNote);
        assertEquals(1, result);

        Admin updatedAdmin = adminService.getById(testAdmin.getAdminId());
        assertEquals(newNote, updatedAdmin.getOther());

        // 测试批量更新备注信息
        List<Integer> adminIds = Collections.singletonList(testAdmin.getAdminId());
        String batchNote = "Batch Updated Note";
        result = adminService.batchUpdateOther(adminIds, batchNote);
        assertEquals(1, result);

        updatedAdmin = adminService.getById(testAdmin.getAdminId());
        assertEquals(batchNote, updatedAdmin.getOther());
    }

    @Test
    public void testCountAndStats() {
        // 插入测试数据
        adminService.insert(testAdmin);

        // 测试统计系统用户数量
        Map<String, Long> userStats = adminService.countSystemUsers();
        assertNotNull(userStats);
        assertTrue(userStats.containsKey("totalUsers"));
        assertTrue(userStats.containsKey("activeUsers"));
        assertTrue(userStats.containsKey("adminCount"));
        assertTrue(userStats.containsKey("teacherCount"));
        assertTrue(userStats.containsKey("studentCount"));
        assertTrue(userStats.get("totalUsers") > 0);
        assertTrue(userStats.get("activeUsers") > 0);
        assertTrue(userStats.get("adminCount") > 0);

        // 测试系统资源统计
        Map<String, Object> resourceStats = adminService.getSystemResourceStats();
        assertNotNull(resourceStats);
        assertTrue(resourceStats.containsKey("totalExams"));
        assertTrue(resourceStats.containsKey("totalQuestions"));
        assertTrue(resourceStats.containsKey("totalSubjects"));
    }

    @Test
    public void testInvalidOperations() {
        // 测试插入空对象
        assertThrows(DataIntegrityViolationException.class, () -> adminService.insert(null));

        // 测试使用不存在的用户ID
        Admin invalidAdmin = new Admin();
        invalidAdmin.setUserId(-1);
        invalidAdmin.setName("Invalid Admin");
        assertThrows(DataIntegrityViolationException.class, () -> adminService.insert(invalidAdmin));

        // 测试使用已存在的用户ID
        adminService.insert(testAdmin);
        Admin duplicateAdmin = new Admin();
        duplicateAdmin.setUserId(testUser.getUserId());
        duplicateAdmin.setName("Duplicate Admin");
        assertThrows(DataIntegrityViolationException.class, () -> adminService.insert(duplicateAdmin));

        // 测试更新不存在的管理员
        Admin nonExistentAdmin = new Admin();
        nonExistentAdmin.setAdminId(-1);
        nonExistentAdmin.setName("Non-existent Admin");
        assertEquals(0, adminService.updateById(nonExistentAdmin));

        // 测试删除不存在的管理员
        assertEquals(0, adminService.deleteById(-1));
    }

    @Test
    public void testImpersonateUser() {
        // 插入测试数据
        adminService.insert(testAdmin);

        // 创建一个测试学生用户
        User studentUser = new User();
        studentUser.setUsername("testudent");
        studentUser.setPassword("password");
        studentUser.setRole(User.ROLE_STUDENT);
        studentUser.setStatus(true);
        studentUser.setEmail("student@test.com");
        userService.insert(studentUser);

        // 测试成功的模拟登录
        User impersonatedUser = adminService.impersonateUser(
            testAdmin.getAdminId(),
            studentUser.getUserId(),
            "127.0.0.1",
            "Test Device"
        );
        assertNotNull(impersonatedUser);
        assertEquals(studentUser.getUsername(), impersonatedUser.getUsername());

        // 测试使用不存在的管理员ID
        User failedImpersonation1 = adminService.impersonateUser(
            -1,
            studentUser.getUserId(),
            "127.0.0.1",
            "Test Device"
        );
        assertNull(failedImpersonation1);

        // 测试使用不存在的目标用户ID
        User failedImpersonation2 = adminService.impersonateUser(
            testAdmin.getAdminId(),
            -1,
            "127.0.0.1",
            "Test Device"
        );
        assertNull(failedImpersonation2);

        // 测试目标用户被禁用的情况
        userService.updateStatus(studentUser.getUserId(), false);
        User failedImpersonation3 = adminService.impersonateUser(
            testAdmin.getAdminId(),
            studentUser.getUserId(),
            "127.0.0.1",
            "Test Device"
        );
        assertNull(failedImpersonation3);
    }
} 