package com.example.authenticatingldap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.ldap.EmbeddedLdapServerContextSourceFactoryBean;
import org.springframework.security.config.ldap.LdapPasswordComparisonAuthenticationManagerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfig {

	@Bean
	public EmbeddedLdapServerContextSourceFactoryBean contextSourceFactoryBean() {
		EmbeddedLdapServerContextSourceFactoryBean contextSourceFactoryBean =
				EmbeddedLdapServerContextSourceFactoryBean.fromEmbeddedLdapServer();
		contextSourceFactoryBean.setPort(8389);
		return contextSourceFactoryBean;
	}

	@Bean
	public LdapAuthoritiesPopulator authorities(BaseLdapPathContextSource contextSource) {
		DefaultLdapAuthoritiesPopulator authorities =
				new DefaultLdapAuthoritiesPopulator(contextSource, "ou=groups");
		authorities.setGroupSearchFilter("uniqueMember={0}");
		return authorities;
	}

	@Bean
	public AuthenticationManager authenticationManager(
			BaseLdapPathContextSource contextSource,
			LdapAuthoritiesPopulator authorities) {
		LdapPasswordComparisonAuthenticationManagerFactory factory =
				new LdapPasswordComparisonAuthenticationManagerFactory(
						contextSource, new BCryptPasswordEncoder());
		factory.setUserDnPatterns("uid={0},ou=people");
		factory.setPasswordAttribute("userPassword");
		factory.setLdapAuthoritiesPopulator(authorities);
		return factory.createAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests((authorize) -> authorize
				.anyRequest().fullyAuthenticated()
			)
			.formLogin(Customizer.withDefaults());

		return http.build();
	}

}
