package ru.yandex.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.model.Task;
import ru.yandex.model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
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
        LocalDateTime nowDateTime = LocalDateTime.now();
        task1 = new Task("_t1name_", "_t1desc_", 42, TaskStatus.NEW, Duration.ofHours(42), nowDateTime);
        task2 = new Task("_t2name_", "_t2desc_", 69, TaskStatus.NEW, Duration.ofHours(69), nowDateTime);
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

    @Test
    void shouldUpdateRepeatingEntries() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task2);
        historyManager.add(task1);
        assertArrayEquals(new Task[]{task2, task1}, historyManager.getHistory().toArray());
    }
}