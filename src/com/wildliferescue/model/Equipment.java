package com.wildliferescue.model;

// A piece of rescue equipment, e.g. "Ladder" or "Animal Carrier".
public class Equipment {

    private String equipmentId;
    private String name;

    // Constructor 1: with an ID
    public Equipment(String equipmentId, String name) {
        this.equipmentId = equipmentId;
        this.name = name.trim();
    }

    // Constructor 2: name only (constructor overloading)
    public Equipment(String name) {
        this("N/A", name);
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public String getName() {
        return name;
    }

    // Two Equipment objects are equal if their names match (ignoring capital letters)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Equipment)) {
            return false;
        }
        Equipment other = (Equipment) obj;
        return name.equalsIgnoreCase(other.name);
    }

    // Must be overridden together with equals()
    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}