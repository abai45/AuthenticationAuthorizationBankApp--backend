package kz.group.reactAndSpring.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class FilterChainCinfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request
                                .requestMatchers("/user/login").permitAll()
                                .anyRequest().authenticated()
                ).build();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService) {
        var myAuthenticationProvider = new MyAuthenticationProvider(userDetailsService);
        return new ProviderManager(myAuthenticationProvider);
    }

//    @Bean
//    public UserDetailsService userDetailsService() { //Default Authentication
//        var user = User.withDefaultPasswordEncoder()
//                .username("user")
//                .password("{noop}12345")
//                .roles("USER")
//                .build();
//        var adil = User.withDefaultPasswordEncoder()
//                .username("adil")
//                .password("{noop}12345")
//                .roles("USER")
//                .build();
//        return new InMemoryUserDetailsManager(List.of(user, adil));
//    }

    @Bean
    public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
        return new InMemoryUserDetailsManager(
                User.withUsername("adil").password("12345").roles("USER").build(),
                User.withUsername("user").password("12345").roles("USER").build()
        );
    }

}
