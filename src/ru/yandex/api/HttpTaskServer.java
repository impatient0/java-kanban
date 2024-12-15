package ru.yandex.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import ru.yandex.service.Managers;
import ru.yandex.service.TaskManager;
import ru.yandex.util.DurationTypeAdapter;
import ru.yandex.util.LocalDateTimeAdapter;

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
        Gson gson = new GsonBuilder().registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .registerTypeAdapter(
                LocalDateTime.class, new LocalDateTimeAdapter()).create();
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

