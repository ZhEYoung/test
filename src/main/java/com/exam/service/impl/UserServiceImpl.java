package com.exam.service.impl;

import com.exam.entity.User;
import com.exam.mapper.UserMapper;
import com.exam.service.UserService;
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

    // 基础CRUD方法
    public int insert(User record) {
        return userMapper.insert(record);
    }

    public int deleteById(Integer id) {
        return userMapper.deleteById(id);
    }

    public int updateById(User record) {
        return userMapper.updateById(record);
    }

    public User selectById(Integer id) {
        return userMapper.selectById(id);
    }

    public List<User> selectAll() {
        return userMapper.selectAll();
    }

    public List<User> selectPage(Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return userMapper.selectPage(offset, pageSize);
    }

    public Long selectCount() {
        return userMapper.selectCount();
    }

    public List<User> selectByCondition(Map<String, Object> condition) {
        return userMapper.selectByCondition(condition);
    }

    public Long selectCountByCondition(Map<String, Object> condition) {
        return userMapper.selectCountByCondition(condition);
    }

    public List<User> selectPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize) {
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