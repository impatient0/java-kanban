package ru.yandex.service;

import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskManager {
    static boolean checkOverlap(Task task1, Task task2) {
        LocalDateTime start1 = task1.getStartTime(), start2 = task2.getStartTime(), end1 = task1.getEndTime(), end2
                = task2.getEndTime();
        return ((!start1.isBefore(start2) && start1.isBefore(end2)) || (!start2.isBefore(start1) && start2.isBefore(
                end1)));
    }

    int addTask(Task task);

    int addSubtask(Subtask subtask);

    int addEpic(Epic epic);

    void updateTask(Task task);

    void updateSubtask(Subtask subtask);

    void updateEpic(Epic epic);

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

    List<Task> getPrioritizedTasks();

    List<Task> getHistory();
}
