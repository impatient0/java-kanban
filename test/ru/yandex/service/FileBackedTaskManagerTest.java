package ru.yandex.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.exceptions.ManagerLoadException;
import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;
import ru.yandex.model.TaskStatus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    private Path tmpSaveFile;
    private LocalDateTime nowDateTime;
    private final Path testSaveFile = Paths.get("test/ru/yandex/service/resource/test_save_file.txt"), badSaveFile
            = Paths.get("test/ru/yandex/service/resource/bad_save_file.txt");

    @BeforeEach
    void setUp() throws IOException {
        tmpSaveFile = File.createTempFile("test_save_file", ".tmp").toPath();
        nowDateTime = LocalDateTime.now();
    }

    @Test
    void shouldLoadFromEmptyFile() {
        TaskManager tm = new FileBackedTaskManager(tmpSaveFile);
        assertTrue(tm.getAllTasks().isEmpty() && tm.getAllSubtasks().isEmpty() && tm.getAllEpics().isEmpty());
    }

    @Test
    void shouldCreateSaveFileWhenNoFileExistsUnderPath() throws IOException {
        Files.delete(tmpSaveFile);
        assertFalse(Files.exists(tmpSaveFile));
        TaskManager tm = new FileBackedTaskManager(tmpSaveFile);
        assertTrue(tm.getAllTasks().isEmpty() && tm.getAllSubtasks().isEmpty() && tm.getAllEpics().isEmpty());
        assertTrue(Files.exists(tmpSaveFile));
    }

    @Test
    void shouldLoadTasksFromFile() {
        TaskManager tm = FileBackedTaskManager.loadFromFile(testSaveFile.toFile());
        Task task = tm.getTask(0);
        Epic epic = tm.getEpic(1);
        Subtask subtask = tm.getSubtask(2);
        assertEquals("Подготовка к экзамену", task.getName());
        assertEquals(TaskStatus.NEW, epic.getStatus());
        assertEquals(
                "Изучение и анализ существующих рабочих процессов компании для выявления узких мест и возможностей "
                        + "для оптимизации.", subtask.getDescription());
        assertEquals(1, subtask.getEpicId());
        assertTrue(epic.getSubtasks().containsKey(2));
    }

    @Test
    void shouldSaveToAndLoadTasksFromFile() {
        TaskManager tm = new FileBackedTaskManager(tmpSaveFile);
        Task task = new Task("Подготовка к экзамену", "Составить план подготовки к экзамену.", Duration.ofHours(42),
                nowDateTime);
        tm.addTask(task);
        Epic epic = new Epic("Оптимизация рабочего процесса",
                "Оптимизация рабочего процесса компании для повышения эффективности и продуктивности сотрудников.");
        int e1 = tm.addEpic(epic);
        Subtask subtask = new Subtask("Анализ текущих процессов",
                "Изучение и анализ существующих рабочих процессов компании для выявления узких мест и возможностей "
                        + "для оптимизации.", e1, Duration.ofHours(69), nowDateTime.plusHours(100));
        tm.addSubtask(subtask);
        TaskManager tm2 = FileBackedTaskManager.loadFromFile(tmpSaveFile.toFile());
        assertEquals(tm.getTask(0).getName(), tm2.getTask(0).getName());
        assertEquals(tm.getTask(0).getDescription(), tm2.getTask(0).getDescription());
        assertEquals(tm.getTask(0).getStatus(), tm2.getTask(0).getStatus());
        assertEquals(tm.getTask(0).getStartTime(), tm2.getTask(0).getStartTime());
        assertEquals(tm.getTask(0).getDuration(), tm2.getTask(0).getDuration());
        assertEquals(tm.getEpic(1).getName(), tm2.getEpic(1).getName());
        assertEquals(tm.getEpic(1).getDescription(), tm2.getEpic(1).getDescription());
        assertEquals(tm.getEpic(1).getStatus(), tm2.getEpic(1).getStatus());
        assertEquals(tm.getEpic(1).getStartTime(), tm2.getEpic(1).getStartTime());
        assertEquals(tm.getEpic(1).getDuration(), tm2.getEpic(1).getDuration());
        assertEquals(tm.getSubtask(2).getName(), tm2.getSubtask(2).getName());
        assertEquals(tm.getSubtask(2).getDescription(), tm2.getSubtask(2).getDescription());
        assertEquals(tm.getSubtask(2).getStatus(), tm2.getSubtask(2).getStatus());
        assertEquals(tm.getSubtask(2).getStartTime(), tm2.getSubtask(2).getStartTime());
        assertEquals(tm.getSubtask(2).getDuration(), tm2.getSubtask(2).getDuration());
        assertEquals(tm.getSubtask(2).getEpicId(), tm2.getSubtask(2).getEpicId());
    }

    @Test
    void shouldSaveTasksToFile() throws IOException {
        TaskManager tm = new FileBackedTaskManager(tmpSaveFile);
        Task task = new Task("Подготовка к экзамену", "Составить план подготовки к экзамену.", Duration.ofHours(42),
                nowDateTime);
        tm.addTask(task);
        Epic epic = new Epic("Оптимизация рабочего процесса",
                "Оптимизация рабочего процесса компании для повышения эффективности и продуктивности сотрудников.");
        int e1 = tm.addEpic(epic);
        Subtask subtask = new Subtask("Анализ текущих процессов",
                "Изучение и анализ существующих рабочих процессов компании для выявления узких мест и возможностей "
                        + "для оптимизации.", e1, Duration.ofHours(69), nowDateTime.plusHours(100));
        tm.addSubtask(subtask);
        try (BufferedReader input = new BufferedReader(new FileReader(tmpSaveFile.toFile()))) {
            input.readLine();
            assertEquals(task.getCSV(), input.readLine());
            assertEquals(epic.getCSV(), input.readLine());
            assertEquals(subtask.getCSV(), input.readLine());
        }
    }

    @Test
    void shouldThrowExceptionOnInvalidFile() {
        assertThrows(ManagerLoadException.class, () -> FileBackedTaskManager.loadFromFile(badSaveFile.toFile()));
    }

    @Test
    void shouldPreserveIdsUponDeletion() {
        TaskManager tm = new FileBackedTaskManager(tmpSaveFile);
        Task task = new Task("Подготовка к экзамену", "Составить план подготовки к экзамену.", Duration.ofHours(42),
                nowDateTime);
        tm.addTask(task);
        Epic epic = new Epic("Оптимизация рабочего процесса",
                "Оптимизация рабочего процесса компании для повышения эффективности и продуктивности сотрудников.");
        int e1 = tm.addEpic(epic);
        Subtask subtask = new Subtask("Анализ текущих процессов",
                "Изучение и анализ существующих рабочих процессов компании для выявления узких мест и возможностей "
                        + "для оптимизации.", e1, Duration.ofHours(69), nowDateTime.plusHours(100));
        tm.addSubtask(subtask);
        tm.removeTask(0);
        TaskManager tm2 = FileBackedTaskManager.loadFromFile(tmpSaveFile.toFile());
        assertTrue(tm2.getAllTasks().isEmpty());
        assertTrue(tm2.getEpic(1).getSubtasks().containsKey(2));
    }
}