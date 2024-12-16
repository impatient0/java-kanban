package ru.yandex.service;

import java.util.List;
import ru.yandex.model.Task;

public interface HistoryManager {

    void add(Task task);

    void remove(int id);

    List<Task> getHistory();
}
