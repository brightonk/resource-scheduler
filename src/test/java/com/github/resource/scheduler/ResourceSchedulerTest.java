package com.github.resource.scheduler;

import com.github.resource.scheduler.api.Resource;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Brighton
 */
public class ResourceSchedulerTest {

    private static final Logger LOG = Logger.getLogger(ResourceSchedulerTest.class.getName());

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }
    private ConcurrentLinkedDeque<Group> queue;
    private LinkedList<Resource> initialResources;
    private ResourceScheduler instance;
    private DefaultResource resource;
    private DefaultMessage message;

    public ResourceSchedulerTest() {
    }

    @Before
    public void setUp() throws Exception {
        this.queue = new ConcurrentLinkedDeque<>();
        this.message = new DefaultMessage(1, 1);
        this.initialResources = new LinkedList<>();
        this.resource = new DefaultResource();
        this.instance = new ResourceScheduler(queue, initialResources);
    }

    @After
    public void tearDown() {
        this.instance.shutdown();
        this.queue.clear();
        this.initialResources.clear();
    }

    /**
     * Test of shutdown method, of class ResourceScheduler.
     */
    @Test
    public void testShutdown() {
        System.out.println("shutdown");
        instance.shutdown();
    }

    /**
     * Test of addResource method, of class ResourceScheduler.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testAddResource() throws Exception {
        System.out.println("addResource");

        ConcurrentLinkedDeque<DefaultMessage> expectedMessageOrder = new ConcurrentLinkedDeque<>();
        expectedMessageOrder.add(message);

        final ConcurrentLinkedDeque<DefaultMessage> completedMessages = new ConcurrentLinkedDeque<>();
        message.addCompletedAction(() -> {
            completedMessages.add(message);
        });

        instance.addResource(resource);
        instance.receive(message);

        // wait for the artificial delay introduced by the external resource
        Thread.sleep(2000L);

        // expecting one messages to be completed
        Object[] expecteds = expectedMessageOrder.toArray();
        Object[] actuals = completedMessages.toArray();

        org.junit.Assert.assertArrayEquals(expecteds, actuals);
    }

    /**
     * Test of removeResource method, of class ResourceScheduler.
     *
     * @throws java.lang.Exception when an error occurs
     */
    @Test
    public void testRemoveResource() throws Exception {
        System.out.println("removeResource");

        ConcurrentLinkedDeque<DefaultMessage> expectedMessageOrder = new ConcurrentLinkedDeque<>();

        final ConcurrentLinkedDeque<DefaultMessage> completedMessages = new ConcurrentLinkedDeque<>();
        message.addCompletedAction(() -> {
            completedMessages.add(message);
        });

        instance.removeResource(resource);
        instance.receive(message);

        // wait for the artificial delay introduced by the external resource
        Thread.sleep(2000L);

        // expecting zero messages completed
        Object[] expecteds = expectedMessageOrder.toArray();
        Object[] actuals = completedMessages.toArray();

        org.junit.Assert.assertArrayEquals(expecteds, actuals);
    }

    /**
     * Test of cancelGroup method, of class ResourceScheduler.
     *
     * @throws java.lang.Exception when an error occurs
     */
    @Test(expected = Exception.class)
    public void testCancelGroup() throws Exception {
        System.out.println("cancelGroup");
        long groupId = 1L;
        instance.cancelGroup(groupId);
        instance.receive(message);
    }

    /**
     * Test of receive method, of class ResourceScheduler.
     *
     * @throws java.lang.Exception when an error occurs
     */
    @Test
    public void testReceive() throws Exception {
        System.out.println("receive");

        ConcurrentLinkedDeque<DefaultMessage> expectedMessageOrder = new ConcurrentLinkedDeque<>();
        expectedMessageOrder.add(message);

        final ConcurrentLinkedDeque<DefaultMessage> completedMessages = new ConcurrentLinkedDeque<>();
        message.addCompletedAction(() -> {
            completedMessages.add(message);
        });

        instance.addResource(resource);
        instance.receive(message);

        // wait for the artificial delay introduced by the external resource
        Thread.sleep(2000L);

        // expecting one messages to be completed
        Object[] expecteds = expectedMessageOrder.toArray();
        Object[] actuals = completedMessages.toArray();

        org.junit.Assert.assertArrayEquals(expecteds, actuals);
    }

    /**
     * Test of receive method, of class ResourceScheduler.
     * <pre>
     * For a single resource, messages received:
     *      message1 (group2)
     *      message2 (group1)
     *      message3 (group2)
     *      message4 (group3)
     *      message1 (group2)
     * was received first so will be processed first
     * as messages complete, the order they are sent to the gateway should be:
     *      message1
     *      message3 (it's part of group2, which is already "in-progress")
     *      message2
     *      message4
     *
     * </pre>
     *
     * @throws java.lang.Exception when an error occurs
     */
    @Test
    public void testPrioritisingFourMsgsOneResource() throws Exception {
        System.out.println("receive");
        DefaultMessage msg1 = new DefaultMessage(1, 2);
        DefaultMessage msg2 = new DefaultMessage(2, 1);
        DefaultMessage msg3 = new DefaultMessage(3, 2);
        msg3.setTerminationMessage(true);
        DefaultMessage msg4 = new DefaultMessage(4, 3);

        ConcurrentLinkedDeque<DefaultMessage> expectedMessageOrder = new ConcurrentLinkedDeque<>();
        expectedMessageOrder.add(msg1);
        expectedMessageOrder.add(msg3);
        expectedMessageOrder.add(msg2);
        expectedMessageOrder.add(msg4);

        final ConcurrentLinkedDeque<DefaultMessage> completedMessages = new ConcurrentLinkedDeque<>();
        msg1.addCompletedAction(() -> {
            completedMessages.add(msg1);
        });
        msg2.addCompletedAction(() -> {
            completedMessages.add(msg2);
        });
        msg3.addCompletedAction(() -> {
            completedMessages.add(msg3);
        });
        msg4.addCompletedAction(() -> {
            completedMessages.add(msg4);
        });

        instance.addResource(resource);
        instance.receive(msg1);
        instance.receive(msg2);
        instance.receive(msg3);
        instance.receive(msg4);

        // wait until all messages are processed
        while (completedMessages.size() < 4) {
            Thread.sleep(2000L);
        }

        Object[] expecteds = expectedMessageOrder.toArray();
        Object[] actuals = completedMessages.toArray();

        org.junit.Assert.assertArrayEquals(expecteds, actuals);
    }
}
