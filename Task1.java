import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Task1 {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int numberOfFiles = 0;

        // Запитуємо кількість файлів із валідацією
        while (true) {
            System.out.print("Enter the number of files to process: ");
            if (scanner.hasNextInt()) {
                numberOfFiles = scanner.nextInt();
                scanner.nextLine();
                if (numberOfFiles > 0) {
                    break;
                } else {
                    System.out.println("Please enter a positive integer.");
                }
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next();
            }
        }

        // Масив для зберігання шляхів до файлів
        String[] filePaths = new String[numberOfFiles];

        // Отримуємо шляхи до файлів від користувача
        for (int i = 0; i < numberOfFiles; i++) {
            System.out.print("Enter the path to file " + (i + 1) + ": ");
            String inputPath = scanner.nextLine();
            filePaths[i] = inputPath.replaceAll("^\"|\"$", "");
        }

        // Масив CompletableFuture для обробки файлів
        CompletableFuture<String>[] futures = new CompletableFuture[numberOfFiles];

        // Ініціалізуємо CompletableFuture для кожного файлу
        for (int i = 0; i < numberOfFiles; i++) {
            final int index = i;
            futures[i] = CompletableFuture.supplyAsync(() -> readFile(filePaths[index]));
        }

        // Для обробки результатів після завершення всіх завдань
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures);

        // allOf.thenRun(() -> {
        try {
            // Отримуємо результати всіх файлів
            StringBuilder combinedResults = new StringBuilder();
            for (int i = 0; i < numberOfFiles; i++) {
                String result = futures[i].get();
                combinedResults.append("Contents of file ").append(filePaths[i]).append(":\n").append(result)
                        .append("\n\n");
            }
            System.out.println("Processed data from all files:");
            System.out.println(combinedResults.toString());
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error processing all files.");
            e.printStackTrace();
        }
        // }).join();

        // Отримання результату першого завершеного файлу
        CompletableFuture<String> anyOf = CompletableFuture.anyOf(futures)
                .thenApply(result -> "First completed file: " + result);

        try {
            System.out.println(anyOf.get());
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting the result of the first completed file.");
            e.printStackTrace();
        }

        scanner.close();
    }

    // Метод для зчитування файлу
    private static String readFile(String filePath) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            return String.join("\n", lines);
        } catch (IOException e) {
            // Обробка помилки
            System.err.println("Error reading file: " + filePath);
            return "Error reading file: " + filePath + " (" + e.getMessage() + ")";
        }
    }
}
