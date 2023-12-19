package com.portal.ratelimit.service;

import com.portal.ratelimit.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @CacheEvict(value = "cache", allEntries = true)
    @Scheduled(fixedDelayString = "${caching.spring.userListTTL}", initialDelay = 10000)
    public void deleteUserList()  {
        LOG.info("Evict User List");
    }
}
