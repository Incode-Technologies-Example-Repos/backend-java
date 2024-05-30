package com.incode.tokenserver.controllers;

import com.incode.tokenserver.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/")
public class IncodeController {

    private final Logger log = LoggerFactory.getLogger(IncodeController.class);

    @Value("${API_KEY}")
    private String apiKey;

    @Value("${API_URL}")
    private String apiUrl;

    @Value("${FLOW_ID}")
    private String configurationId;

    @Value("${CLIENT_ID}")
    private String clientId;

    @Value("${ADMIN_TOKEN}")
    private String adminToken;

    private final WebClient webClient;

    public IncodeController() {
        this.webClient = WebClient.builder().build();
    }

    private Mono<OmniStartResponse> createIncodeSession() {
        String startUrl = apiUrl + "/omni/start";
        log.info("Calling /omni/start");

        return webClient.post()
                .uri(startUrl)
                .header("x-api-key", apiKey)
                .header("api-version", "1.0")
                .bodyValue(Map.of(
                        "configurationId", configurationId,
                        "countryCode", "ALL",
                        "language", "en-US"
                ))
                .retrieve()
                .bodyToMono(OmniStartResponse.class);
    }

    private Mono<FetchOnboardingUrlResponse> getOnboardingUrl(String token) {
        String url = apiUrl + "/omni/onboarding-url?clientId=" + clientId;
        log.info("Calling {}", url);

        return webClient.get()
                .uri(url)
                .header("X-Incode-Hardware-Id", token)
                .header("x-api-key", apiKey)
                .header("api-version", "1.0")
                .retrieve()
                .bodyToMono(FetchOnboardingUrlResponse.class);
    }

    public Mono<FetchScoreResponse> getScoreOfSession(String interviewId, String token) {
        String url = apiUrl + "/omni/get/score?id=" + interviewId;
        log.info("Calling {}", url);

        return webClient.get()
                .uri(url)
                .header("X-Incode-Hardware-Id", token)
                .header("x-api-key", apiKey)
                .header("api-version", "1.0")
                .retrieve()
                .bodyToMono(FetchScoreResponse.class);
    }

    public Mono<OnboardingStatusResponse> getOnboardingStatusOfSession(String interviewId) {
        String url = apiUrl + "/omni/get/onboarding/status?id=" + interviewId;
        log.info("Calling {}", url);

        return webClient.get()
                .uri(url)
                .header("X-Incode-Hardware-Id", adminToken)
                .header("x-api-key", apiKey)
                .header("api-version", "1.0")
                .retrieve()
                .bodyToMono(OnboardingStatusResponse.class);
    }

    @GetMapping("/start")
    public Mono<Map<String, String>> createSession() {
        return createIncodeSession().map( omniStartResponse -> {
            Map<String, String> response = Map.of(
                    "interviewId", omniStartResponse.interviewId(),
                    "token", omniStartResponse.token()
            );
            return response;
        });
    }

    @GetMapping("/onboarding-url")
    public Mono<Map<String, Object>> createSessionWithRedirectUrl() {
        return createIncodeSession()
            .flatMap(omniStartResponse ->
                getOnboardingUrl(omniStartResponse.token())
                    .map(onboardingUrlResponse -> {
                        Map<String, Object> response = Map.of(
                                "interviewId", omniStartResponse.interviewId(),
                                "token", omniStartResponse.token(),
                                "url", onboardingUrlResponse.url(),
                                "success", true
                        );
                        return response;
                    }).onErrorResume(this::buildResponseError)
            );
    }

    @GetMapping("/fetch-score")
    public Mono<Map<String, Object>> getFetchScoreFromASession(@RequestParam String interviewId,
                                                               @RequestHeader("X-Token") String token) {
        return getScoreOfSession(interviewId, token)
                .map(fetchScoreResponse -> {
                    Map<String, Object> response = Map.of(
                            "score", fetchScoreResponse.overall().status(),
                            "success", true
                    );
                    return response;
                }).onErrorResume(this::buildResponseError);
    }

    @GetMapping("onboarding-status")
    public Mono<Map<String, Object>> getOnboardingStatusFromASession(@RequestParam String interviewId) {
        return getOnboardingStatusOfSession(interviewId)
                .map(onboardingStatusResponse -> {
                    Map<String, Object> response = Map.of(
                            "onboardingStatus", onboardingStatusResponse.onboardingStatus(),
                            "success", true
                    );
                    return response;
                }).onErrorResume(this::buildResponseError);
    }

    @PostMapping("/webhook")
    public Mono<ResponseEntity<Map<String, Object>>> webhookAction(@RequestBody Mono<WebhookPayload> dataMono) {
        return dataMono.map(data -> {
            String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            Map<String, Object> response = Map.of(
            "timeStamp", timeStamp,
            "data", data
            );

            System.out.println(response);

            return ResponseEntity.ok(response);
        });
    }

    //This is not the proper way for handling errors, considering changing it on your purposes.
    private Mono<Map<String, Object>> buildResponseError(Throwable error) {
        Map<String, Object> responseError = Map.of(
                "error", error.getMessage(),
                "success", false
        );
        return Mono.just(responseError);
    }
}
