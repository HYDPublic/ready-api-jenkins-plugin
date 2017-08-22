package com.smartbear.ready.jenkins;

import hudson.AbortException;
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;

@SuppressWarnings("unused")
public class ReadyApiJenkinsVirtStarter extends Builder {

    private final String virtNames;
    private final String pathToProjectFile;
    private final String projectFilePassword;
    private final String pathToSettingsFile;
    private final String settingsFilePassword;
    private final boolean saveAfterRun;
    private final int startupTimeOut;
    private final boolean enableUsageStatistics;
    private final boolean enableVirtRunnerOutput;
    private final String systemProperties;
    private final String globalProperties;
    private final String projectProperties;
    private final String additionalCommandLine;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public ReadyApiJenkinsVirtStarter(String virtNames,
                                      String pathToProjectFile,
                                      String projectFilePassword,
                                      String pathToSettingsFile,
                                      String settingsFilePassword,
                                      int startupTimeOut,
                                      boolean saveAfterRun,
                                      boolean enableUsageStatistics,
                                      boolean enableVirtRunnerOutput,
                                      String additionalCommandLine,
                                      String systemProperties,
                                      String globalProperties,
                                      String projectProperties) {
        this.virtNames = virtNames;
        this.pathToProjectFile = pathToProjectFile;
        this.projectFilePassword = projectFilePassword;
        this.pathToSettingsFile = pathToSettingsFile;
        this.settingsFilePassword = settingsFilePassword;
        this.saveAfterRun = saveAfterRun;
        this.startupTimeOut = startupTimeOut;
        this.enableUsageStatistics = enableUsageStatistics;
        this.enableVirtRunnerOutput = enableVirtRunnerOutput;
        this.systemProperties = systemProperties;
        this.globalProperties = globalProperties;
        this.projectProperties = projectProperties;
        this.additionalCommandLine = additionalCommandLine;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getVirtNames() {
        return virtNames;
    }

    public String getPathToProjectFile() {
        return pathToProjectFile;
    }

    public String getProjectFilePassword() {
        return projectFilePassword;
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

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws AbortException {
        Process process = null;
        try {
            process = new ProcessRunner()
                    .run(listener.getLogger(),
                            new ParameterContainer.Builder()
                                    .withPathToProjectFile(getAbsolutePath(pathToProjectFile, build))
                                    .withVirtNames(virtNames)
                                    .withProjectFilePassword(projectFilePassword)
                                    .withjavaHome(getDescriptor().getJavaHome())
                                    .withPathToSettingsFile(getAbsolutePath(pathToSettingsFile, build))
                                    .withSettingsFilePassword(settingsFilePassword)
                                    .withSaveAfterRun(saveAfterRun)
                                    .withStartupTimeOut(startupTimeOut)
                                    .withEnableUsageStatistics(enableUsageStatistics)
                                    .withEnableVirtRunnerOutput(enableVirtRunnerOutput)
                                    .withSystemProperties(systemProperties)
                                    .withGlobalProperties(globalProperties)
                                    .withProjectProperties(projectProperties)
                                    .withAdditionalCommandLine(additionalCommandLine)
                                    .withWorkspace(new File(build.getWorkspace().toURI()))
                                    .build());
            if (process == null) {
                throw new AbortException("Could not start ServiceV Virt(s) process.");
            } else {
                new ProcessKiller(process, build.getId(), getDescriptor().getVirtRunnerTimeout() * 1000L, listener.getLogger())
                        .killAfterTimeout();
                ProcessKeeper.addProcess(build.getId(), process);
            }
        } catch (Exception e) {
            e.printStackTrace(listener.getLogger());
            if (process != null) {
                process.destroy();
            }
            throw new AbortException("Could not start ServiceV Virt(s).");
        }
        return true;
    }

    private String getAbsolutePath(String path, AbstractBuild build) {
        if (StringUtils.isBlank(path)) {
            return null;
        }
        File file;
        try {
            file = new File(Util.replaceMacro(path, build.getEnvironment()));
        } catch (Exception e) {
            file = new File(path);
        }
        if (file.exists()) {
            return file.getAbsolutePath();
        }
        try {
            file = new File(new File(build.getWorkspace().toURI()), path);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (file.exists()) {
            return file.getAbsolutePath();
        }
        return getPathToProjectFile();
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Descriptor for {@link ReadyApiJenkinsVirtStarter}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         * <p/>
         * <p/>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        private String javaHome;

        private long virtRunnerTimeout = 600; // in seconds

        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        /**
         * Performs on-the-fly validation of the form field.
         *
         * @param value This parameter receives the value that the user has typed.
         * @return Indicates the outcome of the validation. This is sent to the browser.
         * <p/>
         * Note that returning {@link FormValidation#error(String)} does not
         * prevent the form from being saved. It just means that a message
         * will be displayed to the user.
         */
        public FormValidation doCheckJavaHome(@QueryParameter String value)
                throws IOException, ServletException {
            value = value.trim();
            if (value.isEmpty()) {
                return FormValidation.ok();
            } else if (value.length() < 4) {
                return FormValidation.warning("Isn't the path too short?");
            } else if (!new File(value).isDirectory()) {
                return FormValidation.warning("This directory does not exist");
            } else if (!new File(value, ProcessRunner.JAVA_PATH_FROM_JAVA_HOME).isFile()) {
                return FormValidation.warning("This directory does not seem to contain a java executable");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckVirtRunnerTimeout(@QueryParameter long value)
                throws IOException, ServletException {
            if (value < 10L) {
                return FormValidation.error("Timeout is too short");
            }
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Start ServiceV Virts";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            javaHome = formData.getString("javaHome");
            virtRunnerTimeout = formData.getLong("virtRunnerTimeout");

            save();
            return super.configure(req, formData);
        }

        public String getJavaHome() {
            return javaHome;
        }

        public long getVirtRunnerTimeout() {
            return virtRunnerTimeout;
        }
    }

}
