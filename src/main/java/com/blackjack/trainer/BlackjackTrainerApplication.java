package com.blackjack.trainer;

import com.blackjack.trainer.ui.MainFrame;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;
import java.awt.*;

@SpringBootApplication
public class BlackjackTrainerApplication {

    public static void main(String[] args) {
        // Swing requires a graphical desktop environment; Spring Boot runs headless by default
        ConfigurableApplicationContext context = new SpringApplicationBuilder(BlackjackTrainerApplication.class)
                .headless(false)
                .run(args);

        // Schedule Swing UI initialization on the Event Dispatch Thread (EDT)
        EventQueue.invokeLater(() -> {
            try {
                // Set system Look and Feel for modern native UI
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            MainFrame mainFrame = context.getBean(MainFrame.class);
            mainFrame.setVisible(true);
        });
    }
}
