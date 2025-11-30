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

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        // 1. Vérifier si le header Authorization est présent et au format Bearer
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        // 2. Si un utilisateur n'est pas déjà authentifié dans le contexte Spring
        if (SecurityContextHolder.getContext().getAuthentication() == null) {

            // 3. Extraire les informations de l'utilisateur (sans validation de signature)
            UserPrincipal userPrincipal = jwtUtil.extractUserPrincipal(jwt);

            if (userPrincipal != null) {

                // 4. Créer le jeton d'authentification pour Spring Security
                // Nous ne faisons pas de validation ici (le booléen 'isAuthenticated' est vrai par défaut)
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userPrincipal,
                        null, // Pas de mot de passe / credentials ici
                        userPrincipal.getAuthorities()
                );

                // 5. Ajouter les détails de la requête au jeton
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 6. Placer l'objet UserPrincipal dans le contexte de sécurité
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}