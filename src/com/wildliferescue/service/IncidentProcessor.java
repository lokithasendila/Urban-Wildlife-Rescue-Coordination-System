package com.wildliferescue.service;

import com.wildliferescue.exception.ProcessingException;
import com.wildliferescue.exception.RescueSystemException;
import com.wildliferescue.model.Animal;
import com.wildliferescue.model.IncidentReport;
import com.wildliferescue.model.Reporter;
import com.wildliferescue.model.RescueIncident;
import com.wildliferescue.model.RescueTeam;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

//uses a pool of worker threads to process many incidents at the same time:
//many incoming rescue reports at once
//searching for teams for all pending incidents at once
public class IncidentProcessor {

    private final IncidentManager manager;
    private final int threadCount;

    public IncidentProcessor(IncidentManager manager, int threadCount) {
        if (manager == null) {
            throw new IllegalArgumentException("IncidentManager is required.");
        }
        if (threadCount < 1) {
            throw new IllegalArgumentException("Thread count must be at least 1.");
        }
        this.manager = manager;
        this.threadCount = threadCount;
    }

    //processes all reports at the same time. Returns one result line per report,
    //in the same order as the reports were given.
    public List<String> processReports(List<IncidentReport> reports) throws ProcessingException {
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        try {
            //hand every report to the pool. submit() returns at once with a Future,
            //we can use later to collect the result.
            List<Future<String>> tickets = new ArrayList<>();
            for (int i = 0; i < reports.size(); i++) {
                IncidentReport report = reports.get(i);
                int number = i + 1;
                tickets.add(pool.submit(() -> processOne(number, report)));
            }

            //collect the results. get() waits until that report has been processed.
            List<String> results = new ArrayList<>();
            for (Future<String> ticket : tickets) {
                results.add(ticket.get());
            }
            return results;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProcessingException("Report processing was interrupted.", e);
        } catch (ExecutionException e) {
            throw new ProcessingException("A report could not be processed: " + e.getCause(), e.getCause());
        } finally {
            pool.shutdown();   //always stop the worker threads
        }
    }

    //runs on a worker thread: checks ONE report and records it.
    //errors are returned as a message, so one bad report does not stop the others.
    private String processOne(int number, IncidentReport report) {
        try {
            Animal animal = Animal.create(report.getAnimalType(), report.getCondition());
            Reporter reporter = new Reporter(report.getReporterName(), report.getReporterContact(),
                    report.getReporterEmail());

            RescueIncident incident = manager.reportIncident(animal, report.getLocation(),
                    report.isHighRiskLocation(), report.getNumberOfAnimals(), report.isImmediateDanger(),
                    reporter, report.getRequiredEquipment());

            int teamsFound = manager.getSuggestedTeams(incident.getIncidentId()).size();
            return "Report " + number + " -> " + incident.getIncidentId() + " | " + incident.getAnimal()
                    + " | priority " + incident.getPriority() + " | suitable teams: " + teamsFound
                    + "  [" + Thread.currentThread().getName() + "]";

        } catch (RescueSystemException e) {
            return "Report " + number + " REJECTED: " + e.getMessage()
                    + "  [" + Thread.currentThread().getName() + "]";
        }
    }

    //searches for suitable teams for every pending incident at the same time.
    //returns incident ID suitable teams.
    public Map<String, List<RescueTeam>> findTeamsForAllPending() throws RescueSystemException {
        List<RescueIncident> pending = manager.getPendingIncidents();   //most urgent first
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        try {
            Map<String, Future<List<RescueTeam>>> tickets = new LinkedHashMap<>();
            for (RescueIncident incident : pending) {
                String id = incident.getIncidentId();
                tickets.put(id, pool.submit(() -> manager.findTeams(id)));
            }

            Map<String, List<RescueTeam>> results = new LinkedHashMap<>();
            for (Map.Entry<String, Future<List<RescueTeam>>> entry : tickets.entrySet()) {
                results.put(entry.getKey(), entry.getValue().get());
            }
            return results;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProcessingException("Team search was interrupted.", e);
        } catch (ExecutionException e) {
            //if the task threw one of our own exceptions, pass it on unchanged
            if (e.getCause() instanceof RescueSystemException) {
                throw (RescueSystemException) e.getCause();
            }
            throw new ProcessingException("Team search failed: " + e.getCause(), e.getCause());
        } finally {
            pool.shutdown();
        }
    }
}