package ru.yandex.api;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import ru.yandex.service.Managers;
import ru.yandex.service.TaskManager;

public class HttpTaskServer {

    private static final int PORT = 8080;
    private final HttpServer taskServer;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        taskServer = HttpServer.create();
        taskServer.bind(new InetSocketAddress(PORT), 0);
        taskServer.createContext("/tasks", new TaskHandler(taskManager));
        taskServer.createContext("/epics", new EpicHandler(taskManager));
        taskServer.createContext("/subtasks", new SubtaskHandler(taskManager));
        taskServer.createContext("/history", new HistoryHandler(taskManager));
        taskServer.createContext("/prioritized", new PrioritizedHandler(taskManager));
    }

    public static void main(String[] args) throws IOException {
        TaskManager tm = Managers.getDefault();
        HttpTaskServer hts = new HttpTaskServer(tm);
        hts.start();
    }

    public void start() {
        taskServer.start();
    }

    public void stop(int delay) {
        taskServer.stop(delay);
    }
}

