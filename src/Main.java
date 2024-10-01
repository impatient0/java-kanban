import ru.yandex.taskmanager.*;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();
        int t1 = taskManager.add(new Task("Подготовка к экзамену", "Составить план подготовки к экзамену."));
        int t2 = taskManager.add(
                new Task("Ремонт в детской комнате", "Составить список необходимых материалов и инструментов."));
        int t3 = taskManager.add(new Task("Организация вечеринки", "Разработать план вечеринки."));
        int t4 = taskManager.add(new Task("Изучение нового языка", "Определить уровень владения языком."));
        int t5 = taskManager.add(new Task("Путешествие мечты", "Выбрать направление путешествия."));
        taskManager.update(
                new Task("Подготовка к экзамену", "Составить план подготовки к экзамену и повесить на холодильник.", t1,
                        TaskStatus.IN_PROGRESS));
        int e1 = taskManager.add(new Epic("Оптимизация рабочего процесса",
                "Оптимизация рабочего процесса компании для повышения эффективности и продуктивности сотрудников."));
        int s1 = taskManager.add(new Subtask("Анализ текущих процессов",
                "Изучение и анализ существующих рабочих процессов компании для выявления узких мест и возможностей "
                        + "для оптимизации.", (Epic) taskManager.get(e1)));
        int s2 = taskManager.add(new Subtask("Разработка рекомендаций",
                "Подготовка предложений по улучшению рабочих процессов на основе проведённого анализа.",
                (Epic) taskManager.get(e1)));
        int s3 = taskManager.add(new Subtask("Внедрение изменений",
                "Реализация предложенных рекомендаций и мониторинг их влияния на эффективность работы компании.",
                (Epic) taskManager.get(e1)));
        taskManager.remove(s2);
        System.out.println(taskManager.getAll());
        System.out.println(taskManager.getSubtasks(taskManager.get(e1)));
        taskManager.remove(e1);
        System.out.println(taskManager.getAll());
    }
}
