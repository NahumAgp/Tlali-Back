package com.tlali.api.security;

import com.tlali.api.user.AppUser;
import com.tlali.api.user.AppUserService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TlaliUserDetailsService implements UserDetailsService {

	private final AppUserService appUserService;

	public TlaliUserDetailsService(AppUserService appUserService) {
		this.appUserService = appUserService;
	}

	@Override
	public UserDetails loadUserByUsername(String username) {
		AppUser appUser;
		try {
			appUser = appUserService.findByEmail(username);
		} catch (IllegalArgumentException exception) {
			throw new UsernameNotFoundException("User not found");
		}

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
