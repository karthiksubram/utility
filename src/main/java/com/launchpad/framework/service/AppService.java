package com.launchpad.framework.service;

import com.launchpad.framework.component.AppRegistryLoader;

import com.launchpad.framework.component.ProcessShutdownHook;
import com.launchpad.framework.dto.AppMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class AppService {

    @Value("${app.logs.location}") // Injects the value
    private  String LOG_DIRECTORY;

    private static final String APP_REGISTRY_FILE = "apps_registry.txt";
    private final AppRegistryLoader appRegistryLoader;
    @Autowired
    ProcessShutdownHook processShutdownHook;
    private static final AtomicLong appIdCounter = new AtomicLong(1);

    @Autowired
    public AppService(AppRegistryLoader appRegistryLoader) {
        this.appRegistryLoader = appRegistryLoader;
    }


    public String registerApp(String name, String description, MultipartFile file) {
        // Logic to save file and metadata
        return "App registered successfully";
    }

    public List<AppMetadata> getAllApps() {
        return appRegistryLoader.getAppRegistry().values().stream().collect(Collectors.toList());
    }

    public ResponseEntity<Resource> getAppFile(Long appId) {
        // Logic to retrieve file
        return ResponseEntity.ok().body(null);
    }

    private final Map<Long, Process> runningProcesses = new ConcurrentHashMap<>();
    private final Map<Long,String> appMap = new ConcurrentHashMap<>();
    private static final ExecutorService logWriterExecutor = Executors.newCachedThreadPool();


    public String executeApp(long appID) {
        try {

            AppMetadata metadata = appRegistryLoader.getAppRegistry().get(appID);

            ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", metadata.getFilePath());
            String projectRoot = System.getProperty("user.dir");
            String srcPath = projectRoot + "/src";
            processBuilder.directory(new File(srcPath));
            processBuilder.redirectErrorStream(true);


            Process process = processBuilder.start();

            processShutdownHook.addProcess(process);
            runningProcesses.put(appID, process);
            // Ensure log file is empty before writing
            Path logFilePath = Path.of(getLogFilePath(process.pid()));
            Files.write(logFilePath, new byte[0], StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);

            // Start background thread to capture logs
            logWriterExecutor.submit(() -> captureLogsToFile(process, logFilePath));
//            List<String> logs= captureLogsForTime(process,10000);
//            logs.add("------------------------------------------");
//            logs.add("Application Started Successfully with PID: " + process.pid());
            // Start a background thread to capture process output
//            new Thread(() -> captureProcessOutput(process)).start();

            Thread.sleep(10000);
            String logs = fetchLastNLinesFromFile(getLogFilePath(process.pid()), 10);
            return logs;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private  String getLogFilePath(long pid) {
        return System.getProperty("user.dir") + "/logs/" + pid + ".txt";
    }

    private  void captureLogsToFile(Process process, Path logFilePath) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath.toFile(), true))) {

            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
                writer.flush();  // Ensure real-time writing
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public  String fetchLastNLinesFromFile(String filePath, int n) {
        Deque<String> lastLines = new LinkedList<>();

        try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
            long fileLength = file.length();
            long pointer = fileLength - 1;
            int lineCount = 0;

            file.seek(pointer);
            StringBuilder sb = new StringBuilder();

            while (pointer >= 0) {
                char c = (char) file.read();
                if (c == '\n') { // Line break found
                    lineCount++;
                    lastLines.addFirst(sb.reverse().toString().trim()); // Store reversed string
                    sb.setLength(0); // Reset buffer
                    if (lineCount == n) {
                        break; // Stop when N lines are collected
                    }
                } else {
                    sb.append(c); // Append character in reverse order
                }
                file.seek(--pointer); // Move backwards
            }

            // Add the last remaining line if there was no trailing newline
            if (sb.length() > 0) {
                lastLines.addFirst(sb.reverse().toString().trim());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return String.join("\n", lastLines);
    }




    private  List<String> captureLogsForTime(Process process, long captureDurationMillis) {
        List<String> logLines = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;

            // Capture logs until the specified time limit is reached
            while ((line = reader.readLine()) != null) {
                logLines.add(line);
                if (System.currentTimeMillis() - startTime > captureDurationMillis) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return logLines;
    }

    private  void captureProcessOutput(Process process) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("OUTPUT: " + line);  // Print to console in background
            }

            // Capture error stream as well
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = errorReader.readLine()) != null) {
                System.err.println("ERROR: " + line);
            }

            int exitCode = process.waitFor();  // Wait for process to complete
            System.out.println("Process exited with code: " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String stopApp(long appID) {
        Process process = runningProcesses.get(appID);
        if (process != null) {
            process.destroy();
            runningProcesses.remove(appID);
            return "Application with PID " + appID + " stopped successfully.";
        }
        return "Application not found.";
    }
}
