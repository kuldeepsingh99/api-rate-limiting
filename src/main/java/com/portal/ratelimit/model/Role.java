package com.portal.ratelimit.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "role")
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @ManyToOne
    private User user;

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", date time='" + new Date() +
                '}';
    }
}
