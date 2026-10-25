package com.simul_tech.netgenius.services;

import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Service
public class EmailTemplateService {
    public String buildPasswordResetEmail(String resetLink, String userName) {
        return """
            <!DOCTYPE html>
            <html lang="ru">
            <head>
                <meta charset="UTF-8">
                <title>Сброс пароля</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        margin: 0;
                        padding: 0;
                        background-color: #f6f9fc
                    }
                    .container {
                        max-width: 600px;
                        margin: 20px auto;
                        background: #ffffff;
                        border-radius: 8px;
                        overflow: hidden;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    .header {
                        background: #4f46e5;
                        padding: 30px;
                        text-align: center;
                        color: white;
                    }
                    .content {
                        padding: 30px;
                    }
                    .button {
                        display: inline-block;
                        background: #4f46e5;
                        color: white;
                        text-decoration: none;
                        padding: 12px 24px;
                        border-radius: 6px;
                        font-weight: bold;
                        margin: 20px 0;
                    }
                    .footer {
                        background: #f8f9fa;
                        padding: 20px;
                        text-align: center;
                        color: #666;
                        border-top: 1px solid #e9ecef;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Сброс пароля</h1>
                    </div>
                    <div class="content">
                        <p><strong>Здравствуйте%s!</strong></p>
                        <p>Вы запросили сброс пароля для вашего аккаунта.</p>
                        <p>Для установки нового пароля нажмите на кнопку ниже:</p>
                        <div style="text-align: center;">
                            <a href="%s" class="button">Сбросить пароль</a>
                        </div>
                        <p>Или скопируйте эту ссылку в браузер:</p>
                        <p style="background: #f8f9fa; padding: 10px; border-radius: 4px; word-break: break-all;">
                            %s
                        </p>
                        <div style="background: #fff3cd; border: 1px solid #ffeaa7; border-radius: 6px; padding: 15px; margin: 20px 0;">
                            <strong>Внимание!</strong><br>
                            Ссылка действительна в течение 24 часов.
                            Если вы не запрашивали сброс пароля, проигнорируйте это письмо.
                        </div>
                    </div>
                    <div class="footer">
                        <p>С уважением,<br><strong>Команда СИМУЛТЕХ</strong></p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                greeting(userName),
                HtmlUtils.htmlEscape(resetLink),
                HtmlUtils.htmlEscape(resetLink)
        );
    }

    public String buildVerificationEmail(String verifyLink, String userName, String websiteUrl) {
        return """
        <!DOCTYPE html>
        <html lang="ru">
        <head>
            <meta charset="UTF-8">
            <title>Подтверждение регистрации — СИМУЛТЕХ</title>
            <style>
                body {
                    font-family: Arial, sans-serif;
                    line-height: 1.6;
                    color: #333;
                    margin: 0;
                    padding: 0;
                    background-color: #f6f9fc
                }
                .container {
                    max-width: 600px;
                    margin: 20px auto;
                    background: #ffffff;
                    border-radius: 8px;
                    overflow: hidden;
                    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                }
                .header {
                    background: #4f46e5;
                    padding: 30px;
                    text-align: center;
                    color: white;
                }
                .content {
                    padding: 30px;
                }
                .button {
                    display: inline-block;
                    background: #4f46e5;
                    color: white;
                    text-decoration: none;
                    padding: 12px 24px;
                    border-radius: 6px;
                    font-weight: bold;
                    margin: 20px 0;
                }
                .footer {
                    background: #f8f9fa;
                    padding: 20px;
                    text-align: center;
                    color: #666;
                    border-top: 1px solid #e9ecef;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>СИМУЛТЕХ</h1>
                </div>
                <div class="content">
                    <p><strong>Здравствуйте%s!</strong></p>
                    <p>Благодарим вас за интерес к симуляторам СИМУЛТЕХ!</p>
                    <p>После завершения регистрации и подтверждения статуса студента
                        вам будет доступна онлайн демо-версия.</p>
                    <p>Если в вашем вузе ещё не проводят практику на базе нашего симулятора
                        компьютерных сетей, передайте преподавателям информацию о такой возможности.
                        После обучения студенты могут стать сертифицированными пользователями.</p>
                    <p>Если вы хотите зарегистрироваться как преподаватель, перейдите в личный профиль
                        и нажмите кнопку «Получить статус преподавателя».</p>
                    <p>Все подробности — на <a href="%s">сайте СИМУЛТЕХ</a>.</p>
                    <p>Для подтверждения регистрации перейдите по ссылке:</p>
                    <div style="text-align: center;">
                        <a href="%s" class="button">Подтвердить регистрацию</a>
                    </div>
                    <p>Или скопируйте эту ссылку в браузер:</p>
                    <p style="background: #f8f9fa; padding: 10px; border-radius: 4px; word-break: break-all;">
                        %s
                    </p>
                    <div style="background: #fff3cd; border: 1px solid #ffeaa7; border-radius: 6px; padding: 15px; margin: 20px 0;">
                        <strong>Внимание!</strong><br>
                        Ссылка действительна в течение 24 часов.
                        Если вы не регистрировались, проигнорируйте это письмо.
                    </div>
                </div>
                <div class="footer">
                    <p>С уважением,<br><strong>Команда СИМУЛТЕХ</strong></p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(
                greeting(userName),
                HtmlUtils.htmlEscape(websiteUrl),
                HtmlUtils.htmlEscape(verifyLink),
                HtmlUtils.htmlEscape(verifyLink)
        );
    }

    private String greeting(String userName) {
        return userName == null || userName.isBlank() ? "" : ", " + HtmlUtils.htmlEscape(userName);
    }
}
