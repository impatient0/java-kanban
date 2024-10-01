import ru.yandex.taskmanager.*;

import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();
        int t1 = taskManager.add(new Task("Подготовка к экзамену", "Составить план подготовки к экзамену."));
        int t2 = taskManager.add(
                new Task("Ремонт в детской комнате", "Составить список необходимых материалов и инструментов."));
        int e1 = taskManager.add(new Epic("Оптимизация рабочего процесса",
                "Оптимизация рабочего процесса компании для повышения эффективности и продуктивности сотрудников."));
        int s1 = taskManager.add(new Subtask("Анализ текущих процессов",
                "Изучение и анализ существующих рабочих процессов компании для выявления узких мест и возможностей "
                        + "для оптимизации.", (Epic) taskManager.get(e1)));
        int s2 = taskManager.add(
                new Subtask("Разработка рекомендаций", "Подготовка предложений по улучшению рабочих процессов.",
                        (Epic) taskManager.get(e1)));
        int e2 = taskManager.add(new Epic("Улучшение пользовательского интерфейса",
                "Улучшение пользовательского интерфейса приложения для повышения удобства использования и "
                        + "привлекательности."));
        int s3 = taskManager.add(new Subtask("Анализ текущего интерфейса",
                "Изучение и анализ текущего пользовательского интерфейса приложения для выявления недостатков и "
                        + "возможностей для улучшения.", (Epic) taskManager.get(e2)));
        System.out.println("Изначальный список задач:\n" + taskManager.getAll().stream().map(Task::toString)
                .collect(Collectors.joining("\n")) + "\n");
        taskManager.update(
                new Task("Подготовка к экзамену", "Составить план подготовки к экзамену и повесить на холодильник.", t1,
                        TaskStatus.IN_PROGRESS));
        taskManager.update(new Subtask("Разработка рекомендаций",
                "Подготовка предложений по улучшению рабочих процессов на основе проведённого анализа.", s2,
                TaskStatus.IN_PROGRESS, (Epic) taskManager.get(e1)));
        taskManager.remove(s3);
        System.out.println("Обновили задачу #00000000 и подзадачу #00000004, а также удалили подзадачу #00000006:\n"
                + taskManager.getAll().stream().map(Task::toString).collect(Collectors.joining("\n")) + "\n");
        taskManager.remove(e1);
        taskManager.remove(t2);
        System.out.println(
                "Удалили задачу #00000001 и эпик #00000002:\n" + taskManager.getAll().stream().map(Task::toString)
                        .collect(Collectors.joining("\n")) + "\n");
        taskManager.clearEpics();
        System.out.println("Очистили эпики:\n" + taskManager.getAll().stream().map(Task::toString)
                .collect(Collectors.joining("\n")) + "\n");
        taskManager.clear();
        System.out.println("Полностью очистили список:\n" + taskManager.getAll());
    }
}
