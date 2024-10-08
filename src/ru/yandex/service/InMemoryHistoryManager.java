package ru.yandex.service;

import ru.yandex.model.Task;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    private final ArrayList<Task> history = new ArrayList<>();
    private final static int MAX_HISTORY_ELEMENTS = 10;

    @Override
    public Task add(Task task) {
        if (history.size() >= MAX_HISTORY_ELEMENTS) {
            history.removeFirst();
        }
        history.add(task);
        return task;
    }

    @Override
    public ArrayList<Task> getHistory() {
        return history;
    }
}
