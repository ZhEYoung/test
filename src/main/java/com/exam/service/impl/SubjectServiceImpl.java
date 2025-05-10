package com.exam.service.impl;

import com.exam.entity.Subject;
import com.exam.entity.QuestionBank;
import com.exam.entity.Question;
import com.exam.mapper.SubjectMapper;
import com.exam.mapper.TeacherMapper;
import com.exam.mapper.ExamMapper;
import com.exam.mapper.QuestionBankMapper;
import com.exam.mapper.QuestionMapper;
import com.exam.mapper.QuestionOptionMapper;
import com.exam.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/**
 * 学科服务实现类
 */
@Service
@Transactional
public class SubjectServiceImpl implements SubjectService {

    @Autowired
    private SubjectMapper subjectMapper;
    
    @Autowired
    private TeacherMapper teacherMapper;
    
    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private QuestionBankMapper questionBankMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private QuestionOptionMapper optionMapper;

    @Override
    public int insert(Subject record) {
        // 验证学科数据
        if (!validateSubject(record)) {
            return 0;
        }
        return subjectMapper.insert(record);
    }

    /**
     * 验证学科数据
     * @param subject 学科信息
     * @return true 如果数据有效，false 如果数据无效
     */
    private boolean validateSubject(Subject subject) {
        if (subject == null) {
            return false;
        }

        // 验证学科名称
        if (subject.getSubjectName() == null || 
            subject.getSubjectName().trim().isEmpty() || 
            subject.getSubjectName().length() > 50) {
            return false;
        }

        // 验证学院ID
        if (subject.getCollegeId() == null || subject.getCollegeId() <= 0) {
            return false;
        }

        return true;
    }

    @Override
    @Transactional
    public int deleteById(Integer id) {
        // 1. 获取该学科下的所有题库
        List<QuestionBank> questionBanks = questionBankMapper.selectBySubjectId(id);
        
        // 2. 对每个题库
        for (QuestionBank qb : questionBanks) {
            // 2.1 获取题库下的所有题目
            List<Question> questions = questionMapper.selectByBankId(qb.getQbId());
            
            // 2.2 对每个题目，删除其选项
            for (Question q : questions) {
                optionMapper.deleteByQuestionId(q.getQuestionId());
            }
            
            // 2.3 删除题库下的所有题目
            questionMapper.deleteByBankId(qb.getQbId());
        }
        
        // 3. 删除所有题库
        questionBankMapper.deleteBySubjectId(id);
        
        // 4. 最后删除学科
        return subjectMapper.deleteById(id);
    }

    @Override
    public int updateById(Subject record) {
        return subjectMapper.updateById(record);
    }

    @Override
    public Subject selectById(Integer id) {
        return subjectMapper.selectById(id);
    }

    @Override
    public List<Subject> selectAll() {
        return subjectMapper.selectAll();
    }

    @Override
    public List<Subject> selectPage(Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return subjectMapper.selectPage(offset, pageSize);
    }

    @Override
    public Long selectCount() {
        return Long.valueOf(subjectMapper.selectCount());
    }

    @Override
    public List<Subject> selectByCondition(Map<String, Object> condition) {
        Subject subject = new Subject();
        if (condition.containsKey("subjectName")) {
            subject.setSubjectName((String) condition.get("subjectName"));
        }
        if (condition.containsKey("collegeId")) {
            subject.setCollegeId((Integer) condition.get("collegeId"));
        }
        if (condition.containsKey("description")) {
            subject.setDescription((String) condition.get("description"));
        }
        return subjectMapper.selectByCondition(subject);
    }

    @Override
    public Long selectCountByCondition(Map<String, Object> condition) {
        Subject subject = new Subject();
        if (condition.containsKey("subjectName")) {
            subject.setSubjectName((String) condition.get("subjectName"));
        }
        if (condition.containsKey("collegeId")) {
            subject.setCollegeId((Integer) condition.get("collegeId"));
        }
        if (condition.containsKey("description")) {
            subject.setDescription((String) condition.get("description"));
        }
        return Long.valueOf(subjectMapper.selectByCondition(subject).size());
    }

