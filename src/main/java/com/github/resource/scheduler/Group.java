package com.github.resource.scheduler;

import com.github.resource.scheduler.api.Message;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

/**
 *
 * @author Brighton
 */
public class Group {

    private static final Logger LOG = Logger.getLogger(Group.class.getName());

    public static Group getInstance(long groupId, Queue<Group> queue, final Object queueLock) {
        synchronized (queueLock) {
            for (Group group : queue) {
                if (groupId == group.getId()) {
                    return group;
                }
            }
            Group group = new Group(groupId);
            queue.add(group);
            return group;
        }
    }
    private final ConcurrentLinkedQueue<Message> messages;
    private final long id;
    private boolean cancelled;
    private boolean terminated;

    private Group(long id) {
        this.id = id;
        this.messages = new ConcurrentLinkedQueue<>();
    }

    public long getId() {
        return id;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isTerminated() {
        return terminated;
    }

    public void setTerminated(boolean terminationMessage) {
        this.terminated = terminationMessage;
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public Message poll() {
        return messages.poll();
    }

    @Override
    public String toString() {
        return "group" + id;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Group) {
            Group other = (Group) o;
            return other.getId() == this.id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }
}
