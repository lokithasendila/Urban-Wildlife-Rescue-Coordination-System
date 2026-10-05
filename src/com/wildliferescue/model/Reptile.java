package com.wildliferescue.model;

// Reptiles such as snakes and monitor lizards need specialist handling skills.
public class Reptile extends Animal {

    public Reptile(AnimalCondition condition) {
        super(condition);
    }

    @Override
    public String getAnimalType() {
        return "Reptile";
    }

    @Override
    public int getRiskFactor() {
        return 2;
    }

    @Override
    public String getRequiredSkill() {
        return "Reptile Handling";
    }
}