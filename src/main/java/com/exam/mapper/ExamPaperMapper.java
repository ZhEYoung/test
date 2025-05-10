package com.exam.mapper;

import com.exam.entity.ExamPaper;
import com.exam.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 试卷Mapper接口
 */
@Mapper
public interface ExamPaperMapper {
    /**
     * 插入试卷记录
     * @param examPaper 试卷实体
     * @return 影响行数
     */
    int insert(ExamPaper examPaper);

    /**
     * 根据ID删除试卷
     * @param paperId 试卷ID
     * @return 影响行数
     */
    int deleteById(@Param("paperId") Integer paperId);

    /**
     * 更新试卷信息
     * @param examPaper 试卷实体
     * @return 影响行数
     */
    int update(ExamPaper examPaper);

    /**
     * 根据ID查询试卷
     * @param paperId 试卷ID
     * @return 试卷实体
     */
    ExamPaper selectById(@Param("paperId") Integer paperId);

    /**
     * 查询所有试卷
     * @return 试卷列表
     */
    List<ExamPaper> selectAll();

    /**
     * 根据学科ID查询试卷列表
     * @param subjectId 学科ID
     * @return 试卷列表
     */
    List<ExamPaper> selectBySubjectId(@Param("subjectId") Integer subjectId);

    /**
     * 根据教师ID查询试卷列表
     * @param teacherId 教师ID
     * @return 试卷列表
     */
    List<ExamPaper> selectByTeacherId(@Param("teacherId") Integer teacherId);

    /**
     * 根据试卷名称查询
     * @param paperName 试卷名称
     * @return 试卷实体
     */
    ExamPaper selectByName(@Param("paperName") String paperName);

    /**
     * 根据试卷状态查询
     * @param paperStatus 试卷状态
     * @return 试卷列表
     */
    List<ExamPaper> selectByStatus(@Param("paperStatus") Integer paperStatus);

    /**
     * 根据考试类型查询
     * @param examType 考试类型
     * @return 试卷列表
     */
    List<ExamPaper> selectByExamType(@Param("examType") Integer examType);

    /**
     * 更新试卷状态
     * @param paperId 试卷ID
     * @param status 状态
     * @return 更新结果
     */
    int updateStatus(@Param("paperId") Integer paperId, @Param("status") Integer status);

    /**
     * 根据难度范围查询试卷
     * @param minDifficulty 最小难度
     * @param maxDifficulty 最大难度
     * @return 试卷列表
     */
    List<ExamPaper> selectByDifficultyRange(
        @Param("minDifficulty") BigDecimal minDifficulty, 
        @Param("maxDifficulty") BigDecimal maxDifficulty
    );

    /**
     * 查询试卷中的所有题目
     * @param paperId 试卷ID
     * @return 题目列表
     */
    List<Question> selectPaperQuestions(@Param("paperId") Integer paperId);

    /**
     * 查询试卷题目及分值
     * @param paperId 试卷ID
     * @return 题目及分值信息
     */
    List<Map<String, Object>> selectPaperQuestionsWithScore(@Param("paperId") Integer paperId);

    /**
     * 更新试卷题目分值
     * @param paperId 试卷ID
     * @param questionId 题目ID
     * @param score 分值
     * @return 更新结果
     */
    int updateQuestionScore(
        @Param("paperId") Integer paperId,
        @Param("questionId") Integer questionId,
        @Param("score") BigDecimal score
    );

    /**
     * 批量更新试卷题目分值
     * @param paperId 试卷ID
     * @param questionIds 题目ID列表
     * @param scores 分值列表
     * @return 更新结果
     */
    int batchUpdateQuestionScores(
        @Param("paperId") Integer paperId,
        @Param("questionIds") List<Integer> questionIds,
        @Param("scores") List<BigDecimal> scores
    );

    /**
     * 查询指定学期的试卷
     * @param academicTerm 学期
     * @return 试卷列表
     */
    List<ExamPaper> selectByAcademicTerm(@Param("academicTerm") Date academicTerm);

    /**
     * 统计试卷总分
     * @param paperId 试卷ID
     * @return 总分
     */
    BigDecimal calculateTotalScore(@Param("paperId") Integer paperId);

    /**
     * 统计试卷平均分
     * @param paperId 试卷ID
     * @return 平均分
     */
    BigDecimal calculateAverageScore(@Param("paperId") Integer paperId);

    /**
     * 统计试卷及格率
     * @param paperId 试卷ID
     * @param passScore 及格分数
     * @return 及格率
     */
    BigDecimal calculatePassRate(
        @Param("paperId") Integer paperId,
        @Param("passScore") BigDecimal passScore
    );

    /**
     * 查询试卷成绩分布
     * @param paperId 试卷ID
     * @return 成绩分布信息
     */
    List<Map<String, Object>> selectScoreDistribution(@Param("paperId") Integer paperId);

    /**
     * 查询最高分
     * @param paperId 试卷ID
     * @return 最高分
     */
    BigDecimal selectHighestScore(@Param("paperId") Integer paperId);

    /**
     * 查询最低分
     * @param paperId 试卷ID
     * @return 最低分
     */
    BigDecimal selectLowestScore(@Param("paperId") Integer paperId);

    /**
     * 批量发布试卷
     * @param paperIds 试卷ID列表
     * @return 更新结果
     */
    int batchPublish(@Param("paperIds") List<Integer> paperIds);

    /**
     * 批量更新试卷状态
     * @param paperIds 试卷ID列表
     * @param status 状态
     * @return 更新结果
     */
    int batchUpdateStatus(
        @Param("paperIds") List<Integer> paperIds,
        @Param("status") Integer status
    );

    /**
     * 复制试卷（包含题目）
     * @param sourcePaperId 源试卷ID
     * @param newPaperName 新试卷名称
     * @return 更新结果
     */
    int copyPaper(
        @Param("sourcePaperId") Integer sourcePaperId,
        @Param("newPaperName") String newPaperName
    );

    // 查询总记录数
    Long selectCount();

    // 条件查询
    List<ExamPaper> selectByCondition(Map<String, Object> condition);

    // 条件查询记录数
    Long selectCountByCondition(Map<String, Object> condition);

    /**
     * 条件分页查询
     * @param condition 查询条件（包含offset和limit）
     * @return 试卷列表
     */
    List<ExamPaper> selectPageByCondition(@Param("condition") Map<String, Object> condition);
} 