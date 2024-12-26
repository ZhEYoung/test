package com.exam.service;

/**
 * 邮件服务接口
 */
public interface EmailService {
    
    /**
     * 发送简单文本邮件
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     * @return 发送结果
     */
    boolean sendSimpleMail(String to, String subject, String content);
    
    /**
     * 发送HTML格式邮件
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content HTML内容
     * @return 发送结果
     */
    boolean sendHtmlMail(String to, String subject, String content);
    
    /**
     * 发送带附件的邮件
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     * @param filePath 附件路径
     * @return 发送结果
     */
    boolean sendAttachmentsMail(String to, String subject, String content, String filePath);
} 