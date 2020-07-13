/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.eventstream.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.manager.ComponentLifecycleException;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.phase.Disposable;
import org.xwiki.component.phase.Initializable;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.concurrent.ContextStoreManager;
import org.xwiki.eventstream.EntityEvent;
import org.xwiki.eventstream.Event;
import org.xwiki.eventstream.EventStatus;
import org.xwiki.eventstream.EventStore;
import org.xwiki.eventstream.EventStreamException;
import org.xwiki.eventstream.events.EventStreamAddedEvent;
import org.xwiki.eventstream.events.EventStreamDeletedEvent;
import org.xwiki.eventstream.events.MailEntityAddedEvent;
import org.xwiki.eventstream.events.MailEntityDeleteEvent;
import org.xwiki.eventstream.internal.events.EventStatusAddOrUpdatedEvent;
import org.xwiki.eventstream.internal.events.EventStatusDeletedEvent;
import org.xwiki.observation.ObservationManager;

/**
 * Helper to implement asynchronous writing of events.
 * 
 * @version $Id$
 * @since 12.5RC1
 */
public abstract class AbstractAsynchronousEventStore implements EventStore, Initializable, Disposable
{
    private static final List<String> CONTEXT_ENTRIES = Arrays.asList("user", "author", "wiki");

    /**
     * The type of task.
     * 
     * @version $Id$
     */
    protected enum EventStoreTaskType
    {
        SAVE_EVENT,

        /**
         * @since 12.6RC1
         */
        SAVE_STATUS,

        /**
         * @since 12.6RC1
         */
        SAVE_MAIL_ENTITY,

        DELETE_EVENT,

        DELETE_EVENT_BY_ID,

        DELETE_STATUS,

        /**
         * @since 12.6RC1
         */
        DELETE_MAIL_ENTITY,

        /**
         * @since 12.6RC1
         */
        PREFILTER_EVENT
    }

    /**
     * A queued store task.
     * 
     * @param <O> the return type of the task
     * @param <I> the input type of the task
     * @version $Id$
     */
    protected static class EventStoreTask<O, I>
    {
        /**
         * An order to stop the processing thread.
         */
        public static final EventStoreTask<Object, Object> STOP = new EventStoreTask<>(null, null, null);

        private final CompletableFuture<O> future;

        private final I input;

        private final EventStoreTaskType type;

        private final Map<String, Serializable> context;

        private O output;

        protected EventStoreTask(I input, EventStoreTaskType type, Map<String, Serializable> contextStore)
        {
            this.input = input;
            this.type = type;
            this.context = contextStore;

            this.future = new CompletableFuture<>();
        }

        /**
         * @return the input the input
         */
        public I getInput()
        {
            return this.input;
        }

        /**
         * @return the type
         */
        public EventStoreTaskType getType()
        {
            return this.type;
        }
    }

    @Inject
    protected Logger logger;

    @Inject
    protected ComponentDescriptor<EventStore> descriptor;

    @Inject
    protected ObservationManager observation;

    @Inject
    private ContextStoreManager contextStore;

    @Inject
    private Execution execution;

    private BlockingQueue<EventStoreTask<?, ?>> queue;

    private boolean notifyEach;

    private boolean notifyAll;

    private boolean disposed;

    private <O, I> CompletableFuture<O> addTask(I input, EventStoreTaskType type)
    {
        // Remember a few standard things from the context
        Map<String, Serializable> context;
        try {
            context = this.contextStore.save(CONTEXT_ENTRIES);
        } catch (ComponentLookupException e) {
            this.logger.error("Failed to save context of the event", e);

            context = null;
        }

        EventStoreTask<O, I> task = new EventStoreTask<>(input, type, context);

        addTask(task);

        return task.future;
    }

    private <O, I> void addTask(EventStoreTask<O, I> task)
    {
        try {
            this.queue.put(task);
        } catch (InterruptedException e) {
            task.future.completeExceptionally(e);

            Thread.currentThread().interrupt();
        }
    }

