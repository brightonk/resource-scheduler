package com.github.resource.scheduler.api;

/**
 * An interface defining any subclass that is capable of receiving messages.
 *
 * @author brighton
 */
public interface MessageReceiver {

    /**
     * A method used to accept outgoing messages.
     *
     * @param message
     * @throws java.lang.Exception 
     */
    public void receive(Message message) throws Exception;
}
