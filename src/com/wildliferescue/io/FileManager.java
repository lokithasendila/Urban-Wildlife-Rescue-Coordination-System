package com.wildliferescue.io;

import com.wildliferescue.exception.FileOperationException;
import com.wildliferescue.exception.InvalidCsvDataException;
import com.wildliferescue.exception.RescueSystemException;
import com.wildliferescue.model.Animal;
import com.wildliferescue.model.AnimalCondition;
import com.wildliferescue.model.Equipment;
import com.wildliferescue.model.IncidentStatus;
import com.wildliferescue.model.PriorityLevel;
import com.wildliferescue.model.Reporter;
import com.wildliferescue.model.RescueIncident;
import com.wildliferescue.model.RescueTeam;
import com.wildliferescue.model.TeamMember;
import com.wildliferescue.model.TeamStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//loads and saves teams and incidents as CSV files, and writes the rescue report.
//lists inside one CSV field are separated by.
public class FileManager {

    public static final String TEAM_HEADER =
            "teamId,members,animalCategories,skills,equipment,status,maxIncidents,currentIncidents";
    public static final String INCIDENT_HEADER =
            "incidentId,animalType,condition,location,highRiskLocation,numberOfAnimals,immediateDanger,"
            + "requiredEquipment,reporterName,reporterContact,reporterEmail,priorityScore,priority,"
            + "status,assignedTeamId";

    //rows that could not be loaded are skipped and described here
    private List<String> warnings = new ArrayList<>();

    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    //loading

