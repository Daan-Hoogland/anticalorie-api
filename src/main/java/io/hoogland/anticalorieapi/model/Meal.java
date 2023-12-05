package io.hoogland.anticalorieapi.model;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "meal")
public class Meal implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private LocalDateTime actualDateTime;

    private LocalDateTime plannedDateTime;

    private Boolean binge;

    private MealType type;

    @OneToMany
    private List<FoodEntry> foodEntries;
}
