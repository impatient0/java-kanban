package ru.yandex.service;

import org.junit.jupiter.api.Test;
import ru.yandex.model.Task;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class InMemoryHistoryManagerTest {

    private static final TaskManager taskManager = new InMemoryTaskManager();
    private static final Task task1 = new Task("_t1name_", "_t1desc_");
    private static final Task task2 = new Task("_t2name_", "_t2desc_");

    @Test
    void shouldStoreUpTo10ItemsInHistory() {
        int firstTaskId = taskManager.addTask(task1);
        int secondTaskId = taskManager.addTask(task2);
        taskManager.getTask(firstTaskId);
        for (int i = 0; i < 10; i++) {
            taskManager.getTask(secondTaskId);
        }
        ArrayList<Task> history = taskManager.getHistory();
        assertArrayEquals(new Task[]{task2, task2, task2, task2, task2, task2, task2, task2, task2, task2},
                history.toArray());
    }
}