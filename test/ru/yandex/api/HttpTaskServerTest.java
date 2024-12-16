package ru.yandex.api;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;
import ru.yandex.service.InMemoryTaskManager;
import ru.yandex.service.TaskManager;
import ru.yandex.util.DurationTypeAdapter;
import ru.yandex.util.LocalDateTimeAdapter;

public class HttpTaskServerTest {

    private TaskManager taskManager;
    private HttpTaskServer taskServer;
    private final Gson gson = new GsonBuilder().registerTypeAdapter(Duration.class,
            new DurationTypeAdapter())
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    public void setUp() throws IOException {
        taskManager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(taskManager);
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop(0);
    }

    @Test
    public void shouldAddTask() throws IOException, InterruptedException {
        Task task = new Task("_t1name_", "_t1desc_", Duration.ZERO, LocalDateTime.MIN);
        String taskJson = gson.toJson(task);
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        List<Task> tasks = taskManager.getAllTasks();
        assertEquals(1, tasks.size());
        assertEquals(0, tasks.getFirst().getId());
        assertEquals("_t1name_", tasks.getFirst().getName());
    }

    @Test
    public void shouldReturnNotAcceptableWhenTaskOverlaps()
        throws IOException, InterruptedException {
        Task firstTask = new Task("_t1name_", "_t1desc_", Duration.ofHours(8), LocalDateTime.MIN);
        Task secondTask = new Task("_t2name_", "_t2desc_", Duration.ofHours(4),
            LocalDateTime.MIN.plusHours(6));
        taskManager.addTask(firstTask);
        String taskJson = gson.toJson(secondTask);
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url)
            .POST(BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
    }


    @Test
    public void shouldUpdateTask() throws IOException, InterruptedException {
        Task task = new Task("_t1name_", "_t1desc_", Duration.ZERO, LocalDateTime.MIN);
        Task updatedTask = new Task("_updated_t1name_", "_t1desc_", Duration.ZERO,
            LocalDateTime.MIN);
        int taskId = taskManager.addTask(task);
        updatedTask.setId(taskId);
        String updatedTaskJson = gson.toJson(updatedTask);
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(updatedTaskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        assertEquals("_updated_t1name_", taskManager.getTask(taskId).getName());
    }

    @Test
    public void shouldGetTask() throws IOException, InterruptedException {
        Task task = new Task("_t1name_", "_t1desc_", Duration.ZERO, LocalDateTime.MIN);
        int taskId = taskManager.addTask(task);
        URI url = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
        assertEquals(200, response.statusCode());
        assertEquals(taskId, jsonObject.get("id").getAsInt());
        assertEquals("_t1name_", jsonObject.get("name").getAsString());
    }

    @Test
    public void shouldReturnNotFoundWhenTaskNotPresent() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/" + 42);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void shouldGetAllTasks() throws IOException, InterruptedException {
        Task firstTask = new Task("_t1name_", "_t1desc_", Duration.ZERO, LocalDateTime.MIN);
        Task secondTask = new Task("_t2name_", "_t2desc_", Duration.ZERO, LocalDateTime.MIN);
        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();
        JsonObject jsonObject0 = jsonArray.get(0).getAsJsonObject();
        JsonObject jsonObject1 = jsonArray.get(1).getAsJsonObject();
        assertEquals("_t1name_", jsonObject0.get("name").getAsString());
        assertEquals("_t2name_", jsonObject1.get("name").getAsString());
    }

    @Test
    public void shouldRemoveTask() throws IOException, InterruptedException {
        Task task = new Task("_t1name_", "_t1desc_", Duration.ZERO, LocalDateTime.MIN);
        int taskId = taskManager.addTask(task);
        URI url = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(taskManager.getAllTasks().isEmpty());
    }

