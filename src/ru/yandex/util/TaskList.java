package ru.yandex.util;

import ru.yandex.model.Task;

import java.util.*;

public class TaskList {

    private int size = 0;
    private final Map<Integer, Node<Task>> indexMap = new HashMap<>();
    private Node<Task> last = null;

    public void addLast(Task task) {
        if (size == 0) {
            last = new Node<>(null, task, null);
        } else {
            Node<Task> prevLast = last;
            last = new Node<>(prevLast, task, null);
            prevLast.next = last;
        }
        indexMap.put(task.getId(), last);
        size++;
    }

    public Task get(int index) {
        return indexMap.get(index).data;
    }

    public void remove(int id) {
        Node<Task> toRemove = indexMap.get(id);
        if (toRemove == null) {
            return;
        }
        toRemove.previous.next = toRemove.next;
        toRemove.next.previous = toRemove.previous;
    }

    public List<Task> toList() {
        ArrayList<Task> result = new ArrayList<>();
        Node<Task> n = last;
        while (n != null) {
            result.add(n.data);
            n = n.previous;
        }
        return result.reversed();
    }

    public int size() {
        return size;
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
