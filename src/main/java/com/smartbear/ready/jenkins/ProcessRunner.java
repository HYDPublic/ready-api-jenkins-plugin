package com.smartbear.ready.jenkins;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ProcessRunner {

    public static final String VIRT_RUNNER_CLASS = "com.smartbear.ready.cmd.runner.pro.CommandLineVirtRunner";
    public static final String JAVA_PATH_FROM_JAVA_HOME = System.getProperty("os.name")
            .toLowerCase(Locale.ENGLISH).contains("windows") ? "jre/bin/java.exe" : "jre/bin/java";

    public Process run(final PrintStream out, final ParameterContainer params)
            throws IOException {
        URL jar = ProcessRunner.class.getResource("/ready-api-libs/ready-api-runners.jar");
        final File readyDir = new File(params.getWorkspace(), "ready-api");
        readyDir.mkdirs();
        final File readyApiRunnersJar = new File(readyDir, "ready-api-runners.jar");
        IOUtils.copy(jar.openStream(), new FileOutputStream(readyApiRunnersJar));

        String java = javaFrom(params.getJavaHome(), System.getenv("JAVA_HOME"));
        List<String> processParameterList = new ArrayList<String>();
        processParameterList.addAll(Arrays.asList(java, "-cp", readyApiRunnersJar.getAbsolutePath(), VIRT_RUNNER_CLASS));
        if (StringUtils.isNotBlank(params.getVirtNames())) {
            processParameterList.addAll(Arrays.asList("-m", params.getVirtNames()));
        }
        if (StringUtils.isNotBlank(params.getProjectFilePassword())) {
            processParameterList.addAll(Arrays.asList("-x", params.getProjectFilePassword()));
        }
        if (StringUtils.isNotBlank(params.getPathToSettingsFile())) {
            processParameterList.addAll(Arrays.asList("-s", params.getPathToSettingsFile()));
        }
        if (StringUtils.isNotBlank(params.getSettingsFilePassword())) {
            processParameterList.addAll(Arrays.asList("-v", params.getSettingsFilePassword()));
        }
        if (params.isSaveAfterRun()) {
            processParameterList.add("-S");
        }
        if (!params.isEnableUsageStatistics()) {
            processParameterList.add("-O");
        }
        addProperties(processParameterList, "-D", params.getSystemProperties());
        addProperties(processParameterList, "-G", params.getGlobalProperties());
        addProperties(processParameterList, "-P", params.getProjectProperties());
        if(StringUtils.isNotBlank(params.getAdditionalCommandLine())){
            for (String additionalCommand : params.getAdditionalCommandLine().split("\n")) {
                processParameterList.add(additionalCommand);
            }
        }
        if (StringUtils.isNotBlank(params.getPathToProjectFile())) {
            processParameterList.add(params.getPathToProjectFile());
        }
        ProcessBuilder pb = new ProcessBuilder(processParameterList)
                .redirectErrorStream(true)
                .directory(readyDir);

        out.println("Starting ServiceV Virts process");
        String lastParameter = null;
        for (String parameter : processParameterList) {
            if("-x".equals(lastParameter) || "-v".equals(lastParameter)){
                parameter = "********";
            }
            out.print(parameter + " ");
            lastParameter = parameter;
        }
        out.println();
        final Process process = pb.start();
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Callable<Boolean> parseTask = new Callable<Boolean>() {
            public Boolean call() throws Exception {
                String s;
                while ((s = bufferedReader.readLine()) != null) {
                    if (params.isEnableVirtRunnerOutput()) {
                        out.println(s);
                    }
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
        if (params.isEnableVirtRunnerOutput()) {
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
        }
        return process;
    }

    private void addProperties(List<String> parameters, String flag, String properties) {
        if (StringUtils.isNotBlank(properties)) {
            for (String property : properties.split("\n")) {
                parameters.addAll(Arrays.asList(flag, property));
            }
        }
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
