package scheduler;

import model.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scheduler {

    private final Map<String, Task> taskMap;

    public Scheduler(List<Task> tasks) {
        this.taskMap = new HashMap<>();

        indexTasks(tasks);
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