package com.infyro.ifc.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "farms", indexes = {
        @Index(name = "idx_farm_name", columnList = "farmName"),
        @Index(name = "idx_farm_owner_id", columnList = "owner_id")
}, uniqueConstraints = {
                @UniqueConstraint(name = "uq_owner_farm_name", columnNames = {"owner_id", "farmName"})
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Farm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @Column(nullable = false)
    private String farmName;

    @CreationTimestamp
    @Column(nullable = false,updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            referencedColumnName = "id",
            name = "owner_id",
            nullable = false
    )
    private User owner;

    @Builder.Default
    @OneToMany(mappedBy = "farm",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<Field> fields = new ArrayList<>();
}
