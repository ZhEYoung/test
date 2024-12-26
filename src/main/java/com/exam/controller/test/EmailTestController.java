package com.exam.controller.test;

import com.exam.service.EmailService;
import com.exam.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/test")
@Api(tags = "测试接口")
public class EmailTestController {

    @Autowired
    private EmailService emailService;

    @ApiOperation("测试发送简单邮件")
    @GetMapping("/email/simple")
    public Result testSimpleEmail(@RequestParam String to) {
        try {
            String subject = "考试系统 - 测试邮件";
            String content = "这是一封测试邮件，如果您收到这封邮件，说明邮件服务配置正确。\n\n祝您使用愉快！";
            
            boolean result = emailService.sendSimpleMail(to, subject, content);
            if (result) {
                return Result.success("邮件发送成功");
            } else {
                return Result.error("邮件发送失败");
            }
        } catch (Exception e) {
            log.error("测试邮件发送失败", e);
            return Result.error("邮件发送失败：" + e.getMessage());
        }
    }

    @ApiOperation("测试发送HTML邮件")
    @GetMapping("/email/html")
    public Result testHtmlEmail(@RequestParam String to) {
        try {
            String subject = "考试系统 - HTML测试邮件";
            String content = "<h1>HTML测试邮件</h1>" +
                           "<p>这是一封HTML格式的测试邮件，如果您能看到格式化的内容，说明HTML邮件服务配置正确。</p>" +
                           "<p style='color: red;'>这行文字应该是红色的。</p>" +
                           "<p><b>祝您使用愉快！</b></p>";
            
            boolean result = emailService.sendHtmlMail(to, subject, content);
            if (result) {
                return Result.success("HTML邮件发送成功");
            } else {
                return Result.error("HTML邮件发送失败");
            }
        } catch (Exception e) {
            log.error("测试HTML邮件发送失败", e);
            return Result.error("HTML邮件发送失败：" + e.getMessage());
        }
    }
} 