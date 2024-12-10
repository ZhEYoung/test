package com.exam.service.impl;

import com.exam.entity.Admin;
import com.exam.mapper.AdminMapper;
import com.exam.mapper.UserMapper;
import com.exam.mapper.ExamMapper;
import com.exam.mapper.QuestionMapper;
import com.exam.mapper.SubjectMapper;
import com.exam.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/**
 * 管理员服务实现类
 */
@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private SubjectMapper subjectMapper;

    // 基础CRUD方法
    @Override
    public int insert(Admin record) {
        if (record == null) {
            throw new DataIntegrityViolationException("管理员记录不能为空");
        }

        if (record.getUserId() == null) {
            throw new DataIntegrityViolationException("用户ID不能为空");
        }

        // 检查用户ID是否存在
        if (userMapper.selectById(record.getUserId()) == null) {
            throw new DataIntegrityViolationException("关联的用户ID不存在");
        }

        // 检查是否已经存在该用户的管理员记录
        if (adminMapper.selectByUserId(record.getUserId()) != null) {
            throw new DataIntegrityViolationException("该用户已经是管理员");
        }

        try {
            return adminMapper.insert(record);
        } catch (Exception e) {
            throw new DataIntegrityViolationException("插入管理员记录失败", e);
        }
    }

    @Override
    public int deleteById(Integer id) {
        return adminMapper.deleteById(id);
    }

    @Override
    public int updateById(Admin record) {
        return adminMapper.updateById(record);
    }

    @Override
    public Admin selectById(Integer id) {
        return adminMapper.selectById(id);
    }

    @Override
    public List<Admin> selectAll() {
        return adminMapper.selectAll();
    }

    @Override
    public List<Admin> selectPage(Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return adminMapper.selectPage(offset, pageSize);
    }

    @Override
    public Long selectCount() {
        return adminMapper.selectCount();
    }

    @Override
    public List<Admin> selectByCondition(Map<String, Object> condition) {
        return adminMapper.selectByCondition(condition);
    }

    @Override
    public Long selectCountByCondition(Map<String, Object> condition) {
        return adminMapper.selectCountByCondition(condition);
    }

    @Override
    public List<Admin> selectPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return adminMapper.selectPageByCondition(condition, offset, pageSize);
    }

    @Override
    public Admin getByUserId(Integer userId) {
        return adminMapper.selectByUserId(userId);
    }

    @Override
    public Admin getByName(String name) {
        return adminMapper.selectByName(name);
    }

    @Override
    public int updateOther(Integer adminId, String other) {
        return adminMapper.updateOther(adminId, other);
    }

    @Override
    public int batchUpdateOther(List<Integer> adminIds, String other) {
        return adminMapper.batchUpdateOther(adminIds, other);
    }

    @Override
    public List<Map<String, Object>> getAdminLogs(Integer adminId, Date startTime, Date endTime) {
        return adminMapper.selectAdminLogs(adminId, startTime, endTime);
    }

    @Override
    public Map<String, Long> countSystemUsers() {
        Map<String, Long> userStats = new HashMap<>();
        
        // 统计各角色用户数量
        userStats.put("adminCount", (long) userMapper.selectByRole(0).size());
        userStats.put("teacherCount", (long) userMapper.selectByRole(1).size());
        userStats.put("studentCount", (long) userMapper.selectByRole(2).size());
        
        // 计算总用户数和活跃用户数
        Long totalUsers = userStats.values().stream().mapToLong(Long::longValue).sum();
        userStats.put("totalUsers", totalUsers);
        
        // 统计活跃用户数
        Map<String, Object> activeCondition = new HashMap<>();
        activeCondition.put("status", true);
        userStats.put("activeUsers", userMapper.selectCountByCondition(activeCondition));
        
        return userStats;
    }

    @Override
    public Map<String, Object> getSystemResourceStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 统计考试总数
        stats.put("totalExams", examMapper.selectAll().size());
        
        // 统计题目总数
        stats.put("totalQuestions", questionMapper.selectCount());
        
        // 统计科目总数
        stats.put("totalSubjects", subjectMapper.selectCount());
        
        return stats;
    }
} 