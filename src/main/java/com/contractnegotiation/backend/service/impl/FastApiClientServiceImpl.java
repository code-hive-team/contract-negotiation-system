package com.contractnegotiation.backend.service.impl;

import com.contractnegotiation.backend.dto.NegotiationResponseDto;
import com.contractnegotiation.backend.exception.FastApiException;
import com.contractnegotiation.backend.exception.FastApiUnavailableException;
import com.contractnegotiation.backend.service.FastApiClientService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class FastApiClientServiceImpl implements FastApiClientService {

    private final WebClient webClient;
    private final String negotiateUrl;

    public FastApiClientServiceImpl(
            WebClient.Builder webClientBuilder,
            @Value("${fastapi.negotiate.url}") String negotiateUrl) {
        this.webClient = webClientBuilder.build();
        this.negotiateUrl = negotiateUrl;
    }

    @Override
    public NegotiationResponseDto sendNegotiationRequest(
            byte[] issuerPdfBytes, String issuerFilename,
            byte[] acquirerPdfBytes, String acquirerFilename) {

        try {
            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();

            ByteArrayResource issuerResource = new ByteArrayResource(issuerPdfBytes) {
                @Override
                public String getFilename() {
                    return (issuerFilename != null && !issuerFilename.trim().isEmpty())
                            ? issuerFilename
                            : "issuer_contract.pdf";
                }
            };

            ByteArrayResource acquirerResource = new ByteArrayResource(acquirerPdfBytes) {
                @Override
                public String getFilename() {
                    return (acquirerFilename != null && !acquirerFilename.trim().isEmpty())
                            ? acquirerFilename
                            : "acquirer_contract.pdf";
                }
            };

            bodyBuilder.part("issuer_file", issuerResource, MediaType.APPLICATION_PDF);
            bodyBuilder.part("acquirer_file", acquirerResource, MediaType.APPLICATION_PDF);

            MultiValueMap<String, HttpEntity<?>> body = bodyBuilder.build();

            return webClient.post()
                    .uri(negotiateUrl)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body))
                    .retrieve()
                    .bodyToMono(NegotiationResponseDto.class)
                    .block();

        } catch (WebClientRequestException e) {
            throw new FastApiUnavailableException("FastAPI GenAI service is unavailable at " + negotiateUrl + ": " + e.getMessage(), e);
        } catch (WebClientResponseException e) {
            throw new FastApiException("FastAPI GenAI service returned HTTP error [" + e.getStatusCode() + "]: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new FastApiException("Failed to communicate with FastAPI GenAI service: " + e.getMessage(), e);
        }
    }
}
