package com.robo.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import java.util.HashMap;
import java.util.Map;

import java.math.BigDecimal;

@Service
public class TelegramService {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.chat.id}")
    private String chatId;

    private final RestTemplate restTemplate;

    public TelegramService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void enviarMensagem(String texto) {
        try {
            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

            Map<String, String> body = new HashMap<>();
            body.put("chat_id",    chatId);
            body.put("text",       texto);
            body.put("parse_mode", "HTML");

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body);
            restTemplate.postForObject(url, request, String.class);

        } catch (Exception e) {
            System.err.println("[TelegramService] Falha ao enviar mensagem: " + e.getMessage());
        }
    }

    public void notificarOportunidade(String modelo, BigDecimal preco, String link,
                                      int score, String classificacao) {
        String emoji = emojiScore(score);

        String mensagem = String.format(
                "🔥 <b>Nova Oportunidade!</b>\n\n"  +
                        "📱 <b>Modelo:</b> %s\n"             +
                        "💰 <b>Preço:</b> R$ %s\n"           +
                        "%s <b>Score:</b> %d/100 — %s\n"     +
                        "🔗 <b>Link:</b> %s",
                modelo,
                preco.toPlainString(),
                emoji, score, classificacao,
                link
        );

        enviarMensagem(mensagem);
    }

    private String emojiScore(int score) {
        if (score >= 75) return "🟢";
        if (score >= 50) return "🟡";
        if (score >= 25) return "🟠";
        return "🔴";
    }
}