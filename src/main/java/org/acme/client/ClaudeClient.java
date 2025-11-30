package org.acme.client;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.dto.ClaudeDTO;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@RegisterRestClient(configKey = "claude")
public interface ClaudeClient {

    @POST
    @Path("/messages")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ClientHeaderParam(name = "x-api-key", value = "${claude.api.key}")
    @ClientHeaderParam(name = "anthropic-version", value = "2023-06-01")
    // ❌ REMOVIDO - @Consumes já define Content-Type automaticamente
    // @ClientHeaderParam(name = "Content-Type", value = "application/json")
    @Retry(
            maxRetries = 3,        // Reduzido para 3x
            delay = 500,           // 500ms entre tentativas
            maxDuration = 10000,   // 10s total
            jitter = 200
    )
//    @Fallback(fallbackMethod = "fallback")
    ClaudeDTO.MessageResponse createMessage(ClaudeDTO.MessageRequest request);

    // Fallback method
    default ClaudeDTO.MessageResponse fallback(ClaudeDTO.MessageRequest request) {
        ClaudeDTO.MessageResponse resp = new ClaudeDTO.MessageResponse();
        resp.id = "fallback";
        resp.type = "message";
        resp.role = "assistant";
        resp.model = "claude-sonnet-4-20250514";

        ClaudeDTO.ContentBlock content = new ClaudeDTO.ContentBlock("Desculpe, o serviço está temporariamente indisponível. " +
                "Por favor, tente novamente em alguns instantes. " +
                "Estamos aqui para ajudá-lo com suas dúvidas sobre terapias integrativas.");

        resp.content = List.of(content);
        resp.stopReason = "end_turn";

        ClaudeDTO.Usage usage = new ClaudeDTO.Usage();
        usage.inputTokens = 0;
        usage.outputTokens = 0;
        resp.usage = usage;

        return resp;
    }
}