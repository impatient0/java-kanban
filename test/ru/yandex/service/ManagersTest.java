package ru.yandex.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ManagersTest {

    private static TaskManager taskManager;

    @BeforeEach
    void createManager() {
        taskManager = Managers.getDefault();
    }

    @Test
    void shouldHaveEmptyTasks() {
        assertTrue(taskManager.getAllTasks().isEmpty());
    }

    @Test
    void shouldHaveEmptyEpics() {
        assertTrue(taskManager.getAllEpics().isEmpty());
    }

    @Test
    void shouldHaveEmptySubtasks() {
        assertTrue(taskManager.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldHaveEmptyHistory() {
        assertTrue(taskManager.getHistory().isEmpty());
    }
}