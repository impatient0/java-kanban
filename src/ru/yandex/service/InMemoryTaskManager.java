package ru.yandex.service;

import org.jetbrains.annotations.NotNull;
import ru.yandex.exceptions.TaskNotFoundException;
import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private int freeId = 0;
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    // методы добавления возвращают id добавленного элемента

    @Override
    public int addTask(@NotNull Task task) {
        int id = freeId++;
        task.setId(id);
        tasks.put(id, task);
        return id;
    }

    @Override
    public int addSubtask(@NotNull Subtask subtask) throws TaskNotFoundException {
        if (!epics.containsKey(subtask.getEpicId())) {
            throw new TaskNotFoundException(
                    "Невозможно добавить подзадачу к эпику #" + String.format("%08d", subtask.getEpicId())
                            + ": такого эпика не существует!");
        }
        int id = freeId++;
        subtask.setId(id);
        subtasks.put(id, subtask);
        epics.get(subtask.getEpicId()).addSubtask(subtask);
        return id;
    }

    @Override
    public int addEpic(@NotNull Epic epic) {
        int id = freeId++;
        epic.setId(id);
        epics.put(id, epic);
        return id;
    }

    @Override
    public void updateTask(@NotNull Task task) throws TaskNotFoundException {
        if (!tasks.containsKey(task.getId())) {
            throw new TaskNotFoundException("Невозможно обновить задачу #" + String.format("%08d", task.getId())
                    + ": такой задачи не существует!");
        }
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateSubtask(@NotNull Subtask subtask) throws TaskNotFoundException {
        if (!subtasks.containsKey(subtask.getId())) {
            throw new TaskNotFoundException("Невозможно обновить подзадачу #" + String.format("%08d", subtask.getId())
                    + ": такой подзадачи не существует!");
        }
        epics.get(subtask.getEpicId()).updateSubtask(subtask);
        subtasks.put(subtask.getId(), subtask);
    }

    @Override
    public void updateEpic(@NotNull Epic epic) throws TaskNotFoundException {
        if (!epics.containsKey(epic.getId())) {
            throw new TaskNotFoundException("Невозможно обновить эпик #" + String.format("%08d", epic.getId())
                    + ": такого эпика не существует!");
        }
        Epic oldEpic = epics.get(epic.getId());
        oldEpic.setName(epic.getName());
        oldEpic.setDescription(epic.getDescription());
        epics.put(epic.getId(), oldEpic);
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void clearTasks() {
        tasks.clear();
    }

    @Override
    public void clearSubtasks() {
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
        }
        subtasks.clear();
    }

    @Override
    public void clearEpics() {
        // подзадачи не могут существовать без эпиков, поэтому также удаляются
        subtasks.clear();
        epics.clear();
    }

    // геттеры возвращают null, если объекта с искомым id не существует

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        historyManager.remove(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        historyManager.remove(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        historyManager.remove(id);
        historyManager.add(subtask);
        return subtask;
    }

    // методы для удаления возвращают true, если элемент был удалён, и false, если элемента с таким id не было

    @Override
    public boolean removeTask(int id) {
        return tasks.remove(id) != null;
    }

    @Override
    public boolean removeEpic(int id) {
        if (!epics.containsKey(id)) {
            return false;
        }
        for (int s : epics.get(id).getSubtasks().keySet()) {
            subtasks.remove(s);
        }
        epics.remove(id);
        return true;
    }

    @Override
    public boolean removeSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            return false;
        }
        epics.get(subtask.getEpicId()).removeSubtask(subtask);
        subtasks.remove(id);
        return true;
    }

    @Override
    public ArrayList<Subtask> getSubtasks(int id) {
        Epic epic = epics.get(id);
        return epic == null ? new ArrayList<>() : epic.getSubtasks().keySet().stream().map(subtasks::get)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}
