package com.smartbear.ready.jenkins;

import java.io.File;

public class ParameterContainer {
    private String virtNames;
    private String pathToProjectFile;
    private String projectFilePassword;
    private String javaHome;
    private String pathToSettingsFile;
    private String settingsFilePassword;
    private boolean saveAfterRun;
    private int startupTimeOut;
    private boolean enableUsageStatistics;
    private boolean enableVirtRunnerOutput;
    private String systemProperties;
    private String globalProperties;
    private String projectProperties;
    private String additionalCommandLine;
    private File workspace;

    public String getVirtNames() {
        return virtNames;
    }

    public String getPathToProjectFile() {
        return pathToProjectFile;
    }

    public String getProjectFilePassword() {
        return projectFilePassword;
    }

    public String getJavaHome() {
        return javaHome;
    }

    public String getPathToSettingsFile() {
        return pathToSettingsFile;
    }

    public String getSettingsFilePassword() {
        return settingsFilePassword;
    }

    public boolean isSaveAfterRun() {
        return saveAfterRun;
    }

    public int getStartupTimeOut() {
        return startupTimeOut;
    }

    public boolean isEnableUsageStatistics() {
        return enableUsageStatistics;
    }

    public boolean isEnableVirtRunnerOutput() {
        return enableVirtRunnerOutput;
    }

    public String getSystemProperties() {
        return systemProperties;
    }

    public String getGlobalProperties() {
        return globalProperties;
    }

    public String getProjectProperties() {
        return projectProperties;
    }

    public String getAdditionalCommandLine() {
        return additionalCommandLine;
    }

    public File getWorkspace() {
        return workspace;
    }

    public static class Builder {
        ParameterContainer parameterContainer = new ParameterContainer();

        public ParameterContainer build() {
            return parameterContainer;
        }

        public Builder withVirtNames(String virtNames) {
            parameterContainer.virtNames = virtNames;
            return this;
        }

        public Builder withPathToProjectFile(String pathToProjectFile) {
            parameterContainer.pathToProjectFile = pathToProjectFile;
            return this;
        }

        public Builder withProjectFilePassword(String projectFilePassword) {
            parameterContainer.projectFilePassword = projectFilePassword;
            return this;
        }

        public Builder withjavaHome(String javaHome) {
            parameterContainer.javaHome = javaHome;
            return this;
        }

        public Builder withPathToSettingsFile(String pathToSettingsFile) {
            parameterContainer.pathToSettingsFile = pathToSettingsFile;
            return this;
        }

        public Builder withSettingsFilePassword(String settingsFilePassword) {
            parameterContainer.settingsFilePassword = settingsFilePassword;
            return this;
        }

        public Builder withSaveAfterRun(boolean saveAfterRun) {
            parameterContainer.saveAfterRun = saveAfterRun;
            return this;
        }

        public Builder withStartupTimeOut(int startupTimeOut) {
            parameterContainer.startupTimeOut = startupTimeOut;
            return this;
        }

        public Builder withEnableUsageStatistics(boolean enableUsageStatistics) {
            parameterContainer.enableUsageStatistics = enableUsageStatistics;
            return this;
        }

        public Builder withEnableVirtRunnerOutput(boolean enableVirtRunnerOutput) {
            parameterContainer.enableVirtRunnerOutput = enableVirtRunnerOutput;
            return this;
        }

        public Builder withSystemProperties(String systemProperties) {
            parameterContainer.systemProperties = systemProperties;
            return this;
        }

        public Builder withGlobalProperties(String globalProperties) {
            parameterContainer.globalProperties = globalProperties;
            return this;
        }

        public Builder withProjectProperties(String projectProperties) {
            parameterContainer.projectProperties = projectProperties;
            return this;
        }
        public Builder withAdditionalCommandLine(String additionalCommandLine) {
            parameterContainer.additionalCommandLine = additionalCommandLine;
            return this;
        }

        public Builder withWorkspace(File workspace) {
            parameterContainer.workspace = workspace;
            return this;
        }
    }
}
