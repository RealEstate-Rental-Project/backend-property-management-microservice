package com.lsiproject.app.propertymanagementmicroservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;


import java.io.IOException;
import java.util.Set;

/**
 * Filtre personnalisé pour extraire le JWT du header et authentifier l'utilisateur.
 * Ce filtre assume que le JWT est valide (vérification faite par l'API Gateway).
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // Dans JwtAuthFilter.java

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        // 1. Pas de token ? On laisse passer (pour les endpoints publics comme /login ou /properties GET)
        // Spring Security bloquera plus tard si l'endpoint est protégé.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        // 2. Si un token est présent, on DOIT le valider
        if (SecurityContextHolder.getContext().getAuthentication() == null) {

            // On tente d'extraire l'utilisateur avec la validation stricte
            UserPrincipal userPrincipal = jwtUtil.extractUserPrincipal(jwt);

            if (userPrincipal != null) {
                // --- CAS SUCCÈS : Tout est présent ---
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userPrincipal,
                        null,
                        userPrincipal.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

            } else {
                // --- CAS ÉCHEC : Un champ manquait ou le token est pourri ---

                // On log l'erreur serveur
                System.err.println("Authentification échouée : Token invalide ou données manquantes (ID/Wallet/Role)");

                // On construit la réponse d'erreur pour le client (Frontend)
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Authentication Failed: Invalid Token\"}");

                // CRUCIAL : On ne continue PAS la chaîne. Le Controller n'est jamais appelé.
                return;
            }
        }

        // On continue vers le contrôleur UNIQUEMENT si l'authentification a réussi
        filterChain.doFilter(request, response);
    }
}