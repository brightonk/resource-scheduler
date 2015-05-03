package com.github.resource.scheduler.api;

/**
 * A representation of each external resource. It describes how to interact with
 * the external resource..
 *
 * @author brighton
 */
public interface Resource extends Gateway {

    /**
     * Indicates that the resource has been removed from service. Upon
     * completing the current task it should be allocated another task.
     *
     * @return true is the resource has been removed
     */
    public boolean isRemoved();

    public void setRemoved();

}
