package com.vmware.action;

import com.vmware.Git;
import com.vmware.Perforce;
import com.vmware.ServiceLocator;
import com.vmware.config.WorkflowConfig;
import com.vmware.util.CommandLineUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;

public abstract class BaseAction {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    protected final WorkflowConfig config;

    protected final ServiceLocator serviceLocator;

    protected final Git git;

    private String[] expectedCommandsToBeAvailable;


    public BaseAction(WorkflowConfig config) {
        this.config = config;
        this.serviceLocator = config.configuredServiceLocator();
        this.git = serviceLocator.getGit();
    }

    /**
     * @return Reason for why this action should not be run, null if it should be run
     */
    public String cannotRunAction() {
        return null;
    }

    /**
     * @return Reason why the workflow should fail, null if it should continue
     */
    public String failWorkflowIfConditionNotMet() {
        if (expectedCommandsToBeAvailable == null) {
            return null;
        }
        for (String command : expectedCommandsToBeAvailable) {
            if (!CommandLineUtils.isCommandAvailable(command)) {
                return "command " + command + " is not available";
            }
        }
        return null;
    }

    /**
     * Override if any setup is needed before the process method is called
     */
    public void preprocess() {

    }

    public abstract void process();

    protected void setExpectedCommandsToBeAvailable(String... commands) {
        this.expectedCommandsToBeAvailable = commands;
    }

}
