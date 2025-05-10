package com.exam.service.impl;

import com.exam.entity.Exam;
import com.exam.entity.StudentScore;
import com.exam.entity.Teacher;
import com.exam.entity.ExamPaper;
import com.exam.entity.Class;
import com.exam.mapper.ExamMapper;
import com.exam.mapper.ExamStudentMapper;
import com.exam.mapper.StudentScoreMapper;
import com.exam.service.ExamService;
import com.exam.service.TeacherService;
import com.exam.service.ExamPaperService;
import com.exam.service.ClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.math.BigDecimal;
import java.util.stream.Collectors;

/**
 * 考试服务实现类
 */
@Service
@Transactional
public class ExamServiceImpl implements ExamService {

    @Autowired
    private ExamMapper examMapper;
    
    @Autowired
    private ExamStudentMapper examStudentMapper;
    
    @Autowired
    private StudentScoreMapper studentScoreMapper;
    
    @Autowired
    private TeacherService teacherService;
    
    @Autowired
    private ExamPaperService examPaperService;
    
    @Autowired
    private ClassService classService;

    @Override
    public int insert(Exam record) {
        return examMapper.insert(record);
    }

    @Override
    public int deleteById(Integer id) {
        return examMapper.deleteById(id);
    }

    @Override
    public int updateById(Exam record) {
        return examMapper.update(record);
    }

    @Override
    public Exam getById(Integer id) {
        return examMapper.selectById(id);
    }

    @Override
    public List<Exam> getAll() {
        return examMapper.selectAll();
    }

    @Override
    public List<Exam> getPage(Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return examMapper.selectByConditions(null, null, null, null, null, null, offset, pageSize);
    }

    @Override
    public Long getCount() {
        return examMapper.countByTimeRange(null, null);
    }

    @Override
    public List<Exam> getByCondition(Map<String, Object> condition) {
        Integer subjectId = (Integer) condition.get("subjectId");
        Integer teacherId = (Integer) condition.get("teacherId");
        Integer examType = (Integer) condition.get("examType");
        Integer examStatus = (Integer) condition.get("examStatus");
        Date startTime = (Date) condition.get("startTime");
        Date endTime = (Date) condition.get("endTime");
        Integer offset = (Integer) condition.get("offset");
        Integer limit = (Integer) condition.get("limit");
        
        return examMapper.selectByConditions(subjectId, teacherId, examType, examStatus, 
            startTime, endTime, offset, limit);
    }

    @Override
    public Long getCountByCondition(Map<String, Object> condition) {
        Date startTime = (Date) condition.get("startTime");
        Date endTime = (Date) condition.get("endTime");
        return examMapper.countByTimeRange(startTime, endTime);
    }

