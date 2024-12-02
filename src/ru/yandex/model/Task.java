package ru.yandex.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;

public class Task implements Cloneable, Comparable<Task> {
    protected String name;
    protected String description;
    protected int id;
    protected TaskStatus status;
    protected Duration duration;
    protected LocalDateTime startTime;

    public int getClassRank() {
        // значение для сравнения по типу: Task < Epic < Subtask
        return 0;
    }

    public Task(String name, String description, int id, TaskStatus status, Duration duration,
                LocalDateTime startTime) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = status;
        this.duration = duration;
        this.startTime = startTime;
    }

    public Task(String name, String description, Duration duration, LocalDateTime startTime) {
        this(name, description, -1, TaskStatus.NEW, duration, startTime);
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return startTime.plus(duration);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Задача #" + String.format("%08d", id) + " \"" + name + "\": " + description + " Статус: " + status;
    }

    public String getCSV() {
        return String.format("%d,%s,%s,%s,%s,%s,%s,", id, TaskType.TASK, name, status, description, duration.toString(),
                startTime.toString());
    }

    @Override
    public Task clone() throws CloneNotSupportedException {
        Task clone = (Task) super.clone();
        clone.name = name;
        clone.description = description;
        clone.duration = duration;
        clone.startTime = startTime;
        return clone;
    }

    @Override
    public int compareTo(Task other) {
        if (other == null) {
            throw new IllegalArgumentException();
        }
        return Comparator.comparing(Task::getStartTime).thenComparing(Task::getClassRank).thenComparing(Task::getId)
                .compare(this, other);
    }
}
