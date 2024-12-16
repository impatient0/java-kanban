package ru.yandex.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import ru.yandex.model.Task;
import ru.yandex.util.TaskList;

public class InMemoryHistoryManager implements HistoryManager {

    private final TaskList history = new TaskList();

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        history.remove(task.getId());
        history.addLast(task);
    }

    @Override
    public void remove(int id) {
        history.remove(id);
    }

    @Override
    public List<Task> getHistory() {
        return history.toList().stream().map(t -> {
            try {
                return t.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toCollection(ArrayList::new)); // возвращает deep copy поля history
    }
}
