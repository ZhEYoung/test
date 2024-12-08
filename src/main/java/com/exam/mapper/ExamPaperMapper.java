package com.exam.mapper;

import com.exam.entity.ExamPaper;
import com.exam.entity.Question;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 试卷Mapper接口
 */
public interface ExamPaperMapper extends BaseMapper<ExamPaper> {
    /**
     * 根据学科ID查询试卷列表
     */
    List<ExamPaper> selectBySubjectId(@Param("subjectId") Integer subjectId);

    /**
     * 根据教师ID查询试卷列表
     */
    List<ExamPaper> selectByTeacherId(@Param("teacherId") Integer teacherId);

    /**
     * 根据试卷名称查询
     */
    ExamPaper selectByName(@Param("paperName") String paperName);

    /**
     * 根据试卷状态查询
     */
    List<ExamPaper> selectByStatus(@Param("paperStatus") Integer paperStatus);

    /**
     * 根据考试类型查询
     */
    List<ExamPaper> selectByExamType(@Param("examType") Integer examType);

    /**
     * 更新试卷状态
     */
    int updateStatus(@Param("paperId") Integer paperId, @Param("status") Integer status);

    /**
     * 根据难度范围查询试卷
     */
    List<ExamPaper> selectByDifficultyRange(
        @Param("minDifficulty") BigDecimal minDifficulty, 
        @Param("maxDifficulty") BigDecimal maxDifficulty
    );

    /**
     * 查询试卷中的所有题目
     */
    List<Question> selectPaperQuestions(@Param("paperId") Integer paperId);

    /**
     * 查询试卷题目及分值
     */
    List<Map<String, Object>> selectPaperQuestionsWithScore(@Param("paperId") Integer paperId);

    /**
     * 更新试卷题目分值
     */
    int updateQuestionScore(
        @Param("paperId") Integer paperId,
        @Param("questionId") Integer questionId,
        @Param("score") BigDecimal score
    );

    /**
     * 批量更新试卷题目分值
     */
    int batchUpdateQuestionScores(
        @Param("paperId") Integer paperId,
        @Param("questionIds") List<Integer> questionIds,
        @Param("scores") List<BigDecimal> scores
    );

    /**
     * 查询指定学期的试卷
     */
    List<ExamPaper> selectByAcademicTerm(@Param("academicTerm") Date academicTerm);

    /**
     * 统计试卷总分
     */
    BigDecimal calculateTotalScore(@Param("paperId") Integer paperId);

    /**
     * 统计试卷平均分
     */
    BigDecimal calculateAverageScore(@Param("paperId") Integer paperId);

    /**
     * 统计试卷及格率
     */
    BigDecimal calculatePassRate(
        @Param("paperId") Integer paperId,
        @Param("passScore") BigDecimal passScore
    );

    /**
     * 查询试卷成绩分布
     */
    List<Map<String, Object>> selectScoreDistribution(@Param("paperId") Integer paperId);

    /**
     * 查询最高分
     */
    BigDecimal selectHighestScore(@Param("paperId") Integer paperId);

    /**
     * 查询最低分
     */
    BigDecimal selectLowestScore(@Param("paperId") Integer paperId);

    /**
     * 批量发布试卷
     */
    int batchPublish(@Param("paperIds") List<Integer> paperIds);

    /**
     * 批量更新试卷状态
     */
    int batchUpdateStatus(
        @Param("paperIds") List<Integer> paperIds,
        @Param("status") Integer status
    );

    /**
     * 复制试卷（包含题目）
     */
    int copyPaper(
        @Param("sourcePaperId") Integer sourcePaperId,
        @Param("newPaperName") String newPaperName
    );
} 