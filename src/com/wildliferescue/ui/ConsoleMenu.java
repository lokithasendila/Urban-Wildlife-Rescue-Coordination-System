package com.wildliferescue.ui;

import com.wildliferescue.exception.DuplicateIncidentException;
import com.wildliferescue.exception.FileOperationException;
import com.wildliferescue.exception.InvalidCsvDataException;
import com.wildliferescue.exception.InvalidIncidentDataException;
import com.wildliferescue.exception.InvalidTeamException;
import com.wildliferescue.exception.RescueSystemException;
import com.wildliferescue.io.FileManager;
import com.wildliferescue.model.Animal;
import com.wildliferescue.model.AnimalCondition;
import com.wildliferescue.model.Equipment;
import com.wildliferescue.model.IncidentReport;
import com.wildliferescue.model.IncidentStatus;
import com.wildliferescue.model.Reporter;
import com.wildliferescue.model.RescueIncident;
import com.wildliferescue.model.RescueTeam;
import com.wildliferescue.model.TeamMember;
import com.wildliferescue.service.IncidentManager;
import com.wildliferescue.service.IncidentProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

//the console user interface
//data is loaded when the program starts and saved after every change.
public class ConsoleMenu {

    private static final String TEAMS_FILE = "data/teams.csv";
    private static final String INCIDENTS_FILE = "data/incidents.csv";
    private static final String INCOMING_FILE = "data/incoming_reports.csv";
    private static final String REPORT_FILE = "data/rescue_report.txt";
    private static final int WORKER_THREADS = 4;

    private final IncidentManager manager;
    private final FileManager fileManager;
    private final IncidentProcessor processor;
    private final InputHelper input;

    public ConsoleMenu(Scanner scanner) {
        this.manager = new IncidentManager();
        this.fileManager = new FileManager();
        this.processor = new IncidentProcessor(manager, WORKER_THREADS);
        this.input = new InputHelper(scanner);
    }

    //main loop

