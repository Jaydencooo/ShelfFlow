package com.shelfflow.services.user.auth.service;

import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.exception.ApplicationException;
import com.shelfflow.services.user.config.UserAuthProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class MailVerificationCodeDeliveryService implements VerificationCodeDeliveryService {

    private static final Logger log = LoggerFactory.getLogger(MailVerificationCodeDeliveryService.class);
    private static final int SECONDS_PER_MINUTE = 60;

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final UserAuthProperties userAuthProperties;

    public MailVerificationCodeDeliveryService(ObjectProvider<JavaMailSender> mailSenderProvider,
                                               UserAuthProperties userAuthProperties) {
        this.mailSenderProvider = mailSenderProvider;
        this.userAuthProperties = userAuthProperties;
    }

    @Override
    public void deliver(String target, String purpose, String code, int expiresInSeconds) {
        if (!isEmail(target)) {
            log.info("Skip mail verification delivery for non-email target, purpose={}", purpose);
            return;
        }
        if (!userAuthProperties.getMail().isEnabled()) {
            log.info("Mail verification delivery disabled, purpose={}", purpose);
            return;
        }

        sendMail(target, purpose, code, expiresInSeconds);
    }

    private void sendMail(String target, String purpose, String code, int expiresInSeconds) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            throw new ApplicationException(ErrorCode.DEPENDENCY_ERROR, "邮箱服务未配置，请检查 SMTP 配置");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setTo(target);
            helper.setFrom(userAuthProperties.getMail().getFrom(), userAuthProperties.getMail().getFromName());
            helper.setSubject(userAuthProperties.getMail().getSubject());
            helper.setText(buildMailContent(purpose, code, expiresInSeconds), false);
            mailSender.send(message);
        } catch (MessagingException | MailException | UnsupportedEncodingException mailError) {
            log.warn("Failed to send verification code email, target={}, purpose={}", target, purpose, mailError);
            throw new ApplicationException(ErrorCode.DEPENDENCY_ERROR, "邮箱验证码发送失败，请稍后重试");
        }
    }

    private String buildMailContent(String purpose, String code, int expiresInSeconds) {
        int expiresInMinutes = Math.max(1, expiresInSeconds / SECONDS_PER_MINUTE);
        return """
                您正在进行 ShelfFlow %s 操作。

                验证码：%s

                验证码 %d 分钟内有效。请勿将验证码泄露给他人。
                """.formatted(resolvePurposeLabel(purpose), code, expiresInMinutes);
    }

    private String resolvePurposeLabel(String purpose) {
        return switch (purpose) {
            case "register" -> "注册";
            case "reset_password" -> "找回密码";
            case "change_email" -> "修改邮箱";
            case "change_phone" -> "修改手机号";
            default -> "身份验证";
        };
    }

    private boolean isEmail(String target) {
        return target != null && target.toLowerCase(Locale.ROOT).contains("@");
    }
}
