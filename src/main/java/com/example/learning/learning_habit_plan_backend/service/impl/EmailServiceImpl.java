package com.example.learning.learning_habit_plan_backend.service.impl;

import com.example.learning.learning_habit_plan_backend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendPasswordResetEmail(String to, String resetToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("密码重置 - 学习习惯养成计划");
            
            // 构建重置链接
            String resetUrl = "http://localhost:5173/reset-password?token=" + resetToken;
            
            String htmlContent = buildPasswordResetEmailContent(resetUrl);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            System.out.println("密码重置邮件已发送到: " + to);
            
        } catch (MessagingException e) {
            System.err.println("邮件发送失败详细信息: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("发送邮件失败: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("邮件发送异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("发送邮件异常: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendVerificationCodeEmail(String to, String verificationCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("邮箱验证码 - 学习习惯养成计划");
            
            String htmlContent = buildVerificationCodeEmailContent(verificationCode);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            System.out.println("验证码邮件已发送到: " + to);
            
        } catch (MessagingException e) {
            System.err.println("验证码邮件发送失败详细信息: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("发送邮件失败: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("验证码邮件发送异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("发送邮件异常: " + e.getMessage(), e);
        }
    }

    /**
     * 构建密码重置邮件内容
     */
    private String buildPasswordResetEmailContent(String resetUrl) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <title>密码重置</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }\n" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }\n" +
                "        .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }\n" +
                "        .content { padding: 20px; background-color: #f9f9f9; }\n" +
                "        .button { display: inline-block; padding: 12px 24px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 4px; margin: 20px 0; }\n" +
                "        .footer { padding: 20px; text-align: center; color: #666; font-size: 12px; }\n" +
                "        .warning { color: #ff6b6b; font-weight: bold; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class='container'>\n" +
                "        <div class='header'>\n" +
                "            <h1>学习习惯养成计划</h1>\n" +
                "            <h2>密码重置</h2>\n" +
                "        </div>\n" +
                "        <div class='content'>\n" +
                "            <p>您好！</p>\n" +
                "            <p>我们收到了您的密码重置请求。请点击下面的按钮来重置您的密码：</p>\n" +
                "            <p style='text-align: center;'>\n" +
                "                <a href='" + resetUrl + "' class='button'>重置密码</a>\n" +
                "            </p>\n" +
                "            <p>如果按钮无法点击，请复制以下链接到浏览器地址栏：</p>\n" +
                "            <p style='word-break: break-all; background-color: #eee; padding: 10px; border-radius: 4px;'>" + resetUrl + "</p>\n" +
                "            <p class='warning'>⚠️ 重要提醒：</p>\n" +
                "            <ul>\n" +
                "                <li>此链接将在30分钟后失效</li>\n" +
                "                <li>如果您没有请求重置密码，请忽略此邮件</li>\n" +
                "                <li>为了您的账户安全，请不要将此链接分享给他人</li>\n" +
                "            </ul>\n" +
                "        </div>\n" +
                "        <div class='footer'>\n" +
                "            <p>此邮件由系统自动发送，请勿回复。</p>\n" +
                "            <p>© 2024 学习习惯养成计划. 保留所有权利。</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * 构建验证码邮件内容
     */
    private String buildVerificationCodeEmailContent(String verificationCode) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <title>邮箱验证码</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }\n" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }\n" +
                "        .header { background-color: #2196F3; color: white; padding: 20px; text-align: center; }\n" +
                "        .content { padding: 20px; background-color: #f9f9f9; }\n" +
                "        .code { font-size: 32px; font-weight: bold; color: #2196F3; text-align: center; padding: 20px; background-color: #e3f2fd; border-radius: 8px; margin: 20px 0; letter-spacing: 8px; }\n" +
                "        .footer { padding: 20px; text-align: center; color: #666; font-size: 12px; }\n" +
                "        .warning { color: #ff6b6b; font-weight: bold; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class='container'>\n" +
                "        <div class='header'>\n" +
                "            <h1>学习习惯养成计划</h1>\n" +
                "            <h2>邮箱验证码</h2>\n" +
                "        </div>\n" +
                "        <div class='content'>\n" +
                "            <p>您好！</p>\n" +
                "            <p>您的邮箱验证码是：</p>\n" +
                "            <div class='code'>" + verificationCode + "</div>\n" +
                "            <p class='warning'>⚠️ 重要提醒：</p>\n" +
                "            <ul>\n" +
                "                <li>验证码有效期为10分钟</li>\n" +
                "                <li>请勿将验证码告诉他人</li>\n" +
                "                <li>如果您没有进行此操作，请忽略此邮件</li>\n" +
                "            </ul>\n" +
                "        </div>\n" +
                "        <div class='footer'>\n" +
                "            <p>此邮件由系统自动发送，请勿回复。</p>\n" +
                "            <p>© 2024 学习习惯养成计划. 保留所有权利。</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}