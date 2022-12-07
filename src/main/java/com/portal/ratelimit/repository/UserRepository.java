package com.portal.ratelimit.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.portal.ratelimit.model.User;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {

	Optional<User> findById(Integer userId);
}
