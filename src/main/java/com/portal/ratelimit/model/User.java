package com.portal.ratelimit.model;


import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;
import java.io.Serial;
import java.util.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username")
})
public class User implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "activities")
    @OneToMany(mappedBy = "user",cascade = CascadeType.REMOVE)
    private List<Activity> activities;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id")
    private Set<Role> roles = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public boolean hasRole(String roleName) {
        for (Role role : roles
        ) {
            if (role.getName().equals(roleName)) {
                return true;
            }
        }
        return false;
    }

    public Long getRoleId(String roleName) {
        for (Role role : roles
        ) {
            if (role.getName().equals(roleName)) {
                return role.getId();
            }
        }
        return -1L;
    }

    public void addRole(Role role) {
        roles.add(role);
    }

    public void removeRole(Long roleId) {
        Role roleForDelete = null;
        for (Role role : roles) {
            if (role.getId().equals(roleId)) {
                roleForDelete = role;
                break;
            }
        }
        if (roleForDelete != null) {
            roles.remove(roleForDelete);
        }
    }

}