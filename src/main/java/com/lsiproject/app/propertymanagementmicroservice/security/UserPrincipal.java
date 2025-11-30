package com.lsiproject.app.propertymanagementmicroservice.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Représente l'utilisateur authentifié.
 * Dans ce modèle Web3, l'adresse du portefeuille est l'identifiant principal (Username / Subject).
 */
public class UserPrincipal implements UserDetails {

    private final Long idUser;
    private final String walletAddress; // Deviendra le 'username' de UserDetails
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long idUser, String walletAddress, Set<String> roles) {
        this.idUser = idUser;
        this.walletAddress = walletAddress;

        // Convertit les rôles (ex: "TENANT", "LANDLORD") en autorités Spring Security (ex: "ROLE_TENANT")
        this.authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());
    }

    // --- Getters spécifiques pour l'application ---

    public Long getIdUser() {
        return idUser;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    // --- Implémentation des méthodes de UserDetails ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        // Non pertinent dans une architecture sans état (stateless) basée sur JWT
        return null;
    }

    @Override
    public String getUsername() {
        // Le walletAddress devient l'identifiant principal (le Subject du JWT)
        return walletAddress;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}