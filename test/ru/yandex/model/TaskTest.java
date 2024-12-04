package ru.yandex.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskTest {

    private static Task task;
    private static LocalDateTime nowDateTime;

    @BeforeEach
    void setUp() {
        nowDateTime = LocalDateTime.now();
        task = new Task("_tname_", "_tdesc_", 42, TaskStatus.NEW, Duration.ofHours(42), nowDateTime);
    }

    @Test
    void shouldConvertTaskToString() {
        String s = "Задача #00000042 \"_tname_\": _tdesc_ Статус: NEW";
        assertEquals(s, task.toString());
    }

    @Test
    void shouldTreatTasksWithSameIdAsEqual() {
        Task anotherTask = new Task("_anothertname_", "_anothertdesc_", 42, TaskStatus.IN_PROGRESS,
                Duration.ofHours(69), nowDateTime);
        assertEquals(task, anotherTask);
    }

    @Test
    void shouldNotCreateSharedReferencesWhenCloning() throws CloneNotSupportedException {
        Task clonedTask = task.clone();
        clonedTask.setId(69);
        assertEquals(42, task.getId());
        assertEquals(69, clonedTask.getId());
        task.setDescription("_anothertdesc_");
        assertEquals("_anothertdesc_", task.getDescription());
        assertEquals("_tdesc_", clonedTask.getDescription());
        clonedTask.setStartTime(nowDateTime.plusNanos(1337));
        assertEquals(nowDateTime, task.getStartTime());
        assertEquals(nowDateTime.plusNanos(1337), clonedTask.getStartTime());
        task.setDuration(Duration.ofHours(69));
        assertEquals(Duration.ofHours(69), task.getDuration());
        assertEquals(Duration.ofHours(42), clonedTask.getDuration());
    }

    @Test
    void shouldReturnCorrectEndTime() {
        assertEquals(nowDateTime.plus(task.getDuration()), task.getEndTime());
    }
}