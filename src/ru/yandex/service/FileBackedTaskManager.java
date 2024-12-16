package ru.yandex.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import ru.yandex.exceptions.ManagerLoadException;
import ru.yandex.exceptions.ManagerSaveException;
import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;
import ru.yandex.model.TaskStatus;
import ru.yandex.model.TaskType;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private static final String HEADER = "id,type,name,status,description,duration,start_time,epic";
    private final Path saveFile;

    public FileBackedTaskManager(Path saveFile) {
        this.saveFile = saveFile;
        if (!Files.exists(saveFile)) {
            try {
                Files.createFile(saveFile);
            } catch (IOException e) {
                throw new ManagerSaveException("Не удалось создать файл сохранения.");
            }
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file.toPath());
        manager.load();
        return manager;
    }

    public Path getSaveFile() {
        return saveFile;
    }

    private void load() {
        try (BufferedReader input = new BufferedReader(new FileReader(saveFile.toFile()))) {
            if (!input.ready()) {
                return;
            }
            if (!input.readLine().equals(HEADER)) {
                throw new ManagerLoadException("Некорректный формат файла сохранения.");
            }
            tasks.clear();
            epics.clear();
            subtasks.clear();
            freeId = 0;
            while (input.ready()) {
                String[] split = input.readLine().split(",");
                int id = Integer.parseInt(split[0]);
                TaskType type = TaskType.valueOf(split[1]);
                String name = split[2];
                TaskStatus status = TaskStatus.valueOf(split[3]);
                String description = split[4];
                Duration duration = Duration.parse(split[5]);
                LocalDateTime startTime = LocalDateTime.parse(split[6]);
                switch (type) {
                    case TaskType.TASK:
                        Task task = new Task(name, description, id, status, duration, startTime);
                        tasks.put(id, task);
                        prioritizedTasks.add(task);
                        freeId = Integer.max(id + 1, freeId);
                        break;
                    case TaskType.EPIC:
                        Epic epic = new Epic(name, description, id);
                        epics.put(id, epic);
                        freeId = Integer.max(id + 1, freeId);
                        break;
                    case TaskType.SUBTASK:
                        int epicId = Integer.parseInt(split[7]);
                        Subtask subtask = new Subtask(name, description, id, status, epicId,
                            duration, startTime);
                        epics.get(epicId).addSubtask(subtask);
                        subtasks.put(id, subtask);
                        prioritizedTasks.add(subtask);
                        freeId = Integer.max(id + 1, freeId);
                        break;
                    default:
                        throw new ManagerLoadException("Некорректный формат файла сохранения.");
                }
            }
        } catch (IOException e) {
            throw new ManagerLoadException("Ошибка при чтении данных из файла.");
        }
    }

    private void save() {
        try (PrintStream print = new PrintStream(saveFile.toFile())) {
            print.println(HEADER);
            int curId = 0;
            while (curId < freeId) {
                Task task = tasks.get(curId), epic = epics.get(curId), subtask = subtasks.get(
                    curId);
                Task curTask = task != null ? task : (epic != null ? epic : subtask);
                if (curTask == null) {
                    curId++;
                    continue;
                }
                print.println(curTask.getCSV());
                curId++;
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении данных в файл.");
        }
    }

    @Override
    public int addTask(Task task) {
        int id = super.addTask(task);
        save();
        return id;
    }

    @Override
    public int addEpic(Epic epic) {
        int id = super.addEpic(epic);
        save();
        return id;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        int id = super.addSubtask(subtask);
        save();
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    @Override
    public void clearSubtasks() {
        super.clearSubtasks();
        save();
    }

    @Override
    public boolean removeTask(int id) {
        boolean result = super.removeTask(id);
        save();
        return result;
    }

    @Override
    public boolean removeEpic(int id) {
        boolean result = super.removeEpic(id);
        save();
        return result;
    }

    @Override
    public boolean removeSubtask(int id) {
        boolean result = super.removeSubtask(id);
        save();
        return result;
    }
}
