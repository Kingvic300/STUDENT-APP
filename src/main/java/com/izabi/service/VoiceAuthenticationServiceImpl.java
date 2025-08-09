package com.izabi.service;

import com.izabi.data.model.Embedding;
import com.izabi.data.repository.EmbeddingRepository;
import com.izabi.dto.response.EmbeddingResponse;
import com.izabi.dto.response.PythonEmbeddingResponse;
import com.izabi.exception.*;
import com.izabi.util.MultipartInputStreamFileResource;
import lombok.RequiredArgsConstructor;
import com.izabi.mapper.EmbeddingMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoiceAuthenticationServiceImpl implements VoiceAuthenticationService {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final int PASSWORD_LENGTH = 8;
    private final SecureRandom random = new SecureRandom();

    @Value("${voice.ml.service.url}")
    private String pythonMicroserviceUrl;

    private final RestTemplate restTemplate;
    private final EmbeddingRepository embeddingRepository;

    @Override
    public EmbeddingResponse extractVoiceFeatures(MultipartFile voiceSample) {
        if (voiceSample == null || voiceSample.isEmpty()) {
            throw new NoVoiceInputException("Please input your voice");
        }

        String contentType = voiceSample.getContentType();
        if (contentType == null || !contentType.startsWith("audio/")) {
            throw new InvalidVoiceInputException("Invalid voice input");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("audio", new MultipartInputStreamFileResource(
                    voiceSample.getInputStream(),
                    voiceSample.getOriginalFilename()));
            String embeddingUrl = pythonMicroserviceUrl + "/extract-embedding";
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<PythonEmbeddingResponse> response = restTemplate.postForEntity(embeddingUrl, requestEntity, PythonEmbeddingResponse.class);

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new FailedToExtractVoiceException("Failed to extract voice features");
            }

            Embedding embedding = new Embedding();
            embedding.setVoicePrint(response.getBody().getEmbedding().toString());
            embedding.setCreatedAt(LocalDateTime.now());
            embedding.setId(response.getBody().getFile_id());
            embedding.setFeature_count(response.getBody().getFeature_count());
            embeddingRepository.save(embedding);

            return EmbeddingMapper.mapToEmbeddingResponse("Voice print embedded successfully", embedding);
        } catch (Exception e) {
            throw new VoiceProcessingFailedException("Voice processing failed: " + e.getMessage());
        }

    }

    @Override
    public boolean verifyVoice(MultipartFile voiceSample, String storedVoicePrint) {
        if (voiceSample == null || voiceSample.isEmpty()) {
            throw new IllegalArgumentException("Voice sample is required.");
        }

        if (storedVoicePrint == null || storedVoicePrint.isBlank()) {
            throw new IllegalArgumentException("Stored voice print is invalid.");
        }

        EmbeddingResponse embeddingResponse = extractVoiceFeatures(voiceSample);
        String newVoicePrintStr = embeddingResponse.getEmbedding().getVoicePrint();

        List<Double> newVoiceVector = parseEmbedding(newVoicePrintStr);
        List<Double> storedVoiceVector = parseEmbedding(storedVoicePrint);

        if (newVoiceVector.size() != storedVoiceVector.size()) {
            throw new VoiceDoesNotMatchException("Embedding length mismatch.");
        }

        double similarity = cosineSimilarity(newVoiceVector, storedVoiceVector);
        double threshold = 0.85;

        if (similarity < threshold) {
            throw new VoiceDoesNotMatchException("Voice doesn't match - similarity: " + similarity);
        }

        return true;
    }

    @Override
    public String generateSecurePassword() {
        StringBuilder generatedPassword = new StringBuilder(PASSWORD_LENGTH);
        for (int count = 0; count < PASSWORD_LENGTH; count++) {
            generatedPassword.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return generatedPassword.toString();
    }

    private List<Double> parseEmbedding(String embedding) {
        if (embedding == null || embedding.isBlank()) {
            return List.of();
        }

        return Arrays.stream(embedding.split(","))
                .map(String::trim)
                .map(Double::parseDouble)
                .collect(Collectors.toList());
    }

    private double cosineSimilarity(List<Double> newVoicePrint, List<Double> storedVoicePrint) {
        double dot = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int count = 0; count < newVoicePrint.size(); count++) {
            dot += newVoicePrint.get(count) * storedVoicePrint.get(count);
            norm1 += Math.pow(newVoicePrint.get(count), 2);
            norm2 += Math.pow(storedVoicePrint.get(count), 2);
        }

        double denominator = Math.sqrt(norm1) * Math.sqrt(norm2);
        return dot / denominator;
    }
}
