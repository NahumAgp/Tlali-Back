package com.tlali.api.auth;

import com.tlali.api.security.JwtService;
import com.tlali.api.user.AppUser;
import com.tlali.api.user.AppUserService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final AppUserService appUserService;
	private final JwtService jwtService;

	public AuthController(
			AuthenticationManager authenticationManager,
			AppUserService appUserService,
			JwtService jwtService
	) {
		this.authenticationManager = authenticationManager;
		this.appUserService = appUserService;
		this.jwtService = jwtService;
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
				request.email(),
				request.password()
		));

		AppUser user = appUserService.findByEmail(request.email());
		return new AuthResponse(
				jwtService.createToken(user),
				"Bearer",
				CurrentUserResponse.from(user)
		);
	}

	@GetMapping("/me")
	public CurrentUserResponse me(@AuthenticationPrincipal Jwt jwt) {
		AppUser user = appUserService.findByEmail(jwt.getSubject());
		return CurrentUserResponse.from(user);
	}
}
