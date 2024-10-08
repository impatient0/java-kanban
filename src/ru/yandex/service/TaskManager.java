package ru.yandex.service;

import org.jetbrains.annotations.NotNull;
import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;

import java.util.List;

public interface TaskManager {
    int addTask(@NotNull Task task);

    int addSubtask(@NotNull Subtask subtask);

    int addEpic(@NotNull Epic epic);

    void updateTask(@NotNull Task task);

    void updateSubtask(@NotNull Subtask subtask);

    void updateEpic(@NotNull Epic epic);

    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<Subtask> getAllSubtasks();

    void clearTasks();

    void clearSubtasks();

    void clearEpics();

    Task getTask(int id);

    Epic getEpic(int id);

    Subtask getSubtask(int id);

    boolean removeTask(int id);

    boolean removeEpic(int id);

    boolean removeSubtask(int id);

    List<Subtask> getSubtasks(int id);

    List<Task> getHistory();
}
