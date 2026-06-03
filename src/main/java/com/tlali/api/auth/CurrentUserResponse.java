package com.tlali.api.auth;

import com.tlali.api.user.AppUser;

public record CurrentUserResponse(
		Long id,
		String email,
		String fullName,
		String role
) {
	public static CurrentUserResponse from(AppUser user) {
		return new CurrentUserResponse(
				user.getId(),
				user.getEmail(),
				user.getFullName(),
				user.getRole().name()
		);
	}
}
