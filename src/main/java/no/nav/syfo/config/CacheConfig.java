package no.nav.syfo.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@Profile({"remote"})
public class CacheConfig {
    public static final String CACHENAME_AKTOER_ID = "aktoerid";
    public static final String CACHENAME_AKTOER_FNR = "aktoerfnr";
    public static final String CACHENAME_ANSATTE = "ansatte";
    public static final String CACHENAME_BEHANDLENDEENHET_FNR = "behandlendeenhetfnr";
    public static final String CACHENAME_DKIF_IDENT = "dkifident";
    public static final String CACHENAME_LEDER = "leder";
    public static final String CACHENAME_ARBEIDSFORHOLD_AT = "arbeidsforholdAT";
    public static final String CACHENAME_TILGANG_TIL_IDENT = "tilgangtilident";
    public static final String CACHENAME_FELLESKODEVERK_BETYDNINGER = "felleskodeverkBetydninger";

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofHours(1L));

        cacheConfigurations.put(CACHENAME_AKTOER_ID, defaultConfig);
        cacheConfigurations.put(CACHENAME_AKTOER_FNR, defaultConfig);
        cacheConfigurations.put(CACHENAME_ANSATTE, defaultConfig);
        cacheConfigurations.put(CACHENAME_BEHANDLENDEENHET_FNR, defaultConfig);
        cacheConfigurations.put(CACHENAME_DKIF_IDENT, defaultConfig);
        cacheConfigurations.put(CACHENAME_LEDER, defaultConfig);
        cacheConfigurations.put(CACHENAME_ARBEIDSFORHOLD_AT, defaultConfig);
        cacheConfigurations.put(CACHENAME_TILGANG_TIL_IDENT, defaultConfig);
        cacheConfigurations.put(CACHENAME_FELLESKODEVERK_BETYDNINGER, defaultConfig);

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig())
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
