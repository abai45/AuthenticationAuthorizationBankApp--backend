package kz.group.reactAndSpring;

import jakarta.transaction.Transactional;
import kz.group.reactAndSpring.domain.RequestContext;
import kz.group.reactAndSpring.entity.RoleEntity;
import kz.group.reactAndSpring.enumeration.AuthorityEnum;
import kz.group.reactAndSpring.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
@EnableAsync
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	@Bean
	@Transactional
	CommandLineRunner commandLineRunner(RoleRepository roleRepository) {
		return args -> {
			RequestContext.setUserId(0L);
			if (roleRepository.findByNameIgnoreCase(AuthorityEnum.USER.name()).isEmpty()) {
				var userRole = new RoleEntity();
				userRole.setName(AuthorityEnum.USER.name());
				userRole.setAuthorities(AuthorityEnum.USER);
				roleRepository.save(userRole);
			}
			if (roleRepository.findByNameIgnoreCase(AuthorityEnum.ADMIN.name()).isEmpty()) {
				var adminRole = new RoleEntity();
				adminRole.setName(AuthorityEnum.ADMIN.name());
				adminRole.setAuthorities(AuthorityEnum.ADMIN);
				roleRepository.save(adminRole);
			}
			RequestContext.start();
		};
	}
}
