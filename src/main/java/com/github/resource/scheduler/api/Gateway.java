package com.github.resource.scheduler.api;

/**
 * An interface that describes how to interact with the external resource.
 *
 * @author Brighton
 */
public interface Gateway {

    public void send(Message msg);
}
