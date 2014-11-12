package com.smartbear.ready.jenkins;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ProcessRunner {

    public static final String VIRT_RUNNER_CLASS = "com.smartbear.ready.cmd.runner.pro.CommandLineVirtRunner";
    public static final String JAVA_PATH_FROM_JAVA_HOME = System.getProperty("os.name")
            .toLowerCase(Locale.ENGLISH).contains("windows") ? "jre/bin/java.exe" : "jre/bin/java";

    public Process run(final PrintStream out, ParameterContainer params)
            throws IOException {
        URL jar = ProcessRunner.class.getResource("/ready-api-libs/ready-api-runners.jar");
        String java = javaFrom(params.getJavaHome(), System.getenv("JAVA_HOME"));
        List<String> parameters = new ArrayList<String>();
        parameters.addAll(Arrays.asList(java, "-cp", jar.getFile(), VIRT_RUNNER_CLASS));
        if (StringUtils.isNotEmpty(params.getVirtNames())) {
            parameters.addAll(Arrays.asList("-m", params.getVirtNames()));
        }
        if (StringUtils.isNotEmpty(params.getPathToProjectFile())) {
            parameters.addAll(Arrays.asList("-p", params.getPathToProjectFile()));
        }
        if (StringUtils.isNotEmpty(params.getProjectFilePassword())) {
            parameters.addAll(Arrays.asList("-x", params.getProjectFilePassword()));
        }
        if (StringUtils.isNotEmpty(params.getPathToSettingsFile())) {
            parameters.addAll(Arrays.asList("-s", params.getPathToSettingsFile()));
        }
        if (StringUtils.isNotEmpty(params.getSettingsFilePassword())) {
            parameters.addAll(Arrays.asList("-v", params.getSettingsFilePassword()));
        }
        if (params.isSaveAfterRun()) {
            parameters.add("-S");
        }
        ProcessBuilder pb = new ProcessBuilder(parameters)
                .redirectErrorStream(true)
                .directory(new File("."));

        final Process process = pb.start();
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Callable<Boolean> parseTask = new Callable<Boolean>() {
            public Boolean call() throws Exception {
                String s;
                while ((s = bufferedReader.readLine()) != null) {
                    out.println(s);
                    if (s.contains("All runners confirmed to be running!")) {
                        return true;
                    } else if (s.contains("Failed to get all runners started! Problems may occur.")) {
                        return false;
                    }
                }
                return false;
            }
        };
        final Future<Boolean> future = executor.submit(parseTask);
        try {
            final Boolean allRunning = future.get(params.getStartupTimeOut(), TimeUnit.SECONDS);
            if (!allRunning) {
                process.destroy();
                return null;
            }
        } catch (Exception e) {
            out.println("Time out waiting for Virts to start");
            process.destroy();
            return null;
        } finally {
            executor.shutdown();
        }
        new Thread(new Runnable() {
            public void run() {
                String s;
                try {
                    while ((s = bufferedReader.readLine()) != null) {
                        out.println(s);
                    }
                } catch (IOException e) {
                    e.printStackTrace(out);
                }
            }
        }).start();
        return process;
    }

    private String javaFrom(String... candidateJavaHomes) {
        for (String candidate : candidateJavaHomes) {
            if (!StringUtils.isBlank(candidate)) {
                if (!candidate.endsWith("/") && !candidate.endsWith("\\")) {
                    candidate += "/";
                }
                return candidate + JAVA_PATH_FROM_JAVA_HOME;
            }
        }
        return "java";
    }
}
