package com.exam.util;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestDataGenerator {
    private static final Random random = new Random();

    // 生成随机用户名
    public static String generateUsername() {
        return "test_user_" + random.nextInt(10000);
    }

    // 生成随机密码
    public static String generatePassword() {
        return "password_" + random.nextInt(10000);
    }

    // 生成随机邮箱
    public static String generateEmail() {
        return "test_" + random.nextInt(10000) + "@example.com";
    }

    // 生成随机手机号
    public static String generatePhone() {
        return "1" + String.format("%010d", random.nextInt(1000000000));
    }

    // 生成随机姓名
    public static String generateName() {
        String[] firstNames = {"张", "李", "王", "赵", "刘", "陈", "杨", "黄", "周", "吴"};
        String[] lastNames = {"伟", "芳", "娜", "秀英", "敏", "静", "丽", "强", "磊", "洋"};
        return firstNames[random.nextInt(firstNames.length)] + lastNames[random.nextInt(lastNames.length)];
    }

    // 生成随机年级
    public static String generateGrade() {
        return "202" + random.nextInt(5);
    }

    // 生成随机分数
    public static double generateScore() {
        return 40 + random.nextDouble() * 60;
    }

    // 生成随机时间
    public static LocalDateTime generateDateTime() {
        return LocalDateTime.now().minusDays(random.nextInt(365));
    }

    // 生成随机题目内容
    public static String generateQuestionContent() {
        String[] templates = {
            "下列关于%s的说法中，正确的是？",
            "请简要说明%s的主要特点。",
            "分析%s的优缺点。",
            "%s的基本原理是什么？",
            "简述%s的应用场景。"
        };
        String[] subjects = {
            "Java多线程", "数据库索引", "设计模式", "网络协议", "算法复杂度",
            "面向对象", "函数式编程", "微服务架构", "缓存机制", "消息队列"
        };
        return String.format(
            templates[random.nextInt(templates.length)],
            subjects[random.nextInt(subjects.length)]
        );
    }

    // 生成随机选项内容
    public static List<String> generateOptions() {
        List<String> options = new ArrayList<>();
        String[] templates = {"A. %s", "B. %s", "C. %s", "D. %s"};
        String[] contents = {
            "这是正确的说法",
            "这种说法不完全正确",
            "这种说法是错误的",
            "这种说法需要具体分析"
        };
        for (int i = 0; i < templates.length; i++) {
            options.add(String.format(templates[i], contents[i]));
        }
        return options;
    }

    // 生成随机答案
    public static String generateAnswer() {
        return "正确答案是A，因为...";
    }

    // 生成随机难度
    public static double generateDifficulty() {
        return 1.0 + random.nextDouble() * 4.0;
    }

    // 生成随机考试名称
    public static String generateExamName() {
        String[] subjects = {"Java", "Python", "数据库", "算法", "网络"};
        String[] types = {"期中考试", "期末考试", "元测试", "模拟考试"};
        return subjects[random.nextInt(subjects.length)] + 
               types[random.nextInt(types.length)] + 
               random.nextInt(100);
    }

    // 生成随机考试时长（分钟）
    public static int generateExamDuration() {
        return (random.nextInt(4) + 1) * 30;  // 30-120分钟
    }
} 