package com.example.oauth2login.OAuth;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.oauth2login.Entity.AuthProvider;
import com.example.oauth2login.Entity.User;
import com.example.oauth2login.Repository.AuthProviderRepository;
import com.example.oauth2login.Repository.UserRepository;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final AuthProviderRepository authProviderRepository;

    public CustomOAuth2UserService(UserRepository userRepository, AuthProviderRepository authProviderRepository) {
        this.userRepository = userRepository;
        this.authProviderRepository = authProviderRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = new DefaultOAuth2UserService().loadUser(request);
        String provider = request.getClientRegistration().getRegistrationId();
        Map<String, Object> attrs = oauthUser.getAttributes();

        String providerUserId, email, name, avatar;
        if ("google".equals(provider)) {
            providerUserId = (String) attrs.get("sub");
            email = (String) attrs.get("email");
            name = (String) attrs.get("name");
            avatar = (String) attrs.get("picture");
        } else { // github
            providerUserId = String.valueOf(attrs.get("id"));
            name = (String) attrs.getOrDefault("name", attrs.get("login"));
            avatar = (String) attrs.get("avatar_url");
            email = (String) attrs.getOrDefault("email", name + "@users.noreply.github.com");
        }

        if (email == null) throw new OAuth2AuthenticationException("No email found from provider");

        AuthProvider authProvider = authProviderRepository
                .findByProviderAndProviderUserId(provider, providerUserId)
                .orElseGet(() -> {
                    User user = userRepository.findByEmail(email)
                            .orElseGet(() -> userRepository.save(new User(email, name, avatar)));
                    AuthProvider ap = new AuthProvider(user, provider, providerUserId, email);
                    return authProviderRepository.save(ap);
                });

        User user = authProvider.getUser();
        Map<String, Object> principalAttrs = new HashMap<>(attrs);
        principalAttrs.put("app_user_id", user.getId());
        principalAttrs.put("app_user_email", user.getEmail());

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                principalAttrs,
                "app_user_email"
        );
    }
}
