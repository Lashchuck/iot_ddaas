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
import java.util.Map;

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
            List<Map<String, String>> containerStatuses = new ArrayList<>();

            for (Container container : containers) {
                String containerName = container.getNames()[0];
                String containerStatus = container.getStatus();

                containerStatuses.add(Map.of(
                        "name", containerName,
                        "status", containerStatus
                ));

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

            // Logika statusu kontenerów
            Health.Builder healthBuilder = anyContainerRunning && databaseHealthy && systemResourcesHealthy
                    ? Health.up() : Health.down();

            healthBuilder
                    .withDetail("containers", containerStatuses)
                    .withDetail("allContainersRunning", allContainersRunning)
                    .withDetail("anyContainerRunning", anyContainerRunning)
                    .withDetail("database", databaseHealthy ? "Available" : "Unavailable")
                    .withDetail("systemResources", systemResourcesHealthy ? "Healthy" : "Resource limits exceeded");

            return healthBuilder.build();

        } catch (Exception e) {
            return Health.down(e).withDetail("error", e.getMessage()).build();
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
