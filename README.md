# Urban Wildlife Rescue Coordination System

**Module:** CM2601 – Object Oriented Development
**Student:** Lokitha -2522838
**Academic Year:** 2026/2027, Semester 1

## Overview

A Java console application that helps a wildlife rescue organisation log
incoming incident reports, automatically assess their priority, and match
each incident to the most suitable available rescue team.

## Project Structure

```
UrbanWildlifeRescueSystem/
├── src/com/wildliferescue/
│   ├── Main.java                          # Application entry point
│   ├── model/                             # Domain/entity classes
│   │   ├── Animal.java                    # Abstract base class
│   │   ├── Bird.java                      # extends Animal
│   │   ├── Mammal.java                    # extends Animal
│   │   ├── Reptile.java                   # extends Animal
│   │   ├── RescueIncident.java
│   │   ├── RescueTeam.java
│   │   └── Equipment.java
│   ├── service/                           # Business logic
│   │   ├── PriorityCalculator.java
│   │   ├── TeamMatcher.java
│   │   └── IncidentManager.java
│   ├── io/                                # File handling
│   │   └── FileManager.java
│   ├── exception/                         # Custom exceptions
│   │   └── InvalidAnimalTypeException.java
│   └── coordinator/
│       └── RescueCoordinator.java
├── data/                                  # CSV data files
│   ├── incidents.csv
│   └── teams.csv
├── docs/uml/                              # UML diagram exports go here
├── .gitignore
└── README.md
```

## How to Compile & Run

From the project root:

```bash
# Compile
javac -d bin $(find src -name "*.java")

# Run
java -cp bin com.wildliferescue.Main
```

## Project Status

- [x] Project skeleton + package structure
- [ ] UML design (use case, class, activity, sequence diagrams)
- [ ] Core class implementation (Part 2)
- [ ] Priority calculation & team matching (Part 3)
- [ ] File handling & exception handling (Part 4)
- [ ] Concurrency (Part 5)
- [ ] Integration & testing (Part 6)
- [ ] Report (Part 7)

## Design Notes

Class diagram maps directly onto the package structure above. See `docs/uml/`
for exported diagrams as they are completed.