    @Override
    public CompletableFuture<Event> saveEvent(Event event)
    {
        return addTask(event, EventStoreTaskType.SAVE_EVENT);
    }

    @Override
    public CompletableFuture<EventStatus> saveEventStatus(EventStatus status)
    {
        return addTask(status, EventStoreTaskType.SAVE_STATUS);
    }

    @Override
    public CompletableFuture<EventStatus> saveMailEntityEvent(EntityEvent event)
    {
        return addTask(event, EventStoreTaskType.SAVE_MAIL_ENTITY);
    }

    @Override
    public CompletableFuture<Optional<Event>> deleteEvent(String eventId)
    {
        return addTask(eventId, EventStoreTaskType.DELETE_EVENT_BY_ID);
    }

    @Override
    public CompletableFuture<Optional<Event>> deleteEvent(Event event)
    {
        return addTask(event, EventStoreTaskType.DELETE_EVENT);
    }

    @Override
    public CompletableFuture<Optional<EventStatus>> deleteEventStatus(EventStatus status)
    {
        return addTask(status, EventStoreTaskType.DELETE_STATUS);
    }

    @Override
    public CompletableFuture<Optional<EventStatus>> deleteMailEntityEvent(EntityEvent event)
    {
        return addTask(event, EventStoreTaskType.DELETE_MAIL_ENTITY);
    }

    @Override
    public CompletableFuture<Event> prefilterEvent(Event event)
    {
        return addTask(event, EventStoreTaskType.PREFILTER_EVENT);
    }

    private void run()
    {
        while (!this.disposed) {
            EventStoreTask<?, ?> firstTask;
            try {
                firstTask = this.queue.take();
            } catch (InterruptedException e) {
                this.logger.warn("The thread handling asynchronous storage for event store [{}] has been interrupted",
                    this.descriptor.getRoleHint(), e);

                Thread.currentThread().interrupt();
                break;
            }

            processTasks(firstTask);
        }
    }

    private void processTasks(EventStoreTask<?, ?> firstTask)
    {
        this.execution.setContext(new ExecutionContext());

        List<EventStoreTask<?, ?>> tasks = new ArrayList<>();
        try {
            for (EventStoreTask<?, ?> task = firstTask; task != null; task = this.queue.poll()) {
                if (task != EventStoreTask.STOP) {
                    try {
                        processTask(task);
                    } catch (Exception e) {
                        task.future.completeExceptionally(e);
                    }
                }

                tasks.add(task);
            }
        } finally {
            afterTasks(tasks);

            this.execution.removeContext();
        }
    }

    private boolean processTask(EventStoreTask<?, ?> task) throws EventStreamException
    {
        switch (task.type) {
            case DELETE_EVENT:
                processTaskOutput((EventStoreTask<Optional<Event>, Event>) task, syncDeleteEvent((Event) task.input));
                break;

            case DELETE_EVENT_BY_ID:
                processTaskOutput((EventStoreTask<Optional<Event>, Event>) task, syncDeleteEvent((String) task.input));
                break;

            case SAVE_EVENT:
                processTaskOutput((EventStoreTask<Event, Event>) task, syncSaveEvent((Event) task.input));
                break;

            case DELETE_STATUS:
                processTaskOutput((EventStoreTask<Optional<EventStatus>, EventStatus>) task,
                    syncDeleteEventStatus((EventStatus) task.input));
                break;

            case SAVE_STATUS:
                processTaskOutput((EventStoreTask<EventStatus, EventStatus>) task,
                    syncSaveEventStatus((EventStatus) task.input));
                break;

            case DELETE_MAIL_ENTITY:
                processTaskOutput((EventStoreTask<Optional<EntityEvent>, EntityEvent>) task,
                    syncDeleteMailEntityEvent((EntityEvent) task.input));
                break;

            case SAVE_MAIL_ENTITY:
                processTaskOutput((EventStoreTask<EntityEvent, EntityEvent>) task,
                    syncSaveMailEntityEvent((EntityEvent) task.input));
                break;

            case PREFILTER_EVENT:
                processTaskOutput((EventStoreTask<Event, Event>) task, syncPrefilterEvent((Event) task.input));
                break;

            default:
                break;
        }

        return false;
    }

