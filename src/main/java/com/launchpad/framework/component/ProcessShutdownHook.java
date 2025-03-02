package com.launchpad.framework.component;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ProcessShutdownHook implements ApplicationListener<ContextClosedEvent> {

    private final List<Process> processes = new CopyOnWriteArrayList<>();

    public void addProcess(Process process) {
        processes.add(process);
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        System.out.println("Spring Boot is shutting down. Killing all child processes.");
        for (Process process : processes) {
            if (process.isAlive()) {
                process.destroy();
                try {
                    if (!process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                        process.destroyForcibly();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
