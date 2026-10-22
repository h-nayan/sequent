package main.java.scheduler;

import main.java.model.Task;

import java.util.*;

public class Scheduler {

    private final Map<String, Task> taskMap;
    private final List<Task> schedule;

    public Scheduler(List<Task> tasks) {
        this.taskMap = new HashMap<>();
        this.schedule = new ArrayList<>();

        indexTasks(tasks);  //Accesses the ID of each task and adds the respective task ID and its task reference as Key-Value pairs into taskMap.
        validateDependencies(tasks);    //Checks whether the task list is valid.
        linkDependencies(tasks);    //Links the prerequisites with its dependents.
        scheduleTasks(tasks);   //Uses already built-in graph to produce the actual task order.
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

    public List<Task> getSchedule() {
        return schedule;
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

    private void scheduleTasks(List<Task> tasks) {
        Queue<Task> readyTasks = new PriorityQueue<>(Comparator.comparing(Task::getPriority).reversed().
                thenComparing(Task::getDeadline, Comparator.nullsLast(Comparator.naturalOrder())).
                thenComparing(Task::getId));

        for (Task task : tasks) {
            if (task.getPendingPrerequisites() == 0) {
                readyTasks.add(task);
            }
        }

        while (!readyTasks.isEmpty()) {
            Task currentTask = readyTasks.poll();
            schedule.add(currentTask);

            for (Task dependent : currentTask.getDependents()) {
                dependent.decrementPendingPrerequisites();

                if (dependent.getPendingPrerequisites() == 0) {
                    readyTasks.add(dependent);
                }
            }
        }

        if (schedule.size() != tasks.size()) {
            Set<String> scheduledTaskIds = new HashSet<>();

            for (Task task : schedule) {
                scheduledTaskIds.add(task.getId());
            }

            List<String> unresolvedTaskIds = new ArrayList<>();

            for (Task task : tasks) {
                if (!scheduledTaskIds.contains(task.getId())) {
                    unresolvedTaskIds.add(task.getId());
                }
            }

            throw new IllegalStateException("Circular dependency detected. "+unresolvedTaskIds.size()+" of "+tasks.size() + " tasks cannot be scheduled: "+ unresolvedTaskIds);
        }
    }
}