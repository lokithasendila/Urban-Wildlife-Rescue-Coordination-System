package com.wildliferescue.model;

// Birds are fragile and often stuck in high places, so they have a higher risk factor.
public class Bird extends Animal {

    public Bird(AnimalCondition condition) {
        super(condition);
    }

    @Override
    public String getAnimalType() {
        return "Bird";
    }

    @Override
    public int getRiskFactor() {
        return 3;
    }

    @Override
    public String getRequiredSkill() {
        return "Bird Handling";
    }
}