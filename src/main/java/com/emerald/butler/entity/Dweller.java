package com.emerald.butler.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Dweller")
public class Dweller {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;
}
