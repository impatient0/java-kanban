package ru.yandex.service;

import ru.yandex.exceptions.ManagerLoadException;
import ru.yandex.exceptions.ManagerSaveException;
import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;
import ru.yandex.model.TaskStatus;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {
    private final Path saveFile;
    private final static String HEADER = "id,type,name,status,description,epic";

    FileBackedTaskManager(Path saveFile) {
        this.saveFile = saveFile;
        if (!Files.exists(saveFile)) {
            try {
                Files.createFile(saveFile);
            } catch (IOException e) {
                throw new ManagerSaveException("Не удалось создать файл сохранения.");
            }
        } else {
            load();
        }
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
                String name = split[2];
                TaskStatus status = TaskStatus.valueOf(split[3]);
                String description = split[4];
                switch (split[1]) {
                    case "task":
                        Task task = new Task(name, description, id, status);
                        tasks.put(id, task);
                        freeId = Integer.max(id + 1, freeId);
                        break;
                    case "epic":
                        Epic epic = new Epic(name, description, id);
                        epics.put(id, epic);
                        freeId = Integer.max(id + 1, freeId);
                        break;
                    case "subtask":
                        int epicId = Integer.parseInt(split[5]);
                        Subtask subtask = new Subtask(name, description, id, status, epicId);
                        epics.get(epicId).addSubtask(subtask);
                        subtasks.put(id, subtask);
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
            while (curId < tasks.size() + epics.size() + subtasks.size()) {
                Task task = tasks.get(curId), epic = epics.get(curId), subtask = subtasks.get(curId);
                Task curTask = task != null ? task : (epic != null ? epic : subtask);
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

    public static void main(String[] args) throws IOException {
        Path saveFile = File.createTempFile("save_file", ".tmp").toPath();
        TaskManager tm = new FileBackedTaskManager(saveFile);
        tm.addTask(new Task("Подготовка к экзамену", "Составить план подготовки к экзамену."));
        int e1 = tm.addEpic(new Epic("Оптимизация рабочего процесса",
                "Оптимизация рабочего процесса компании для повышения эффективности и продуктивности сотрудников."));
        tm.addSubtask(new Subtask("Анализ текущих процессов",
                "Изучение и анализ существующих рабочих процессов компании для выявления узких мест и возможностей "
                        + "для оптимизации.", e1));
        TaskManager tm2 = new FileBackedTaskManager(saveFile);
        showTasks(tm2);
    }

    public static void showTasks(TaskManager taskManager) {
        System.out.println("Задачи:");
        System.out.println(
                taskManager.getAllTasks().stream().map(t -> "\t" + t.toString()).collect(Collectors.joining("\n")));
        System.out.println("Эпики:");
        System.out.println(
                taskManager.getAllEpics().stream().map(t -> "\t" + t.toString()).collect(Collectors.joining("\n")));
        System.out.println("Подзадачи:");
        System.out.println(
                taskManager.getAllSubtasks().stream().map(t -> "\t" + t.toString()).collect(Collectors.joining("\n")));
    }
}
