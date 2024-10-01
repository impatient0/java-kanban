package ru.yandex.service;

import ru.yandex.exceptions.TaskNotFoundException;
import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;
import ru.yandex.model.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private int freeId;

    public TaskManager() {
        freeId = 0;
    }

    // методы добавления возвращают id добавленного элемента

    public int addTask(Task task) {
        int id = freeId++;
        task.setId(id);
        tasks.put(id, task);
        return id;
    }

    public int addSubtask(Subtask subtask) throws TaskNotFoundException {
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

    public int addEpic(Epic epic) {
        int id = freeId++;
        epic.setId(id);
        epics.put(id, epic);
        return id;
    }

    public void updateTask(Task task) throws TaskNotFoundException {
        if (!tasks.containsKey(task.getId())) {
            throw new TaskNotFoundException("Невозможно обновить задачу #" + String.format("%08d", task.getId())
                    + ": такой задачи не существует!");
        }
        tasks.put(task.getId(), task);
    }

    public void updateSubtask(Subtask subtask) throws TaskNotFoundException {
        if (!subtasks.containsKey(subtask.getId())) {
            throw new TaskNotFoundException("Невозможно обновить подзадачу #" + String.format("%08d", subtask.getId())
                    + ": такой подзадачи не существует!");
        }
        epics.get(subtask.getEpicId()).updateSubtask(subtask);
    }

    public void updateEpic(Epic epic) throws TaskNotFoundException {
        if (!epics.containsKey(epic.getId())) {
            throw new TaskNotFoundException("Невозможно обновить эпик #" + String.format("%08d", epic.getId())
                    + ": такого эпика не существует!");
        }
        HashMap<Integer, TaskStatus> subtasks = epics.get(epic.getId()).getSubtasks();
        epic.setSubtasks(subtasks);
        epics.put(epic.getId(), new Epic(epic.getName(), epic.getDescription(), epic.getId()));
    }

    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void clearTasks() {
        tasks.clear();
    }

    public void clearSubtasks() {
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
        }
        subtasks.clear();
    }

    public void clearEpics() {
        // подзадачи не могут существовать без эпиков, поэтому также удаляются
        subtasks.clear();
        epics.clear();
    }

    // геттеры возвращают null, если объекта с искомым id не существует

    public Task getTask(int id) {
        return tasks.get(id);
    }

    public Epic getEpic(int id) {
        return epics.get(id);
    }

    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }

    // методы для удаления возвращают true, если элемент был удалён, и false, если элемента с таким id не было

    public boolean removeTask(int id) {
        return tasks.remove(id) != null;
    }

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

    public boolean removeSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            return false;
        }
        epics.get(subtask.getEpicId()).removeSubtask(subtask);
        subtasks.remove(id);
        return true;
    }

    public ArrayList<Subtask> getSubtasks(int id) {
        Epic epic = epics.get(id);
        return epic == null ? null : epic.getSubtasks().keySet().stream().map(subtasks::get)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
