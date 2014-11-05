package com.smartbear.ready.jenkins;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Locale;

public class ProcessRunner {

    public static final String VIRT_RUNNER_CLASS = "com.smartbear.ready.cmd.runner.pro.CommandLineVirtRunner";
    public static final String JAVA_PATH_FROM_JAVA_HOME = System.getProperty("os.name")
            .toLowerCase(Locale.ENGLISH).contains("windows") ? "jre/bin/java.exe" : "jre/bin/java";

    public Process run(PrintStream out, String pathToProjectFile, String virtNames, String javaHome)
            throws IOException {
        URL jar = ProcessRunner.class.getResource("/ready-api-libs/ready-api-runners.jar");
        String java = javaFrom(javaHome, System.getenv("JAVA_HOME"));
        out.println("Using java: " + java);
        out.println("Classpath: " + jar.getFile());
        out.println("Project File: " + new File(pathToProjectFile).exists());
        ProcessBuilder pb = new ProcessBuilder(java, "-cp", jar.getFile(),
                VIRT_RUNNER_CLASS,
                "-m", virtNames, "-p", pathToProjectFile)
                .inheritIO()
                .directory(new File("."));

        out.println("Here we start the process!!!!!!!!!!!!");
        return pb.start();
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

    public static void main(String[] args) throws Exception {
        Process p = new ProcessRunner().run(System.out, "D:\\SoapUI Projects\\Weather-soapui-project.xml", "Virt2", null);
        Thread.sleep(20000);
        p.destroy();
    }
}
