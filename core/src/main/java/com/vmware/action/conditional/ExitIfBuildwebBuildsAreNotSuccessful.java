package com.vmware.action.conditional;

import com.vmware.action.base.BaseCommitWithBuildwebBuildsAction;
import com.vmware.config.ActionDescription;
import com.vmware.config.WorkflowConfig;

@ActionDescription("Reads the testing done section and checks the status for all buildweb builds found. Exits if any are not successful.")
public class ExitIfBuildwebBuildsAreNotSuccessful extends BaseCommitWithBuildwebBuildsAction {

    public ExitIfBuildwebBuildsAreNotSuccessful(WorkflowConfig config) {
        super(config);
    }

    @Override
    public void process() {
        if (draft.buildwebBuildsAreSuccessful == null) {
            log.info("");
            buildweb.checkStatusOfBuilds(draft);
            if (!draft.buildwebBuildsAreSuccessful) {
                exitDueToFailedBuilds();
            }
        } else if (!draft.buildwebBuildsAreSuccessful) {
            exitDueToFailedBuilds();
        }
    }

    private void exitDueToFailedBuilds() {
        log.info("One or more builds were not successful!");
        System.exit(0);
    }
}