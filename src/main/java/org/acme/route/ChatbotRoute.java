package org.acme.route;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.dto.ChatDTO;
import org.acme.entity.Message;
import org.acme.service.TherapyService;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;

import java.util.List;

@ApplicationScoped
public class ChatbotRoute extends RouteBuilder {

    @Inject
    TherapyService therapyService;

    @Override
    public void configure() throws Exception {

        // Configuração REST
        restConfiguration()
                .component("platform-http")
                .bindingMode(RestBindingMode.json)
                .dataFormatProperty("prettyPrint", "true")
                .enableCORS(true)
                .contextPath("/api")
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "Chatbot Terapias Integrativas API")
                .apiProperty("api.version", "1.0.0");

        // Endpoints REST
        rest("/chat")
                .description("Chat endpoints")

                .post("/message")
                .description("Enviar mensagem para o chatbot")
                .type(ChatDTO.ChatRequest.class)
                .outType(ChatDTO.ChatResponse.class)
                .to("direct:processMessage")

                .get("/history/{sessionId}")
                .description("Obter histórico de conversas")
                .param().name("sessionId").type(RestParamType.path).description("ID da sessão").endParam()
                .to("direct:getHistory")

                .get("/therapies")
                .description("Listar terapias disponíveis")
                .to("direct:getTherapies");

        // Rota: Processar mensagem
        from("direct:processMessage")
                .routeId("process-message-route")
                .log("Recebida mensagem: ${body}")
                .bean(therapyService, "processMessage")
                .log("Resposta gerada: ${body}");

        // Rota: Obter histórico
        from("direct:getHistory")
                .routeId("get-history-route")
                .log("Buscando histórico para sessão: ${header.sessionId}")
                .process(exchange -> {
                    String sessionId = exchange.getIn().getHeader("sessionId", String.class);
                    List<Message> messages = Message.findBySessionId(sessionId);
                    exchange.getIn().setBody(messages);
                });

        // Rota: Listar terapias
        from("direct:getTherapies")
                .routeId("get-therapies-route")
                .log("Listando terapias disponíveis")
                .bean(therapyService, "getAvailableTherapies");

        // Rota assíncrona para processamento em background
        from("direct:analyzeConversation")
                .routeId("analyze-conversation-route")
                .log("Analisando padrões da conversa...")
                .delay(1000)
                .process(exchange -> {
                    // Aqui você pode adicionar análises mais complexas
                    // como detecção de padrões, ML, etc.
                    log.info("Análise de conversa concluída");
                });
    }
}