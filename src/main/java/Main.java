package main.java;

import main.java.model.Task;
import main.java.parser.CSVParser;
import main.java.parser.InvalidCSVRowException;
import main.java.scheduler.Scheduler;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            CSVParser parser = new CSVParser();
            List<Task> tasks = parser.parse("src/task.csv");

            System.out.println("\n" + tasks.size() + " tasks loaded successfully.\n");

            for (Task task : tasks) {
                System.out.println(task);
            }
            System.out.println();

            Scheduler scheduler = new Scheduler(tasks);

            System.out.println("Dependency graph:");

            for (Task task : tasks) {
                System.out.println(task.getId() + " | prerequisites: " + task.getPrerequisiteIds().size());

                System.out.print("Dependents: ");

                List<Task> dependents = scheduler.getDependents(task.getId());

                if (dependents.isEmpty()) {
                    System.out.println("None\n");
                } else {
                    for (Task dependent : dependents) {
                        System.out.print(dependent.getId() + " ");
                    }

                    System.out.println("\n");
                }
            }

                Task selectedTask = scheduler.getTaskById("T2");

                if (selectedTask != null) {
                    System.out.println("\nFound task: " + selectedTask.getId() + " | " + selectedTask.getTitle() + "\n");
                } else {
                    System.out.println("\nTask not found.\n");
                }

                System.out.println("Scheduled Task Order:");

                for (Task task : scheduler.getSchedule()) {
                    System.out.println(task.getId() + " | " + task.getTitle());
                }

            } catch(InvalidCSVRowException exception){
                System.err.println("CSV error: " + exception.getMessage());
            } catch(IOException exception){
                System.err.println("Could not read the file: " + exception.getMessage());
            } catch(IllegalArgumentException exception){
                System.err.println("Graph error: " + exception.getMessage());
            } catch(IllegalStateException exception){
                System.err.println("Scheduling error: " + exception.getMessage());
            }
        }
    }