    @Test
    public void shouldAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("_e1name_", "_e1desc_");
        String epicJson = gson.toJson(epic);
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url)
            .POST(BodyPublishers.ofString(epicJson)).build();
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        List<Epic> epics = taskManager.getAllEpics();
        assertEquals(1, epics.size());
        assertEquals(0, epics.getFirst().getId());
        assertEquals("_e1name_", epics.getFirst().getName());
    }

    @Test
    public void shouldGetEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("_e1name_", "_e1desc_");
        int epicId = taskManager.addEpic(epic);
        URI url = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
        assertEquals(200, response.statusCode());
        assertEquals(epicId, jsonObject.get("id").getAsInt());
        assertEquals("_e1name_", jsonObject.get("name").getAsString());
    }

    @Test
    public void shouldReturnNotFoundWhenEpicNotPresent() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/epics/" + 42);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void shouldGetAllEpics() throws IOException, InterruptedException {
        Epic firstEpic = new Epic("_e1name_", "_e1desc_");
        Epic secondEpic = new Epic("_e2name_", "_e2desc_");
        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();
        JsonObject jsonObject0 = jsonArray.get(0).getAsJsonObject();
        JsonObject jsonObject1 = jsonArray.get(1).getAsJsonObject();
        assertEquals("_e1name_", jsonObject0.get("name").getAsString());
        assertEquals("_e2name_", jsonObject1.get("name").getAsString());
    }

    @Test
    public void shouldGetEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("_e1name_", "_e1desc_");
        int epicId = taskManager.addEpic(epic);
        taskManager.addSubtask(
            new Subtask("_s1name_", "_s1desc_", epicId, Duration.ZERO, LocalDateTime.MIN));
        taskManager.addSubtask(new Subtask("_s2name_", "_s2desc_", epicId, Duration.ZERO,
            LocalDateTime.MIN.plusHours(1)));
        URI url = URI.create("http://localhost:8080/epics/" + epicId + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();
        JsonObject jsonObject0 = jsonArray.get(0).getAsJsonObject();
        JsonObject jsonObject1 = jsonArray.get(1).getAsJsonObject();
        assertEquals("_s1name_", jsonObject0.get("name").getAsString());
        assertEquals("_s2name_", jsonObject1.get("name").getAsString());
    }

    @Test
    public void shouldRemoveEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("_e1name_", "_e1desc_");
        int epicId = taskManager.addEpic(epic);
        URI url = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(taskManager.getAllEpics().isEmpty());
    }

    @Test
    public void shouldAddSubtask() throws IOException, InterruptedException {
        int epicId = taskManager.addEpic(new Epic("_e1name_", "_e1desc_"));
        Subtask subtask = new Subtask("_s1name_", "_s1desc_", epicId, Duration.ZERO,
            LocalDateTime.MIN);
        String subtaskJson = gson.toJson(subtask);
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url)
            .POST(BodyPublishers.ofString(subtaskJson)).build();
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        List<Subtask> subtasks = taskManager.getAllSubtasks();
        assertEquals(1, subtasks.size());
        assertEquals(1, subtasks.getFirst().getId());
        assertEquals("_s1name_", subtasks.getFirst().getName());
    }

    @Test
    public void shouldReturnNotAcceptableWhenSubtaskOverlaps()
        throws IOException, InterruptedException {
        int epicId = taskManager.addEpic(new Epic("_e1name_", "_e1desc_"));
        Subtask subtask = new Subtask("_s1name_", "_s1desc_", epicId, Duration.ofHours(4),
            LocalDateTime.MIN.plusHours(2));
        taskManager.addTask(
            new Task("_t1name_", "_t1desc_", Duration.ofHours(8), LocalDateTime.MIN));
        String subtaskJson = gson.toJson(subtask);
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url)
            .POST(BodyPublishers.ofString(subtaskJson)).build();
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
    }

    @Test
    public void shouldReturnNotFoundWhenParentEpicNotPresent()
        throws IOException, InterruptedException {
        Subtask subtask = new Subtask("_s1name_", "_s1desc_", 42, Duration.ZERO, LocalDateTime.MIN);
        String subtaskJson = gson.toJson(subtask);
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url)
            .POST(BodyPublishers.ofString(subtaskJson)).build();
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void shouldUpdateSubtask() throws IOException, InterruptedException {
        int epicId = taskManager.addEpic(new Epic("_e1name_", "_e1desc_"));
        Subtask subtask = new Subtask("_s1name_", "_s1desc_", epicId, Duration.ZERO,
            LocalDateTime.MIN);
        Subtask updatedSubtask = new Subtask("_updated_s1name_", "_s1desc_", epicId, Duration.ZERO,
            LocalDateTime.MIN);
        int subtaskId = taskManager.addSubtask(subtask);
        updatedSubtask.setId(subtaskId);
        String updatedSubtaskJson = gson.toJson(updatedSubtask);
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url)
            .POST(BodyPublishers.ofString(updatedSubtaskJson)).build();
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        List<Subtask> subtasks = taskManager.getAllSubtasks();
        assertEquals(1, subtasks.size());
        assertEquals(1, subtasks.getFirst().getId());
        assertEquals("_updated_s1name_", subtasks.getFirst().getName());
    }

    @Test
    public void shouldGetSubtask() throws IOException, InterruptedException {
        int epicId = taskManager.addEpic(new Epic("_e1name_", "_e1desc_"));
        Subtask subtask = new Subtask("_s1name_", "_s1desc_", epicId, Duration.ZERO,
            LocalDateTime.MIN);
        int subtaskId = taskManager.addSubtask(subtask);
        URI url = URI.create("http://localhost:8080/subtasks/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
        assertEquals(200, response.statusCode());
        assertEquals(subtaskId, jsonObject.get("id").getAsInt());
        assertEquals("_s1name_", jsonObject.get("name").getAsString());
    }

    @Test
    public void shouldReturnNotFoundWhenSubtaskNotPresent()
        throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/subtasks/" + 42);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void shouldGetAllSubtasks() throws IOException, InterruptedException {
        int epicId = taskManager.addEpic(new Epic("_e1name_", "_e1desc_"));
        Subtask firstSubtask = new Subtask("_s1name_", "_s1desc_", epicId, Duration.ZERO,
            LocalDateTime.MIN);
        Subtask secondSubtask = new Subtask("_s2name_", "_s2desc_", epicId, Duration.ZERO,
            LocalDateTime.MIN);
        taskManager.addSubtask(firstSubtask);
        taskManager.addSubtask(secondSubtask);
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();
        JsonObject jsonObject0 = jsonArray.get(0).getAsJsonObject();
        JsonObject jsonObject1 = jsonArray.get(1).getAsJsonObject();
        assertEquals("_s1name_", jsonObject0.get("name").getAsString());
        assertEquals("_s2name_", jsonObject1.get("name").getAsString());
    }

    @Test
    public void shouldRemoveSubtask() throws IOException, InterruptedException {
        int epicId = taskManager.addEpic(new Epic("_e1name_", "_e1desc_"));
        Subtask subtask = new Subtask("_s1name_", "_s1desc_", epicId, Duration.ZERO,
            LocalDateTime.MIN);
        int subtaskId = taskManager.addSubtask(subtask);
        URI url = URI.create("http://localhost:8080/subtasks/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(taskManager.getAllSubtasks().isEmpty());
    }

    @Test
    public void shouldGetHistory() throws IOException, InterruptedException {
        Task task = new Task("_t1name_", "_t1desc_", Duration.ZERO, LocalDateTime.MIN);
        int taskId = taskManager.addTask(task);
        Epic epic1 = new Epic("_e1name_", "_e1desc_");
        Epic epic2 = new Epic("_e2name_", "_e2desc_");
        int firstEpicId = taskManager.addEpic(epic1);
        int secondEpicId = taskManager.addEpic(epic2);
        Subtask subtask = new Subtask("_s2name_", "_s2desc_", firstEpicId, Duration.ZERO,
            LocalDateTime.MIN.plusHours(1));
        int subtaskId = taskManager.addSubtask(subtask);
        taskManager.getTask(taskId);
        taskManager.getEpic(secondEpicId);
        taskManager.getSubtask(subtaskId);
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();
        int[] historyIds = jsonArray.asList().stream()
            .map(e -> e.getAsJsonObject().get("id").getAsInt()).mapToInt(Integer::intValue)
            .toArray();
        assertArrayEquals(new int[]{taskId, secondEpicId, subtaskId}, historyIds);
    }

    @Test
    public void shouldGetPrioritized() throws IOException, InterruptedException {
        Task task = new Task("_t1name_", "_t1desc_", Duration.ZERO, LocalDateTime.MIN.plusHours(1));
        int taskId = taskManager.addTask(task);
        Epic epic1 = new Epic("_e1name_", "_e1desc_");
        Epic epic2 = new Epic("_e2name_", "_e2desc_");
        int firstEpicId = taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);
        Subtask subtask = new Subtask("_s2name_", "_s2desc_", firstEpicId, Duration.ZERO,
            LocalDateTime.MIN.plusHours(2));
        int subtaskId = taskManager.addSubtask(subtask);
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();
        int[] historyIds = jsonArray.asList().stream()
            .map(e -> e.getAsJsonObject().get("id").getAsInt()).mapToInt(Integer::intValue)
            .toArray();
        assertArrayEquals(new int[]{taskId, subtaskId}, historyIds);
    }

    @Test
    public void shouldReturnBadRequest() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/foo/bar/baz");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());
        url = URI.create("http://localhost:8080/epics/qwerty");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());
        url = URI.create("http://localhost:8080/subtasks/1/1/2/3/5");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());
    }
}
