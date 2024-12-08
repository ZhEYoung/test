package com.exam.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import com.exam.util.TestDataGenerator;

@SpringBootTest
@Transactional
@Sql(scripts = {"/sql/schema.sql", "/sql/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class BaseMapperTest {
    
    @BeforeEach
    void setUp() {
        // 在每个测试方法执行前运行
        // 可以在这里添加通用的设置代码
    }

    // 通用的辅助方法
    protected void clearDatabase() {
        // 清理数据库的方法
    }

    protected void assertDatabaseState() {
        // 验证数据库状态的方法
    }
} 