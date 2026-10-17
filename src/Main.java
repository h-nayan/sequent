import model.Task;
import parser.CSVParser;
import parser.InvalidCSVRowException;

import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        String filePath = "src/task.csv";

        try {
            CSVParser parser = new CSVParser();
            List<Task> tasks = parser.parse(filePath);

            System.out.println("Loaded " + tasks.size() + " tasks successfully.");
            System.out.println();

            for (Task task : tasks) {
                System.out.println(task);
            }
        } catch (InvalidCSVRowException exception) {
            System.err.println("CSV error: " + exception.getMessage());
        } catch (IOException exception) {
            System.err.println("Could not read file: " + exception.getMessage());
        }
    }
}