    public List<RescueTeam> loadTeams(String path) throws FileOperationException, InvalidCsvDataException {
        warnings.clear();
        List<String> lines = readAllLines(path);
        checkHeader(lines, TEAM_HEADER, path);

        List<RescueTeam> teams = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).isBlank()) {
                continue;
            }
            try {
                teams.add(parseTeam(lines.get(i)));
            } catch (RescueSystemException | IllegalArgumentException e) {
                warnings.add("Line " + (i + 1) + " skipped: " + e.getMessage());
            }
        }
        return teams;
    }

    //the teams are needed so an assigned incident can be linked back to its team
    public List<RescueIncident> loadIncidents(String path, List<RescueTeam> teams)
            throws FileOperationException, InvalidCsvDataException {
        warnings.clear();
        List<String> lines = readAllLines(path);
        checkHeader(lines, INCIDENT_HEADER, path);

        List<RescueIncident> incidents = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).isBlank()) {
                continue;
            }
            try {
                incidents.add(parseIncident(lines.get(i), teams));
            } catch (RescueSystemException | IllegalArgumentException e) {
                warnings.add("Line " + (i + 1) + " skipped: " + e.getMessage());
            }
        }
        return incidents;
    }

    private RescueTeam parseTeam(String line) throws RescueSystemException {
        List<String> f = parseCsvLine(line);
        if (f.size() != 8) {
            throw new InvalidCsvDataException("expected 8 fields but found " + f.size());
        }

        RescueTeam team = new RescueTeam(f.get(0), parseInt(f.get(6), "maxIncidents"));

        for (String m : splitList(f.get(1))) {
            String[] p = m.split(":");
            if (p.length != 3) {
                throw new InvalidCsvDataException("invalid member '" + m + "' (use id:name:phone)");
            }
            team.addMember(new TeamMember(p[0].trim(), p[1].trim(), p[2].trim()));
        }
        for (String category : splitList(f.get(2))) {
            team.addAnimalCategory(category);
        }
        for (String skill : splitList(f.get(3))) {
            team.addSkill(skill);
        }
        for (String item : splitList(f.get(4))) {
            team.addEquipment(parseEquipment(item));
        }

        team.restoreState(parseEnum(TeamStatus.class, f.get(5), "status"),
                parseInt(f.get(7), "currentIncidents"));
        return team;
    }

    private RescueIncident parseIncident(String line, List<RescueTeam> teams) throws RescueSystemException {
        List<String> f = parseCsvLine(line);
        if (f.size() != 15) {
            throw new InvalidCsvDataException("expected 15 fields but found " + f.size());
        }

        Animal animal = Animal.create(f.get(1), parseEnum(AnimalCondition.class, f.get(2), "condition"));
        Reporter reporter = new Reporter(f.get(8), f.get(9), f.get(10));

        RescueIncident incident = new RescueIncident(f.get(0), animal, f.get(3),
                parseBoolean(f.get(4), "highRiskLocation"), parseInt(f.get(5), "numberOfAnimals"),
                parseBoolean(f.get(6), "immediateDanger"), reporter);

        for (String item : splitList(f.get(7))) {
            incident.addRequiredEquipment(parseEquipment(item));
        }

        PriorityLevel level = f.get(12).isBlank() ? null : parseEnum(PriorityLevel.class, f.get(12), "priority");
        int score = f.get(11).isBlank() ? 0 : parseInt(f.get(11), "priorityScore");

        // Find the assigned team (if any)
        RescueTeam team = null;
        String teamId = f.get(14).trim();
        if (!teamId.isEmpty()) {
            for (RescueTeam t : teams) {
                if (t.getTeamId().equals(teamId)) {
                    team = t;
                }
            }
            if (team == null) {
                throw new InvalidCsvDataException("assigned team '" + teamId + "' does not exist");
            }
        }

        incident.restoreState(score, level, parseEnum(IncidentStatus.class, f.get(13), "status"), team);
        return incident;
    }

    //saving

    public void saveTeams(String path, List<RescueTeam> teams) throws FileOperationException {
        List<String> lines = new ArrayList<>();
        lines.add(TEAM_HEADER);

        for (RescueTeam t : teams) {
            List<String> members = new ArrayList<>();
            for (TeamMember m : t.getMembers()) {
                members.add(m.getMemberId() + ":" + m.getName() + ":" + m.getContactNumber());
            }
            lines.add(String.join(",",
                    escape(t.getTeamId()),
                    escape(String.join(";", members)),
                    escape(String.join(";", t.getAnimalCategories())),
                    escape(String.join(";", t.getSkills())),
                    escape(equipmentToText(t.getEquipment())),
                    t.getStatus().name(),
                    String.valueOf(t.getMaxIncidents()),
                    String.valueOf(t.getCurrentIncidents())));
        }
        writeAllLines(path, lines);
    }

    public void saveIncidents(String path, List<RescueIncident> incidents) throws FileOperationException {
        List<String> lines = new ArrayList<>();
        lines.add(INCIDENT_HEADER);

        for (RescueIncident i : incidents) {
            lines.add(String.join(",",
                    escape(i.getIncidentId()),
                    escape(i.getAnimal().getAnimalType()),
                    i.getAnimal().getCondition().name(),
                    escape(i.getLocation()),
                    String.valueOf(i.isHighRiskLocation()),
                    String.valueOf(i.getNumberOfAnimals()),
                    String.valueOf(i.isImmediateDanger()),
                    escape(equipmentToText(i.getRequiredEquipment())),
                    escape(i.getReporter().getName()),
                    escape(i.getReporter().getContactNumber()),
                    escape(i.getReporter().getEmail() == null ? "" : i.getReporter().getEmail()),
                    String.valueOf(i.getPriorityScore()),
                    i.getPriority() == null ? "" : i.getPriority().name(),
                    i.getStatus().name(),
                    i.getAssignedTeam() == null ? "" : escape(i.getAssignedTeam().getTeamId())));
        }
        writeAllLines(path, lines);
    }

    //text report of completed and pending incidents
    public void generateReport(String path, List<RescueIncident> incidents) throws FileOperationException {
        List<RescueIncident> completed = new ArrayList<>();
        List<RescueIncident> pending = new ArrayList<>();
        int inProgress = 0;

        for (RescueIncident i : incidents) {
            if (i.getStatus() == IncidentStatus.COMPLETED) {
                completed.add(i);
            } else if (i.getStatus() == IncidentStatus.PENDING_ASSIGNMENT) {
                pending.add(i);
            } else {
                inProgress++;
            }
        }

        List<String> lines = new ArrayList<>();
        lines.add("URBAN WILDLIFE RESCUE - INCIDENT REPORT");
        lines.add("========================================");
        lines.add("Total incidents: " + incidents.size() + " | Completed: " + completed.size()
                + " | Pending: " + pending.size() + " | Assigned / in progress: " + inProgress);
        lines.add("");
        lines.add("COMPLETED INCIDENTS (" + completed.size() + ")");
        addReportLines(lines, completed);
        lines.add("");
        lines.add("PENDING INCIDENTS (" + pending.size() + ")");
        addReportLines(lines, pending);

        writeAllLines(path, lines);
    }

    private void addReportLines(List<String> lines, List<RescueIncident> list) {
        if (list.isEmpty()) {
            lines.add("  None");
            return;
        }
        for (RescueIncident i : list) {
            lines.add("  " + i.getIncidentId() + " | " + i.getAnimal() + " x" + i.getNumberOfAnimals()
                    + " | " + i.getLocation() + " | priority: " + i.getPriority()
                    + " | team: " + (i.getAssignedTeam() == null ? "None" : i.getAssignedTeam().getTeamId()));
        }
    }

    //file helpers

    private List<String> readAllLines(String path) throws FileOperationException {
        Path file = Path.of(path);
        if (!Files.exists(file)) {
            throw new FileOperationException("File not found: " + path, new IOException("missing file"));
        }
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FileOperationException("Could not read file '" + path + "'", e);
        }
    }

    private void writeAllLines(String path, List<String> lines) throws FileOperationException {
        Path file = Path.of(path);
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());   // creates the folder if it is missing
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FileOperationException("Could not write file '" + path + "'", e);
        }
    }

    private void checkHeader(List<String> lines, String expected, String path) throws InvalidCsvDataException {
        if (lines.isEmpty()) {
            throw new InvalidCsvDataException("File is empty: " + path);
        }
        String header = lines.get(0).replace("\uFEFF", "").trim();   //hidden marker some editors add
        if (!header.equals(expected)) {
            throw new InvalidCsvDataException("Wrong header in " + path + ". Expected: " + expected);
        }
    }

    //CSV

    //splits one CSV line into fields
    private static List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');   //nside quotes means one quote character
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                fields.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString().trim());
        return fields;
    }

    //wraps a value in quotes if it contains a comma
    private static String escape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static List<String> splitList(String text) {
        List<String> items = new ArrayList<>();
        for (String part : text.split(";")) {
            if (!part.isBlank()) {
                items.add(part.trim());
            }
        }
        return items;
    }


    private static Equipment parseEquipment(String text) {
        int colon = text.indexOf(':');
        if (colon < 0) {
            return new Equipment(text);
        }
        return new Equipment(text.substring(0, colon), text.substring(colon + 1));
    }

    private static String equipmentToText(List<Equipment> items) {
        List<String> parts = new ArrayList<>();
        for (Equipment e : items) {
            parts.add(e.getEquipmentId().equals("N/A") ? e.getName() : e.getEquipmentId() + ":" + e.getName());
        }
        return String.join(";", parts);
    }

    private static int parseInt(String text, String field) throws InvalidCsvDataException {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new InvalidCsvDataException(field + " must be a whole number (got '" + text + "')");
        }
    }

    private static boolean parseBoolean(String text, String field) throws InvalidCsvDataException {
        if (text.trim().equalsIgnoreCase("true")) {
            return true;
        }
        if (text.trim().equalsIgnoreCase("false")) {
            return false;
        }
        throw new InvalidCsvDataException(field + " must be true or false (got '" + text + "')");
    }

    //works for any enum turns text like
    private static <E extends Enum<E>> E parseEnum(Class<E> type, String text, String field)
            throws InvalidCsvDataException {
        try {
            return Enum.valueOf(type, text.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidCsvDataException(field + " has an invalid value '" + text + "'");
        }
    }
}