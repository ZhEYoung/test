package com.exam.service;

import com.exam.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@SpringBootTest
@Transactional  // 这个注解确保测试后数据会回滚，不会影响数据库
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    public void testRegisterAndLogin() {
        // 创建测试用户
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("123456");
        user.setRole(1);  // 假设1是普通用户角色
        user.setStatus(true);
        user.setCreatedTime(new Date());

        // 测试注册
        int result = userService.register(user);
        assert result == 1 : "注册失败";

        // 测试登录
        User loginUser = userService.login("testuser", "123456");
        assert loginUser != null : "登录失败";
        assert loginUser.getUsername().equals("testuser") : "登录用户名不匹配";

        // 测试查询
        User queryUser = userService.getByUsername("testuser");
        assert queryUser != null : "查询用户失败";
        
        // 测试更新密码
        result = userService.updatePassword(queryUser.getUserId(), "123456", "654321");
        assert result == 1 : "更新密码失败";

        // 测试更新联系方式
        result = userService.updateContact(queryUser.getUserId(), "13800138000", "test@example.com");
        assert result == 1 : "更新联系方式失败";

        // 测试通过邮箱查询
        User emailUser = userService.getByEmail("test@example.com");
        assert emailUser != null : "通过邮箱查询失败";

        // 测试通过电话查询
        User phoneUser = userService.getByPhone("13800138000");
        assert phoneUser != null : "通过电话查询失败";
    }
} 