    @Override
    public List<Subject> selectPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize) {
        Subject subject = new Subject();
        if (condition.containsKey("subjectName")) {
            subject.setSubjectName((String) condition.get("subjectName"));
        }
        if (condition.containsKey("collegeId")) {
            subject.setCollegeId((Integer) condition.get("collegeId"));
        }
        if (condition.containsKey("description")) {
            subject.setDescription((String) condition.get("description"));
        }
        List<Subject> allResults = subjectMapper.selectByCondition(subject);
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, allResults.size());
        return start < end ? allResults.subList(start, end) : new ArrayList<>();
    }

    @Override
    public List<Subject> getByCollegeId(Integer collegeId) {
        return subjectMapper.selectByCollegeId(collegeId);
    }

    @Override
    public Subject getBySubjectName(String subjectName) {
        return subjectMapper.selectBySubjectName(subjectName);
    }

    @Override
    public int updateDescription(Integer subjectId, String description) {
        return subjectMapper.updateDescription(subjectId, description);
    }

    @Override
    public int batchUpdateDescription(List<Integer> subjectIds, String description) {
        return subjectMapper.batchUpdateDescription(subjectIds, description);
    }

    @Override
    public List<Subject> getSubjectsWithExams() {
        return subjectMapper.selectSubjectsWithExams();
    }

    @Override
    public List<Subject> getByTeacherId(Integer teacherId) {
        return subjectMapper.selectByTeacherId(teacherId);
    }

    @Override
    public List<Subject> getByStudentId(Integer studentId) {
        return subjectMapper.selectByStudentId(studentId);
    }

    @Override
    public List<Map<String, Object>> countExamsBySubject() {
        return subjectMapper.countExamsBySubject();
    }

    @Override
    public List<Map<String, Object>> getAvgScoreBySubject() {
        return subjectMapper.avgScoreBySubject();
    }

    @Override
    public List<Subject> getHotSubjects(Integer limit) {
        return subjectMapper.selectHotSubjects(limit);
    }

    @Override
    public List<Subject> getDifficultSubjects(Integer limit) {
        return subjectMapper.selectDifficultSubjects(limit);
    }

    @Override
    public int createSubject(Subject subject, List<Integer> teacherIds) {
        // 插入学科记录
        int result = subjectMapper.insert(subject);
        if (result == 0) {
            return 0;
        }
        return result;
    }

    @Override
    public int deleteSubject(Integer subjectId) {
        // 检查是否有关联的考试
        List<Map<String, Object>> examStats = countExamsBySubject();
        for (Map<String, Object> stat : examStats) {
            if (subjectId.equals(stat.get("subjectId")) && (Long)stat.get("examCount") > 0) {
                return 0; // 有关联的考试，不能删除
            }
        }
        return subjectMapper.deleteById(subjectId);
    }

    @Override
    public Map<String, Object> getSubjectStatistics(Integer subjectId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取学科基本信息
        Subject subject = subjectMapper.selectById(subjectId);
        if (subject == null) {
            return statistics;
        }
        statistics.put("subject", subject);
        
        // 统计考试信息
        List<Map<String, Object>> examStats = countExamsBySubject();
        for (Map<String, Object> stat : examStats) {
            if (subjectId.equals(stat.get("subjectId"))) {
                statistics.put("examStats", stat);
                break;
            }
        }
        
        // 统计成绩信息
        List<Map<String, Object>> scoreStats = getAvgScoreBySubject();
        for (Map<String, Object> stat : scoreStats) {
            if (subjectId.equals(stat.get("subjectId"))) {
                statistics.put("scoreStats", stat);
                break;
            }
        }
        
        return statistics;
    }
} 