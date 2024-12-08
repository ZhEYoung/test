package com.exam.service.impl;

import com.exam.entity.User;
import com.exam.mapper.UserMapper;
import com.exam.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import java.util.List;
import java.util.Date;

/**
 * 用户服务实现类
 */
@Service
@Transactional
public class UserServiceImpl extends BaseServiceImpl<User, UserMapper> implements UserService {

    @Override
    public User login(String username, String password) {
        User user = baseMapper.selectByUsername(username);
        if (user != null && user.getPassword().equals(DigestUtils.md5DigestAsHex(password.getBytes()))) {
            return user;
        }
        return null;
    }

    @Override
    public int register(User user) {
        // 检查用户名是否已存在
        if (baseMapper.selectByUsername(user.getUsername()) != null) {
            return 0;
        }
        // 密码加密
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        user.setCreatedTime(new Date());
        return baseMapper.insert(user);
    }

    @Override
    public int updatePassword(Integer userId, String oldPassword, String newPassword) {
        User user = baseMapper.selectById(userId);
        if (user != null && user.getPassword().equals(DigestUtils.md5DigestAsHex(oldPassword.getBytes()))) {
            return baseMapper.updatePassword(userId, DigestUtils.md5DigestAsHex(newPassword.getBytes()));
        }
        return 0;
    }

    @Override
    public int updateStatus(Integer userId, Boolean status) {
        return baseMapper.updateStatus(userId, status);
    }

    @Override
    public int batchUpdateStatus(List<Integer> userIds, Boolean status) {
        return baseMapper.batchUpdateStatus(userIds, status);
    }

    @Override
    public User getByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

    @Override
    public List<User> getByRole(Integer role) {
        return baseMapper.selectByRole(role);
    }

    @Override
    public int updateContact(Integer userId, String phone, String email) {
        return baseMapper.updateContact(userId, phone, email);
    }

    @Override
    public User getByEmail(String email) {
        return baseMapper.selectByEmail(email);
    }

    @Override
    public User getByPhone(String phone) {
        return baseMapper.selectByPhone(phone);
    }

    @Override
    public int updateCreatedTime(Integer userId, Date createdTime) {
        return baseMapper.updateCreatedTime(userId, createdTime);
    }
} 