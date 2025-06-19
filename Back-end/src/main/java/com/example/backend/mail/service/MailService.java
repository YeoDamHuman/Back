package com.example.backend.mail.service;

import com.example.backend.mail.dto.request.MailRequest;
import com.example.backend.mail.dto.response.MailResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;
    private final Logger log = LoggerFactory.getLogger(MailService.class);
    // 1️⃣ 이메일 발송
    public MailResponse.mailSendResponse sendVerificationEmail(MailRequest.mailSendRequest email) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(token, email.getEmail(), 30, TimeUnit.MINUTES);

        String message = "인증 코드는 다음과 같습니다:\n" + token + "\n이 코드를 인증 화면에 입력해주세요.";

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(email.getEmail());
        mail.setSubject("인증 코드");
        mail.setText(message);
        mailSender.send(mail);

        return MailResponse.mailSendResponse.builder()
                .message("메일을 전송했습니다.")
                .build();
    }


    // 2️⃣ 이메일 인증 (Redis에 토큰이 있으면 성공)
    public MailResponse.mailVerifyResponse verifyToken(MailRequest.mailVerifyRequest request) {
        String token = request.getToken();
        String email = redisTemplate.opsForValue().get(token);

        if (email != null) {
            redisTemplate.delete(token);
            return MailResponse.mailVerifyResponse.builder()
                    .message("이메일 인증이 완료되었습니다.")
                    .build();
        }
        return MailResponse.mailVerifyResponse.builder()
                .message("유효하지 않은 토큰입니다.")
                .build();
    }
}