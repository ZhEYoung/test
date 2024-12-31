package com.exam.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.exam.entity.Admin;
import com.exam.entity.Teacher;
import com.exam.entity.User;
import com.exam.entity.dto.StaffRegisterDTO;
import com.exam.entity.dto.StudentDTO;
import com.exam.mapper.AdminMapper;
import com.exam.mapper.TeacherMapper;
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

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    // 检查用户名是否已存在
    public boolean checkUsernameExists(String username) {
        return userMapper.selectByUsername(username) != null;
    }

    // 检查邮箱是否已存在
    public boolean checkEmailExists(String email) {
        return userMapper.selectByEmail(email) != null;
    }

    // 密码加密
    private String encryptPassword(String password) {
        return SecureUtil.sha256(password);
    }

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
            record.setPassword(encryptPassword(record.getPassword()));
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
        if (user != null && user.getPassword().equals(encryptPassword(password))) {
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
        user.setPassword(encryptPassword(user.getPassword()));
        user.setCreatedTime(new Date());
        return userMapper.insert(user);
    }

    @Override
    public int updatePassword(Integer userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user != null && user.getPassword().equals(encryptPassword(oldPassword))) {
            return userMapper.updatePassword(userId, encryptPassword(newPassword));
        }
        return 0;
    }

    @Override
    public int updatePasswordWithoutPassword(Integer userId,  String newPassword) {

        return userMapper.updatePassword(userId, encryptPassword(newPassword));

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

    @Transactional
    public int registerStudent(User user, StudentDTO dto) {
        // 1. 检查用户名和邮箱是否已存在
        if (checkUsernameExists(user.getUsername()) || checkEmailExists(user.getEmail())) {
            throw new RuntimeException("用户名或邮箱已存在");
        }
        // 2. 对密码进行MD5加密
        user.setPassword(encryptPassword(user.getPassword()));

        // 3. 创建用户
        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new RuntimeException("用户创建失败");
        }

        // 4. 创建学生信息
        Student student = new Student();
        student.setUserId(user.getUserId());
        student.setName(dto.getName());
        student.setGrade(dto.getGrade());
        student.setCollegeId(dto.getCollegeId());
        student.setOther(dto.getOther());

        result = studentMapper.insert(student);
        if (result <= 0) {
            throw new RuntimeException("学生信息创建失败");
        }

        return result;
    }

    @Transactional
    public int registerStaff(User user, StaffRegisterDTO dto) {
        // 1. 检查用户名和邮箱是否已存在
        if (checkUsernameExists(user.getUsername()) || checkEmailExists(user.getEmail())) {
            throw new RuntimeException("用户名或邮箱已存在");
        }

        // 2. 对密码进行加密
        user.setPassword(encryptPassword(user.getPassword()));

        // 3. 创建用户
        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new RuntimeException("用户创建失败");
        }

        // 4. 根据角色创建管理员或教师信息
        if (user.getRole() == 0) {
            Admin admin = new Admin();
            admin.setUserId(user.getUserId());
            admin.setName(dto.getName());
            admin.setOther(dto.getOther());

            result = adminMapper.insert(admin);
            if (result <= 0) {
                throw new RuntimeException("管理员信息创建失败");
            }


        } else if (user.getRole() == 1) {
            Teacher teacher = new Teacher();
            teacher.setUserId(user.getUserId());
            teacher.setName(dto.getName());
            teacher.setPermission(dto.getPermission());
            teacher.setCollegeId(dto.getCollegeId());
            teacher.setOther(dto.getOther());

            result = teacherMapper.insert(teacher);
            if (result <= 0) {
                throw new RuntimeException("教师信息创建失败");
            }


        }

        return result;
    }
}