package tr.com.infumia;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import tr.com.infumia.agones4j.Agones;

public final class Main {
    public static void main(final String[] args) throws InterruptedException {
        final var agones = Agones.builder()
            .withChannel()
            .withHealthCheck(Duration.ofSeconds(1L), Duration.ofSeconds(1L))
            .withGameServerWatcherExecutor(Executors.newSingleThreadExecutor())
            .build();

        agones.addGameServerWatcher(server -> System.out.println("Game server update: " + server));
        agones.ready();
        agones.startHealthChecking();

        sleep();
    }

    private static void sleep() throws InterruptedException {
        while (true) {
            Thread.sleep(5L);
        }
    }
}
