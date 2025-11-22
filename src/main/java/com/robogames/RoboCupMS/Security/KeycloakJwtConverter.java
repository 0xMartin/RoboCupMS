package com.robogames.RoboCupMS.Security;

import com.robogames.RoboCupMS.Entity.UserRC;
import com.robogames.RoboCupMS.Repository.UserRepository;
import com.robogames.RoboCupMS.Business.Enum.ERole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class KeycloakJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // 1. Získání rolí z Keycloak tokenu (obvykle jsou v claimu realm_access nebo resource_access)
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);

        // 2. Získání emailu z tokenu
        String email = jwt.getClaimAsString("email");
        
        // 3. Synchronizace s vaší DB (UserRC)
        UserRC userRC = syncUserWithDb(jwt, email);

        // 4. Vrátíme Authentication objekt, který má uvnitř UserRC jako "Principal"
        // Díky tomuto bude fungovat vaše stávající logika, která castuje Principal na UserRC
        return new UsernamePasswordAuthenticationToken(userRC, jwt, authorities);
    }

    private UserRC syncUserWithDb(Jwt jwt, String email) {
        Optional<UserRC> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            // Uživatel existuje, můžeme aktualizovat třeba jméno, pokud se změnilo v Keycloaku
            return existingUser.get(); 
        } else {
            // Uživatel je v Keycloaku, ale ne v naší DB -> Auto-registrace
            // Keycloak řeší hesla, my už jen ukládáme profil
            UserRC newUser = new UserRC();
            newUser.setEmail(email);
            newUser.setName(jwt.getClaimAsString("given_name"));
            newUser.setSurname(jwt.getClaimAsString("family_name"));
            
            // Nastavení defaultní role, data narození atd. (možná bude potřeba vytáhnout z tokenu, pokud to tam Keycloak posílá)
            // newUser.setBirthDate(...); 
            
            return userRepository.save(newUser);
        }
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null || realmAccess.isEmpty()) {
            return new ArrayList<>();
        }

        Collection<String> roles = (Collection<String>) realmAccess.get("roles");
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());
    }
}