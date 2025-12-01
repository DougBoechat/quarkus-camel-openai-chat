package org.acme.route;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.dto.CheckInDTO;
import org.acme.dto.CheckInResponseDTO;
import org.acme.service.CheckInService;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;

import java.time.LocalDate;

@ApplicationScoped
public class CheckInRoute extends RouteBuilder {

    @Inject
    CheckInService checkInService;

    // ObjectMapper configurado para Java 8 Date/Time
    private final ObjectMapper objectMapper;

    public CheckInRoute() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void configure() throws Exception {

        JacksonDataFormat jsonDataFormat = new JacksonDataFormat();
        jsonDataFormat.setObjectMapper(objectMapper);
        jsonDataFormat.setPrettyPrint(false);

        restConfiguration()
                .component("platform-http")
                .bindingMode(RestBindingMode.json)
                .enableCORS(true)
                .dataFormatProperty("prettyPrint", "true");

        // Configuração de tratamento de erros
        onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    Exception cause = exchange.getProperty(org.apache.camel.Exchange.EXCEPTION_CAUGHT, Exception.class);
                    exchange.getIn().setHeader("CamelHttpResponseCode", 500);
                    exchange.getIn().setHeader("Content-Type", "application/json");
                    exchange.getIn().setBody("{\"error\":\"" + cause.getMessage() + "\"}");
                })
                .log("❌ Erro: ${exception.message}");

        rest("/checkin")
                .consumes("application/json")
                .produces("application/json")
                .post()
                .description("Criar ou atualizar check-in")
                .to("direct:createCheckIn");



        // Endpoints REST
        rest("/checkin")
                .description("Check-in diário endpoints")
                .consumes("application/json")
                .produces("application/json")

                .get("/today/{userId}")
                .description("Obter check-in de hoje")
                .param().name("userId").type(RestParamType.path).endParam()
                .to("direct:getTodayCheckIn")

                .get("/date/{userId}/{date}")
                .description("Obter check-in por data")
                .param().name("userId").type(RestParamType.path).endParam()
                .param().name("date").type(RestParamType.path).endParam()
                .to("direct:getCheckInByDate")

                .get("/history/{userId}")
                .description("Obter histórico")
                .param().name("userId").type(RestParamType.path).endParam()
                .param().name("days").type(RestParamType.query).endParam()
                .to("direct:getCheckInHistory")

                .get("/stats/{userId}")
                .description("Obter estatísticas")
                .param().name("userId").type(RestParamType.path).endParam()
                .param().name("days").type(RestParamType.query).endParam()
                .to("direct:getCheckInStats")

                .delete("/{userId}/{date}")
                .description("Deletar check-in")
                .param().name("userId").type(RestParamType.path).endParam()
                .param().name("date").type(RestParamType.path).endParam()
                .to("direct:deleteCheckIn");

        // ========== IMPLEMENTAÇÕES ==========

        // Criar Check-in
        from("direct:createCheckIn")
                .routeId("create-checkin-route")
                .log("📝 Request recebido: ${body}")
                .process(exchange -> {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> bodyMap = exchange.getIn().getBody(java.util.Map.class);

                    CheckInDTO.CheckInRequest req = new CheckInDTO.CheckInRequest();
                    req.setUserId((String) bodyMap.get("userId"));
                    req.setEmotion((String) bodyMap.get("emotion"));
                    req.setEnergyLevel((Integer) bodyMap.get("energyLevel"));
                    req.setSleepQuality((Integer) bodyMap.get("sleepQuality"));
                    req.setNotes((String) bodyMap.get("notes"));

                    if (bodyMap.get("checkinDate") != null) {
                        req.setCheckinDate(LocalDate.parse((String) bodyMap.get("checkinDate")));
                    } else {
                        req.setCheckinDate(LocalDate.now());
                    }

                    if (req.getUserId() == null || req.getUserId().isBlank()) {
                        exchange.getIn().setHeader("CamelHttpResponseCode", 400);
                        exchange.getIn().setHeader("Content-Type", "application/json");
                        exchange.getMessage().setBody("{\"error\":\"userId is required\"}");
                        return;
                    }

                    CheckInResponseDTO res = checkInService.createOrUpdateCheckIn(req);

                    exchange.getMessage().setHeader("checkInResponse", res);
                })
                .setBody(header("checkInResponse"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"));
//                .log("✅ Check-in salvo!");

        // Obter check-in de hoje
        from("direct:getTodayCheckIn")
                .routeId("get-today-route")
                .log("🔍 Buscando check-in de hoje: ${header.userId}")
                .process(exchange -> {
                    String userId = exchange.getIn().getHeader("userId", String.class);
                    CheckInResponseDTO res = checkInService.getTodayCheckIn(userId);

                    exchange.getIn().setHeader("Content-Type", "application/json");

                    if (res == null) {
                        exchange.getIn().setHeader("CamelHttpResponseCode", 404);
                        exchange.getIn().setBody("{\"message\":\"No check-in found for today\"}");
                    } else {
                        String jsonResponse = objectMapper.writeValueAsString(res);
                        exchange.getIn().setHeader("CamelHttpResponseCode", 200);
                        exchange.getIn().setBody(jsonResponse);
                    }
                });

        // Obter check-in por data
        from("direct:getCheckInByDate")
                .routeId("get-by-date-route")
                .log("🔍 Buscando check-in: ${header.userId} - ${header.date}")
                .process(exchange -> {
                    String userId = exchange.getIn().getHeader("userId", String.class);
                    String dateStr = exchange.getIn().getHeader("date", String.class);

                    exchange.getIn().setHeader("Content-Type", "application/json");

                    try {
                        LocalDate date = LocalDate.parse(dateStr);
                        CheckInResponseDTO res = checkInService.getCheckInByDate(userId, date);

                        if (res == null) {
                            exchange.getIn().setHeader("CamelHttpResponseCode", 404);
                            exchange.getIn().setBody("{\"message\":\"No check-in found for this date\"}");
                        } else {
                            String jsonResponse = objectMapper.writeValueAsString(res);
                            exchange.getIn().setHeader("CamelHttpResponseCode", 200);
                            exchange.getIn().setBody(jsonResponse);
                        }
                    } catch (Exception e) {
                        exchange.getIn().setHeader("CamelHttpResponseCode", 400);
                        exchange.getIn().setBody("{\"error\":\"Invalid date format. Use YYYY-MM-DD\"}");
                    }
                });

        // Obter histórico
        from("direct:getCheckInHistory")
                .routeId("get-history-route")
                .log("📋 Buscando histórico: ${header.userId}")
                .process(exchange -> {
                    String userId = exchange.getIn().getHeader("userId", String.class);
                    String daysStr = exchange.getIn().getHeader("days", String.class);

                    Integer days = null;
                    if (daysStr != null && !daysStr.isBlank()) {
                        try {
                            days = Integer.parseInt(daysStr);
                        } catch (NumberFormatException ignored) {}
                    }

                    var history = checkInService.getCheckInHistory(userId, days);
                    String jsonResponse = objectMapper.writeValueAsString(history);

                    exchange.getIn().setHeader("CamelHttpResponseCode", 200);
                    exchange.getIn().setHeader("Content-Type", "application/json");
                    exchange.getIn().setBody(jsonResponse);
                });

        // Obter estatísticas
        from("direct:getCheckInStats")
                .routeId("get-stats-route")
                .log("📊 Buscando estatísticas: ${header.userId}")
                .process(exchange -> {
                    String userId = exchange.getIn().getHeader("userId", String.class);
                    String daysStr = exchange.getIn().getHeader("days", String.class);

                    Integer days = null;
                    if (daysStr != null && !daysStr.isBlank()) {
                        try {
                            days = Integer.parseInt(daysStr);
                        } catch (NumberFormatException ignored) {}
                    }

                    CheckInDTO.CheckInStats stats = checkInService.getCheckInStats(userId, days);
                    String jsonResponse = objectMapper.writeValueAsString(stats);

                    exchange.getIn().setHeader("CamelHttpResponseCode", 200);
                    exchange.getIn().setHeader("Content-Type", "application/json");
                    exchange.getIn().setBody(jsonResponse);
                });

        // Deletar check-in
        from("direct:deleteCheckIn")
                .routeId("delete-route")
                .log("🗑️ Deletando check-in: ${header.userId} - ${header.date}")
                .process(exchange -> {
                    String userId = exchange.getIn().getHeader("userId", String.class);
                    String dateStr = exchange.getIn().getHeader("date", String.class);

                    exchange.getIn().setHeader("Content-Type", "application/json");

                    try {
                        LocalDate date = LocalDate.parse(dateStr);
                        boolean deleted = checkInService.deleteCheckIn(userId, date);

                        if (deleted) {
                            exchange.getIn().setHeader("CamelHttpResponseCode", 200);
                            exchange.getIn().setBody("{\"message\":\"Check-in deleted successfully\"}");
                        } else {
                            exchange.getIn().setHeader("CamelHttpResponseCode", 404);
                            exchange.getIn().setBody("{\"message\":\"Check-in not found\"}");
                        }
                    } catch (Exception e) {
                        exchange.getIn().setHeader("CamelHttpResponseCode", 400);
                        exchange.getIn().setBody("{\"error\":\"Invalid date format. Use YYYY-MM-DD\"}");
                    }
                });
    }
}