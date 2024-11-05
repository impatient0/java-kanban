package ru.yandex.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.model.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;
    private Task task1;
    private Task task2;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
        task1 = new Task("_t1name_", "_t1desc_");
        task2 = new Task("_t2name_", "_t2desc_");
    }

    @Test
    void shouldStoreItemsInHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        assertArrayEquals(new Task[]{task1, task2}, historyManager.getHistory().toArray());
    }

    @Test
    void shouldNotCreateSharedReferenceWhenReturningHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        List<Task> history = historyManager.getHistory();
        history.get(1).setName("_anothertname_");
        assertEquals("_t2name_", historyManager.getHistory().get(1).getName());
        assertEquals("_anothertname_", history.get(1).getName());
    }
}