package com.lsiproject.app.propertymanagementmicroservice.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class JwtUtil {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Extrait le Payload du JWT, le décode de Base64 et le parse en Map de Claims.
     */
    public Map<String, Object> extractClaimsFromPayload(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            String payloadEncoded = parts[1];
            String payloadPadded = payloadEncoded.replace('-', '+').replace('_', '/');
            while (payloadPadded.length() % 4 != 0) {
                payloadPadded += "=";
            }

            byte[] decodedBytes = Base64.getDecoder().decode(payloadPadded);
            String payloadJson = new String(decodedBytes, StandardCharsets.UTF_8);

            return objectMapper.readValue(payloadJson, Map.class);

        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException | JsonProcessingException e) {
            System.err.println("Erreur decoding JWT Payload: " + e.getMessage());
            return null;
        }
    }

    // Dans JwtUtil.java

    public UserPrincipal extractUserPrincipal(String token) {
        Map<String, Object> claims = extractClaimsFromPayload(token);

        // 1. Si le payload est illisible
        if (claims == null) {
            System.err.println("JWT Error: Payload illisible ou token malformé.");
            return null;
        }

        try {
            // --- VALIDATION 1 : Wallet (Subject) ---
            String walletAddress = String.valueOf(claims.getOrDefault("sub", claims.get("wallet")));
            if (walletAddress == null || walletAddress.isEmpty() || "null".equals(walletAddress)) {
                System.err.println("JWT Error: Adresse Wallet manquante.");
                return null; // On rejette
            }

            // --- VALIDATION 2 : ID Utilisateur ---
            // On s'assure que l'ID existe et qu'il n'est pas null
            Object idObj = claims.get("id");
            if (idObj == null) {
                System.err.println("JWT Error: ID utilisateur manquant.");
                return null; // On rejette
            }
            Long idUser = Long.valueOf(idObj.toString());

            // --- VALIDATION 3 : Rôle ---
            Object roleObj = claims.get("role");
            if (roleObj == null || roleObj.toString().trim().isEmpty()) {
                System.err.println("JWT Error: Rôle utilisateur manquant.");
                return null; // On rejette
            }

            // Nettoyage du rôle (ex: ROLE_USER -> USER pour éviter ROLE_ROLE_USER)
            String roleStr = roleObj.toString();
            if (roleStr.startsWith("ROLE_")) {
                roleStr = roleStr.replace("ROLE_", "");
            }
            Set<String> roles = new HashSet<>();
            roles.add(roleStr);

            // Si on arrive ici, TOUT est bon
            return new UserPrincipal(idUser, walletAddress, roles);

        } catch (Exception e) {
            // En cas d'erreur de conversion (ex: ID qui n'est pas un nombre)
            System.err.println("JWT Error: Données corrompues - " + e.getMessage());
            return null;
        }
    }
}