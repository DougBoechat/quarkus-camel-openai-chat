package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.client.ClaudeClient;
import org.acme.dto.ClaudeDTO;
import org.acme.entity.Message;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ClaudeService {

    private static final Logger LOG = Logger.getLogger(ClaudeService.class);

    @Inject
    @RestClient
    ClaudeClient claudeClient;

    @ConfigProperty(name = "claude.model", defaultValue = "claude-sonnet-4-20250514")
    String model;

    @ConfigProperty(name = "claude.max.tokens", defaultValue = "1024")
    Integer maxTokens;

    @ConfigProperty(name = "claude.temperature", defaultValue = "0.7")
    Double temperature;

    /**
     * Gera resposta usando Claude com contexto de terapias integrativas
     */
    public String generateResponse(String userMessage, String therapyType, String sessionId) {
        try {
            // Validações básicas
            if (userMessage == null || userMessage.trim().isEmpty()) {
                throw new IllegalArgumentException("Mensagem do usuário não pode estar vazia");
            }

            // Busca histórico da conversa
            List<Message> history = Message.findBySessionId(sessionId);

            // Constrói o system prompt
            String systemPrompt = buildSystemPrompt(therapyType);

            // Constrói as mensagens com histórico
            List<ClaudeDTO.Message> messages = buildMessages(userMessage, history);

            // Cria requisição
            ClaudeDTO.MessageRequest request = new ClaudeDTO.MessageRequest();
            request.model = model;
            request.maxTokens = maxTokens;
            request.temperature = temperature;
            request.system = systemPrompt;
            request.messages = messages;
            request.topP = 1.0;
            // ❌ REMOVIDO - Claude API não aceita top_k
            // request.topK = 5;

            // Log para debug
            LOG.infof("📤 Chamando Claude API - modelo: %s, tokens: %d, mensagens: %d",
                    model, maxTokens, messages.size());

            // Chama API Claude
            ClaudeDTO.MessageResponse response = claudeClient.createMessage(request);

            // Extrai resposta
            if (response.content != null && !response.content.isEmpty()) {
                String claudeResponse = extractTextFromContent(response.content);
                LOG.infof("✅ Resposta recebida - Input tokens: %d, Output tokens: %d",
                        response.usage.inputTokens,
                        response.usage.outputTokens);
                return claudeResponse;
            }

            return "Desculpe, não consegui processar sua mensagem no momento.";

        } catch (Exception e) {
            LOG.errorf(e, "❌ Erro ao chamar Claude API: %s", e.getMessage());
            return "Desculpe, ocorreu um erro ao processar sua mensagem. Por favor, tente novamente em alguns instantes.";
        }
    }

    /**
     * Extrai texto dos blocos de conteúdo
     */
    private String extractTextFromContent(List<ClaudeDTO.ContentBlock> content) {
        StringBuilder text = new StringBuilder();
        for (ClaudeDTO.ContentBlock block : content) {
            if ("text".equals(block.type) && block.text != null) {
                text.append(block.text);
            }
        }
        return text.toString();
    }

    /**
     * Constrói lista de mensagens incluindo histórico
     */
    private List<ClaudeDTO.Message> buildMessages(String userMessage, List<Message> history) {
        List<ClaudeDTO.Message> messages = new ArrayList<>();

        // Adiciona histórico (últimas 5 interações)
        int historyLimit = Math.min(5, history.size());
        for (int i = history.size() - historyLimit; i < history.size(); i++) {
            Message msg = history.get(i);

            // Adiciona mensagem do usuário
            if (msg.userMessage != null && !msg.userMessage.trim().isEmpty()) {
                messages.add(new ClaudeDTO.Message("user", msg.userMessage));
            }

            // Adiciona resposta do assistant
            if (msg.botResponse != null && !msg.botResponse.trim().isEmpty()) {
                messages.add(new ClaudeDTO.Message("assistant", msg.botResponse));
            }
        }

        // Adiciona mensagem atual do usuário
        messages.add(new ClaudeDTO.Message("user", userMessage));

        return messages;
    }

    // ... (resto dos métodos buildSystemPrompt, analyzeSentiment, etc permanecem iguais)

    private String buildSystemPrompt(String therapyType) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Você é um assistente virtual especializado em Terapias Integrativas e Medicina Complementar. ");
        prompt.append("Seu objetivo é ajudar, orientar e educar sobre práticas terapêuticas de forma acolhedora, empática e baseada em evidências.\n\n");

        prompt.append("DIRETRIZES IMPORTANTES:\n");
        prompt.append("• Seja empático, acolhedor e respeitoso com as experiências e crenças do usuário\n");
        prompt.append("• Forneça informações baseadas em evidências científicas quando disponível\n");
        prompt.append("• NUNCA substitua diagnóstico, prescrição ou tratamento médico profissional\n");
        prompt.append("• Sempre incentive consulta com profissionais qualificados e regulamentados\n");
        prompt.append("• Mantenha respostas concisas e objetivas (máximo 3-4 parágrafos)\n");
        prompt.append("• Use linguagem clara, acessível e livre de jargões desnecessários\n");
        prompt.append("• Respeite todas as tradições terapêuticas sem preconceitos\n");
        prompt.append("• Em caso de sintomas graves, oriente busca imediata por atendimento médico\n");
        prompt.append("• Seja honesto sobre limitações e incertezas do conhecimento\n\n");
        prompt.append("• Evite responder à perguntas que saiam do contexto fornecido, a não ser que seja possível usar a resposta para direcionar novamente ao contexto\n\n");

        // ... (resto do switch case permanece igual)

        switch (therapyType != null ? therapyType.toLowerCase() : "geral") {
            case "ayurveda":
                prompt.append("FOCO ATUAL: Ayurveda\n");
                prompt.append("Sistema medicinal milenar da Índia que busca equilíbrio através dos doshas (Vata, Pitta, Kapha), ");
                prompt.append("alimentação adequada ao biotipo, rotinas diárias (dinacharya), estações (ritucharya) e práticas de autocuidado.\n");
                prompt.append("Aborde: constituição individual, desequilíbrios, alimentação, ervas ayurvédicas, yoga e meditação.");
                break;
            // ... (demais cases)
            default:
                prompt.append("FOCO ATUAL: Terapias Integrativas em Geral\n");
                prompt.append("Abordagem holística da saúde que integra práticas complementares à medicina convencional.");
        }

        return prompt.toString();
    }

    public String analyzeSentiment(String text) {
        try {
            ClaudeDTO.MessageRequest request = new ClaudeDTO.MessageRequest();
            request.model = model;
            request.maxTokens = 50;
            request.temperature = 0.3;
            request.system = "Analise o sentimento do texto e responda apenas com uma palavra: positivo, negativo ou neutro";
            request.messages = List.of(new ClaudeDTO.Message("user", text));

            ClaudeDTO.MessageResponse response = claudeClient.createMessage(request);

            if (response.content != null && !response.content.isEmpty()) {
                String sentiment = extractTextFromContent(response.content).trim().toLowerCase();
                if (sentiment.contains("positivo")) return "positivo";
                if (sentiment.contains("negativo")) return "negativo";
                return "neutro";
            }
        } catch (Exception e) {
            LOG.errorf(e, "Erro ao analisar sentimento: %s", e.getMessage());
        }
        return "neutro";
    }

    public String[] generateSuggestions(String context, String therapyType) {
        try {
            String userPrompt = String.format(
                    "Baseado neste contexto de conversa sobre %s: '%s', " +
                            "sugira exatamente 3 perguntas curtas e diretas (máximo 10 palavras cada) " +
                            "que o usuário pode fazer para aprofundar o tema. " +
                            "Retorne apenas as 3 perguntas, uma por linha, sem numeração, prefixos ou explicações.",
                    therapyType, context
            );

            ClaudeDTO.MessageRequest request = new ClaudeDTO.MessageRequest();
            request.model = model;
            request.maxTokens = 150;
            request.temperature = 0.8;
            request.system = "Você é um especialista em gerar perguntas relevantes sobre terapias integrativas.";
            request.messages = List.of(new ClaudeDTO.Message("user", userPrompt));

            ClaudeDTO.MessageResponse response = claudeClient.createMessage(request);

            if (response.content != null && !response.content.isEmpty()) {
                String suggestions = extractTextFromContent(response.content);
                String[] lines = suggestions.split("\n");
                List<String> validSuggestions = new ArrayList<>();

                for (String line : lines) {
                    String cleaned = line.trim()
                            .replaceAll("^[0-9]+[.)\\-]\\s*", "")
                            .replaceAll("^[•\\-*]\\s*", "");
                    if (!cleaned.isEmpty() && validSuggestions.size() < 3) {
                        validSuggestions.add(cleaned);
                    }
                }

                return validSuggestions.toArray(new String[0]);
            }
        } catch (Exception e) {
            LOG.errorf(e, "Erro ao gerar sugestões: %s", e.getMessage());
        }

        return getDefaultSuggestions(therapyType);
    }

    private String[] getDefaultSuggestions(String therapyType) {
        return switch (therapyType != null ? therapyType.toLowerCase() : "geral") {
            case "ayurveda" -> new String[]{
                    "Como descobrir meu dosha?",
                    "Alimentação ayurvédica para iniciantes",
                    "Rotinas diárias recomendadas"
            };
            case "acupuntura" -> new String[]{
                    "Quais problemas a acupuntura trata?",
                    "Como funciona uma sessão?",
                    "Acupuntura para ansiedade funciona?"
            };
            case "aromaterapia" -> new String[]{
                    "Melhores óleos para relaxar",
                    "Como usar óleos essenciais com segurança?",
                    "Aromaterapia para insônia"
            };
            default -> new String[]{
                    "Quais terapias você recomenda?",
                    "Como começar na medicina integrativa?",
                    "Benefícios científicos comprovados"
            };
        };
    }
}