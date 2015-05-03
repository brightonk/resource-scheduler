package com.github.resource.scheduler;

import com.github.resource.scheduler.api.Action;
import com.github.resource.scheduler.api.Message;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Logger;

/**
 *
 * @author Brighton
 */
public class DefaultMessage implements Message {

    private static final Logger LOG = Logger.getLogger(DefaultMessage.class.getName());

    private final ConcurrentLinkedDeque<Action> completedActions;
    private Group group;
    private long groupId;
    private boolean terminationMessage;
    private final int id;

    public DefaultMessage(int id, int groupId) {
        this.completedActions = new ConcurrentLinkedDeque<>();
        this.id = id;
        this.groupId = groupId;
    }

    public int getId() {
        return id;
    }

    @Override
    public void completed() {
        completedActions.stream().forEach((action) -> {
            action.perform();
        });
    }

    @Override
    public void addCompletedAction(Action action) {
        this.completedActions.add(action);
    }

    @Override
    public void setGroup(Group group) {
        this.group = group;
    }

    @Override
    public Group getGroup() {
        return this.group;
    }

    @Override
    public long getGroupId() {
        return this.groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public void setTerminationMessage(boolean terminationMessage) {
        this.terminationMessage = terminationMessage;
    }

    @Override
    public boolean isTerminationMessage() {
        return this.terminationMessage;
    }

    @Override
    public String toString() {
        return "message" + getId() + " (group" + groupId + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DefaultMessage) {
            DefaultMessage other = (DefaultMessage) o;
            return other.getId() == this.getId() && other.getGroupId() == this.groupId;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (int) (this.groupId ^ (this.groupId >>> 32));
        hash = 53 * hash + this.id;
        return hash;
    }
}
