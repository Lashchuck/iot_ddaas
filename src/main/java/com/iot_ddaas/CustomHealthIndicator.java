package com.iot_ddaas;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.okhttp.OkDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Component
public class CustomHealthIndicator implements HealthIndicator {

    // Do sprawdzania dostępności bazy danych PostgreSQL
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Health health() {
        try {
            DockerHttpClient httpClient = new OkDockerHttpClient.Builder()
                    .dockerHost(URI.create("tcp://localhost:2375"))
                    .build();

            DockerClient dockerClient = DockerClientBuilder.getInstance()
                    .withDockerHttpClient(httpClient)
                    .build();

            System.out.println("Docker info: " + dockerClient.infoCmd().exec());

            // Pobieranie wszystkich kontenerów
            List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();
            boolean allContainersRunning = true;
            boolean anyContainerRunning = false;
            List<String> containerStatuses = new ArrayList<>();

            for (Container container : containers) {
                String containerName = container.getNames()[0];
                String containerStatus = container.getStatus();
                System.out.println("Container Name: " + containerName + ", Status: " + containerStatus);
                containerStatuses.add(containerName + ": " + containerStatus);

                // Sprawdzanie czy kontener just uruchomiony
                if (!containerStatus.contains("Up")) {
                    // Co najmniej 1 kontener jest uruchomiony
                    anyContainerRunning = true;
                } else {
                    // Wszystkie kontenery są zatrzymane
                    allContainersRunning = false;
                }
            }

            // Sprawdzanie zdrowia bazy danych i zasobów systemowych
            boolean databaseHealthy = checkDatabaseHealth();
            boolean systemResourcesHealthy = checkSystemResources();
            System.out.println("CustomHealthIndicator is called");

            // Logika statusu kontenerów
            Health.Builder healthBuilder = allContainersRunning ? Health.up() : Health.down();

            // Żaden kontener nie jest uruchomiony
            if (!anyContainerRunning) {
                healthBuilder.withDetail("containers", "All containers are stopped");
            } else if (!allContainersRunning) {
                // Nie wszystkie kontenery są uruchomione
                healthBuilder.withDetail("containers", "Some containers are running");
            } else {
                // Wszystkie kontenery są wyłączone
                healthBuilder.withDetail("containers", "All containers are down");
            }

            // Kontenery do zwrócenia
            healthBuilder.withDetail("containerStatuses", containerStatuses);

            if (!databaseHealthy) {
                healthBuilder.withDetail("database", "Unavailable");
            }

            if (!systemResourcesHealthy) {
                healthBuilder.withDetail("systemResources", "Resource limits exceeded");
            }
            return healthBuilder.build();

        } catch (Exception e) {
            return Health.down(e).withDetail("error", "Exception occurred").build();
        }
    }

    // Sprawdzanie stanu bazy danych PostgreSQL
    private boolean checkDatabaseHealth() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    // Sprawdzanie dostepności zasobów systemowych (CPI i pamięć)
    private boolean checkSystemResources(){

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

        // Sprawdzanie dostępności CPU (% zużycia CPU)
        double cpuLoad = osBean.getSystemLoadAverage();

        // Sprawdzanie dostępności pamięci
        long freeMemory = Runtime.getRuntime().freeMemory();
        long totalMemory = Runtime.getRuntime().totalMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = Runtime.getRuntime().maxMemory();
        double memoryUsage = (double) usedMemory / maxMemory;

        // Limit: CPU < 80% i pamięć < 80% zużycia
        return cpuLoad < 0.8 && memoryUsage < 0.8;
    }
}
