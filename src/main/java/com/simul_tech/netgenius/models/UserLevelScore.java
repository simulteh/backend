package com.simul_tech.netgenius.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Check;

/** Фиксированные уровни игры 0–10; не зависят от ID и порядка лабораторных LMS. */
@Entity
@Getter
@Setter
@Table(name = "user_level_scores", uniqueConstraints =
        @UniqueConstraint(name = "uk_user_level_score", columnNames = {"user_id", "level_number"}))
@Check(constraints = "level_number between 0 and 10 and best_score between 0 and 10")
public class UserLevelScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "level_number", nullable = false)
    private Integer level;

    @Column(name = "best_score", nullable = false)
    private Integer bestScore;
}
