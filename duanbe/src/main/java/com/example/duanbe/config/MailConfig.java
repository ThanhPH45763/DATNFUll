package com.example.duanbe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {
    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com"); // Thay bằng SMTP server của bạn
        mailSender.setPort(587);

        mailSender.setUsername("thanhnvph45763@fpt.edu.vn"); // Email gửi
        mailSender.setPassword("swok ecbf dfpf ixgc"); // Mật khẩu email

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com"); // Thêm dòng này

        return mailSender;
    }
}
