package com.wildliferescue.model;

import java.lang.annotation.AnnotationTypeMismatchException;

import com.wildliferescue.exception.InvalidAnimalTypeException;

//Abstartct the parent class for all animals
//cant create animal directly
public abstract class Animal {

    //Encapsulation to use this other classes must use getters and setters
    private AnimalCondition condition;

    //constructor
    public Animal(AnimalCondition condition){
        this.condition = condition;
    }

    //Getter
    public AnimalCondition getCondition(){
        return condition;
    }

    //setter
    public void setCondition(AnimalCondition condition){
        this.condition = condition;
    }

    //Abstartct methods msut writes own 
    public abstract String getAnimalType();

    public abstract int getRiskFactor();

    public abstract String getRequiredSkill();

    //factory method to creat the correct animal obj form text - bird
    public static Animal create(String type, AnimalCondition condition) throws InvalidAnimalTypeException{

        if(type == null || type.isBlank()){
            throw new InvalidAnimalTypeException("Animal Type Cannot be Empty.");
        }
        
        switch (type.trim().toLowerCase()){
            case "bird":
                return new Bird(condition);
            case "mammal":
                return new Mammal(condition);
            case "reptile":
                return new Reptile(condition);
            default:
                throw new InvalidAnimalTypeException(
                        "Invalid animal type: '" + type + "'. Use Bird, Mammal or Reptile.");
        }
    }

    // Controls how an Animal is printed
    @Override
    public String toString() {
        return getAnimalType() + " (" + condition + ")";
    }
}
