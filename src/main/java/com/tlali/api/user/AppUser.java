package com.tlali.api.user;

import java.time.Instant;

public class AppUser {

	private Long id;

	private String email;

	private String passwordHash;

	private String fullName;

	private AppRole role;

	private AuthProvider provider;

	private boolean enabled;

	private Instant createdAt;

	public AppUser() {
	}

	private AppUser(
			Long id,
			String email,
			String passwordHash,
			String fullName,
			AppRole role,
			AuthProvider provider
	) {
		this.id = id;
		this.email = email;
		this.passwordHash = passwordHash;
		this.fullName = fullName;
		this.role = role;
		this.provider = provider;
		this.enabled = true;
		this.createdAt = Instant.now();
	}

	public static AppUser localSuperAdmin(Long id, String email, String passwordHash, String fullName) {
		return new AppUser(id, email, passwordHash, fullName, AppRole.SUPER_ADMIN, AuthProvider.LOCAL);
	}

	public static AppUser googleUser(Long id, String email, String fullName) {
		return new AppUser(id, email, null, fullName, AppRole.USER, AuthProvider.GOOGLE);
	}

	public void syncLocalSuperAdmin(String passwordHash, String fullName) {
		this.passwordHash = passwordHash;
		this.fullName = fullName;
		this.role = AppRole.SUPER_ADMIN;
		this.provider = AuthProvider.LOCAL;
		this.enabled = true;
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public String getFullName() {
		return fullName;
	}

	public AppRole getRole() {
		return role;
	}

	public AuthProvider getProvider() {
		return provider;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
