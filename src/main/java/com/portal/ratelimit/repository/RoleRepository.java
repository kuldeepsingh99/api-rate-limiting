package com.portal.ratelimit.repository;


import com.portal.ratelimit.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {
    List<Role> findByName(String roleName);
}
