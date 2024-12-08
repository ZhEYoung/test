-- 创建数据库
DROP DATABASE IF EXISTS exam_system;
CREATE DATABASE exam_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE exam_system;

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    user_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(10) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    role INT NOT NULL COMMENT '0: 管理员；1: 教师；2: 学生',
    status BOOLEAN DEFAULT 1 COMMENT '账号状态',
    sex BOOLEAN COMMENT '0: 女；1: 男',
    phone VARCHAR(20) COMMENT '联系方式',
    email VARCHAR(50) UNIQUE COMMENT '邮箱',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    INDEX idx_username (username),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 管理员表
CREATE TABLE IF NOT EXISTS admin (
    admin_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '管理员ID',
    user_id INT NOT NULL COMMENT '用户ID',
    name VARCHAR(20) NOT NULL COMMENT '姓名',
    other VARCHAR(100) COMMENT '备注',
    INDEX idx_admin_id (admin_id),
    INDEX idx_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 学院表
CREATE TABLE IF NOT EXISTS college (
    college_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '学院ID',
    college_name VARCHAR(50) UNIQUE NOT NULL COMMENT '学院名称',
    description VARCHAR(255) COMMENT '学院描述',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_college_id (college_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学院表';

-- 教师表
CREATE TABLE IF NOT EXISTS teacher (
    teacher_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '教师ID',
    user_id INT NOT NULL COMMENT '用户ID',
    name VARCHAR(20) NOT NULL COMMENT '姓名',
    permission INT COMMENT '0: 可以组卷与发布所有考试；1: 可以组卷与发布普通考试；2: 可以组卷',
    college_id INT COMMENT '学院ID',
    other VARCHAR(100) COMMENT '备注',
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_user_id (user_id),
    INDEX idx_college_id (college_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (college_id) REFERENCES college(college_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师表';

-- 学生表
CREATE TABLE IF NOT EXISTS student (
    student_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '学生ID',
    user_id INT NOT NULL COMMENT '用户ID',
    name VARCHAR(20) NOT NULL COMMENT '姓名',
    grade VARCHAR(10) NOT NULL COMMENT '年级',
    college_id INT COMMENT '学院ID',
    other VARCHAR(100) COMMENT '备注',
    INDEX idx_student_id (student_id),
    INDEX idx_user_id (user_id),
    INDEX idx_college_id (college_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (college_id) REFERENCES college(college_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';

-- 学科表
CREATE TABLE IF NOT EXISTS subject (
    subject_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '学科ID',
    subject_name VARCHAR(50) UNIQUE NOT NULL COMMENT '学科名称',
    description VARCHAR(255) COMMENT '学科描述',
    college_id INT COMMENT '学院ID',
    INDEX idx_subject_id (subject_id),
    INDEX idx_college_id (college_id),
    FOREIGN KEY (college_id) REFERENCES college(college_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学科表';

-- 班级表
CREATE TABLE IF NOT EXISTS class (
    class_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '班级ID',
    teacher_id INT NOT NULL COMMENT '教师ID',
    class_name VARCHAR(100) COMMENT '课程名',
    subject_id INT NOT NULL COMMENT '学科编号',
    final_exam BOOLEAN DEFAULT 0 COMMENT '是否有期末考试',
    INDEX idx_class_id (class_id),
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_subject_id (subject_id),
    FOREIGN KEY (teacher_id) REFERENCES teacher(teacher_id),
    FOREIGN KEY (subject_id) REFERENCES subject(subject_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- 学生-班级关联表
CREATE TABLE IF NOT EXISTS student_class (
    sc_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    student_id INT NOT NULL COMMENT '学生ID',
    class_id INT NOT NULL COMMENT '班级ID',
    status BOOLEAN DEFAULT 1 COMMENT '状态',
    join_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    left_time TIMESTAMP COMMENT '退出时间',
    INDEX idx_sc_id (sc_id),
    INDEX idx_student_id (student_id),
    INDEX idx_class_id (class_id),
    FOREIGN KEY (student_id) REFERENCES student(student_id),
    FOREIGN KEY (class_id) REFERENCES class(class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生-班级关联表';

-- 题库表
CREATE TABLE IF NOT EXISTS question_bank (
    qb_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '题库ID',
    qb_name VARCHAR(20) UNIQUE NOT NULL COMMENT '题库名称',
    subject_id INT NOT NULL COMMENT '学科编号',
    INDEX idx_qb_id (qb_id),
    INDEX idx_subject_id (subject_id),
    FOREIGN KEY (subject_id) REFERENCES subject(subject_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题库表';

-- 题目表
CREATE TABLE IF NOT EXISTS question (
    question_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '题目ID',
    qb_id INT NOT NULL COMMENT '题库ID',
    content TEXT NOT NULL COMMENT '题目内容',
    answer VARCHAR(255) COMMENT '答案',
    type INT NOT NULL COMMENT '0: 单选；1: 多选；2: 判断；3: 填空；4: 简答',
    difficulty DECIMAL(5,2) COMMENT '难度',
    INDEX idx_question_id (question_id),
    INDEX idx_qb_id (qb_id),
    FOREIGN KEY (qb_id) REFERENCES question_bank(qb_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表';

-- 题目选项表
CREATE TABLE IF NOT EXISTS question_option (
    option_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '选项ID',
    question_id INT NOT NULL COMMENT '题目ID',
    content VARCHAR(255) NOT NULL COMMENT '选项内容',
    is_correct BOOLEAN DEFAULT 0 COMMENT '是否正确',
    INDEX idx_option_id (option_id),
    INDEX idx_question_id (question_id),
    FOREIGN KEY (question_id) REFERENCES question(question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目选项表';

-- 试卷表
CREATE TABLE IF NOT EXISTS exam_paper (
    paper_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '试卷ID',
    paper_name VARCHAR(50) UNIQUE NOT NULL COMMENT '试卷名称',
    paper_status INT DEFAULT 0 COMMENT '0: 未发布，1: 已发布',
    subject_id INT NOT NULL COMMENT '学科编号',
    teacher_id INT NOT NULL COMMENT '创建教师ID',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    exam_type INT COMMENT '0: 期末，1: 普通考试',
    academic_term DATETIME COMMENT '学年学期',
    paper_difficulty DECIMAL(5,2) COMMENT '试卷难度',
    INDEX idx_paper_id (paper_id),
    INDEX idx_subject_id (subject_id),
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_exam_type (exam_type),
    FOREIGN KEY (subject_id) REFERENCES subject(subject_id),
    FOREIGN KEY (teacher_id) REFERENCES teacher(teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷表';

-- 试卷-题目关联表
CREATE TABLE IF NOT EXISTS exam_paper_question (
    epq_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    paper_id INT NOT NULL COMMENT '试卷ID',
    question_id INT NOT NULL COMMENT '题目ID',
    question_order INT COMMENT '题目顺序',
    question_score DECIMAL(5,2) COMMENT '题目分值',
    INDEX idx_epq_id (epq_id),
    INDEX idx_paper_id (paper_id),
    INDEX idx_question_id (question_id),
    FOREIGN KEY (paper_id) REFERENCES exam_paper(paper_id),
    FOREIGN KEY (question_id) REFERENCES question(question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷-题目关联表';

-- 考试表
CREATE TABLE IF NOT EXISTS exam (
    exam_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '考试ID',
    exam_name VARCHAR(100) NOT NULL COMMENT '考试名称',
    subject_id INT NOT NULL COMMENT '学科编号',
    paper_id INT NOT NULL COMMENT '试卷编号',
    exam_start_time TIMESTAMP NOT NULL COMMENT '考试开始时间',
    exam_end_time TIMESTAMP NOT NULL COMMENT '考试结束时间',
    exam_duration INT NOT NULL COMMENT '考试时长（分钟）',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    teacher_id INT NOT NULL COMMENT '监考教师ID',
    exam_status INT DEFAULT 0 COMMENT '0: 未开始；1: 进行中；2: 已结束',
    exam_type INT DEFAULT 0 COMMENT '0: 正常考试；1: 重考',
    INDEX idx_exam_id (exam_id),
    INDEX idx_subject_id (subject_id),
    INDEX idx_paper_id (paper_id),
    INDEX idx_teacher_id (teacher_id),
    FOREIGN KEY (subject_id) REFERENCES subject(subject_id),
    FOREIGN KEY (paper_id) REFERENCES exam_paper(paper_id),
    FOREIGN KEY (teacher_id) REFERENCES teacher(teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试表';

-- 考试-班级关联表
CREATE TABLE IF NOT EXISTS exam_class (
    ec_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    exam_id INT NOT NULL COMMENT '考试ID',
    class_id INT NOT NULL COMMENT '班级ID',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_ec_id (ec_id),
    INDEX idx_exam_id (exam_id),
    INDEX idx_class_id (class_id),
    FOREIGN KEY (exam_id) REFERENCES exam(exam_id),
    FOREIGN KEY (class_id) REFERENCES class(class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试-班级关联表';

-- 考试-学生关联表
CREATE TABLE IF NOT EXISTS exam_student (
    es_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    exam_id INT NOT NULL COMMENT '考试ID',
    student_id INT NOT NULL COMMENT '学生ID',
    student_start_time TIMESTAMP COMMENT '参加时间',
    student_submit_time TIMESTAMP COMMENT '提交时间',
    absent BOOLEAN DEFAULT FALSE COMMENT '缺考标记',
    retake_needed BOOLEAN DEFAULT FALSE COMMENT '是否需要重考',
    disciplinary BOOLEAN DEFAULT FALSE COMMENT '违纪标记',
    teacher_comment VARCHAR(100) COMMENT '教师评语',
    INDEX idx_es_id (es_id),
    INDEX idx_exam_id (exam_id),
    INDEX idx_student_id (student_id),
    FOREIGN KEY (exam_id) REFERENCES exam(exam_id),
    FOREIGN KEY (student_id) REFERENCES student(student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试-学生关联表';

-- 学生成绩表
CREATE TABLE IF NOT EXISTS student_score (
    score_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '成绩ID',
    student_id INT NOT NULL COMMENT '学生ID',
    exam_id INT NOT NULL COMMENT '考试ID',
    score DECIMAL(5,2) COMMENT '成绩',
    upload_time TIMESTAMP COMMENT '成绩上传时间',
    INDEX idx_score_id (score_id),
    INDEX idx_student_id (student_id),
    INDEX idx_exam_id (exam_id),
    FOREIGN KEY (student_id) REFERENCES student(student_id),
    FOREIGN KEY (exam_id) REFERENCES exam(exam_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生成绩表';

-- 学生题目成绩表
CREATE TABLE IF NOT EXISTS student_question_score (
    record_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    exam_id INT NOT NULL COMMENT '考试ID',
    student_id INT NOT NULL COMMENT '学生ID',
    question_id INT NOT NULL COMMENT '题目ID',
    score_id INT NOT NULL COMMENT '成绩ID',
    answer TEXT COMMENT '学生答案',
    score DECIMAL(5,2) DEFAULT 0 COMMENT '得分',
    status INT DEFAULT 0 COMMENT '0: 未批改；1: 已批改',
    INDEX idx_record_id (record_id),
    INDEX idx_exam_id (exam_id),
    INDEX idx_student_id (student_id),
    INDEX idx_question_id (question_id),
    INDEX idx_score_id (score_id),
    FOREIGN KEY (exam_id) REFERENCES exam(exam_id),
    FOREIGN KEY (student_id) REFERENCES student(student_id),
    FOREIGN KEY (question_id) REFERENCES question(question_id),
    FOREIGN KEY (score_id) REFERENCES student_score(score_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生题目成绩表';

-- 日志表
CREATE TABLE IF NOT EXISTS log (
    log_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id INT NOT NULL COMMENT '用户ID',
    action_type INT NOT NULL COMMENT '0: INSERT、1: UPDATE、2: DELETE、3: LOGIN、4: SUBMIT_TEST',
    action_description TEXT COMMENT '操作描述',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    object_type TEXT COMMENT '操作对象',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    device_info TEXT COMMENT '设备信息',
    status VARCHAR(20) COMMENT '操作状态',
    INDEX idx_log_id (log_id),
    INDEX idx_user_id (user_id),
    INDEX idx_action_type (action_type),
    INDEX idx_created_time (created_time),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日志表'; 