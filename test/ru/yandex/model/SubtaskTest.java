package ru.yandex.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class SubtaskTest {

    private static final Subtask subtask = new Subtask("_sname_", "_sdesc_", 1, TaskStatus.NEW, 42,
        Duration.ofHours(42), LocalDateTime.now());

    @Test
    void shouldConvertSubtaskToString() {
        String s = "Подзадача #00000001 \"_sname_\": _sdesc_ Статус: NEW; Относится к эпику #00000042";
        assertEquals(s, subtask.toString());
    }

    @Test
    void shouldTreatSubtasksWithSameIdAsEqual() {
        Subtask anotherSubtask = new Subtask("_anothersname_", "_anothersdesc_", 1, TaskStatus.DONE,
            69,
            Duration.ofHours(42), LocalDateTime.now());
        assertEquals(subtask, anotherSubtask);
    }

}