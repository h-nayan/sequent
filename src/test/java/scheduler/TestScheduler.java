import main.java.model.Priority;
import main.java.model.Task;
import main.java.scheduler.Scheduler;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SchedulerTest {

    private Task createTask(String id, int priorityIndex, List<String> prerequisiteIds) {
        return new Task(id, "Task " + id, Priority.values()[priorityIndex], 2, LocalDate.parse("2026-10-30"), prerequisiteIds);
    }

    @Test
    void schedulesTasksAfterTheirPrerequisites() {
        Task t1 = createTask("T1", 2, List.of());
        Task t2 = createTask("T2", 2, List.of("T1"));
        Task t4 = createTask("T4", 0, List.of("T1", "T2"));

        Scheduler scheduler = new Scheduler(List.of(t1, t2, t4));

        List<Task> schedule = scheduler.getSchedule();

        assertEquals(3, schedule.size());
        assertEquals("T1", schedule.get(0).getId());
        assertEquals("T2", schedule.get(1).getId());
        assertEquals("T4", schedule.get(2).getId());
    }

    @Test
    void throwsErrorForUnknownPrerequisite() {
        Task t1 = createTask("T1", 2, List.of());
        Task t2 = createTask("T2", 2, List.of("T99"));

        assertThrows(
                IllegalArgumentException.class,
                () -> new Scheduler(List.of(t1, t2))
        );
    }

    @Test
    void throwsErrorForDuplicateTaskId() {
        Task t1 = createTask("T1", 2, List.of());
        Task duplicateT1 = createTask("T1", 1, List.of());

        assertThrows(
                IllegalArgumentException.class,
                () -> new Scheduler(List.of(t1, duplicateT1))
        );
    }

    @Test
    void throwsErrorForCircularDependency() {
        Task t1 = createTask("T1", 2, List.of("T2"));
        Task t2 = createTask("T2", 2, List.of("T1"));

        assertThrows(
                IllegalStateException.class,
                () -> new Scheduler(List.of(t1, t2))
        );
    }
}