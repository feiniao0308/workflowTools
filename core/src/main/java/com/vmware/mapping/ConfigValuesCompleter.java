package com.vmware.mapping;

import com.vmware.action.BaseAction;
import com.vmware.config.WorkflowValuesParser;
import com.vmware.config.WorkflowConfig;
import com.vmware.util.input.ArgumentListAware;
import com.vmware.util.input.ImprovedStringsCompleter;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;

import java.util.List;
import java.util.Set;

/**
 * Completer for JLine2.
 * Allows tab completion of workflow config values on the command line.
 */
public class ConfigValuesCompleter extends ImprovedStringsCompleter implements Completer, ArgumentListAware {

    private ConfigMappings configMappings;
    private ArgumentCompleter.ArgumentList argumentList;
    private WorkflowConfig config;

    public ConfigValuesCompleter(WorkflowConfig config) {
        this.configMappings = new ConfigMappings();
        this.config = config;
        super.values.addAll(configMappings.allConfigValues());
    }



    @Override
    public int complete(final String buffer, final int cursor, final List<CharSequence> candidates) {
        if (argumentList == null || argumentList.getArguments().length == 0) {
            return super.complete(buffer, cursor, candidates);
        }

        String workflowString = argumentList.getArguments()[0];
        WorkflowValuesParser valuesParser = null;
        valuesParser = new WorkflowValuesParser(config.workflows, config.workFlowActions);
        valuesParser.parse(workflowString.split(","));
        values.clear();
        valuesShownWhenNoBuffer.clear();
        for (Class<? extends BaseAction> foundAction : valuesParser.getActionClasses()) {
            Set<String> matchingConfigValues = configMappings.getConfigValuesForAction(foundAction);
            values.addAll(matchingConfigValues);
        }
        valuesShownWhenNoBuffer.addAll(values);
        return super.complete(buffer, cursor, candidates);
    }



    @Override
    public void setArgumentList(ArgumentCompleter.ArgumentList argumentList) {
        this.argumentList = argumentList;
    }
}
