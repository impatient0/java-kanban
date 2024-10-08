package ru.yandex.service;

import org.junit.jupiter.api.Test;
import ru.yandex.model.Task;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class InMemoryHistoryManagerTest {

    private static final HistoryManager historyManager = new InMemoryHistoryManager();
    private static final Task task1 = new Task("_t1name_", "_t1desc_");
    private static final Task task2 = new Task("_t2name_", "_t2desc_");

    @Test
    void shouldStoreUpTo10ItemsInHistory() {
        historyManager.add(task1);
        for (int i = 0; i < 10; i++) {
            historyManager.add(task2);
        }
        ArrayList<Task> history = historyManager.getHistory();
        assertArrayEquals(new Task[]{task2, task2, task2, task2, task2, task2, task2, task2, task2, task2},
                history.toArray());
    }
}