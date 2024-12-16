package ru.yandex.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import ru.yandex.exceptions.TaskNotFoundException;
import ru.yandex.model.Task;
import ru.yandex.model.TaskStatus;

class ManagersTest {

    @Test
    void shouldReturnWorkingTaskManager() {
        TaskManager taskManager = Managers.getDefault();
        Task task = new Task("_tname_", "_tdesc_", Duration.ZERO, LocalDateTime.MIN);
        int taskId = taskManager.addTask(task);
        assertEquals(task, taskManager.getTask(taskId));
        assertTrue(taskManager.getAllTasks().contains(task));
        taskManager.updateTask(
            new Task("_tname_", "_anothertdesc_", taskId, TaskStatus.IN_PROGRESS, Duration.ZERO,
                LocalDateTime.MIN));
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getTask(taskId).getStatus());
        taskManager.removeTask(taskId);
        assertThrows(TaskNotFoundException.class, () -> taskManager.getTask(taskId));
    }

    @Test
    void shouldReturnWorkingHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertTrue(historyManager.getHistory().isEmpty());
        Task task = new Task("_tname_", "_tdesc_", Duration.ZERO, LocalDateTime.MIN);
        historyManager.add(task);
        assertTrue(historyManager.getHistory().contains(task));
    }
}