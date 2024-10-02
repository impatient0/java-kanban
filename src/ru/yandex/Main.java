package ru.yandex;

import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;
import ru.yandex.model.TaskStatus;
import ru.yandex.service.*;

import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();
        int t1 = taskManager.addTask(new Task("Подготовка к экзамену", "Составить план подготовки к экзамену."));
        int t2 = taskManager.addTask(
                new Task("Ремонт в детской комнате", "Составить список необходимых материалов и инструментов."));
        int e1 = taskManager.addEpic(new Epic("Оптимизация рабочего процесса",
                "Оптимизация рабочего процесса компании для повышения эффективности и продуктивности сотрудников."));
        int s1 = taskManager.addSubtask(new Subtask("Анализ текущих процессов",
                "Изучение и анализ существующих рабочих процессов компании для выявления узких мест и возможностей "
                        + "для оптимизации.", e1));
        int s2 = taskManager.addSubtask(
                new Subtask("Разработка рекомендаций", "Подготовка предложений по улучшению рабочих процессов.", e1));
        int e2 = taskManager.addEpic(new Epic("Улучшение пользовательского интерфейса",
                "Улучшение пользовательского интерфейса приложения для повышения удобства использования и "
                        + "привлекательности."));
        int s3 = taskManager.addSubtask(new Subtask("Анализ текущего интерфейса",
                "Изучение и анализ текущего пользовательского интерфейса приложения для выявления недостатков и "
                        + "возможностей для улучшения.", e2));
        System.out.println("Изначальный список задач:");
        show(taskManager);
        taskManager.updateTask(
                new Task("Подготовка к экзамену", "Составить план подготовки к экзамену и повесить на холодильник.", t1,
                        TaskStatus.IN_PROGRESS));
        taskManager.updateSubtask(new Subtask("Разработка рекомендаций",
                "Подготовка предложений по улучшению рабочих процессов на основе проведённого анализа.", s2,
                TaskStatus.IN_PROGRESS, e1));
        taskManager.removeSubtask(s3);
        System.out.println("\nОбновили задачу #00000000 и подзадачу #00000004, а также удалили подзадачу #00000006:");
        show(taskManager);
        taskManager.removeEpic(e1);
        taskManager.removeTask(t2);
        System.out.println("\nУдалили задачу #00000001 и эпик #00000002:");
        show(taskManager);
        taskManager.clearEpics();
        System.out.println("\nОчистили эпики:");
        show(taskManager);
    }

    public static void show(TaskManager taskManager) {
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
}