    private <O, I> void processTaskOutput(EventStoreTask<O, I> task, O output)
    {
        task.output = output;

        if (this.notifyEach) {
            complete(task, output);
        }
    }

    private <O, I> void complete(EventStoreTask<O, I> task, O output)
    {
        if (task.context != null) {
            // Restore a few things from the context in case the listener need them (for example to lookup the right
            // components for the context of the event)
            try {
                this.contextStore.restore(task.context);
            } catch (ComponentLookupException e) {
                this.logger.error("Failed to restore context of the event", output, e);
            }
        }

        // Notify Future listeners
        task.future.complete(output);

        // Notify event listeners
        switch (task.type) {
            case DELETE_EVENT:
                this.observation.notify(new EventStreamDeletedEvent(), task.output);
                break;

            case DELETE_EVENT_BY_ID:
                this.observation.notify(new EventStreamDeletedEvent(), task.output);
                break;

            case SAVE_EVENT:
                this.observation.notify(new EventStreamAddedEvent(), task.output);
                break;

            case DELETE_STATUS:
                this.observation.notify(new EventStatusDeletedEvent(), task.output);
                break;

            case SAVE_STATUS:
                this.observation.notify(new EventStatusAddOrUpdatedEvent(), task.output);
                break;

            case DELETE_MAIL_ENTITY:
                this.observation.notify(new MailEntityAddedEvent(), task.output);
                break;

            case SAVE_MAIL_ENTITY:
                this.observation.notify(new MailEntityDeleteEvent(), task.output);
                break;

            default:
                break;
        }
    }

    /**
     * @param status the event status to save
     */
    protected abstract EventStatus syncSaveEventStatus(EventStatus status) throws EventStreamException;

    /**
     * @param event the event/entity relation to save
     * @since 12.6RC1
     */
    protected abstract EntityEvent syncSaveMailEntityEvent(EntityEvent event) throws EventStreamException;

    /**
     * @param event the event to save
     */
    protected abstract Event syncSaveEvent(Event event) throws EventStreamException;

    /**
     * @param event the event to save update
     * @since 12.6RC1
     */
    protected abstract Event syncPrefilterEvent(Event event) throws EventStreamException;

    /**
     * @param status the event status to save
     */
    protected abstract Optional<EventStatus> syncDeleteEventStatus(EventStatus status) throws EventStreamException;

    /**
     * @param event the event/entity relation to delete
     * @since 12.6RC1
     */
    protected abstract Optional<EntityEvent> syncDeleteMailEntityEvent(EntityEvent event) throws EventStreamException;

    /**
     * @param eventId the id of the event to delete
     */
    protected abstract Optional<Event> syncDeleteEvent(String eventId) throws EventStreamException;

    /**
     * @param event the event to delete
     */
    protected abstract Optional<Event> syncDeleteEvent(Event event) throws EventStreamException;

    protected void afterTasks(List<EventStoreTask<?, ?>> tasks)
    {
        if (this.notifyAll) {
            for (EventStoreTask task : tasks) {
                complete(task, task.output);
            }
        }
    }

    protected void initialize(int queueSize, boolean notifyEach, boolean notifyAll)
    {
        this.notifyEach = notifyEach;
        this.notifyAll = !notifyEach && notifyAll;

        this.queue = new LinkedBlockingQueue<>(queueSize);

        Thread thread = new Thread(this::run);
        thread.setName("Asynchronous handler for event store [" + descriptor.getRoleHint() + "]");
        thread.setPriority(Thread.NORM_PRIORITY - 1);
        thread.start();
    }

    @Override
    public void dispose() throws ComponentLifecycleException
    {
        this.disposed = true;

        // Make sure to wake up the thread
        addTask(EventStoreTask.STOP);
    }
}
