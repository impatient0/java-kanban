package ru.yandex.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    private static Task task;

    @BeforeEach
    void setUp() {
        task = new Task("_tname_", "_tdesc_", 42, TaskStatus.NEW);
    }

    @Test
    void shouldConvertTaskToString() {
        String s = "Задача #00000042 \"_tname_\": _tdesc_ Статус: NEW";
        assertEquals(s, task.toString());
    }

    @Test
    void shouldTreatTasksWithSameIdAsEqual() {
        Task anotherTask = new Task("_anothertname_", "_anothertdesc_", 42, TaskStatus.IN_PROGRESS);
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
    }
}