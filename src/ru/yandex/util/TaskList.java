package ru.yandex.util;

import ru.yandex.model.Task;

import java.util.*;

public class TaskList {
    private final Map<Integer, Node<Task>> indexMap = new HashMap<>();
    private Node<Task> first = null, last = null;

    public void addLast(Task task) {
        if (first == null) {
            last = new Node<>(null, task, null);
            first = last;
        } else {
            Node<Task> prevLast = last;
            last = new Node<>(prevLast, task, null);
            prevLast.next = last;
        }
        indexMap.put(task.getId(), last);
    }

    public void remove(int id) {
        Node<Task> toRemove = indexMap.get(id);
        if (toRemove == null) {
            return;
        }
        if (toRemove.previous == null) {
            first = toRemove.next;
        } else {
            toRemove.previous.next = toRemove.next;
        }
        if (toRemove.next == null) {
            last = toRemove.previous;
        } else {
            toRemove.next.previous = toRemove.previous;
        }
    }

    public List<Task> toList() {
        ArrayList<Task> result = new ArrayList<>();
        Node<Task> n = first;
        while (n != null) {
            result.add(n.data);
            n = n.next;
        }
        return result;
    }

    private static class Node<E> {
        E data;
        Node<E> next;
        Node<E> previous;

        Node(Node<E> previous, E data, Node<E> next) {
            this.data = data;
            this.previous = previous;
            this.next = next;
        }
    }
}
