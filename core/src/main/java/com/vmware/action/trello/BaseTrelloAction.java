package com.vmware.action.trello;

import com.vmware.action.base.BaseMultiActionDataSupport;
import com.vmware.config.WorkflowConfig;
import com.vmware.trello.Trello;
import com.vmware.trello.domain.Board;
import com.vmware.trello.domain.Swimlane;
import com.vmware.util.logging.Padder;

public abstract class BaseTrelloAction extends BaseMultiActionDataSupport {

    protected Trello trello;

    protected Board selectedBoard;

    public BaseTrelloAction(WorkflowConfig config) {
        super(config);
    }

    @Override
    public void preprocess() {
        this.trello = serviceLocator.getTrello();
    }

    public void setSelectedBoard(Board selectedBoard) {
        this.selectedBoard = selectedBoard;
    }

    public Board getSelectedBoard() {
        return selectedBoard;
    }

    protected void createTrelloBoard(String boardName) {
        Board boardToCreate = new Board(boardName);

        Padder padder = new Padder("Trello Board {} creation", boardToCreate.name);
        padder.infoTitle();
        log.info("Creating trello board");

        Board createdBoard = trello.createBoard(boardToCreate);
        Swimlane[] swimlanes = trello.getSwimlanesForBoard(createdBoard);

        log.info("Closing all existing lanes except for todo lane");
        for (int i = 1; i < swimlanes.length; i ++) {
            log.info("Closing unneeded board {}", swimlanes[i].name);
            trello.closeSwimlane(swimlanes[i]);
        }

        for (Integer storyPointValue : config.storyPointValues) {
            Swimlane swimlaneToCreate = new Swimlane(createdBoard, storyPointValue + Swimlane.STORY_POINTS_SUFFIX);
            log.info("Creating swimlane {}", swimlaneToCreate.name);
            trello.createSwimlane(swimlaneToCreate);
        }

        log.info("Creating parking lot lane");
        trello.createSwimlane(new Swimlane(createdBoard, "Parking Lot"));
        padder.infoTitle();

        selectedBoard = createdBoard;
    }
}
