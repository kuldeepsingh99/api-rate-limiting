package com.portal.ratelimit.service;


import com.portal.ratelimit.model.Role;
import com.portal.ratelimit.model.User;
import com.portal.ratelimit.repository.RoleRepository;
import com.portal.ratelimit.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;


    public void addRoleToUser(User user, String role) {
        if (!user.hasRole(role)){
            Role newRole = Role.builder().name(role).build();
            roleRepository.save(newRole);
            user.addRole(newRole);
            userRepository.save(user);
        }


    }

    public void removeRoleFromUser(User user, String role) {
        Long id = user.getRoleId(role);
        if (id != -1L){
            user.removeRole(id);
            roleRepository.deleteById(id);
            userRepository.save(user);
        }
    }

}
