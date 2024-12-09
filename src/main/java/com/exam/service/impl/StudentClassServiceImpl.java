package com.exam.service.impl;

import com.exam.entity.StudentClass;
import com.exam.entity.Student;
import com.exam.entity.Class;
import com.exam.mapper.StudentClassMapper;
import com.exam.mapper.StudentMapper;
import com.exam.mapper.ClassMapper;
import com.exam.service.StudentClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * 学生-班级关联服务实现类
 */
@Service
@Transactional
public class StudentClassServiceImpl implements StudentClassService {

    @Autowired
    private StudentClassMapper studentClassMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private ClassMapper classMapper;

    @Override
    public int insert(StudentClass studentClass) {
        if (!validateStudentClass(studentClass)) {
            return 0;
        }
        // 设置默认值
        if (studentClass.getStatus() == null) {
            studentClass.setStatus(true);
        }
        if (studentClass.getJoinTime() == null) {
            studentClass.setJoinTime(new Date());
        }
        return studentClassMapper.insert(studentClass);
    }

    @Override
    public int deleteById(Integer scId) {
        return studentClassMapper.deleteById(scId);
    }

    @Override
    public int update(StudentClass studentClass) {
        if (!validateStudentClass(studentClass)) {
            return 0;
        }
        return studentClassMapper.update(studentClass);
    }

    @Override
    public StudentClass selectById(Integer scId) {
        return studentClassMapper.selectById(scId);
    }

    @Override
    public List<StudentClass> selectAll() {
        return studentClassMapper.selectAll();
    }

    @Override
    public List<StudentClass> getByStudentId(Integer studentId) {
        return studentClassMapper.selectByStudentId(studentId);
    }

    @Override
    public List<StudentClass> getByClassId(Integer classId) {
        return studentClassMapper.selectByClassId(classId);
    }

    @Override
    public StudentClass getByStudentAndClass(Integer studentId, Integer classId) {
        return studentClassMapper.selectByStudentAndClass(studentId, classId);
    }

    @Override
    public int updateStatusAndTime(Integer scId, Boolean status, Date joinTime, Date leftTime) {
        return studentClassMapper.updateStatusAndTime(scId, status, joinTime, leftTime);
    }

    @Override
    public int batchInsert(List<StudentClass> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }
        // 验证所有记录
        for (StudentClass sc : list) {
            if (!validateStudentClass(sc)) {
                return 0;
            }
            // 设置默认值
            if (sc.getStatus() == null) {
                sc.setStatus(true);
            }
            if (sc.getJoinTime() == null) {
                sc.setJoinTime(new Date());
            }
        }
        return studentClassMapper.batchInsert(list);
    }

    @Override
    public int batchUpdateStatus(List<Integer> studentIds, Integer classId, Boolean status) {
        if (studentIds == null || studentIds.isEmpty() || classId == null || status == null) {
            return 0;
        }
        return studentClassMapper.batchUpdateStatus(studentIds, classId, status);
    }

    @Override
    public int countStudentsByClass(Integer classId) {
        return studentClassMapper.countStudentsByClass(classId);
    }

    @Override
    public int countClassesByStudent(Integer studentId) {
        return studentClassMapper.countClassesByStudent(studentId);
    }

    @Override
    public List<Student> getActiveStudents(Integer classId) {
        return studentClassMapper.selectActiveStudents(classId);
    }

    @Override
    public List<Class> getStudentClasses(Integer studentId) {
        return studentClassMapper.selectStudentClasses(studentId);
    }

    @Override
    public List<Map<String, Object>> analyzeClassStudentFlow(Integer classId, Date startTime, Date endTime) {
        return studentClassMapper.analyzeClassStudentFlow(classId, startTime, endTime);
    }

    @Override
    public int batchDelete(List<Integer> scIds) {
        if (scIds == null || scIds.isEmpty()) {
            return 0;
        }
        return studentClassMapper.batchDelete(scIds);
    }

    @Override
    public boolean checkStudentInClass(Integer studentId, Integer classId) {
        return studentClassMapper.checkStudentInClass(studentId, classId);
    }

    @Override
    public int joinClass(Integer studentId, Integer classId) {
        // 检查学生和班级是否存在
        if (studentMapper.selectById(studentId) == null || 
            classMapper.selectById(classId) == null) {
            return 0;
        }
        // 检查是否已经在班级中
        if (checkStudentInClass(studentId, classId)) {
            return 0;
        }
        // 创建关联记录
        StudentClass sc = new StudentClass();
        sc.setStudentId(studentId);
        sc.setClassId(classId);
        sc.setStatus(true);
        sc.setJoinTime(new Date());
        return insert(sc);
    }

    @Override
    public int leaveClass(Integer studentId, Integer classId) {
        StudentClass sc = getByStudentAndClass(studentId, classId);
        if (sc == null || !sc.getStatus()) {
            return 0;
        }
        return updateStatusAndTime(sc.getScId(), false, sc.getJoinTime(), new Date());
    }

    @Override
    public boolean validateStudentClass(StudentClass studentClass) {
        if (studentClass == null) {
            return false;
        }
        // 验证必填字段
        if (studentClass.getStudentId() == null || studentClass.getClassId() == null) {
            return false;
        }
        // 如果设置了ID，确保记录存在
        if (studentClass.getScId() != null && selectById(studentClass.getScId()) == null) {
            return false;
        }
        // 验证学生和班级是否存在
        if (studentMapper.selectById(studentClass.getStudentId()) == null || 
            classMapper.selectById(studentClass.getClassId()) == null) {
            return false;
        }
        return true;
    }
} 