    @Override
    public List<Exam> getPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize) {
        condition.put("offset", (pageNum - 1) * pageSize);
        condition.put("limit", pageSize);
        return getByCondition(condition);
    }

    @Override
    public List<Exam> getBySubjectId(Integer subjectId) {
        return examMapper.selectBySubjectId(subjectId);
    }

    @Override
    public List<Exam> getByPaperId(Integer paperId) {
        return examMapper.selectByPaperId(paperId);
    }

    @Override
    public List<Exam> getByTeacherId(Integer teacherId) {
        return examMapper.selectByTeacherId(teacherId);
    }

    @Override
    public List<Exam> getByStatus(Integer examStatus) {
        return examMapper.selectByStatus(examStatus);
    }

    @Override
    public List<Exam> getByType(Integer examType) {
        return examMapper.selectByType(examType);
    }

    @Override
    public int updateStatus(Integer examId, Integer status) {
        // 检查考试时间，自动更新状态
        Exam exam = examMapper.selectById(examId);
        if (exam != null) {
            Date now = new Date();
            if (now.before(exam.getExamStartTime())) {
                status = 0; // 未开始
            } else if (now.after(exam.getExamEndTime())) {
                status = 2; // 已结束
            } else {
                status = 1; // 进行中
            }
        }
        return examMapper.updateStatus(examId, status);
    }

    @Override
    public List<Exam> getByStudentId(Integer studentId) {
        return examMapper.selectByStudentId(studentId);
    }

    @Override
    public List<Exam> getByClassId(Integer classId) {
        return examMapper.selectByClassId(classId);
    }

    @Override
    public List<Exam> getByTimeRange(Date startTime, Date endTime) {
        return examMapper.selectByTimeRange(startTime, endTime);
    }

    @Override
    public List<Exam> getByConditions(Integer subjectId, Integer teacherId, 
                                    Integer examType, Integer examStatus,
                                    Date startTime, Date endTime,
                                    Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return examMapper.selectByConditions(subjectId, teacherId, examType, examStatus,
            startTime, endTime, offset, pageSize);
    }

    @Override
    public Long countByTimeRange(Date startTime, Date endTime) {
        return examMapper.countByTimeRange(startTime, endTime);
    }

    @Override
    public int updateDuration(Integer examId, Integer duration) {
        return examMapper.updateDuration(examId, duration);
    }

    @Override
    public int batchAddExamClass(Integer examId, List<Integer> classIds) {
        return examMapper.batchInsertExamClass(examId, classIds);
    }

    @Override
    public int removeExamClass(Integer examId, Integer classId) {
        return examMapper.deleteExamClass(examId, classId);
    }

    @Override
    @Transactional
    public int publishExam(Integer examId) {
        // 检查考试时间是否合法
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            return 0;
        }
        
        Date now = new Date();
        if (exam.getExamStartTime().before(now)) {
            return 0; // 开始时间不能早于当前时间
        }
        
        if (exam.getExamEndTime().before(exam.getExamStartTime())) {
            return 0; // 结束时间不能早于开始时间
        }
        
        return examMapper.updateStatus(examId, 0); // 设置为未开始状态
    }

    @Override
    @Transactional
    public int startExam(Integer examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            return 0;
        }
        
        Date now = new Date();
        if (now.before(exam.getExamStartTime()) || now.after(exam.getExamEndTime())) {
            return 0; // 不在考试时间范围内
        }
        
        return examMapper.updateStatus(examId, 1); // 设置为进行中状态
    }

    @Override
    @Transactional
    public int endExam(Integer examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            return 0;
        }
        
        Date now = new Date();
        if (now.before(exam.getExamEndTime())) {
            return 0; // 未到结束时间
        }
        
        return examMapper.updateStatus(examId, 2); // 设置为已结束状态
    }

    @Override
    public Map<String, Object> getExamStatistics(Integer examId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取考试信息
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            return statistics;
        }
        
        // 获取所有成绩
        List<StudentScore> scores = studentScoreMapper.selectByExamId(examId);
        if (scores == null || scores.isEmpty()) {
            return statistics;
        }
        
        // 计算统计信息
        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal maxScore = BigDecimal.ZERO;
        BigDecimal minScore = new BigDecimal("100");
        int totalStudents = scores.size();
        int passedStudents = 0;
        BigDecimal passScore = new BigDecimal("60");
        
        for (StudentScore score : scores) {
            BigDecimal currentScore = score.getScore();
            if (currentScore == null) continue;
            
            totalScore = totalScore.add(currentScore);
            maxScore = maxScore.max(currentScore);
            minScore = minScore.min(currentScore);
            
            if (currentScore.compareTo(passScore) >= 0) {
                passedStudents++;
            }
        }
        
        // 计算平均分和及格率
        BigDecimal avgScore = totalScore.divide(new BigDecimal(totalStudents), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal passRate = new BigDecimal(passedStudents)
            .divide(new BigDecimal(totalStudents), 4, BigDecimal.ROUND_HALF_UP)
            .multiply(new BigDecimal(100));
        
        statistics.put("examId", examId);
        statistics.put("totalStudents", totalStudents);
        statistics.put("maxScore", maxScore);
        statistics.put("minScore", minScore);
        statistics.put("avgScore", avgScore);
        statistics.put("passRate", passRate);
        
        return statistics;
    }

    @Override
    public Map<String, Object> getExamProgress(Integer examId) {
        Map<String, Object> progress = new HashMap<>();
        
        // 获取考试信息
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            return progress;
        }
        
        Date now = new Date();
        
        // 计算考试进度
        if (now.before(exam.getExamStartTime())) {
            progress.put("status", "未开始");
            progress.put("remainingTime", exam.getExamStartTime().getTime() - now.getTime());
        } else if (now.after(exam.getExamEndTime())) {
            progress.put("status", "已结束");
            progress.put("remainingTime", 0);
        } else {
            progress.put("status", "进行中");
            progress.put("remainingTime", exam.getExamEndTime().getTime() - now.getTime());
        }
        
        // 获取考试人数统计
        int totalStudents = examStudentMapper.countByExamId(examId);
        int startedStudents = examStudentMapper.countStartedStudents(examId);
        int submittedStudents = examStudentMapper.countSubmittedStudents(examId);
        int absentStudents = examStudentMapper.countAbsentStudents(examId);
        
        progress.put("totalStudents", totalStudents);
        progress.put("startedStudents", startedStudents);
        progress.put("submittedStudents", submittedStudents);
        progress.put("absentStudents", absentStudents);
        
        return progress;
    }

    @Override
    @Transactional
    public Exam publishFinalExam(Integer teacherId, Integer subjectId, Integer classId, Date academicTerm) {
        // 1. 验证教师权限
        Teacher teacher = teacherService.getById(teacherId);
        if (teacher == null || teacher.getPermission() != 0) {
            throw new RuntimeException("权限不足，只有管理员教师可以发布期末考试");
        }

        // 2. 查找符合条件的期末试卷（同学科同学期的期末试卷）
        Map<String, Object> condition = new HashMap<>();
        condition.put("subjectId", subjectId);
        condition.put("examType", 0); // 期末试卷
        condition.put("academicTerm", academicTerm);
        condition.put("paperStatus", 0); // 未发布状态
        List<ExamPaper> finalPapers = examPaperService.getByCondition(condition);

        // 3. 验证试卷数量
        if (finalPapers == null || finalPapers.size() < 2) {
            throw new RuntimeException("期末试卷数量不足，需要至少2份同学科同学期的期末试卷");
        }

        // 4. 随机选择一份试卷
        Random random = new Random();
        ExamPaper selectedPaper = finalPapers.get(random.nextInt(finalPapers.size()));

        // 5. 创建期末考试
        Exam finalExam = new Exam();
        finalExam.setExamName("期末考试-" + selectedPaper.getPaperName());
        finalExam.setSubjectId(subjectId);
        finalExam.setPaperId(selectedPaper.getPaperId());
        finalExam.setTeacherId(teacherId);
        finalExam.setExamStatus(0); // 未开始
        finalExam.setExamType(0); // 正常考试
        
        // 设置考试时间（默认设置为7天后，考试时长120分钟）
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        calendar.set(Calendar.HOUR_OF_DAY, 9); // 上午9点开始
        finalExam.setExamStartTime(calendar.getTime());
        
        calendar.add(Calendar.MINUTE, 120);
        finalExam.setExamEndTime(calendar.getTime());
        finalExam.setExamDuration(120);
        finalExam.setCreatedTime(new Date());

        // 6. 保存考试信息
        if (examMapper.insert(finalExam) <= 0) {
            throw new RuntimeException("创建期末考试失败");
        }

        // 7. 创建考试-班级关联
        Map<String, Object> examClass = new HashMap<>();
        examClass.put("examId", finalExam.getExamId());
        examClass.put("classId", classId);
        examMapper.insertExamClass(examClass);

        // 8. 更新所有相关期末试卷状态为已发布
        List<Integer> paperIds = finalPapers.stream()
            .map(ExamPaper::getPaperId)
            .collect(Collectors.toList());
        examPaperService.batchUpdateStatus(paperIds, 1);

        // 9. 更新班级期末考试状态
        Class classInfo = new Class();
        classInfo.setClassId(classId);
        classInfo.setFinalExam(Boolean.TRUE);
        classService.updateById(classInfo);

        return finalExam;
    }

    @Override
    @Transactional
    public int deleteExamClassByClassId(Integer classId) {
        return examMapper.deleteExamClass(null,classId);
    }
} 