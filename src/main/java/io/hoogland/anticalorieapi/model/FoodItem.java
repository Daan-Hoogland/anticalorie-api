package io.hoogland.anticalorieapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
@Entity
@Table(name = "food_item", uniqueConstraints = { @UniqueConstraint(columnNames = {"user_id", "name"})})
public class FoodItem implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 50)
    @NotBlank
    private String name;

    @ManyToOne
    @JoinColumn(name = "user_id", updatable = false)
    private User user;
}
