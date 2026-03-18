package com.example.authenticatingldap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.ldap.LdapPasswordComparisonAuthenticationManagerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class WebSecurityConfig {

	@Bean
	public AuthenticationManager authenticationManager(
			BaseLdapPathContextSource contextSource) {
		LdapPasswordComparisonAuthenticationManagerFactory factory =
				new LdapPasswordComparisonAuthenticationManagerFactory(
						contextSource, new BCryptPasswordEncoder());
		factory.setUserDnPatterns("uid={0},ou=people");
		return factory.createAuthenticationManager();
	}

}
