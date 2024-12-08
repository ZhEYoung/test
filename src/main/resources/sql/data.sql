-- 插入管理员用户
INSERT INTO user (username, password, role, status, sex, phone, email, created_time) 
VALUES ('admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 0, 1, 1, '13800000000', 'admin@example.com', CURRENT_TIMESTAMP);

-- 插入管理员信息
INSERT INTO admin (user_id, name, other) 
VALUES (LAST_INSERT_ID(), '系统管理员', '超级管理员');

-- 插入示例学院
INSERT INTO college (college_name, description) VALUES 
('计算机学院', '计算机科学与技术学院'),
('数学学院', '数学与统计学院'),
('外语学院', '外国语言文学学院');

-- 插入示例学科
INSERT INTO subject (subject_name, description, college_id) VALUES 
('Java程序设计', 'Java语言程序设计基础课程', 1),
('数据结构', '计算机基础课程', 1),
('高等数学', '大学数学基础课程', 2),
('大学英语', '大学英语基础课程', 3);

-- 插入示例教师用户
INSERT INTO user (username, password, role, status, sex, phone, email) VALUES 
('teacher1', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 1, 1, 1, '13800000001', 'teacher1@example.com'),
('teacher2', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 1, 1, 0, '13800000002', 'teacher2@example.com');

-- 插入示例教师信息
INSERT INTO teacher (user_id, name, permission, college_id) VALUES 
(LAST_INSERT_ID()-1, '张老师', 0, 1),
(LAST_INSERT_ID(), '李老师', 1, 1);

-- 插入示例学生用户
INSERT INTO user (username, password, role, status, sex, phone, email) VALUES 
('student1', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 2, 1, 1, '13800000003', 'student1@example.com'),
('student2', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 2, 1, 0, '13800000004', 'student2@example.com');

-- 插入示例学生信息
INSERT INTO student (user_id, name, grade, college_id) VALUES 
(LAST_INSERT_ID()-1, '王同学', '2024', 1),
(LAST_INSERT_ID(), '赵同学', '2024', 1);

-- 插入示例班级
INSERT INTO class (teacher_id, class_name, subject_id) VALUES 
(1, 'Java程序设计-1班', 1),
(2, 'Java程序设计-2班', 1);

-- 插入学生-班级关联
INSERT INTO student_class (student_id, class_id) VALUES 
(1, 1),
(2, 2);

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