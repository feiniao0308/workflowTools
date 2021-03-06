package com.vmware.action.jenkins;

import com.vmware.JobBuild;
import com.vmware.action.base.BaseCommitAction;
import com.vmware.config.ActionDescription;
import com.vmware.config.WorkflowConfig;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Iterator;

@ActionDescription("Strips jenkins build text from testing done section of commit.")
public class StripJenkinsBuilds extends BaseCommitAction {

    public StripJenkinsBuilds(WorkflowConfig config) {
        super(config);
    }

    @Override
    public String cannotRunAction() {
        if (draft.jobBuildsMatchingUrl(config.jenkinsUrl).isEmpty()) {
            return "commit has no Jenkins builds";
        }
        return super.cannotRunAction();
    }

    @Override
    public void process() {
        log.info("Stripping jenkins builds from commit");
        for (JobBuild jobBuild : draft.jobBuildsMatchingUrl(config.jenkinsUrl)) {
            draft.jobBuilds.remove(jobBuild);
        }
    }
}
