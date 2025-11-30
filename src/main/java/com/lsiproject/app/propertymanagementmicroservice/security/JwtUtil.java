package com.lsiproject.app.propertymanagementmicroservice.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

import java.util.*;

import java.util.stream.Collectors;

/**
 * Utilitaire pour extraire les informations (Claims) du JWT par décodage manuel Base64.
 * Cette méthode suppose que la validation de la signature (sécurité) est effectuée
 * par l'API Gateway en amont.
 */
@Service
public class JwtUtil {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Extrait le Payload du JWT, le décode de Base64 et le parse en Map de Claims.
     * @param token Le JWT complet (Header.Payload.Signature).
     * @return Map<String, Object> contenant les claims, ou null en cas d'erreur.
     */
    public Map<String, Object> extractClaimsFromPayload(String token) {
        try {
            // 1. Séparer les parties du JWT
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                System.err.println("Le token JWT n'a pas le format attendu (Header.Payload.Signature)");
                return null;
            }

            // 2. Extraire la partie Payload (index 1)
            String payloadEncoded = parts[1];

            // 3. Remplacer les caractères Base64 URL-safe (- et _) par des caractères standard (+ et /)
            // et ajouter le padding si nécessaire (pour certains implémentations Base64)
            String payloadPadded = payloadEncoded.replace('-', '+').replace('_', '/');
            while (payloadPadded.length() % 4 != 0) {
                payloadPadded += "=";
            }

            // 4. Décoder de Base64
            byte[] decodedBytes = Base64.getDecoder().decode(payloadPadded);
            String payloadJson = new String(decodedBytes, StandardCharsets.UTF_8);

            // 5. Parser le JSON en Map
            // Utilisez le type Map<String, Object> pour représenter les claims
            return objectMapper.readValue(payloadJson, Map.class);

        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException | JsonProcessingException e) {
            System.err.println("Erreur lors de l'extraction ou du décodage du Payload du JWT: " + e.getMessage());
            return null;
        }
    }

    /**
     * Extrait les informations nécessaires pour créer un UserPrincipal.
     * @param token Le JWT.
     * @return L'objet UserPrincipal, ou null en cas d'échec.
     */
    public UserPrincipal extractUserPrincipal(String token) {
        Map<String, Object> claims = extractClaimsFromPayload(token);
        if (claims == null) {
            return null;
        }

        try {
            // NOTE: Assurez-vous que les noms des claims correspondent à ceux générés par votre microservice d'authentification.

            // 1. Extraire l'adresse du portefeuille (walletAddress)
            // C'est désormais le 'sub' (Subject) ou le claim 'walletAddress'
            String walletAddress = claims.getOrDefault("sub", claims.get("walletAddress")).toString();

            // 2. Extraire l'ID utilisateur interne (userId)
            // L'ID est maintenant un claim personnalisé et non le subject.
            Long idUser = Long.valueOf(claims.get("userId").toString());

            // 3. Extraire les rôles (supposons un tableau de chaînes ou une chaîne séparée par des virgules)
            Set<String> roles = Collections.emptySet();
            Object rolesClaim = claims.get("roles");

            if (rolesClaim instanceof String) {
                roles = Arrays.stream(((String) rolesClaim).split(","))
                        .map(String::trim)
                        .collect(Collectors.toSet());
            } else if (rolesClaim instanceof List) {
                roles = ((List<String>) rolesClaim).stream().map(String::valueOf).collect(Collectors.toSet());
            }

            return new UserPrincipal(idUser, walletAddress, roles);

        } catch (Exception e) {
            System.err.println("Erreur de conversion des Claims en UserPrincipal: " + e.getMessage());
            return null;
        }
    }
}