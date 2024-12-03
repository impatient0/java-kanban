package ru.yandex.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    private static Epic epic;
    private static Subtask subtask1;
    private static Subtask subtask2;
    private static Subtask subtask3;
    private static LocalDateTime nowDateTime;

    @BeforeEach
    void setUp() {
        nowDateTime = LocalDateTime.now();
        subtask1 = new Subtask("_s1name_", "_s1desc_", 1, TaskStatus.NEW, 42, Duration.ofHours(42), nowDateTime);
        subtask2 = new Subtask("_s2name_", "_s2desc_", 2, TaskStatus.NEW, 42, Duration.ofHours(69),
                nowDateTime.plusHours(100));
        subtask3 = new Subtask("_s3name_", "_s3desc_", 3, TaskStatus.IN_PROGRESS, 42, Duration.ofHours(14),
                nowDateTime.plusHours(200));
        epic = new Epic("_ename_", "_edesc_", 42);
    }

    @Test
    void shouldConvertEpicToString() {
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        String s = "Эпик #00000042 \"_ename_\": _edesc_ Статус: NEW; Подзадачи: #00000001, #00000002";
        assertEquals(s, epic.toString());
    }

    @Test
    void shouldBeIN_PROGRESSWhenSubtasksUpdate() {
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        epic.addSubtask(subtask3);
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void shouldAddSubtask() {
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        epic.addSubtask(subtask3);
        assertTrue(
                epic.getSubtasks().containsKey(3) && epic.getSubtasks().get(3).getStatus() == TaskStatus.IN_PROGRESS);
    }

    @Test
    void shouldReturnFalseIfAddingAlreadyPresentSubtask() {
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        assertFalse(epic.addSubtask(subtask2));
    }

    @Test
    void shouldRemoveSubtask() {
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        epic.removeSubtask(subtask2);
        assertFalse(epic.getSubtasks().containsKey(2));
    }

    @Test
    void shouldClearSubtasks() {
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        epic.clearSubtasks();
        assertTrue(epic.getSubtasks().isEmpty());
    }

    @Test
    void shouldUpdateSubtask() {
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        subtask1 = new Subtask("_s1name_", "_s1desc_", 1, TaskStatus.DONE, 42, Duration.ofHours(42), nowDateTime);
        epic.updateSubtask(subtask1);
        assertEquals(TaskStatus.DONE, epic.getSubtasks().get(1).getStatus());
    }

    @Test
    void shouldTreatEpicsWithSameIdAsEqual() {
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        Epic anotherEpic = new Epic("_anotherename_", "_anotheredesc_", 42);
        assertEquals(epic, anotherEpic);
    }

    @Test
    void shouldNotCreateSharedReferencesWhenCloning() throws CloneNotSupportedException {
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        epic.addSubtask(subtask3);
        Epic clonedEpic = epic.clone();
        epic.removeSubtask(subtask1);
        assertTrue(clonedEpic.getSubtasks().containsKey(subtask1.getId()));
        clonedEpic.removeSubtask(subtask3);
        assertTrue(epic.getSubtasks().containsKey(subtask3.getId()));
        clonedEpic.updateSubtask(
                new Subtask(subtask2.getName(), subtask2.getDescription(), subtask2.getId(), TaskStatus.DONE,
                        epic.getId(), subtask2.getDuration(), subtask2.getStartTime()));
        assertEquals(TaskStatus.NEW, epic.getSubtasks().get(subtask2.getId()).getStatus());
        epic.clearSubtasks();
        assertFalse(clonedEpic.getSubtasks().isEmpty());
    }

    @Test
    void shouldCalculateTimeAndDuration() {
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        epic.addSubtask(subtask3);
        assertEquals(subtask1.getStartTime(), epic.getStartTime());
        assertEquals(subtask1.getDuration().plus(subtask2.getDuration()).plus(subtask3.getDuration()),
                epic.getDuration());
        assertEquals(subtask3.getEndTime(), epic.getEndTime());
    }

    @Test
    void shouldUpdateTimeAndDuration() {
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        epic.addSubtask(subtask3);
        epic.removeSubtask(subtask1);
        Subtask anotherSubtask = new Subtask(subtask3.getName(), subtask3.getDescription(), subtask3.getId(),
                subtask3.getStatus(), epic.getId(), Duration.ofHours(28), nowDateTime.plusHours(420));
        epic.updateSubtask(anotherSubtask);
        assertEquals(subtask2.getStartTime(), epic.getStartTime());
        assertEquals(subtask2.getDuration().plus(anotherSubtask.getDuration()), epic.getDuration());
        assertEquals(anotherSubtask.getEndTime(), epic.getEndTime());
    }

    @Test
    void shouldDeriveStatusFromAllNewSubtasks() {
        subtask1.setStatus(TaskStatus.NEW);
        subtask2.setStatus(TaskStatus.NEW);
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        assertEquals(TaskStatus.NEW, epic.getStatus());
    }

    @Test
    void shouldDeriveStatusFromAllDoneSubtasks() {
        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.DONE);
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        assertEquals(TaskStatus.DONE, epic.getStatus());
    }

    @Test
    void shouldDeriveStatusFromNewAndDoneSubtasks() {
        subtask1.setStatus(TaskStatus.NEW);
        subtask2.setStatus(TaskStatus.DONE);
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void shouldDeriveStatusFromAllInProgressSubtasks() {
        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        subtask2.setStatus(TaskStatus.IN_PROGRESS);
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }
}