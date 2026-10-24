package main.java.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Task {
    private final String id;
    private final String title;
    private final Priority priority;
    private final int duration;
    private final LocalDate deadline;
    private final List<String> prerequisiteIds;

    public Task(String id, String title, Priority priority, int duration, LocalDate deadline, List<String> prerequisiteIds) {
        this.id = id;
        this.title = title;
        this.priority = priority;
        this.duration = duration;
        this.deadline = deadline;
        this.prerequisiteIds = new ArrayList<>(prerequisiteIds);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Priority getPriority() {
        return priority;
    }

    public int getDuration() {
        return duration;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public List<String> getPrerequisiteIds() {
        return prerequisiteIds;
    }

    public boolean hasDeadline() {
        return deadline != null;
    }

    @Override
    public String toString() {
        String deadlineText;
        if(hasDeadline()) {
            deadlineText = deadline.toString();
        } else {
            deadlineText = "No Deadline";
        }

        return id + " | " + title + " | " + priority + " | " + duration + " hours | " + deadlineText;
    }
}