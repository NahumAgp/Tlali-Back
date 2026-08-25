package com.tlali.api.user;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AppUserService {

	private final Map<String, AppUser> usersByEmail = new ConcurrentHashMap<>();
	private final AtomicLong ids = new AtomicLong(1);
	private final PasswordEncoder passwordEncoder;
	private final String superAdminEmail;
	private final String superAdminPassword;
	private final String superAdminName;

	public AppUserService(
			PasswordEncoder passwordEncoder,
			@Value("${tlali.security.superadmin.email}") String superAdminEmail,
			@Value("${tlali.security.superadmin.password}") String superAdminPassword,
			@Value("${tlali.security.superadmin.name}") String superAdminName
	) {
		this.passwordEncoder = passwordEncoder;
		this.superAdminEmail = superAdminEmail;
		this.superAdminPassword = superAdminPassword;
		this.superAdminName = superAdminName;
	}

	@PostConstruct
	void bootstrapSuperAdmin() {
		ensureSuperAdmin(superAdminEmail, superAdminPassword, superAdminName);
	}

	public AppUser findByEmail(String email) {
		AppUser user = usersByEmail.get(normalizeEmail(email));
		if (user == null) {
			throw new IllegalArgumentException("User not found");
		}
		return user;
	}

	public AppUser createGoogleUserIfMissing(String email, String fullName) {
		return usersByEmail.computeIfAbsent(
				normalizeEmail(email),
				key -> AppUser.googleUser(ids.getAndIncrement(), email, fullName)
		);
	}

	public void ensureSuperAdmin(String email, String rawPassword, String fullName) {
		String passwordHash = passwordEncoder.encode(rawPassword);
		usersByEmail.compute(
				normalizeEmail(email),
				(key, existingUser) -> {
					if (existingUser == null) {
						return AppUser.localSuperAdmin(ids.getAndIncrement(), email, passwordHash, fullName);
					}
					existingUser.syncLocalSuperAdmin(passwordHash, fullName);
					return existingUser;
				}
		);
	}

	private String normalizeEmail(String email) {
		return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
	}
}
