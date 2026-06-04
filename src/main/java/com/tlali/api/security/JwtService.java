package com.tlali.api.security;

import com.tlali.api.user.AppUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JwtService {

	private final JwtEncoder jwtEncoder;
	private final long expiresInMinutes;

	public JwtService(
			JwtEncoder jwtEncoder,
			@Value("${tlali.security.jwt.expires-in-minutes}") long expiresInMinutes
	) {
		this.jwtEncoder = jwtEncoder;
		this.expiresInMinutes = expiresInMinutes;
	}

	public String createToken(AppUser user) {
		Instant now = Instant.now();
		Instant expiresAt = now.plus(expiresInMinutes, ChronoUnit.MINUTES);

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("tlali-back")
				.issuedAt(now)
				.expiresAt(expiresAt)
				.subject(user.getEmail())
				.claim("name", user.getFullName())
				.claim("role", user.getRole().name())
				.claim("scope", "ROLE_" + user.getRole().name())
				.build();
		JwsHeader headers = JwsHeader.with(MacAlgorithm.HS256).build();

		return jwtEncoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();
	}
}
