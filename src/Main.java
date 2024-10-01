public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();
        int t1 = taskManager.addTask("Подготовка к экзамену", "Составить план подготовки к экзамену.");
        int t2 = taskManager.addTask("Ремонт в детской комнате", "Составить список необходимых материалов и инструментов.");
        int t3 = taskManager.addTask("Организация вечеринки", "Разработать план вечеринки.");
        int t4 = taskManager.addTask("Изучение нового языка", "Определить уровень владения языком.");
        int t5 = taskManager.addTask("Путешествие мечты", "Выбрать направление путешествия.");
        System.out.println(taskManager.getAllTasks());
        taskManager.updateTask(t1, "Подготовка к экзамену", "Составить план подготовки к экзамену.", TaskStatus.IN_PROGRESS);
        System.out.println(taskManager.getAllTasks());
    }
}
