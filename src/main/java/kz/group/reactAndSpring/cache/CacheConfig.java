package kz.group.reactAndSpring.cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {
//    @Bean(name = {"userLoginCache"})
    @Bean
    public CacheStore<String, Integer> userCache() {
        return new CacheStore<>(900, TimeUnit.SECONDS);
    }

//    @Bean(name = {"registrationCache"})
//    public CacheStore<String, Integer> registrationCacheStore() {
//        return new CacheStore<>(900, TimeUnit.SECONDS);
//    }
}
