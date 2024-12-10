package com.exam.service.impl;

import com.exam.entity.ExamClass;
import com.exam.entity.Class;
import com.exam.entity.Exam;
import com.exam.mapper.ExamClassMapper;
import com.exam.service.ExamClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/**
 * 考试-班级关联服务实现类
 */
@Service
@Transactional
public class ExamClassServiceImpl implements ExamClassService {

    @Autowired
    private ExamClassMapper examClassMapper;

    @Override
    public int insert(ExamClass record) {
        return examClassMapper.insert(record);
    }

    @Override
    public int deleteById(Integer id) {
        return examClassMapper.deleteById(id);
    }

    @Override
    public int updateById(ExamClass record) {
        return examClassMapper.updateById(record);
    }

    @Override
    public ExamClass selectById(Integer id) {
        return examClassMapper.selectById(id);
    }

    @Override
    public List<ExamClass> selectAll() {
        return examClassMapper.selectAll();
    }

    @Override
    public List<ExamClass> selectPage(Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return examClassMapper.selectPage(offset, pageSize);
    }

    @Override
    public Long selectCount() {
        return examClassMapper.selectCount();
    }

    @Override
    public List<ExamClass> selectByCondition(Map<String, Object> condition) {
        return examClassMapper.selectByCondition(condition);
    }

    @Override
    public Long selectCountByCondition(Map<String, Object> condition) {
        return examClassMapper.selectCountByCondition(condition);
    }

    @Override
    public List<ExamClass> selectPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return examClassMapper.selectPageByCondition(condition, offset, pageSize);
    }

    @Override
    public int batchInsert(List<ExamClass> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }
        return examClassMapper.batchInsert(list);
    }

    @Override
    public int batchDelete(List<Integer> ecIds) {
        if (ecIds == null || ecIds.isEmpty()) {
            return 0;
        }
        return examClassMapper.batchDelete(ecIds);
    }

    @Override
    public int batchUpdate(List<ExamClass> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }
        return examClassMapper.batchUpdate(list);
    }

    @Override
    public List<Class> getClassesByExamId(Integer examId) {
        return examClassMapper.selectClassesByExamId(examId);
    }

    @Override
    public List<Exam> getExamsByClassId(Integer classId) {
        return examClassMapper.selectExamsByClassId(classId);
    }

    @Override
    public ExamClass getByExamIdAndClassId(Integer examId, Integer classId) {
        return examClassMapper.selectByExamIdAndClassId(examId, classId);
    }

    @Override
    public int batchAddClasses(Integer examId, List<Integer> classIds) {
        if (examId == null || classIds == null || classIds.isEmpty()) {
            return 0;
        }
        return examClassMapper.batchAddClasses(examId, classIds);
    }

    @Override
    public int batchRemoveClasses(Integer examId, List<Integer> classIds) {
        if (examId == null || classIds == null || classIds.isEmpty()) {
            return 0;
        }
        return examClassMapper.batchRemoveClasses(examId, classIds);
    }

    @Override
    public List<Exam> getExamsByTimeRange(Integer classId, Date startTime, Date endTime) {
        return examClassMapper.selectExamsByTimeRange(classId, startTime, endTime);
    }

    @Override
    public Long countClassesByExamId(Integer examId) {
        return examClassMapper.countClassesByExamId(examId);
    }

    @Override
    public Long countExamsByClassId(Integer classId) {
        return examClassMapper.countExamsByClassId(classId);
    }
} 