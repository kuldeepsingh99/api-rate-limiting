package com.portal.ratelimit.config;

import javax.cache.CacheManager;
import javax.cache.Caching;

import org.redisson.config.Config;
import org.redisson.jcache.configuration.RedissonConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.grid.jcache.JCacheProxyManager;

@Configuration
public class RedisConfig {

	@Bean
    public Config config() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://localhost:6379");
        return config;
    }
    
    @Bean(name="springCM")
    public CacheManager cacheManager(Config config) {
        CacheManager manager = Caching.getCachingProvider().getCacheManager();
        manager.createCache("cache", RedissonConfiguration.fromConfig(config));
        manager.createCache("userList", RedissonConfiguration.fromConfig(config));
        return manager;
    }

    @Bean
    ProxyManager<String> proxyManager(CacheManager cacheManager) {
        return new JCacheProxyManager<>(cacheManager.getCache("cache"));
    }
}
