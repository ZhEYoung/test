package com.exam.service.impl;

import com.exam.entity.User;
import com.exam.mapper.UserMapper;
import com.exam.mapper.StudentMapper;
import com.exam.service.UserService;
import com.exam.entity.Student;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * 用户服务实现类
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StudentMapper studentMapper;

    // 基础CRUD方法
    public int insert(User record) {
        if (record == null || !record.isUsernameValid() || !record.isRoleValid()) {
            System.out.println("用户数据无效");
            return 0;
        }

        // 检查用户名是否已存在
        User existingUser = userMapper.selectByUsername(record.getUsername());
        if (existingUser != null) {
            System.out.println("用户名已存在: " + record.getUsername());
            return 0;
        }

        // 设置默认状态为启用
        if (record.getStatus() == null) {
            record.setStatus(true);
        }

        // 设置创建时间
        if (record.getCreatedTime() == null) {
            record.setCreatedTime(new Date());
        }

        // 密码加密
        if (record.getPassword() != null) {
            record.setPassword(DigestUtils.md5DigestAsHex(record.getPassword().getBytes()));
        }

        return userMapper.insert(record);
    }

    @Override
    @Transactional
    public int deleteById(Integer id) {
        // 获取用户信息
        User user = userMapper.selectById(id);
        if (user == null || !user.isRoleValid()) {
            return 0;
        }
        // 如果用户已经是禁用状态，不需要再次禁用
        if (!user.getStatus()) {
            return 0;
        }
        // 所有用户都只禁用账号，不删除记录
        return userMapper.updateStatus(id, false);
    }

    public int updateById(User record) {
        if (record == null || !record.isRoleValid()) {
            return 0;
        }
        return userMapper.updateById(record);
    }

    public User getById(Integer id) {
        return userMapper.selectById(id);
    }

    public List<User> getAll() {
        return userMapper.selectAll();
    }

    public List<User> getPage(Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return userMapper.selectPage(offset, pageSize);
    }

    public Long getCount() {
        return userMapper.selectCount();
    }

    public List<User> getByCondition(Map<String, Object> condition) {
        return userMapper.selectByCondition(condition);
    }

    public Long getCountByCondition(Map<String, Object> condition) {
        return userMapper.selectCountByCondition(condition);
    }

    public List<User> getPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return userMapper.selectPageByCondition(condition, offset, pageSize);
    }

    @Override
    public User login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user != null && user.getPassword().equals(DigestUtils.md5DigestAsHex(password.getBytes()))) {
            return user;
        }
        return null;
    }

    @Override
    public int register(User user) {
        // 验证用户名长度和角色
        if (user == null || !user.isUsernameValid() || !user.isRoleValid()) {
            return 0;
        }
        // 检查用户名是否已存在
        if (userMapper.selectByUsername(user.getUsername()) != null) {
            return 0;
        }
        // 密码加密
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        user.setCreatedTime(new Date());
        return userMapper.insert(user);
    }

    @Override
    public int updatePassword(Integer userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user != null && user.getPassword().equals(DigestUtils.md5DigestAsHex(oldPassword.getBytes()))) {
            return userMapper.updatePassword(userId, DigestUtils.md5DigestAsHex(newPassword.getBytes()));
        }
        return 0;
    }

    @Override
    public int updateStatus(Integer userId, Boolean status) {
        return userMapper.updateStatus(userId, status);
    }

    @Override
    public int batchUpdateStatus(List<Integer> userIds, Boolean status) {
        return userMapper.batchUpdateStatus(userIds, status);
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public List<User> getByRole(Integer role) {
        return userMapper.selectByRole(role);
    }

    @Override
    public int updateContact(Integer userId, String phone, String email) {
        return userMapper.updateContact(userId, phone, email);
    }

    @Override
    public User getByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    @Override
    public User getByPhone(String phone) {
        return userMapper.selectByPhone(phone);
    }

    @Override
    public int updateCreatedTime(Integer userId, Date createdTime) {
        return userMapper.updateCreatedTime(userId, createdTime);
    }
}