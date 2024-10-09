package ru.yandex.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    private static Epic epic;
    private static Subtask subtask1;
    private static Subtask subtask2;
    private static Subtask subtask3;

    @BeforeEach
    void setUp() {
        subtask1 = new Subtask("_s1name_", "_s1desc_", 1, TaskStatus.NEW, 42);
        subtask2 = new Subtask("_s2name_", "_s2desc_", 2, TaskStatus.NEW, 42);
        subtask3 = new Subtask("_s3name_", "_s3desc_", 3, TaskStatus.IN_PROGRESS, 42);
        epic = new Epic("_ename_", "_edesc_", 42);
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
    }

    @Test
    void shouldConvertEpicToString() {
        String s = "Эпик #00000042 \"_ename_\": _edesc_ Статус: NEW; Подзадачи: #00000001, #00000002";
        assertEquals(s, epic.toString());
    }

    @Test
    void shouldBeIN_PROGRESSWhenSubtasksUpdate() {
        epic.addSubtask(subtask3);
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void shouldAddSubtask() {
        epic.addSubtask(subtask3);
        assertTrue(epic.getSubtasks().containsKey(3) && epic.getSubtasks().get(3) == TaskStatus.IN_PROGRESS);
    }

    @Test
    void shouldReturnFalseIfAddingAlreadyPresentSubtask() {
        assertFalse(epic.addSubtask(subtask2));
    }

    @Test
    void shouldRemoveSubtask() {
        epic.removeSubtask(subtask2);
        assertFalse(epic.getSubtasks().containsKey(2));
    }

    @Test
    void shouldClearSubtasks() {
        epic.clearSubtasks();
        assertTrue(epic.getSubtasks().isEmpty());
    }

    @Test
    void shouldUpdateSubtask() {
        subtask1 = new Subtask("_s1name_", "_s1desc_", 1, TaskStatus.DONE, 42);
        epic.updateSubtask(subtask1);
        assertEquals(epic.getSubtasks().get(1), TaskStatus.DONE);
    }

    @Test
    void shouldTreatEpicsWithSameIdAsEqual() {
        Epic anotherEpic = new Epic("_anotherename_", "_anotheredesc_", 42);
        assertEquals(epic, anotherEpic);
    }

    @Test
    void shouldNotCreateSharedReferencesWhenCloning() throws CloneNotSupportedException {
        epic.addSubtask(subtask3);
        Epic clonedEpic = epic.clone();
        epic.removeSubtask(subtask1);
        assertTrue(clonedEpic.getSubtasks().containsKey(subtask1.getId()));
        clonedEpic.removeSubtask(subtask3);
        assertTrue(epic.getSubtasks().containsKey(subtask3.getId()));
        clonedEpic.updateSubtask(new Subtask(subtask2.getName(), subtask2.getDescription(), subtask2.getId(), TaskStatus.DONE, epic.getId()));
        assertEquals(TaskStatus.NEW, epic.getSubtasks().get(subtask2.getId()));
        epic.clearSubtasks();
        assertFalse(clonedEpic.getSubtasks().isEmpty());
    }

    // не вполне понятно, как проверить, что эпик нельзя назначить своей же собственной подзадачей: т.к. addSubtask()
    // принимает аргумент типа Subtask, при попытке передать туда Epic код просто не скомпилируется

    /*@Test
    void shouldNotAllowAddingEpicAsItsOwnSubtask() {
        epic.addSubtask(epic);
    }*/
}