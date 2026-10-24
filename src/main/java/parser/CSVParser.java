package main.java.parser;

import main.java.model.Priority;
import main.java.model.Task;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class CSVParser {

    private static final int EXPECTED_COLUMNS = 6;

    public List<Task> parse(String filepath) throws IOException {   //The parse method opens the CSV file and calls the parseRow method for each line and eventually returns tasks.
        List<Task> tasks = new ArrayList<>();

        try(BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            String line;
            int linenumber = 0;

            while((line = reader.readLine()) != null) {
                linenumber++;

                if (linenumber == 1) {
                    continue;
                }

                if (line.isBlank()) {
                    continue;
                }

                tasks.add(parseRow(line, linenumber));
            }
        }

        return tasks;
    }

    private Task parseRow(String line, int linenumber) {    //The parseRow method splits each details of a Task, converts each detail into its necessary type and passes these into the Task constructor thereby creating a Task.

        String[] columns = line.split(",", -1);

        if (columns.length != EXPECTED_COLUMNS) {
            throw new InvalidCSVRowException("Line "+linenumber+": "+"Expected "+EXPECTED_COLUMNS+" columns but found "+columns.length+" columns.");
        }

        String id = columns[0].trim();
        String title = columns[1].trim();
        String taskPriority = columns[2].trim();
        String taskDuration = columns[3].trim();
        String taskDeadline = columns[4].trim();
        String taskPrerequesites = columns[5].trim();

        if (id.isBlank()) {
            throw new InvalidCSVRowException("Line "+linenumber+": Task ID cannot be empty.");
        }

        if (title.isBlank()) {
            throw new InvalidCSVRowException("Line "+linenumber+": Task Title cannot be empty.");
        }

        Priority priority = parsePriority(taskPriority,linenumber);
        int duration = parseDuration(taskDuration,linenumber);
        LocalDate deadline = parseDeadline(taskDeadline,linenumber);
        List<String> prerequesiteIds = parsePrerequesites(taskPrerequesites,linenumber);

        return new Task(id, title, priority, duration, deadline, prerequesiteIds);
    }

    private Priority parsePriority(String taskPriority, int linenumber) {
        try {
            return Priority.valueOf(taskPriority.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidCSVRowException("Line "+linenumber+": Invalid Priority \""+taskPriority+"\".\nExpected priority: LOW, MEDIUM or HIGH.");
        }
    }

    private int parseDuration(String taskDuration, int linenumber) {
        try {
            int duration = Integer.parseInt(taskDuration);

            if(duration <= 0) {
                throw new InvalidCSVRowException("Line "+linenumber+": Duration hours must be greater than zero.");
            }

            return duration;
        } catch (NumberFormatException e) {
            throw new InvalidCSVRowException("Line "+linenumber+": Invalid Duration \""+taskDuration+"\".\nExpected Duration: A positive natural number." );
        }
    }

    private LocalDate parseDeadline(String taskDeadline, int linenumber) {
        try {

            if(taskDeadline.isBlank()) {
                return null;
            }

            return LocalDate.parse(taskDeadline);
        } catch (DateTimeParseException e) {
            throw new InvalidCSVRowException("Line "+linenumber+": Invalid Deadline \""+taskDeadline+"\".\nExpected Deadline Format: YYYY-MM-DD.");
        }
    }

    private List<String> parsePrerequesites(String taskPrerequesites, int linenumber) {

        List<String> prerequesiteIds = new ArrayList<>();

        if (taskPrerequesites.isBlank()) {
            return prerequesiteIds;
        }

        String[] prerequesites = taskPrerequesites.split("\\|");

        for (String prerequsite : prerequesites) {
            String prerequesiteId = prerequsite.trim();

            if (prerequesiteId.isBlank()) {
                throw new InvalidCSVRowException("Line "+linenumber+": A Prerequesite ID cannot be empty.");
            }

            prerequesiteIds.add(prerequesiteId);
        }
        return prerequesiteIds;
    }
}