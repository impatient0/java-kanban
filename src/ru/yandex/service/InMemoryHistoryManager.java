package ru.yandex.service;

import ru.yandex.model.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InMemoryHistoryManager implements HistoryManager {
    private final ArrayList<Task> history = new ArrayList<>();
    private final static int MAX_HISTORY_ELEMENTS = 10;

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        if (history.size() >= MAX_HISTORY_ELEMENTS) {
            history.removeFirst();
        }
        history.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return history.stream().map(t -> {
            try {
                return t.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toCollection(ArrayList::new)); // возвращает deep copy поля history
    }
}
