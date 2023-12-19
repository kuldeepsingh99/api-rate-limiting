package com.portal.ratelimit.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="activity")
public class Activity {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "user_id")
    private User user;

}
