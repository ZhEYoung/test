package com.exam.service.impl;

import com.exam.entity.*;
import com.exam.entity.Class;
import com.exam.mapper.ExamMapper;
import com.exam.mapper.ExamStudentMapper;
import com.exam.mapper.StudentScoreMapper;
import com.exam.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;

/**
 * 考试服务实现类
 */
@Slf4j
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
    private StudentService studentService;

    
    @Autowired
    private ExamPaperService examPaperService;
    
    @Autowired
    private ClassService classService;

    @Autowired
    private ExamStudentService examStudentService;

    @Override
    @CacheEvict(value = {"exam", "exam_list"}, allEntries = true)
    public int insert(Exam record) {
        return examMapper.insert(record);
    }

    @Override
    @CacheEvict(value = {"exam", "exam_list"}, allEntries = true)
    public int deleteById(Integer id) {
        return examMapper.deleteById(id);
    }

    @Override
    @CacheEvict(value = {"exam", "exam_list"}, allEntries = true)
    public int updateById(Exam record) {
        if (record == null || record.getExamId() == null) {
            return 0;
        }
        
        try {
            // 如果修改了考试时间或时长，重新计算结束时间
            if (record.getExamStartTime() != null && record.getExamDuration() != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(record.getExamStartTime());
                calendar.add(Calendar.MINUTE, record.getExamDuration());
                record.setExamEndTime(calendar.getTime());
            }
            
            // 验证时间的合法性
            Date now = new Date();
            if (record.getExamStartTime() != null && record.getExamStartTime().before(now)) {
                throw new RuntimeException("考试开始时间不能早于当前时间");
            }
            
            if (record.getExamDuration() != null && (record.getExamDuration() <= 0 || record.getExamDuration() > 180)) {
                throw new RuntimeException("考试时长必须在1-180分钟之间");
            }
            
            return examMapper.update(record);
        } catch (Exception e) {
            throw new RuntimeException("更新考试信息失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "exam", key = "#id")
    public Exam getById(Integer id) {
        return examMapper.selectById(id);
    }

    @Override
    @Cacheable(value = "exam_list")
    public List<Exam> getAll() {
        return examMapper.selectAll();
    }

    @Override
    @Cacheable(value = "exam_page", key = "#pageNum + '_' + #pageSize")
    public List<Exam> getPage(Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return examMapper.selectByConditions(null, null, null, null, null, null, offset, pageSize);
    }

    @Override
    @Cacheable(value = "exam_count")
    public Long getCount() {
        return examMapper.countByTimeRange(null, null);
    }

    @Override
    @Cacheable(value = "exam_by_condition", 
               key = "'subj_'+#condition.get('subjectId')+'_teach_'+#condition.get('teacherId')+'_type_'+#condition.get('examType')+'_status_'+#condition.get('examStatus')")
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
    @Cacheable(value = "exam_count_by_condition",
               key = "'start_'+#condition.get('startTime')+'_end_'+#condition.get('endTime')")
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
    @Cacheable(value = "exam_by_subject", key = "#subjectId")
    public List<Exam> getBySubjectId(Integer subjectId) {
        return examMapper.selectBySubjectId(subjectId);
    }

    @Override
    @Cacheable(value = "exam_by_paper", key = "#paperId")
    public List<Exam> getByPaperId(Integer paperId) {
        return examMapper.selectByPaperId(paperId);
    }

    @Override
    @Cacheable(value = "exam_by_teacher", key = "#teacherId")
    public List<Exam> getByTeacherId(Integer teacherId) {
        return examMapper.selectByTeacherId(teacherId);
    }

    @Override
    @Cacheable(value = "exam_by_status", key = "#examStatus")
    public List<Exam> getByStatus(Integer examStatus) {
        return examMapper.selectByStatus(examStatus);
    }

    @Override
    @Cacheable(value = "exam_by_type", key = "#examType")
    public List<Exam> getByType(Integer examType) {
        return examMapper.selectByType(examType);
    }

    @Override
    public int updateStatus(Integer examId, Integer status) {
        // 验证考试是否存在
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new RuntimeException("考试不存在");
        }

        // 验证状态转换是否合法
        validateStatusTransition(exam.getExamStatus(), status);

        // 添加日志
        log.info("Updating exam {} status from {} to {}", examId, exam.getExamStatus(), status);

        // 更新考试状态
        int result = examMapper.updateStatus(examId, status);
        
        // 验证更新结果
        if (result > 0) {
            log.info("Successfully updated exam {} status to {}", examId, status);
        } else {
            log.warn("Failed to update exam {} status", examId);
        }
        
        return result;
    }

    private void validateStatusTransition(Integer currentStatus, Integer newStatus) {
        // 考试状态：0-未开始，1-进行中，2-已结束
        if (currentStatus.equals(newStatus)) {
            return; // 状态相同，允许
        }

        // 定义合法的状态转换
        if ((currentStatus == 0 && newStatus == 1) ||    // 未开始 -> 进行中
            (currentStatus == 1 && newStatus == 2)) {    // 进行中 -> 已结束
            return;
        }

        throw new RuntimeException("非法的考试状态转换");
    }

    @Override
    @Cacheable(value = "exam_by_student", key = "#studentId")
    public List<Exam> getByStudentId(Integer studentId) {
        return examMapper.selectByStudentId(studentId);
    }

    @Override
    @Cacheable(value = "exam_by_class", key = "#classId")
    public List<Exam> getByClassId(Integer classId) {
        return examMapper.selectByClassId(classId);
    }

    @Override
    @Cacheable(value = "exam_by_timerange", key = "#startTime.getTime() + '_' + #endTime.getTime()")
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
    @CacheEvict(value = {"exam", "exam_list", "exam_by_class"}, allEntries = true)
    public int updateDuration(Integer examId, Integer duration) {
        return examMapper.updateDuration(examId, duration);
    }

    @Override
    @CacheEvict(value = {"exam", "exam_list", "exam_by_class"}, allEntries = true)
    public int batchAddExamClass(Integer examId, List<Integer> classIds) {
        return examMapper.batchInsertExamClass(examId, classIds);
    }

    @Override
    @CacheEvict(value = {"exam", "exam_list", "exam_by_class"}, allEntries = true)
    public int removeExamClass(Integer examId, Integer classId) {
        return examMapper.deleteExamClass(examId, classId);
    }

    @Override
    @CacheEvict(value = {"exam", "exam_list", "exam_by_subject", "exam_by_paper", "exam_by_teacher", 
                        "exam_by_status", "exam_by_type", "exam_by_student", "exam_by_class",
                        "exam_page", "exam_count", "exam_by_condition", "exam_count_by_condition"}, 
                allEntries = true)
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
    @CacheEvict(value = {"exam", "exam_list", "exam_by_status"}, allEntries = true)
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
    @CacheEvict(value = {"exam", "exam_list", "exam_by_status"}, allEntries = true)
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
    @Cacheable(value = "exam_statistics", key = "#examId")
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
    @Cacheable(value = "exam_progress", key = "#examId")
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
    public Exam publishFinalExam(Integer teacherId, Integer subjectId, List<Integer> classIds, Date academicTerm,
                                Date examStartTime, Integer examDuration) {
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

        // 4. 验证考试时间
        Date now = new Date();
        if (examStartTime.before(now)) {
            throw new RuntimeException("考试开始时间不能早于当前时间");
        }
        if (examDuration <= 0 || examDuration > 180) {
            throw new RuntimeException("考试时长必须在1-180分钟之间");
        }

        // 5. 随机选择一份试卷
        Random random = new Random();
        ExamPaper selectedPaper = finalPapers.get(random.nextInt(finalPapers.size()));

        // 6. 创建期末考试
        Exam finalExam = new Exam();
        finalExam.setExamName("期末考试-" + selectedPaper.getPaperName());
        finalExam.setSubjectId(subjectId);
        finalExam.setPaperId(selectedPaper.getPaperId());
        finalExam.setTeacherId(teacherId);
        finalExam.setExamStatus(0); // 未开始
        finalExam.setExamType(0); // 期末考试
        
        // 设置考试时间
        finalExam.setExamStartTime(examStartTime);
        finalExam.setExamDuration(examDuration);
        
        // 计算考试结束时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(examStartTime);
        calendar.add(Calendar.MINUTE, examDuration);
        finalExam.setExamEndTime(calendar.getTime());
        
        finalExam.setCreatedTime(new Date());

        // 7. 保存考试信息
        if (examMapper.insert(finalExam) <= 0) {
            throw new RuntimeException("创建期末考试失败");
        }

        // 8. 为每个班级创建考试-班级关联
        examMapper.batchInsertExamClass(finalExam.getExamId(), classIds);

        // 9. 为参加考试班级的在班学生创建考试-学生关联
        for (Integer classId : classIds) {
            // 获取班级中的在班学生列表
            List<Student> students = classService.getClassStudents(classId);
            if (students != null && !students.isEmpty()) {
                List<ExamStudent> examStudents = students.stream()
                        .filter(student -> {
                            // 检查学生是否已经与该考试建立关联
                            ExamStudent existingAssociation = examStudentService.getByExamIdAndStudentId(finalExam.getExamId(), student.getStudentId());
                            return existingAssociation == null; // 只保留未建立关联的学生
                        })
                        .map(student -> {
                            ExamStudent examStudent = new ExamStudent();
                            examStudent.setExamId(finalExam.getExamId());
                            examStudent.setStudentId(student.getStudentId());
                            examStudent.setStudentStartTime(null); // 参加时间为空
                            examStudent.setStudentSubmitTime(null); // 提交时间为空
                            examStudent.setAbsent(false); // 未缺考
                            examStudent.setRetakeNeeded(false); // 无需重考
                            examStudent.setDisciplinary(false); // 无违纪
                            examStudent.setTeacherComment(null); // 教师评语为空
                            return examStudent;
                        })
                        .collect(Collectors.toList());
                // 批量插入考试-学生关联记录
                if (!examStudents.isEmpty()) {
                    examStudentMapper.batchInsert(examStudents);
                }
            }
        }

        // 10. 更新所有相关期末试卷状态为已发布
        List<Integer> paperIds = finalPapers.stream()
            .map(ExamPaper::getPaperId)
            .collect(Collectors.toList());
        examPaperService.batchUpdateStatus(paperIds, 1);

        // 11. 更新所有班级的期末考试状态
        for (Integer classId : classIds) {
            Class classInfo = new Class();
            classInfo.setClassId(classId);
            classInfo.setFinalExam(Boolean.TRUE);
            classService.updateById(classInfo);
        }

        return finalExam;
    }

    @Override
    @Transactional
    public int deleteExamClassByClassId(Integer classId) {
        return examMapper.deleteExamClass(null,classId);
    }

    // 每分钟检查一次考试状态
    @Scheduled(cron = "0 * * * * ?")
    public void checkExamStatus() {
        Date now = new Date();
        
        // 检查需要开始的考试
        List<Exam> toStartExams = examMapper.selectByStatus(0); // 未开始的考试
        for (Exam exam : toStartExams) {
            if (exam.getExamStartTime() != null && exam.getExamStartTime().before(now)) {
                updateStatus(exam.getExamId(), 1); // 更新为进行中
            }
        }
        
        // 检查需要结束的考试
        List<Exam> ongoingExams = examMapper.selectByStatus(1); // 进行中的考试
        for (Exam exam : ongoingExams) {
            if (exam.getExamEndTime() != null && exam.getExamEndTime().before(now)) {
                updateStatus(exam.getExamId(), 2); // 更新为已结束
            }
        }
    }

    // 获取考试剩余时间（分钟）
    @Override
    @Cacheable(value = "exam_remaining_time", key = "#examId")
    public Map<String, Object> getRemainingTime(Integer examId) {
        Exam exam = examMapper.selectById(examId);
        Map<String, Object> result = new HashMap<>();
        
        if (exam == null) {
            return result;
        }

        Date now = new Date();
        
        // 如果考试未开始，计算距离开始的时间
        if (exam.getExamStatus() == 0 && exam.getExamStartTime() != null) {
            long remainingToStart = (exam.getExamStartTime().getTime() - now.getTime()) / (1000 * 60);
            result.put("status", "未开始");
            result.put("remainingToStart", remainingToStart);
        }
        
        // 如果考试进行中，计算距离结束的时间
        else if (exam.getExamStatus() == 1 && exam.getExamEndTime() != null) {
            long remainingToEnd = (exam.getExamEndTime().getTime() - now.getTime()) / (1000 * 60);
            result.put("status", "进行中");
            result.put("remainingToEnd", remainingToEnd);
            
            // 计算考试总时长和已用时间
            long totalDuration = exam.getExamDuration();
            long usedTime = totalDuration - remainingToEnd;
            result.put("totalDuration", totalDuration);
            result.put("usedTime", usedTime);
            
            // 计算进度百分比
            double progress = (usedTime * 100.0) / totalDuration;
            result.put("progress", Math.min(100, Math.max(0, progress)));
        }
        // 考试已结束
        else if (exam.getExamStatus() == 2) {
            result.put("status", "已结束");
            result.put("endTime", exam.getExamEndTime());
        }

        return result;
    }

    @Override
    public List<Exam> getRecentlyEndedExams(Date now) {
        // 获取结束时间在当前时间前后1分钟内的考试
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        
        // 当前时间前1分钟
        calendar.add(Calendar.MINUTE, -1);
        Date oneMinuteAgo = calendar.getTime();
        
        // 当前时间后1分钟
        calendar.add(Calendar.MINUTE, 1);
        Date oneMinuteAfter = calendar.getTime();
        
        Map<String, Object> params = new HashMap<>();
        params.put("startTime", oneMinuteAgo);
        params.put("endTime", oneMinuteAfter);
        
        return examMapper.selectRecentlyEndedExams(params);
    }

    @Override
    public List<Exam> getRecentlyStartedExams(Date now) {
        // 获取开始时间在10分钟前的考试
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -10);
        Date tenMinutesAgo = calendar.getTime();
        // 获取开始时间在9-10分钟前的考试（避免重复处理）
        calendar.add(Calendar.MINUTE, 1);
        Date nineMinutesAgo = calendar.getTime();
        
        Map<String, Object> params = new HashMap<>();
        params.put("startTime", tenMinutesAgo);
        params.put("endTime", nineMinutesAgo);
        
        return examMapper.selectRecentlyStartedExams(params);
    }







    @Override
    @Transactional
    public Exam publishNormalExam(Integer teacherId, Integer subjectId, List<Integer> classIds, Integer paperId, 
                                Date examStartTime, Integer examDuration) {
        // 1. 验证教师权限
        Teacher teacher = teacherService.getById(teacherId);
        if (teacher == null || (teacher.getPermission() != 0 && teacher.getPermission() != 1)) {
            throw new RuntimeException("权限不足，只有管理员教师或普通教师可以发布普通考试");
        }

        // 2. 验证试卷是否存在
        ExamPaper examPaper = examPaperService.getById(paperId);
        if (examPaper == null) {
            throw new RuntimeException("试卷不存在");
        }

        // 3. 验证考试时间
        Date now = new Date();
        if (examStartTime.before(now)) {
            throw new RuntimeException("考试开始时间不能早于当前时间");
        }
        if (examDuration <= 0 || examDuration > 180) {
            throw new RuntimeException("考试时长必须在1-180分钟之间");
        }

        // 4. 创建普通考试
        Exam normalExam = new Exam();
        normalExam.setExamName("普通考试-" + examPaper.getPaperName());
        normalExam.setSubjectId(subjectId);
        normalExam.setPaperId(paperId);
        normalExam.setTeacherId(teacherId);
        normalExam.setExamStatus(0); // 未开始
        normalExam.setExamType(0); // 普通考试
        
        // 设置考试时间
        normalExam.setExamStartTime(examStartTime);
        normalExam.setExamDuration(examDuration);
        
        // 计算考试结束时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(examStartTime);
        calendar.add(Calendar.MINUTE, examDuration);
        normalExam.setExamEndTime(calendar.getTime());
        
        normalExam.setCreatedTime(new Date());

        // 5. 保存考试信息
        if (examMapper.insert(normalExam) <= 0) {
            throw new RuntimeException("创建普通考试失败");
        }

        // 6. 为每个班级创建考试-班级关联
        examMapper.batchInsertExamClass(normalExam.getExamId(), classIds);

        // 7. 为参加考试班级的在班学生创建考试-学生关联
        for (Integer classId : classIds) {
            // 获取班级中的在班学生列表
            List<Student> students = classService.getClassStudents(classId);
            
            if (students != null && !students.isEmpty()) {
                // 过滤掉已经与该考试建立关联的学生
                List<ExamStudent> examStudents = students.stream()
                        .filter(student -> {
                            // 检查学生是否已经与该考试建立关联
                            ExamStudent existingAssociation = examStudentService.getByExamIdAndStudentId(normalExam.getExamId(), student.getStudentId());
                            return existingAssociation == null; // 只保留未建立关联的学生
                        })
                        .map(student -> {
                            ExamStudent examStudent = new ExamStudent();
                            examStudent.setExamId(normalExam.getExamId());
                            examStudent.setStudentId(student.getStudentId());
                            examStudent.setStudentStartTime(null); // 参加时间为空
                            examStudent.setStudentSubmitTime(null); // 提交时间为空
                            examStudent.setAbsent(false); // 未缺考
                            examStudent.setRetakeNeeded(false); // 无需重考
                            examStudent.setDisciplinary(false); // 无违纪
                            examStudent.setTeacherComment(null); // 教师评语为空
                            return examStudent;
                        })
                        .collect(Collectors.toList());
                
                // 只有当有新的学生需要添加时才执行批量插入
                if (!examStudents.isEmpty()) {
                    examStudentMapper.batchInsert(examStudents);
                }
            }
        }

        // 8. 将试卷状态更新为已发布
        examPaperService.updateStatus(paperId, 1);

        return normalExam;
    }

    @Override
    @Transactional
    public Exam publishRetakeExam(Integer teacherId, Integer subjectId, List<Integer> studentIds, Integer paperId,
                                Date examStartTime, Integer examDuration) {
        // 1. 验证教师权限
        Teacher teacher = teacherService.getById(teacherId);
        if (teacher == null || (teacher.getPermission() != 0 && teacher.getPermission() != 1)) {
            throw new RuntimeException("权限不足，只有管理员教师或普通教师可以发布重考考试");
        }

        // 2. 验证试卷是否存在
        ExamPaper examPaper = examPaperService.getById(paperId);
        if (examPaper == null) {
            throw new RuntimeException("试卷不存在");
        }

        // 3. 验证考试时间
        Date now = new Date();
        if (examStartTime.before(now)) {
            throw new RuntimeException("考试开始时间不能早于当前时间");
        }
        if (examDuration <= 0 || examDuration > 180) {
            throw new RuntimeException("考试时长必须在1-180分钟之间");
        }

        // 4. 创建重考考试
        Exam retakeExam = new Exam();
        retakeExam.setExamName("重考考试-" + examPaper.getPaperName());
        retakeExam.setSubjectId(subjectId);
        retakeExam.setPaperId(paperId);
        retakeExam.setTeacherId(teacherId);
        retakeExam.setExamStatus(0); // 未开始
        retakeExam.setExamType(1); // 重考考试
        
        // 设置考试时间
        retakeExam.setExamStartTime(examStartTime);
        retakeExam.setExamDuration(examDuration);
        
        // 计算考试结束时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(examStartTime);
        calendar.add(Calendar.MINUTE, examDuration);
        retakeExam.setExamEndTime(calendar.getTime());
        
        retakeExam.setCreatedTime(new Date());

        // 5. 保存考试信息
        if (examMapper.insert(retakeExam) <= 0) {
            throw new RuntimeException("创建重考考试失败");
        }

        // 6. 为重考学生创建考试-学生关联
        List<ExamStudent> examStudents = studentIds.stream()
                .map(studentId -> {
                    ExamStudent examStudent = new ExamStudent();
                    examStudent.setExamId(retakeExam.getExamId());
                    examStudent.setStudentId(studentId);
                    examStudent.setStudentStartTime(null); // 参加时间为空
                    examStudent.setStudentSubmitTime(null); // 提交时间为空
                    examStudent.setAbsent(false); // 未缺考
                    examStudent.setRetakeNeeded(false); // 无需重考
                    examStudent.setDisciplinary(false); // 无违纪
                    examStudent.setTeacherComment(null); // 教师评语为空
                    return examStudent;
                })
                .collect(Collectors.toList());

        // 批量插入考试-学生关联记录
        if (!examStudents.isEmpty()) {
            examStudentMapper.batchInsert(examStudents);
        }

        // 7. 将试卷状态更新为已发布
        examPaperService.updateStatus(paperId, 1);

        return retakeExam;
    }
} 