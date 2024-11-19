package ru.yandex.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.exceptions.ManagerLoadException;
import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    private Path tmpSaveFile;
    private final Path testSaveFile = Paths.get("test/ru/yandex/service/resource/test_save_file.txt"), badSaveFile
            = Paths.get("test/ru/yandex/service/resource/bad_save_file.txt");

    @BeforeEach
    void setUp() throws IOException {
        tmpSaveFile = File.createTempFile("test_save_file", ".tmp").toPath();
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
        assertEquals(1, tm.getAllTasks().size());
        assertEquals(1, tm.getAllEpics().size());
        assertEquals(1, tm.getAllSubtasks().size());
    }

    @Test
    void shouldSaveTasksToFile() throws IOException {
        TaskManager tm = new FileBackedTaskManager(tmpSaveFile);
        Task task = new Task("Подготовка к экзамену", "Составить план подготовки к экзамену.");
        tm.addTask(task);
        Epic epic = new Epic("Оптимизация рабочего процесса",
                "Оптимизация рабочего процесса компании для повышения эффективности и продуктивности сотрудников.");
        int e1 = tm.addEpic(epic);
        Subtask subtask = new Subtask("Анализ текущих процессов",
                "Изучение и анализ существующих рабочих процессов компании для выявления узких мест и возможностей "
                        + "для оптимизации.", e1);
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
}