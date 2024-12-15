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

    public void start() {
        taskServer.start();
    }

    public void stop(int delay) {
        taskServer.stop(delay);
    }

    public static void main(String[] args) throws IOException {
        Gson gson = new GsonBuilder().registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .registerTypeAdapter(
                LocalDateTime.class, new LocalDateTimeAdapter()).create();
        TaskManager tm = Managers.getDefault();
        HttpTaskServer hts = new HttpTaskServer(tm);
//        Task t = new Task("Подготовка к экзамену", "Составить план подготовки к экзамену.",
//            Duration.ofHours(1),
//            LocalDateTime.MIN.plusHours(2));
//        Epic e = new Epic("Оптимизация рабочего процесса",
//            "Оптимизация рабочего процесса компании для повышения эффективности и продуктивности сотрудников.");
//
//        int t1 = tm.addTask(t);
//        int e1 = tm.addEpic(e);
//        Subtask s = new Subtask("Анализ текущих процессов",
//            "Изучение и анализ существующих рабочих процессов компании для выявления узких мест и возможностей "
//                + "для оптимизации.", e1, Duration.ofHours(2), LocalDateTime.MIN.plusHours(128));
//        int s1 = tm.addSubtask(s);
//        String ts = gson.toJson(t);
//        System.out.println(ts);
//        System.out.println(gson.fromJson(ts, Task.class));
        hts.start();
    }
}

