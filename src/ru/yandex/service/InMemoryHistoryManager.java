package ru.yandex.service;

import ru.yandex.model.Task;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    private final ArrayList<Integer> ids = new ArrayList<>();
    private final static int MAX_HISTORY_ELEMENTS = 10;

    @Override
    public Task add(Task task) {
        if (ids.size() >= MAX_HISTORY_ELEMENTS) {
            ids.removeFirst();
        }
        ids.add(task.getId());
        return task;
    }

    @Override
    public ArrayList<Integer> getHistory() {
        return ids;
    }
}
