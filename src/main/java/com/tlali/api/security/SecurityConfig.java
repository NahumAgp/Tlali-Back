package com.tlali.api.security;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

	private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;
	private final ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository;
	private final DeviceApiKeyAuthenticationFilter deviceApiKeyAuthenticationFilter;

	public SecurityConfig(
			OAuth2LoginSuccessHandler oauth2LoginSuccessHandler,
			ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository,
			DeviceApiKeyAuthenticationFilter deviceApiKeyAuthenticationFilter
	) {
		this.oauth2LoginSuccessHandler = oauth2LoginSuccessHandler;
		this.clientRegistrationRepository = clientRegistrationRepository;
		this.deviceApiKeyAuthenticationFilter = deviceApiKeyAuthenticationFilter;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(
								"/api/v1/health",
								"/api/v1/auth/login",
								"/oauth2/**",
								"/login/oauth2/**"
						).permitAll()
						.anyRequest().authenticated()
				)
				.addFilterBefore(deviceApiKeyAuthenticationFilter, BearerTokenAuthenticationFilter.class)
				.oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwt -> {
				}));

		if (clientRegistrationRepository.getIfAvailable() != null) {
			http.oauth2Login(oauth -> oauth.successHandler(oauth2LoginSuccessHandler));
		}

		return http.build();
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of(
				"http://localhost:5173",
				"http://127.0.0.1:5173"
		));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setExposedHeaders(List.of("Authorization"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
