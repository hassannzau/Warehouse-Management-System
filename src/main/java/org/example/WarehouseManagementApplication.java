package org.example;

import javafx.application.Application;
import org.example.ui.WarehouseDesktopApp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class WarehouseManagementApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(WarehouseManagementApplication.class, args);

        // Hand the JavaFX client (a plain Application, not a Spring bean) the one
        // setting it needs, via a system property, since it runs outside the Spring context.
        String lowStockIntervalMinutes = context.getEnvironment()
                .getProperty("lowstock.check.interval.minutes", "30");
        System.setProperty("lowstock.check.interval.minutes", lowStockIntervalMinutes);

        Application.launch(WarehouseDesktopApp.class, args);
    }
}
