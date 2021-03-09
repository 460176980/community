package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@SpringBootTest
public class tempTest implements ApplicationContextAware {
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Test
    public void test(){
        String content="test success";
        String to="460176980@qq.com";
        String subject="success";
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            System.out.println("发送邮件失败:" + e.getMessage());
        }finally {
            System.out.println("结束发送状态");
            return;
        }
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

    }
}
