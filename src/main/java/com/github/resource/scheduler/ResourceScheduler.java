package com.github.resource.scheduler;

import com.github.resource.scheduler.api.Message;
import com.github.resource.scheduler.api.MessageReceiver;
import com.github.resource.scheduler.api.Resource;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Resource scheduler class that ensure optimal use of an external resource(s).
 * It achieves this by managing the message and resource utilisation.
 *
 * @author Brighton
 */
public class ResourceScheduler implements MessageReceiver {

    private static final Logger LOG = Logger.getLogger(ResourceScheduler.class.getName());

    private static boolean isSelectable(Group group) {
        return !group.isCancelled() && !group.isTerminated() && !group.isEmpty();
    }

    private final Object queueLock;
    private final Queue<Group> queue;
    private final ConcurrentLinkedQueue<Resource> resources;
    private final ConcurrentLinkedQueue<Resource> resourcesAvailable;
    private final ExecutorService pool;

    /**
     * Constructor for the resource scheduler class. It enables different queue
     * strategy by passing a different queue as a parameter to the constructor.
     *
     * @param queue used to store messages that can not be immediately
     * processed.
     * @param resources objects used to communicate with the external objects.
     * one connection will be provided for each available external resource.
     * @throws java.lang.Exception
     */
    public ResourceScheduler(Queue<Group> queue, List<Resource> resources) throws Exception {
        if (queue == null) {
            throw new Exception("Queue<Group> is null");
        }
        this.queueLock = new Object();
        this.queue = queue;
        this.resources = new ConcurrentLinkedQueue<>();
        this.resourcesAvailable = new ConcurrentLinkedQueue<>();
        this.resources.addAll(resources);
        this.resourcesAvailable.addAll(resources);
        this.pool = Executors.newWorkStealingPool();
    }

    /**
     * Should be call to release thread pool used by the resource scheduler
     * before a graceful shutdown.
     */
    public void shutdown() {
        this.pool.shutdownNow();
    }

    /**
     * Enables new resources to be added at runtime.
     *
     * @param resource an additional resource
     * @throws java.lang.Exception
     */
    public void addResource(Resource resource) throws Exception {
        if (resource == null) {
            throw new Exception("Resource is null");
        }
        this.resources.add(resource);
        processNextGroup(resource);
    }

    /**
     * Enables an existing resources to be removed at runtime. If resource is
     * currently processing a message it will be allowed to complete the current
     * task but it will not be allocated any further messages.
     *
     * @param resource an additional resource
     */
    public void removeResource(Resource resource) {
        this.resources.remove(resource);
        this.resourcesAvailable.remove(resource);
    }

    /**
     * Tells the scheduler that a group of messages has been cancelled. Once
     * cancelled, no further messages from that group should be sent to the
     * gateway.
     *
     * @param groupId of the group that has been cancelled
     */
    public void cancelGroup(long groupId) {
        Group group = Group.getInstance(groupId, queue, queueLock);
        group.setCancelled(true);
    }

    /**
     * Receives message from internal connections and dispatches to the external
     * resource. Messages are stored in a queue when no free resource is
     * available.
     *
     * @param message received
     * @throws java.lang.Exception
     */
    @Override
    public void receive(Message message) throws Exception {
        Group group = Group.getInstance(message.getGroupId(), queue, queueLock);
        if (group.isTerminated()) {
            throw new Exception("Group has terminated. No further message will be processed from group " + message.getGroupId());
        }
        if (group.isCancelled()) {
            throw new Exception("Group was cancelled. No further message will be processed from group " + message.getGroupId());
        }
        group.setTerminated(message.isTerminationMessage());
        message.setGroup(group);

        Resource resource = resourcesAvailable.poll();
        if (resource == null) {
            group.addMessage(message);
        } else {
            processNextGroup(message, resource);
        }
    }

    /**
     * Processes the next message in the same group as the previously completed
     * message. If there are no more messages in the same group it will proceed
     * to the next group or return the resource to the collection of available
     * resources.
     *
     * @param completedMessage message that was processed
     * @param resource that has become available due to process completed
     */
    private void messageCompleted(Message completedMessage, Resource resource) {
        if (resource.isRemoved()) {
            return;
        }
        Group group = completedMessage.getGroup();
        if (group.isEmpty()) {
            processNextGroup(resource);
        } else {
            processNextGroup(group.poll(), resource);
        }
    }

    private void addMessageCompletedAction(final Message message, final Resource resource) {
        message.addCompletedAction(() -> {
            messageCompleted(message, resource);
        });
    }

    /**
     * Incorporates a resource that has recently become available. Either is was
     * add as a new resource to the system or the resource has completed
     * processing the previous message.
     *
     * @param resource the recently available resource
     */
    private void processNextGroup(Resource resource) {
        Group nextGroup = null;
        Message message = null;
        // synchronize when performing queue operations, just in case 
        // the queue provided is not thread-safe
        synchronized (queueLock) {
            for (Group group : queue) {
                if (isSelectable(group)) {
                    nextGroup = group;
                    message = nextGroup.poll();
                    break;
                }
            }
        }
        if (nextGroup == null || message == null) {
            resourcesAvailable.add(resource);
        } else {
            processNextGroup(message, resource);
        }
    }

    private void processNextGroup(Message message, Resource resource) {
        addMessageCompletedAction(message, resource);
        pool.submit(() -> {
            resource.send(message);
        });
    }

}
