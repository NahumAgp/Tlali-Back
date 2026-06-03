package com.tlali.api.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppUserService {

	private final AppUserRepository repository;
	private final PasswordEncoder passwordEncoder;

	public AppUserService(AppUserRepository repository, PasswordEncoder passwordEncoder) {
		this.repository = repository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional(readOnly = true)
	public AppUser findByEmail(String email) {
		return repository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
	}

	@Transactional
	public AppUser createGoogleUserIfMissing(String email, String fullName) {
		return repository.findByEmailIgnoreCase(email)
				.orElseGet(() -> repository.save(AppUser.googleUser(email, fullName)));
	}

	@Transactional
	public void ensureSuperAdmin(String email, String rawPassword, String fullName) {
		if (repository.existsByEmailIgnoreCase(email)) {
			return;
		}

		repository.save(AppUser.localSuperAdmin(
				email,
				passwordEncoder.encode(rawPassword),
				fullName
		));
	}

	@Component
	static class SuperAdminBootstrap implements CommandLineRunner {

		private final AppUserService appUserService;
		private final String email;
		private final String password;
		private final String fullName;

		SuperAdminBootstrap(
				AppUserService appUserService,
				@Value("${tlali.security.superadmin.email}") String email,
				@Value("${tlali.security.superadmin.password}") String password,
				@Value("${tlali.security.superadmin.name}") String fullName
		) {
			this.appUserService = appUserService;
			this.email = email;
			this.password = password;
			this.fullName = fullName;
		}

		@Override
		public void run(String... args) {
			appUserService.ensureSuperAdmin(email, password, fullName);
		}
	}
}
