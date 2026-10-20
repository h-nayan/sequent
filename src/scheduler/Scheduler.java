package scheduler;

import model.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

public class Scheduler {

    private final Map<String, Task> taskMap;

    public Scheduler(List<Task> tasks) {
        this.taskMap = new HashMap<>();

        indexTasks(tasks);  //Accesses the ID of each task and adds the respective task ID and its task reference as Key-Value pairs into taskMap.
        validateDependencies(tasks);
        linkDependencies(tasks);
    }

    private void indexTasks(List<Task> tasks) {
        for (Task task : tasks) {
            String id = task.getId();

            if (taskMap.containsKey(id)) {
                throw new IllegalArgumentException(
                        "Duplicate Task ID: " + id
                );
            }

            taskMap.put(id, task);
        }
    }

    public Task getTaskById(String id) {
        return taskMap.get(id);
    }

    private void validateDependencies(List<Task> tasks) {
        for (Task task : tasks) {
            Set<String> seenPrerequisites = new HashSet<>();

            for (String prerequisiteId : task.getPrerequisiteIds()) {
                if (prerequisiteId.equals(task.getId())) {
                    throw new IllegalArgumentException("Task "+task.getId()+" cannot depend on itself.");
                }

                if (!taskMap.containsKey(prerequisiteId)) {
                    throw new IllegalArgumentException("Task "+task.getId()+" references unknown prerequisite: "+prerequisiteId);
                }

                boolean added = seenPrerequisites.add(prerequisiteId);

                if (!added) {
                    throw new IllegalArgumentException("Task "+task.getId()+" lists prerequisite "+prerequisiteId + " more than once.");
                }
            }
        }
    }

    private void linkDependencies(List<Task> tasks) {
        for (Task task : tasks) {
            for (String prerequisiteId : task.getPrerequisiteIds()) {
                Task prerequisite = taskMap.get(prerequisiteId);

                if (prerequisite == null) {
                    throw new IllegalArgumentException("Task "+task.getId()+" references unknown prerequisite: "+prerequisiteId);
                }

                prerequisite.addDependent(task);
                task.incrementPendingPrerequisites();
            }
        }
    }
}