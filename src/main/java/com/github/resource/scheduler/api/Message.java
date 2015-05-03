package com.github.resource.scheduler.api;

import com.github.resource.scheduler.Group;

/**
 * An interface describing the message that will be queued and processed by the external resource.
 *
 * @author brighton
 */
public interface Message {

    /**
     * Is invoked when the processing of the message is completed. It is up to
     * the implementation to perform any action synchronously or asynchronously.
     */
    public void completed();

    /**
     * Indicates whether the message is the last message in the group.
     *
     * @return true if the message is the last message on the group
     */
    public boolean isTerminationMessage();

    /**
     * An identifier for the group.
     *
     * @return the group identifier
     */
    public long getGroupId();

    /**
     * Perform these actions when the message processing is completed.
     *
     * @param action to be performed
     */
    public void addCompletedAction(Action action);

    /**
     * Stores reference to the group. Saves time by avoiding unnecessary lookup
     * for the group.
     *
     * @param group
     */
    public void setGroup(Group group);

    /**
     * Provides a reference to the message group
     *
     * @return a reference to the group
     */
    public Group getGroup();
}
