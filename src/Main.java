import model.Task;
import parser.CSVParser;
import parser.InvalidCSVRowException;
import scheduler.Scheduler;

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

            Scheduler scheduler = new Scheduler(tasks);

            System.out.println("Dependency graph:");

            for (Task task : tasks) {
                System.out.println(task.getId()+" | pending prerequisites: "+task.getPendingPrerequisites());

                System.out.print("Dependents: ");

                List<Task> dependents = task.getDependents();

                if (dependents.isEmpty()) {
                    System.out.println("None\n");
                } else {
                    for (Task dependent : task.getDependents()) {
                        System.out.print(dependent.getId() + " ");
                    }

                    System.out.println("\n");
                }
            }

            Task selectedTask = scheduler.getTaskById("T2");

            if (selectedTask != null) {
                System.out.println("\nFound task: " + selectedTask.getId() + " | " + selectedTask.getTitle());
            } else {
                System.out.println("\nTask not found.");
            }

        } catch (InvalidCSVRowException exception) {
            System.err.println("CSV error: " + exception.getMessage());
        } catch (IOException exception) {
            System.err.println("Could not read the file: " + exception.getMessage());
        } catch (IllegalArgumentException exception) {
            System.err.println(
                    "Graph error: " + exception.getMessage()
            );
        }
    }
}