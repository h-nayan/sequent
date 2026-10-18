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

            Task selectedTask = scheduler.getTaskById("T2");

            if (selectedTask != null) {
                System.out.println("\nTask lookup successful: " + selectedTask.getId() + " | " + selectedTask.getTitle());
            } else {
                System.out.println("\nTask not found.");
            }

        } catch (InvalidCSVRowException exception) {
            System.err.println("CSV error: " + exception.getMessage());
        } catch (IOException exception) {
            System.err.println("Could not read the file: " + exception.getMessage());
        } catch (IllegalArgumentException exception) {
            System.err.println("Scheduling error: " + exception.getMessage());
        }
    }
}