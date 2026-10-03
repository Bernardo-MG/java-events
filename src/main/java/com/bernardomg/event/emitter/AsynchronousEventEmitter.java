/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2023-2025 the original author or authors.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.bernardomg.event.emitter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bernardomg.event.domain.AbstractEvent;
import com.bernardomg.event.listener.EventListener;

/**
 * Event emitter which works in an asynchronous way.
 */
public final class AsynchronousEventEmitter implements EventEmitter {

    /**
     * Logger for the class.
     */
    private static final Logger                               log = LoggerFactory
        .getLogger(AsynchronousEventEmitter.class);

    private final BiConsumer<AbstractEvent, RuntimeException> errorHandler;

    private final Executor                                    executor;

    /**
     * Listeners which can capture the events.
     */
    private final Map<Class<?>, List<EventListener<?>>>       listeners;

    /**
     * @param lsts
     *            listeners for the emitter
     * @param executor
     *            application-owned executor
     * @param errorHandler
     *            handler for listener failures from fire-and-forget emit; must be thread-safe and must not throw
     */
    public AsynchronousEventEmitter(final Collection<EventListener<?>> lsts, final Executor executor,
            final BiConsumer<AbstractEvent, RuntimeException> errorHandler) {
        listeners = Objects.requireNonNull(lsts)
            .stream()
            .collect(Collectors.groupingBy(EventListener::getEventType));
        this.executor = Objects.requireNonNull(executor);
        this.errorHandler = Objects.requireNonNull(errorHandler);
    }

    /**
     * Submits an event without waiting. Submission failures, including rejection, propagate to the caller; listener
     * runtime failures go to the error handler.
     */
    @Override
    public final <E extends AbstractEvent> void emit(final E event) {

        log.debug("Emiting event of type {} to listeners", event.getClass());

        executor.execute(() -> {
            try {
                dispatch(event);
            } catch (final RuntimeException exception) {
                errorHandler.accept(event, exception);
            }
        });

        log.debug("Emited event of type {} to listeners", event.getClass());
    }

    @SuppressWarnings("unchecked")
    private final <E extends AbstractEvent> void dispatch(final E event) {
        final Collection<EventListener<?>> found;

        log.debug("Emiting async event of type {} to listeners", event.getClass());

        found = listeners.getOrDefault(event.getClass(), List.of());

        log.debug("Found listeners for event of type {}: {}", event.getClass(), found);

        found.stream()
            .map(listener -> (EventListener<E>) listener)
            .forEach(listener -> listener.handle(event));

        log.debug("Emited async event of type {} to listeners", event.getClass());
    }
}
