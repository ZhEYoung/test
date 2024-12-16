-- 插入管理员用户（密码：8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92，原始密码：123456，使用SHA-256加密）
INSERT INTO user (username, password, role, status, sex, phone, email, created_time)
VALUES ('admin', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 0, 1, 1, '13800000000', 'admin@example.com', CURRENT_TIMESTAMP);

-- 插入管理员信息
INSERT INTO admin (user_id, name, other)
SELECT LAST_INSERT_ID(), '系统管理员', '超级管理员';

-- 插入示例学院
INSERT INTO college (college_name, description, created_time) VALUES
                                                                  ('计算机学院', '计算机科学与技术学院', CURRENT_TIMESTAMP),
                                                                  ('数学学院', '数学与统计学院', CURRENT_TIMESTAMP),
                                                                  ('外语学院', '外国语言文学学院', CURRENT_TIMESTAMP);

-- 插入示例学科
INSERT INTO subject (subject_name, description, college_id) VALUES
                                                                ('Java程序设计', 'Java语言程序设计基础课程', 1),
                                                                ('数据结构', '计算机基础课程', 1),
                                                                ('高等数学', '大学数学基础课程', 2),
                                                                ('大学英语', '大学英语基础课程', 3);

-- 插入示例教师用户和教师信息
INSERT INTO user (username, password, role, status, sex, phone, email, created_time)
VALUES ('teacher1', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 1, 1, 1, '13800000001', 'teacher1@example.com', CURRENT_TIMESTAMP);
INSERT INTO teacher (user_id, name, permission, college_id)
SELECT LAST_INSERT_ID(), '张老师', 0, 1;

INSERT INTO user (username, password, role, status, sex, phone, email, created_time)
VALUES ('teacher2', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 1, 1, 0, '13800000002', 'teacher2@example.com', CURRENT_TIMESTAMP);
INSERT INTO teacher (user_id, name, permission, college_id)
SELECT LAST_INSERT_ID(), '李老师', 1, 1;

-- 插入示例学生用户和学生信息
INSERT INTO user (username, password, role, status, sex, phone, email, created_time)
VALUES ('student1', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 2, 1, 1, '13800000003', 'student1@example.com', CURRENT_TIMESTAMP);
INSERT INTO student (user_id, name, grade, college_id)
SELECT LAST_INSERT_ID(), '王同学', '2024', 1;

INSERT INTO user (username, password, role, status, sex, phone, email, created_time)
VALUES ('student2', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 2, 1, 0, '13800000004', 'student2@example.com', CURRENT_TIMESTAMP);
INSERT INTO student (user_id, name, grade, college_id)
SELECT LAST_INSERT_ID(), '赵同学', '2024', 1;

-- 插入示例班级（使用SELECT获取正确的teacher_id）
INSERT INTO class (teacher_id, class_name, subject_id)
SELECT t.teacher_id, 'Java程序设计-1班', 1
FROM teacher t
         JOIN user u ON t.user_id = u.user_id
WHERE u.username = 'teacher1';

INSERT INTO class (teacher_id, class_name, subject_id)
SELECT t.teacher_id, 'Java程序设计-2班', 1
FROM teacher t
         JOIN user u ON t.user_id = u.user_id
WHERE u.username = 'teacher2';

-- 插入学生-班级关联（使用SELECT获取正确的student_id和class_id）
INSERT INTO student_class (student_id, class_id, status, join_time)
SELECT s.student_id, c.class_id, 1, CURRENT_TIMESTAMP
FROM student s
         JOIN user u ON s.user_id = u.user_id
         JOIN class c ON c.class_name = 'Java程序设计-1班'
WHERE u.username = 'student1';

INSERT INTO student_class (student_id, class_id, status, join_time)
SELECT s.student_id, c.class_id, 1, CURRENT_TIMESTAMP
FROM student s
         JOIN user u ON s.user_id = u.user_id
         JOIN class c ON c.class_name = 'Java程序设计-2班'
WHERE u.username = 'student2';

-- 插入示例题库
INSERT INTO question_bank (qb_name, subject_id) VALUES
                                                    ('Java基础题库', 1),
                                                    ('Java进阶题库', 1);

-- 插入示例题目
INSERT INTO question (qb_id, content, answer, type, difficulty) VALUES
                                                                    (1, 'Java的基本数据类型有哪些？', '基本数据类型包括byte、short、int、long、float、double、boolean和char', 4, 1.00),
                                                                    (1, 'Java是否支持多重继承？', '不支持，Java只支持单继承多实现', 2, 2.00);

-- 插入示例题目选项
INSERT INTO question_option (question_id, content, is_correct) VALUES
                                                                   (2, '是', 0),
                                                                   (2, '否', 1);