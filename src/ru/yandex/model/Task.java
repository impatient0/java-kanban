package ru.yandex.model;

import java.util.Objects;

public class Task implements Cloneable {
    protected String name;
    protected String description;
    protected int id;
    protected TaskStatus status;

    public Task(String name, String description, int id, TaskStatus status) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = status;
    }

    public Task(String name, String description) {
        this(name, description, -1, TaskStatus.NEW);
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
        return Objects.hash(name, description, id, status);
    }

    @Override
    public String toString() {
        return "Задача #" + String.format("%08d", id) + " \"" + name + "\": " + description + " Статус: " + status;
    }

    public String getCSV() {
        return String.format("%d,task,%s,%s,%s,", id, name, status, description);
    }

    @Override
    public Task clone() throws CloneNotSupportedException {
        Task clone = (Task) super.clone();
        clone.name = name;
        clone.description = description;
        return clone;
    }
}
