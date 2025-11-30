package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.dto.ChatDTO;
import org.acme.entity.Message;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@ApplicationScoped
public class TherapyService {

    private static final Logger LOG = Logger.getLogger(TherapyService.class);

    @Inject
    ClaudeService claudeService;  // Mudado de OpenAIService para ClaudeService

    private static final Map<String, String[]> THERAPY_KEYWORDS = new HashMap<>();

    static {
        THERAPY_KEYWORDS.put("ayurveda", new String[]{"dosha", "vata", "pitta", "kapha", "ayurvédica", "equilíbrio"});
        THERAPY_KEYWORDS.put("acupuntura", new String[]{"agulha", "meridiano", "qi", "energia", "ponto"});
        THERAPY_KEYWORDS.put("aromaterapia", new String[]{"óleo essencial", "aroma", "lavanda", "eucalipto", "essência"});
        THERAPY_KEYWORDS.put("reiki", new String[]{"energia", "chakra", "imposição", "mãos", "energização"});
        THERAPY_KEYWORDS.put("meditacao", new String[]{"meditar", "mindfulness", "respiração", "concentração"});
        THERAPY_KEYWORDS.put("yoga", new String[]{"asana", "postura", "pranayama", "yoga"});
        THERAPY_KEYWORDS.put("fitoterapia", new String[]{"planta", "chá", "erva", "fitoterápico"});
    }

    @Transactional
    public ChatDTO.ChatResponse processMessage(ChatDTO.ChatRequest request) {
        LOG.infof("Processando mensagem da sessão: %s", request.sessionId);

        try {
            // Identifica o tipo de terapia se não foi especificado
            String therapyType = request.therapyType;
            if (therapyType == null || therapyType.isEmpty()) {
                therapyType = detectTherapyType(request.message);
                LOG.infof("Terapia detectada: %s", therapyType);
            }

            // Gera resposta usando Claude
            String botResponse = claudeService.generateResponse(
                    request.message,
                    therapyType,
                    request.sessionId
            );

            // Analisa o sentimento da mensagem do usuário
            String sentiment = claudeService.analyzeSentiment(request.message);

            // Salva no banco
            Message message = new Message();
            message.sessionId = request.sessionId;
            message.userMessage = request.message;
            message.botResponse = botResponse;
            message.therapyType = therapyType;
            message.sentiment = sentiment;
            message.persist();

            LOG.infof("Mensagem salva com ID: %d", message.id);

            // Gera sugestões contextuais
            String[] suggestions = claudeService.generateSuggestions(request.message, therapyType);

            // Prepara resposta
            ChatDTO.ChatResponse response = new ChatDTO.ChatResponse();
            response.sessionId = request.sessionId;
            response.message = botResponse;
            response.therapyType = therapyType;
            response.sentiment = sentiment;
            response.suggestions = suggestions;
            response.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            return response;

        } catch (Exception e) {
            LOG.errorf(e, "Erro ao processar mensagem: %s", e.getMessage());
            throw new RuntimeException("Erro ao processar mensagem", e);
        }
    }

    private String detectTherapyType(String message) {
        String lowerMessage = message.toLowerCase();

        for (Map.Entry<String, String[]> entry : THERAPY_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (lowerMessage.contains(keyword)) {
                    return entry.getKey();
                }
            }
        }

        return "geral";
    }

    public List<ChatDTO.TherapyInfo> getAvailableTherapies() {
        List<ChatDTO.TherapyInfo> therapies = new ArrayList<>();

        ChatDTO.TherapyInfo ayurveda = new ChatDTO.TherapyInfo();
        ayurveda.type = "Ayurveda";
        ayurveda.description = "Sistema medicinal milenar da Índia focado no equilíbrio dos doshas";
        ayurveda.benefits = new String[]{"Equilíbrio corporal", "Autoconhecimento", "Prevenção"};
        therapies.add(ayurveda);

        ChatDTO.TherapyInfo acupuntura = new ChatDTO.TherapyInfo();
        acupuntura.type = "Acupuntura";
        acupuntura.description = "Técnica da medicina tradicional chinesa usando agulhas em pontos específicos";
        acupuntura.benefits = new String[]{"Alívio de dores", "Redução de estresse", "Equilíbrio energético"};
        therapies.add(acupuntura);

        ChatDTO.TherapyInfo aromaterapia = new ChatDTO.TherapyInfo();
        aromaterapia.type = "Aromaterapia";
        aromaterapia.description = "Uso terapêutico de óleos essenciais extraídos de plantas";
        aromaterapia.benefits = new String[]{"Relaxamento", "Bem-estar emocional", "Alívio de sintomas"};
        therapies.add(aromaterapia);

        ChatDTO.TherapyInfo reiki = new ChatDTO.TherapyInfo();
        reiki.type = "Reiki";
        reiki.description = "Terapia energética japonesa de canalização da energia vital";
        reiki.benefits = new String[]{"Equilíbrio energético", "Redução de ansiedade", "Relaxamento profundo"};
        therapies.add(reiki);

        ChatDTO.TherapyInfo meditacao = new ChatDTO.TherapyInfo();
        meditacao.type = "Meditação";
        meditacao.description = "Práticas contemplativas para treinar a mente e consciência";
        meditacao.benefits = new String[]{"Clareza mental", "Redução de estresse", "Foco e concentração"};
        therapies.add(meditacao);

        ChatDTO.TherapyInfo yoga = new ChatDTO.TherapyInfo();
        yoga.type = "Yoga";
        yoga.description = "Prática integrativa que une corpo, mente e espírito";
        yoga.benefits = new String[]{"Flexibilidade", "Força", "Equilíbrio mental"};
        therapies.add(yoga);

        ChatDTO.TherapyInfo fitoterapia = new ChatDTO.TherapyInfo();
        fitoterapia.type = "Fitoterapia";
        fitoterapia.description = "Tratamento através do uso de plantas medicinais";
        fitoterapia.benefits = new String[]{"Natural", "Preventivo", "Complementar"};
        therapies.add(fitoterapia);

        return therapies;
    }
}