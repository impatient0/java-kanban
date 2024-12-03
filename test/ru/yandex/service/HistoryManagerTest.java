package ru.yandex.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.model.Task;
import ru.yandex.model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

abstract class HistoryManagerTest<T extends HistoryManager> {

    private HistoryManager historyManager;
    protected final Supplier<? extends T> factory;
    private Task task1;
    private Task task2;
    private Task task3;

    HistoryManagerTest(Supplier<? extends T> factory) {
        this.factory = factory;
    }

    @BeforeEach
    void setUp() {
        historyManager = factory.get();
        task1 = new Task("_t1name_", "_t1desc_", 42, TaskStatus.NEW, Duration.ZERO, LocalDateTime.MIN);
        task2 = new Task("_t2name_", "_t2desc_", 69, TaskStatus.NEW, Duration.ZERO, LocalDateTime.MIN);
        task3 = new Task("_t3name_", "_t3desc_", 1337, TaskStatus.NEW, Duration.ZERO, LocalDateTime.MIN);
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

    @Test
    void shouldReturnEmptyHistory() {
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    void shouldDeleteItemsFromStart() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(task1.getId());
        assertArrayEquals(new Task[]{task2, task3}, historyManager.getHistory().toArray());
    }

    @Test
    void shouldDeleteItemsFromEnd() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(task3.getId());
        assertArrayEquals(new Task[]{task1, task2}, historyManager.getHistory().toArray());
    }

    @Test
    void shouldDeleteItemsFromMiddle() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(task2.getId());
        assertArrayEquals(new Task[]{task1, task3}, historyManager.getHistory().toArray());
    }
}