    public void start() {
        System.out.println("=========================================");
        System.out.println("   URBAN WILDLIFE RESCUE COORDINATION");
        System.out.println("=========================================");
        loadData();

        try {
            boolean running = true;
            while (running) {
                printMenu();
                int choice = input.readInt("Choose an option (0-10): ", 0, 10);
                try {
                    switch (choice) {
                        case 1 -> reportIncident();
                        case 2 -> viewAllIncidents();
                        case 3 -> viewPendingIncidents();
                        case 4 -> registerTeam();
                        case 5 -> searchTeams();
                        case 6 -> assignTeam();
                        case 7 -> updateStatus();
                        case 8 -> processBatchReports();
                        case 9 -> searchTeamsForAllPending();
                        case 10 -> generateReport();
                        case 0 -> running = false;
                    }
                } catch (RescueSystemException e) {
                    //any error not handled inside an option ends up here this show it and go back to the menu
                    System.out.println("ERROR: " + e.getMessage());
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("\nInput ended.");   //the user pressed
        }

        saveAll();
        System.out.println("Goodbye.");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("----------------- MENU -----------------");
        System.out.println(" 1. Report an incident");
        System.out.println(" 2. View all incidents");
        System.out.println(" 3. View pending incidents (most urgent first)");
        System.out.println(" 4. Register a rescue team");
        System.out.println(" 5. Search suitable teams for an incident");
        System.out.println(" 6. Assign a rescue team");
        System.out.println(" 7. Update incident status");
        System.out.println(" 8. Process a file of incoming reports (multi-threaded)");
        System.out.println(" 9. Search teams for ALL pending incidents (multi-threaded)");
        System.out.println("10. Generate rescue report");
        System.out.println(" 0. Save and exit");
        System.out.println("----------------------------------------");
    }

    //report insident part

    private void reportIncident() throws RescueSystemException {
        System.out.println("\n--- Report an incident ---");
        String type = input.readChoice("Animal type (Bird / Mammal / Reptile): ", "Bird", "Mammal", "Reptile");
        AnimalCondition condition = readCondition();
        Animal animal = Animal.create(type, condition);

        String location = input.readText("Location: ");
        boolean highRisk = input.readYesNo("Is it a high-risk location, e.g. a main road or rooftop? (y/n): ");
        int count = input.readInt("Number of animals (1-" + RescueIncident.MAX_ANIMALS + "): ",
                1, RescueIncident.MAX_ANIMALS);
        boolean danger = input.readYesNo("Is the animal in immediate danger? (y/n): ");

        List<Equipment> equipment = new ArrayList<>();
        for (String name : input.readList("Required equipment, separated by commas (Enter for none): ")) {
            equipment.add(new Equipment(name));
        }
        Reporter reporter = readReporter();

        RescueIncident incident = manager.reportIncident(animal, location, highRisk, count, danger,
                reporter, equipment);

        System.out.println("\nIncident " + incident.getIncidentId() + " recorded as Pending Assignment.");
        System.out.println("Priority: " + incident.getPriority() + " (score " + incident.getPriorityScore() + ")");
        System.out.println("Suitable teams right now: " + teamIds(manager.getSuggestedTeams(incident.getIncidentId())));
        saveAll();
    }

    private AnimalCondition readCondition() {
        AnimalCondition[] conditions = AnimalCondition.values();
        StringBuilder prompt = new StringBuilder("Condition (");
        for (int i = 0; i < conditions.length; i++) {
            prompt.append(i + 1).append(" ").append(conditions[i]).append(i < conditions.length - 1 ? ", " : "): ");
        }
        return conditions[input.readInt(prompt.toString(), 1, conditions.length) - 1];
    }

    //keeps asking until the Reporter object accepts the details
    private Reporter readReporter() {
        while (true) {
            String name = input.readText("Your name: ");
            String contact = input.readText("Contact number (10 digits, e.g. 0771234567): ");
            String email = input.readOptionalText("Email (Enter to skip): ");
            try {
                return new Reporter(name, contact, email);
            } catch (InvalidIncidentDataException e) {
                System.out.println("  " + e.getMessage() + " Please enter your details again.");
            }
        }
    }

    //view incidents

    private void viewAllIncidents() {
        System.out.println("\n--- All incidents ---");
        printIncidents(manager.getAllIncidents());
    }

    private void viewPendingIncidents() throws RescueSystemException {
        System.out.println("\n--- Pending incidents (most urgent first) ---");
        printIncidents(manager.getPendingIncidents());
    }

    private void printIncidents(List<RescueIncident> list) {
        if (list.isEmpty()) {
            System.out.println("No incidents to show.");
            return;
        }
        for (RescueIncident incident : list) {
            System.out.println(incident);
        }
    }

    //reg team

    private void registerTeam() throws RescueSystemException {
        System.out.println("\n--- Register a rescue team ---");
        String teamId = input.readText("Team ID (e.g. T005): ").toUpperCase();
        for (RescueTeam existing : manager.getAllTeams()) {
            if (existing.getTeamId().equals(teamId)) {
                System.out.println("ERROR: Team ID " + teamId + " already exists.");
                return;
            }
        }
        int max = input.readInt("Maximum incidents at the same time (1-10): ", 1, 10);
        RescueTeam team = new RescueTeam(teamId, max);

        //each animal cate need a matching class
        boolean any = false;
        while (!any) {
            for (String type : new String[] {"Bird", "Mammal", "Reptile"}) {
                if (input.readYesNo("Can the team rescue " + type + "s? (y/n): ")) {
                    team.addAnimalCategory(type);
                    team.addSkill(type + " Handling");
                    any = true;
                }
            }
            if (!any) {
                System.out.println("  A team must handle at least one animal type.");
            }
        }
        for (String skill : input.readList("Other skills, separated by commas (Enter for none): ")) {
            team.addSkill(skill);
        }
        for (String name : input.readList("Equipment, separated by commas (Enter for none): ")) {
            team.addEquipment(new Equipment(name));
        }

        int memberNumber = 1;
        while (true) {
            String name = input.readOptionalText("Member " + memberNumber + " name (Enter to finish): ");
            if (name.isEmpty()) {
                break;
            }
            String phone = input.readPhone("Member " + memberNumber + " contact number: ");
            team.addMember(new TeamMember(teamId + "-M" + memberNumber, name, phone));
            memberNumber++;
        }

        manager.registerTeam(team);
        System.out.println("Team registered: " + team);
        saveAll();
    }

    //search teams

    private void searchTeams() throws RescueSystemException {
        System.out.println("\n--- Search suitable teams ---");
        String id = input.readText("Incident ID: ").toUpperCase();
        printTeamOptions(manager.findTeams(id), id);
    }

    private void printTeamOptions(List<RescueTeam> teams, String incidentId) {
        if (teams.isEmpty()) {
            System.out.println("No suitable team is available. " + incidentId + " stays Pending Assignment.");
            return;
        }
        System.out.println("Suitable teams (best first):");
        for (RescueTeam team : teams) {
            System.out.println("  " + team.getTeamId() + " - free capacity " + team.getRemainingCapacity()
                    + " - " + team.getStatus());
        }
    }

    //assign teams

    private void assignTeam() throws RescueSystemException {
        System.out.println("\n--- Assign a rescue team ---");
        List<RescueIncident> pending = manager.getPendingIncidents();
        if (pending.isEmpty()) {
            System.out.println("There are no pending incidents.");
            return;
        }
        printIncidents(pending);

        //Select an incident
        RescueIncident incident = null;
        while (incident == null) {
            String id = input.readText("Incident ID (0 to cancel): ").toUpperCase();
            if (id.equals("0")) {
                return;
            }
            try {
                RescueIncident found = manager.getIncident(id);
                if (found.isPending()) {
                    incident = found;
                } else {
                    System.out.println("  " + id + " is already " + found.getStatus() + ".");
                }
            } catch (InvalidIncidentDataException e) {
                System.out.println("  " + e.getMessage());
            }
        }

        List<RescueTeam> teams = manager.findTeams(incident.getIncidentId());
        printTeamOptions(teams, incident.getIncidentId());
        if (teams.isEmpty()) {
            return;   //alternate flow
        }

        //choose a team
        while (true) {
            String teamId = input.readText("Team ID to assign (0 to cancel): ").toUpperCase();
            if (teamId.equals("0")) {
                return;
            }
            try {
                manager.assignTeam(incident.getIncidentId(), teamId);
                System.out.println("Team " + teamId + " assigned to " + incident.getIncidentId() + ".");
                saveAll();
                return;
            } catch (RescueSystemException e) {
                System.out.println("  Rejected: " + e.getMessage());
            }
        }
    }

    //update status

    private void updateStatus() {
        System.out.println("\n--- Update incident status ---");
        List<RescueIncident> active = new ArrayList<>();
        for (RescueIncident incident : manager.getAllIncidents()) {
            if (incident.getStatus() == IncidentStatus.ASSIGNED
                    || incident.getStatus() == IncidentStatus.IN_PROGRESS) {
                active.add(incident);
            }
        }
        if (active.isEmpty()) {
            System.out.println("There are no assigned incidents to update.");
            return;
        }
        printIncidents(active);

        while (true) {
            String id = input.readText("Incident ID (0 to cancel): ").toUpperCase();
            if (id.equals("0")) {
                return;
            }
            int choice = input.readInt("New status (1 IN_PROGRESS, 2 COMPLETED): ", 1, 2);
            IncidentStatus status = (choice == 1) ? IncidentStatus.IN_PROGRESS : IncidentStatus.COMPLETED;
            try {
                manager.updateStatus(id, status);
                RescueIncident incident = manager.getIncident(id);
                System.out.println(id + " is now " + status + ".");
                if (status == IncidentStatus.COMPLETED) {
                    System.out.println("Team " + incident.getAssignedTeam().getTeamId() + " workload is now "
                            + incident.getAssignedTeam().getCurrentIncidents() + "/"
                            + incident.getAssignedTeam().getMaxIncidents() + ".");
                }
                saveAll();
                return;
            } catch (InvalidIncidentDataException e) {
                System.out.println("  " + e.getMessage());
            }
        }
    }

    //the batch of the reoirts

    private void processBatchReports() throws RescueSystemException {
        System.out.println("\n--- Process incoming reports ---");
        String path = input.readOptionalText("Reports file (Enter for " + INCOMING_FILE + "): ");
        if (path.isEmpty()) {
            path = INCOMING_FILE;
        }
        List<IncidentReport> reports = fileManager.loadIncomingReports(path);
        printWarnings();
        if (reports.isEmpty()) {
            System.out.println("No reports to process.");
            return;
        }

        long startTime = System.currentTimeMillis();
        List<String> results = processor.processReports(reports);
        long time = System.currentTimeMillis() - startTime;

        for (String line : results) {
            System.out.println(line);
        }
        System.out.println("Processed " + reports.size() + " reports with " + WORKER_THREADS
                + " threads in " + time + " ms.");
        saveAll();
    }

    //serch for all pendings

    private void searchTeamsForAllPending() throws RescueSystemException {
        System.out.println("\n--- Suitable teams for all pending incidents ---");
        Map<String, List<RescueTeam>> results = processor.findTeamsForAllPending();
        if (results.isEmpty()) {
            System.out.println("There are no pending incidents.");
            return;
        }
        for (Map.Entry<String, List<RescueTeam>> entry : results.entrySet()) {
            String teams = entry.getValue().isEmpty() ? "none - stays pending" : teamIds(entry.getValue());
            System.out.println(entry.getKey() + " -> " + teams);
        }
    }

    //reporting part

    private void generateReport() throws RescueSystemException {
        fileManager.generateReport(REPORT_FILE, manager.getAllIncidents());
        System.out.println("\nReport saved to " + REPORT_FILE + ":\n");
        try {
            for (String line : Files.readAllLines(Path.of(REPORT_FILE))) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("(The report was saved but could not be displayed: " + e.getMessage() + ")");
        }
    }

    //loading and the saving part

    private void loadData() {
        List<RescueTeam> teams = new ArrayList<>();
        try {
            teams = fileManager.loadTeams(TEAMS_FILE);
            printWarnings();
            for (RescueTeam team : teams) {
                try {
                    manager.registerTeam(team);
                } catch (InvalidTeamException e) {
                    System.out.println("WARNING: " + e.getMessage());
                }
            }
        } catch (FileOperationException | InvalidCsvDataException e) {
            System.out.println("WARNING: " + e.getMessage() + " Starting with no teams.");
        }

        try {
            List<RescueIncident> incidents = fileManager.loadIncidents(INCIDENTS_FILE, teams);
            printWarnings();
            for (RescueIncident incident : incidents) {
                try {
                    manager.addIncident(incident);
                } catch (DuplicateIncidentException e) {
                    System.out.println("WARNING: " + e.getMessage() + " Skipped.");
                }
            }
        } catch (FileOperationException | InvalidCsvDataException e) {
            System.out.println("WARNING: " + e.getMessage() + " Starting with no incidents.");
        }

        System.out.println("Loaded " + manager.getAllTeams().size() + " teams and "
                + manager.getAllIncidents().size() + " incidents.");
    }

    //called after every change
    private void saveAll() {
        try {
            fileManager.saveTeams(TEAMS_FILE, manager.getAllTeams());
            fileManager.saveIncidents(INCIDENTS_FILE, manager.getAllIncidents());
            System.out.println("(Saved to data folder)");
        } catch (FileOperationException e) {
            System.out.println("WARNING: " + e.getMessage() + ". Your changes are kept until you exit.");
        }
    }

    private void printWarnings() {
        for (String warning : fileManager.getWarnings()) {
            System.out.println("WARNING: " + warning);
        }
    }

    private String teamIds(List<RescueTeam> teams) {
        if (teams.isEmpty()) {
            return "none";
        }
        List<String> ids = new ArrayList<>();
        for (RescueTeam team : teams) {
            ids.add(team.getTeamId());
        }
        return String.join(", ", ids);
    }
}