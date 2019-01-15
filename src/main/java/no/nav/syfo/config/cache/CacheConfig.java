package no.nav.syfo.config.cache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static net.sf.ehcache.config.PersistenceConfiguration.Strategy.NONE;
import static net.sf.ehcache.store.MemoryStoreEvictionPolicy.LRU;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    private static final int CACHE_DEFAULT_MAKS_ELEMENTER = 1000;
    private static final int CACHE_DEFAULT_LEVETID_MS = 3600;

    @Bean
    public CacheManager ehCacheManager() {
        net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration();
        config.addCache(setupCache("aktoer", 5000));
        config.addCache(setupCache("arbeidsforhold", 5000));
        config.addCache(setupCache("dkif", CACHE_DEFAULT_MAKS_ELEMENTER));
        config.addCache(setupCache("ereg", CACHE_DEFAULT_MAKS_ELEMENTER));
        config.addCache(setupCache("syfo", CACHE_DEFAULT_MAKS_ELEMENTER));
        config.addCache(setupCache("tilgang", CACHE_DEFAULT_MAKS_ELEMENTER));
        config.addCache(setupCache("tps", CACHE_DEFAULT_MAKS_ELEMENTER));
        return CacheManager.newInstance(config);
    }

    @Bean
    public UserKeyGenerator userkeygenerator() {
        return new UserKeyGenerator();
    }

    @Override
    public org.springframework.cache.CacheManager cacheManager() {
        return new EhCacheCacheManager(ehCacheManager());
    }

    @Override
    public CacheResolver cacheResolver() {
        return null;
    }

    @Bean
    public KeyGenerator keyGenerator() {
        return new SimpleKeyGenerator();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return null;
    }

    private static CacheConfiguration setupCache(String name, int maksElementer) {
        return new CacheConfiguration(name, maksElementer)
                .memoryStoreEvictionPolicy(LRU)
                .timeToIdleSeconds(CACHE_DEFAULT_LEVETID_MS)
                .timeToLiveSeconds(CACHE_DEFAULT_LEVETID_MS)
                .persistence(new PersistenceConfiguration().strategy(NONE));
    }
}
