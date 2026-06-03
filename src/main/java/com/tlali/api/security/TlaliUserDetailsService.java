package com.tlali.api.security;

import com.tlali.api.user.AppUser;
import com.tlali.api.user.AppUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TlaliUserDetailsService implements UserDetailsService {

	private final AppUserRepository repository;

	public TlaliUserDetailsService(AppUserRepository repository) {
		this.repository = repository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) {
		AppUser appUser = repository.findByEmailIgnoreCase(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		return new User(
				appUser.getEmail(),
				appUser.getPasswordHash() == null ? "" : appUser.getPasswordHash(),
				appUser.isEnabled(),
				true,
				true,
				true,
				List.of(new SimpleGrantedAuthority("ROLE_" + appUser.getRole().name()))
		);
	}
}
