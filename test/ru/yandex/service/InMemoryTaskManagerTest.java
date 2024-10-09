package ru.yandex.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.exceptions.TaskNotFoundException;
import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;
import ru.yandex.model.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private static TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void shouldAddTaskAndAssignIncrementingIds() {
        int firstTaskId = taskManager.addTask(new Task("_t1name_", "_t1desc_"));
        int secondTaskId = taskManager.addTask(new Task("_t2name_", "_t2desc_"));
        assertEquals(0, firstTaskId);
        assertEquals(1, secondTaskId);
        assertEquals("_t1name_", taskManager.getTask(firstTaskId).getName());
        assertEquals("_t2name_", taskManager.getTask(secondTaskId).getName());
    }

    @Test
    void shouldAddSubtaskAndAssignIncrementingIds() {
        int epicId = taskManager.addEpic(new Epic("_e1name_", "_e1desc_"));
        int firstSubtaskId = taskManager.addSubtask(new Subtask("_s1name_", "_s1desc_", epicId));
        int secondSubtaskId = taskManager.addSubtask(new Subtask("_s2name_", "_s2desc_", epicId));
        assertEquals(1, firstSubtaskId);
        assertEquals(2, secondSubtaskId);
        assertEquals("_s1name_", taskManager.getSubtask(firstSubtaskId).getName());
        assertEquals("_s2name_", taskManager.getSubtask(secondSubtaskId).getName());
    }

    @Test
    void shouldAddEpicAndAssignIncrementingIds() {
        int firstEpicId = taskManager.addEpic(new Epic("_e1name_", "_e1desc_"));
        int secondEpicId = taskManager.addEpic(new Epic("_e2name_", "_e2desc_"));
        assertEquals(0, firstEpicId);
        assertEquals(1, secondEpicId);
        assertEquals("_e1name_", taskManager.getEpic(firstEpicId).getName());
        assertEquals("_e2name_", taskManager.getEpic(secondEpicId).getName());
    }

    // в том числе проверяет, что подзадача не может быть собственным эпиком
    @Test
    void shouldThrowExceptionIfAddingSubtaskWithInvalidEpicId() {
        Subtask subtask = new Subtask("_s1name_", "_s1desc_", 42);
        assertThrows(TaskNotFoundException.class, () -> taskManager.addSubtask(subtask));
    }

    @Test
    void shouldUpdateTaskData() {
        int taskId = taskManager.addTask(new Task("_t1name_", "_t1desc_"));
        taskManager.updateTask(new Task("_anothertname_", "_anothertdesc_", taskId, TaskStatus.IN_PROGRESS));
        assertEquals("_anothertname_", taskManager.getTask(taskId).getName());
        assertEquals("_anothertdesc_", taskManager.getTask(taskId).getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getTask(taskId).getStatus());
    }

    @Test
    void shouldUpdateSubtaskData() {
        int epicId = taskManager.addEpic(new Epic("_e1name_", "_e1desc_"));
        int subtaskId = taskManager.addSubtask(new Subtask("_s1name_", "_s1desc_", epicId));
        taskManager.updateSubtask(
                new Subtask("_anothersname_", "_anothersdesc_", subtaskId, TaskStatus.IN_PROGRESS, epicId));
        assertEquals("_anothersname_", taskManager.getSubtask(subtaskId).getName());
        assertEquals("_anothersdesc_", taskManager.getSubtask(subtaskId).getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getSubtask(subtaskId).getStatus());
    }

    @Test
    void shouldUpdateEpicData() {
        int epicId = taskManager.addEpic(new Epic("_e1name_", "_e1desc_"));
        taskManager.updateEpic(new Epic("_anotherename_", "_anotheredesc_", epicId));
        assertEquals("_anotherename_", taskManager.getEpic(epicId).getName());
        assertEquals("_anotheredesc_", taskManager.getEpic(epicId).getDescription());
    }

    @Test
    void shouldClearTasks() {
        taskManager.addTask(new Task("_t1name_", "_t1desc_"));
        taskManager.addTask(new Task("_t2name_", "_t2desc_"));
        taskManager.clearTasks();
        assertTrue(taskManager.getAllTasks().isEmpty());
    }

    @Test
    void shouldClearSubtasksAndRemoveThemFromEpics() {
        int firstEpicId = taskManager.addEpic(new Epic("_e1name_", "_e1desc_"));
        taskManager.addSubtask(new Subtask("_s1name_", "_s1desc_", firstEpicId));
        taskManager.addSubtask(new Subtask("_s2name_", "_s2desc_", firstEpicId));
        taskManager.clearSubtasks();
        assertTrue(taskManager.getSubtasks(firstEpicId).isEmpty());
        assertTrue(taskManager.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldClearEpicsAndRemoveAllSubtasks() {
        int firstEpicId = taskManager.addEpic(new Epic("_e1name_", "_e1desc_"));
        taskManager.addSubtask(new Subtask("_s1name_", "_s1desc_", firstEpicId));
        taskManager.addSubtask(new Subtask("_s2name_", "_s2desc_", firstEpicId));
        taskManager.clearEpics();
        assertTrue(taskManager.getAllEpics().isEmpty());
        assertTrue(taskManager.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldReturnCorrectTaskById() {
        Task task = new Task("_t1name_", "_t1desc_");
        int taskId = taskManager.addTask(task);
        assertEquals(task.hashCode(), taskManager.getTask(taskId).hashCode());
    }

    @Test
    void shouldReturnCorrectEpicById() {
        Epic epic = new Epic("_e1name_", "_e1desc_");
        int epicId = taskManager.addEpic(epic);
        assertEquals(epic.hashCode(), taskManager.getEpic(epicId).hashCode());
    }

    @Test
    void shouldReturnCorrectSubtaskById() {
        int epicId = taskManager.addEpic(new Epic("_e1name_", "_e1desc_"));
        Subtask subtask = new Subtask("_s1name_", "_s1desc_", epicId);
        int subtaskId = taskManager.addSubtask(subtask);
        assertEquals(subtask.hashCode(), taskManager.getSubtask(subtaskId).hashCode());
    }

    @Test
    void shouldNotHaveTaskPresentAfterRemoval() {
        taskManager.addTask(new Task("_t1name_", "_t1desc_"));
        int secondTaskId = taskManager.addTask(new Task("_t2name_", "_t2desc_"));
        int numberOfTasksSnapshot = taskManager.getAllTasks().size();
        taskManager.removeTask(secondTaskId);
        assertEquals(numberOfTasksSnapshot - 1, taskManager.getAllTasks().size());
        assertNull(taskManager.getTask(secondTaskId));
    }

    @Test
    void shouldNotHaveEpicPresentAfterRemoval() {
        taskManager.addEpic(new Epic("_e1name_", "_e1desc_"));
        int secondEpicId = taskManager.addEpic(new Epic("_e2name_", "_e2desc_"));
        int numberOfEpicsSnapshot = taskManager.getAllEpics().size();
        taskManager.removeEpic(secondEpicId);
        assertEquals(numberOfEpicsSnapshot - 1, taskManager.getAllEpics().size());
        assertNull(taskManager.getEpic(secondEpicId));
    }

    @Test
    void shouldNotHaveSubtaskPresentAfterRemoval() {
        int epicId = taskManager.addEpic(new Epic("_e1name_", "_e1desc_"));
        taskManager.addSubtask(new Subtask("_s1name_", "_s1desc_", epicId));
        int secondSubtaskId = taskManager.addSubtask(new Subtask("_s2name_", "_s2desc_", epicId));
        int numberOfSubtasksSnapshot = taskManager.getAllSubtasks().size();
        taskManager.removeSubtask(secondSubtaskId);
        assertEquals(numberOfSubtasksSnapshot - 1, taskManager.getAllSubtasks().size());
        assertNull(taskManager.getSubtask(secondSubtaskId));
    }

    @Test
    void shouldReturnSubtasksOfEpicById() {
        int epicId = taskManager.addEpic(new Epic("_e1name_", "_e1desc_"));
        Subtask subtask1 = new Subtask("_s1name_", "_s1desc_", epicId);
        Subtask subtask2 = new Subtask("_s2name_", "_s2desc_", epicId);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        assertTrue(taskManager.getSubtasks(epicId).contains(subtask1));
        assertTrue(taskManager.getSubtasks(epicId).contains(subtask2));
    }

    @Test
    void shouldReturnHistoryForAccessedObjects() {
        Task task = new Task("_t1name_", "_t1desc_");
        int taskId = taskManager.addTask(task);
        Epic epic1 = new Epic("_e1name_", "_e1desc_");
        Epic epic2 = new Epic("_e2name_", "_e2desc_");
        int firstEpicId = taskManager.addEpic(epic1);
        int secondEpicId = taskManager.addEpic(epic2);
        Subtask subtask = new Subtask("_s2name_", "_s2desc_", firstEpicId);
        int subtaskId = taskManager.addSubtask(subtask);
        taskManager.getTask(taskId);
        taskManager.getEpic(secondEpicId);
        taskManager.getSubtask(subtaskId);
        List<Task> history = taskManager.getHistory();
        assertArrayEquals(new Task[]{task, epic2, subtask}, history.toArray());
    }

    @Test
    void shouldStoreUnmodifiedVersionsOfTasksInHistory() {
        Task task = new Task("_t1name_", "_t1desc_");
        int taskId = taskManager.addTask(task);
        Epic epic = new Epic("_e1name_", "_e1desc_");
        int epicId = taskManager.addEpic(epic);
        Subtask subtask = new Subtask("_s2name_", "_s2desc_", epicId);
        int subtaskId = taskManager.addSubtask(subtask);
        taskManager.getTask(taskId);
        taskManager.getEpic(epicId);
        taskManager.getSubtask(subtaskId);
        Task anotherTask = new Task("_anothertname_", "_anothertdesc_", taskId, TaskStatus.IN_PROGRESS);
        taskManager.updateTask(anotherTask);
        taskManager.updateTask(anotherTask);
        taskManager.removeEpic(epicId);
        List<Task> history = taskManager.getHistory();
        assertEquals("_t1name_", history.get(0).getName());
        assertEquals("_e1name_", history.get(1).getName());
        assertEquals("_s2name_", history.get(2).getName());
    }

    @Test
    void shouldAddTaskUnchanged() {
        Task task = new Task("_tname_", "_tdesc_", -1, TaskStatus.IN_PROGRESS);
        int taskId = taskManager.addTask(task);
        assertEquals("_tname_", taskManager.getTask(taskId).getName());
        assertEquals("_tdesc_", taskManager.getTask(taskId).getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getTask(taskId).getStatus());
    }
}