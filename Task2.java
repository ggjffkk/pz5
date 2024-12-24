import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Task2 {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the Travel Planner!");
        System.out.println("Choose transportation options to compare:");
        System.out.println("1 - Trains\n2 - Buses\n3 - Flights");
        System.out.println("Enter your choices separated by commas (e.g., 1,3):");
        String[] choices = scanner.nextLine().split(",");

        List<String> options = new ArrayList<>();
        for (String choice : choices) {
            switch (choice.trim()) {
                case "1":
                    options.add("trains");
                    break;
                case "2":
                    options.add("buses");
                    break;
                case "3":
                    options.add("flights");
                    break;
                default:
                    System.out.println("Invalid option: " + choice);
            }
        }

        System.out.println("Fetching data for: " + options);

        // Створення CompletableFuture для обраних варіантів транспорту
        List<CompletableFuture<List<Route>>> futures = new ArrayList<>();
        if (options.contains("trains")) {
            futures.add(fetchTrainRoutes());
        }
        if (options.contains("buses")) {
            futures.add(fetchBusRoutes());
        }
        if (options.contains("flights")) {
            futures.add(fetchFlightRoutes());
        }

        // Обробка результатів
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        allOf.thenRun(() -> {
            try {
                List<Route> allRoutes = new ArrayList<>();
                for (CompletableFuture<List<Route>> future : futures) {
                    allRoutes.addAll(future.get());
                }
                displayResults(allRoutes);

                // Вибір критеріїв користувача
                System.out.println("\nChoose your preference:");
                System.out.println("1 - Expensive\n2 - Medium price\n3 - Cheap");
                System.out.print("Enter your price preference: ");
                int pricePreference = scanner.nextInt();

                System.out.println("1 - Fast\n2 - Slow");
                System.out.print("Enter your time preference: ");
                int timePreference = scanner.nextInt();

                Route bestRoute = findBestRouteByPreference(allRoutes, pricePreference, timePreference);
                System.out.println("\nBest Route based on your preference:");
                System.out.println(bestRoute);

            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error processing transportation data.");
                e.printStackTrace();
            }
        }).join();

        scanner.close();
    }

    // Симуляція отримання даних для поїздів
    private static CompletableFuture<List<Route>> fetchTrainRoutes() {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Fetching train routes...");
            List<Route> routes = Arrays.asList(
                    new Route("Train A", "Trains", 100, 5),
                    new Route("Train B", "Trains", 120, 6),
                    new Route("Train C", "Trains", 90, 4));
            simulateDelay();
            return routes;
        });
    }

    // Симуляція отримання даних для автобусів
    private static CompletableFuture<List<Route>> fetchBusRoutes() {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Fetching bus routes...");
            List<Route> routes = Arrays.asList(
                    new Route("Bus X", "Buses", 50, 8),
                    new Route("Bus Y", "Buses", 70, 7));
            simulateDelay();
            return routes;
        });
    }

    // Симуляція отримання даних для літаків
    private static CompletableFuture<List<Route>> fetchFlightRoutes() {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Fetching flight routes...");
            List<Route> routes = Arrays.asList(
                    new Route("Flight 1", "Flights", 300, 2),
                    new Route("Flight 2", "Flights", 250, 3),
                    new Route("Flight 3", "Flights", 400, 1),
                    new Route("Flight 4", "Flights", 350, 2));
            simulateDelay();
            return routes;
        });
    }

    // А-ля затримка обробки
    private static void simulateDelay() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Результати у вигляді таблички
    private static void displayResults(List<Route> routes) {
        System.out.println("\nAvailable Routes:");
        System.out.printf("%-10s %-10s %-10s %-10s\n", "Name", "Type", "Price", "Time");
        System.out.println("-------------------------------------------------");
        for (Route route : routes) {
            System.out.printf("%-10s %-10s %-10d %-10d\n", route.getName(), route.getType(), route.getPrice(),
                    route.getTime());
        }
    }

    // Відбір найкращого маршруту за критеріями, що обрав користувач
    private static Route findBestRouteByPreference(List<Route> routes, int pricePreference, int timePreference) {
        Comparator<Route> comparator = Comparator.comparingInt(Route::getPrice);
        if (pricePreference == 1) { // Дорого
            comparator = comparator.reversed();
        } else if (pricePreference == 2) { // Середньо
            comparator = Comparator.comparingInt(route -> Math.abs(route.getPrice() - getAveragePrice(routes)));
        }
        if (timePreference == 1) { // Швидко
            comparator = comparator.thenComparingInt(Route::getTime);
        } else if (timePreference == 2) { // Довго
            comparator = comparator.thenComparingInt(Route::getTime).reversed();
        }
        return routes.stream().sorted(comparator).findFirst()
                .orElseThrow(() -> new RuntimeException("No routes available"));
    }

    private static int getAveragePrice(List<Route> routes) {
        return (int) routes.stream().mapToInt(Route::getPrice).average().orElse(0);
    }
}

// Зберігання інформації про маршрут
class Route {
    private final String name;
    private final String type;
    private final int price;
    private final int time;

    public Route(String name, String type, int price, int time) {
        this.name = name;
        this.type = type;
        this.price = price;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getPrice() {
        return price;
    }

    public int getTime() {
        return time;
    }

    @Override
    public String toString() {
        return String.format("Route{name='%s', type='%s', price=%d, time=%d}", name, type, price, time);
    }
}
