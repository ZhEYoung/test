package com.exam.service;

import com.exam.entity.ExamPaper;
import com.exam.entity.Question;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

/**
 * 试卷服务接口
 */
public interface ExamPaperService {
    
    /**
     * 插入一条记录
     */
    int insert(ExamPaper record);

    /**
     * 根据ID删除
     */
    int deleteById(Integer id);

    /**
     * 根据ID更新
     */
    int updateById(ExamPaper record);

    /**
     * 根据ID查询
     */
    ExamPaper getById(Integer id);

    /**
     * 查询所有记录
     */
    List<ExamPaper> getAll();

    /**
     * 分页查询
     */
    List<ExamPaper> getPage(Integer pageNum, Integer pageSize);

    /**
     * 查询总记录数
     */
    Long getCount();

    /**
     * 条件查询
     */
    List<ExamPaper> getByCondition(Map<String, Object> condition);

    /**
     * 条件查询记录数
     */
    Long getCountByCondition(Map<String, Object> condition);

    /**
     * 条件分页查询
     */
    List<ExamPaper> getPageByCondition(Map<String, Object> condition, Integer pageNum, Integer pageSize);
    
    /**
     * 根据学科ID查询试卷列表
     */
    List<ExamPaper> getBySubjectId(Integer subjectId);
    
    /**
     * 根据教师ID查询试卷列表
     */
    List<ExamPaper> getByTeacherId(Integer teacherId);
    
    /**
     * 根据试卷名称查询
     */
    ExamPaper getByName(String paperName);
    
    /**
     * 根据试卷状态查询
     */
    List<ExamPaper> getByStatus(Integer paperStatus);
    
    /**
     * 根据考试类型查询
     */
    List<ExamPaper> getByExamType(Integer examType);
    
    /**
     * 更新试卷状态
     */
    int updateStatus(Integer paperId, Integer status);
    
    /**
     * 根据难度范围查询试卷
     */
    List<ExamPaper> getByDifficultyRange(BigDecimal minDifficulty, BigDecimal maxDifficulty);
    
    /**
     * 查询试卷中的所有题目
     */
    List<Question> getPaperQuestions(Integer paperId);
    
    /**
     * 查询试卷题目及分值
     */
    List<Map<String, Object>> getPaperQuestionsWithScore(Integer paperId);
    
    /**
     * 更新试卷题目分值
     */
    int updateQuestionScore(Integer paperId, Integer questionId, BigDecimal score);
    
    /**
     * 批量更新试卷题目分值
     */
    int batchUpdateQuestionScores(Integer paperId, List<Integer> questionIds, List<BigDecimal> scores);
    
    /**
     * 统计试卷总分
     */
    BigDecimal calculateTotalScore(Integer paperId);
    
    /**
     * 统计试卷平均分
     */
    BigDecimal calculateAverageScore(Integer paperId);
    
    /**
     * 统计试卷及格率
     */
    BigDecimal calculatePassRate(Integer paperId, BigDecimal passScore);
    
    /**
     * 查询试卷成绩分布
     */
    List<Map<String, Object>> getScoreDistribution(Integer paperId);
    
    /**
     * 查询最高分
     */
    BigDecimal getHighestScore(Integer paperId);
    
    /**
     * 查询最低分
     */
    BigDecimal getLowestScore(Integer paperId);
    
    /**
     * 批量发布试卷
     */
    int batchPublish(List<Integer> paperIds);
    
    /**
     * 批量更新试卷状态
     */
    int batchUpdateStatus(List<Integer> paperIds, Integer status);
    
    /**
     * 复制试卷（包含题目）
     */
    int copyPaper(Integer sourcePaperId, String newPaperName);
    
    /**
     * 自动组卷
     * @param subjectId 学科ID
     * @param paperName 试卷名称
     * @param difficulty 难度系数
     * @param questionTypeCount 各题型数量
     * @param typeScoreRatio 各题型分数比例（可选，如果为null则根据题目数量自动计算）
     * @param teacherId 教师ID
     * @param academicTerm 学年学期
     * @param examType 0: 期末考试，1: 普通考试
     * @return 组卷结果
     */
    ExamPaper generatePaper(Integer subjectId, String paperName,
                          BigDecimal difficulty, Map<Integer, Integer> questionTypeCount,
                          Map<Integer, BigDecimal> typeScoreRatio, Integer teacherId,
                          Date academicTerm, Integer examType);
    
    /**
     * 手动组卷
     * @param subjectId 学科ID
     * @param paperName 试卷名称
     * @param questionScores 题目ID和对应分值的映射
     * @param difficulty 难度系数
     * @param examType 试卷类型
     * @param academicTerm 学年学期
     * @param teacherId 教师ID
     * @return 组卷结果
     */
    ExamPaper generatePaperManually(Integer subjectId, String paperName,
                                    Map<Integer, BigDecimal> questionScores,
                                    BigDecimal difficulty, Integer examType,
                                    Date academicTerm, Integer teacherId);
} 