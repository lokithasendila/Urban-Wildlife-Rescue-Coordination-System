package com.wildliferescue.model;

// Mammals such as cats, dogs and monkeys have a moderate risk factor.
public class Mammal extends Animal {

    public Mammal(AnimalCondition condition) {
        super(condition);
    }

    @Override
    public String getAnimalType() {
        return "Mammal";
    }

    @Override
    public int getRiskFactor() {
        return 2;
    }

    @Override
    public String getRequiredSkill() {
        return "Mammal Handling";
    }
}