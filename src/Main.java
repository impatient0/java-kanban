import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;
import ru.yandex.service.FileBackedTaskManager;
import ru.yandex.service.TaskManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException {
        Path saveFile = File.createTempFile("save_file", ".tmp").toPath();
        TaskManager taskManager = new FileBackedTaskManager(saveFile);
        LocalDateTime nowDateTime = LocalDateTime.now();
        int t1 = taskManager.addTask(
                new Task("Подготовка к экзамену", "Составить план подготовки к экзамену.", Duration.ofHours(1),
                        nowDateTime.plusHours(2)));
        int t2 = taskManager.addTask(
                new Task("Ремонт в детской комнате", "Составить список необходимых материалов и инструментов.",
                        Duration.ofHours(72), nowDateTime.plusHours(4)));
        int e1 = taskManager.addEpic(new Epic("Оптимизация рабочего процесса",
                "Оптимизация рабочего процесса компании для повышения эффективности и продуктивности сотрудников."));
        int e2 = taskManager.addEpic(new Epic("Улучшение пользовательского интерфейса",
                "Улучшение пользовательского интерфейса приложения для повышения удобства использования и "
                        + "привлекательности."));
        int s1 = taskManager.addSubtask(new Subtask("Анализ текущих процессов",
                "Изучение и анализ существующих рабочих процессов компании для выявления узких мест и возможностей "
                        + "для оптимизации.", e1, Duration.ofHours(2), nowDateTime.plusHours(128)));
        int s2 = taskManager.addSubtask(
                new Subtask("Разработка рекомендаций", "Подготовка предложений по улучшению рабочих процессов.", e1,
                        Duration.ofHours(1), nowDateTime.plusHours(130)));
        int s3 = taskManager.addSubtask(new Subtask("Анализ текущего интерфейса",
                "Изучение и анализ текущего пользовательского интерфейса приложения для выявления недостатков и "
                        + "возможностей для улучшения.", e1, Duration.ofHours(3), nowDateTime.plusHours(144)));
        System.out.println("Изначальный список задач:");
        showTasks(taskManager);
        showHistory(taskManager);
        System.out.println("\nЗапрашиваем задачу #00000000, эпик #00000002, подзадачи #00000004 и #00000006:");
        taskManager.getTask(t1);
        taskManager.getEpic(e1);
        taskManager.getSubtask(s1);
        taskManager.getSubtask(s3);
        showHistory(taskManager);
        System.out.println(
                "\nЗапрашиваем задачу #00000001, эпик #00000003 и подзадачу #00000005, ещё раз запрашиваем подзадачу "
                        + "#00000004 и эпик #00000002:");
        taskManager.getTask(t2);
        taskManager.getEpic(e2);
        taskManager.getSubtask(s2);
        taskManager.getSubtask(s1);
        taskManager.getEpic(e1);
        showHistory(taskManager);
        System.out.println("\nУдаляем задачу #00000001:");
        taskManager.removeTask(t2);
        showTasks(taskManager);
        showHistory(taskManager);
        System.out.println("\nУдаляем эпик #00000002:");
        taskManager.removeEpic(e1);
        showTasks(taskManager);
        showHistory(taskManager);
    }

    public static void showTasks(TaskManager taskManager) {
        System.out.println("Задачи:");
        System.out.println(
                taskManager.getAllTasks().stream().map(t -> "\t" + t.toString()).collect(Collectors.joining("\n")));
        System.out.println("Эпики:");
        System.out.println(
                taskManager.getAllEpics().stream().map(t -> "\t" + t.toString()).collect(Collectors.joining("\n")));
        System.out.println("Подзадачи:");
        System.out.println(
                taskManager.getAllSubtasks().stream().map(t -> "\t" + t.toString()).collect(Collectors.joining("\n")));
    }

    public static void showHistory(TaskManager taskManager) {
        System.out.println("История просмотров:");
        System.out.println(
                taskManager.getHistory().stream().map(t -> "\t" + t.toString()).collect(Collectors.joining("\n")));
    }
}
