package com.vmware.action.commitInfo;

import com.vmware.action.base.BaseCommitAction;
import com.vmware.config.ActionDescription;
import com.vmware.config.WorkflowConfig;

import static com.vmware.util.StringUtils.addNewLinesIfNeeded;

@ActionDescription("Formats commit lines to match max lengths.")
public class FormatCommitText extends BaseCommitAction {

    public FormatCommitText(WorkflowConfig config) {
        super(config);
    }

    @Override
    public void process() {
        if (draft.summary.length() > config.maxSummaryLength) {
            throw new IllegalArgumentException("Commit summary is greater than max length " + config.maxSummaryLength);
        }

        draft.description = addNewLinesIfNeeded(draft.description, config.maxDescriptionLength, 0);
        draft.testingDone = addNewLinesIfNeeded(draft.testingDone, config.maxDescriptionLength,
                config.testingDoneLabel.length());
    }
}
