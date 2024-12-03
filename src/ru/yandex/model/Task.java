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

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return startTime.plus(duration);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
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

    public boolean overlaps(Task other) {
        LocalDateTime start1 = this.getStartTime(), start2 = other.getStartTime(), end1 = this.getEndTime(), end2
                = other.getEndTime();
        return ((!start1.isBefore(start2) && start1.isBefore(end2)) || (!start2.isBefore(start1) && start2.isBefore(
                end1)));
    }

    @Override
    public int compareTo(Task other) {
        if (other == null) {
            throw new IllegalArgumentException();
        }
        return Comparator.comparing(Task::getStartTime).thenComparing(Task::getId).compare(this, other);
    }
}
