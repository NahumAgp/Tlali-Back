package com.tlali.api.auth;

public record AuthResponse(
		String token,
		String tokenType,
		CurrentUserResponse user
) {
}
