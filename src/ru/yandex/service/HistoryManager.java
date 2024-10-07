package ru.yandex.service;

import ru.yandex.model.Task;

import java.util.ArrayList;

public interface HistoryManager {
    Task add(Task task);

    ArrayList<Integer> getHistory();
}
