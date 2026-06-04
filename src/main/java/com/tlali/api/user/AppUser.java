package com.tlali.api.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "app_users")
public class AppUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 160)
	private String email;

	@Column(name = "password_hash")
	private String passwordHash;

	@Column(name = "full_name", nullable = false, length = 120)
	private String fullName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private AppRole role;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private AuthProvider provider;

	@Column(nullable = false)
	private boolean enabled;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected AppUser() {
	}

	private AppUser(
			String email,
			String passwordHash,
			String fullName,
			AppRole role,
			AuthProvider provider
	) {
		this.email = email;
		this.passwordHash = passwordHash;
		this.fullName = fullName;
		this.role = role;
		this.provider = provider;
		this.enabled = true;
		this.createdAt = Instant.now();
	}

	public static AppUser localSuperAdmin(String email, String passwordHash, String fullName) {
		return new AppUser(email, passwordHash, fullName, AppRole.SUPER_ADMIN, AuthProvider.LOCAL);
	}

	public static AppUser googleUser(String email, String fullName) {
		return new AppUser(email, null, fullName, AppRole.USER, AuthProvider.GOOGLE);
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
