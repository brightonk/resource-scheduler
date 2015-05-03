package com.github.resource.scheduler;

import com.github.resource.scheduler.api.Message;
import com.github.resource.scheduler.api.Resource;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default implementation of the resource. For use in test cases or can be
 * extended to provide more functionality
 *
 * @author Brighton
 */
public class DefaultResource implements Resource {

    private static final Logger LOG = Logger.getLogger(DefaultResource.class.getName());

    private boolean removed;
    private long DELAY = 1000L;

    @Override
    public boolean isRemoved() {
        return this.removed;
    }

    @Override
    public void setRemoved() {
        this.removed = true;
    }

    @Override
    public void send(Message msg) {
        LOG.log(Level.INFO, "message sent. {0}", msg.toString());
        try {
            Thread.sleep(DELAY);
        } catch (InterruptedException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
        msg.completed();
    }
}
