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
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bernardomg.event.domain.AbstractEvent;
import com.bernardomg.event.listener.EventListener;

/**
 * Event emitter which works in a synchronous manner.
 */
public final class SynchronousEventEmitter implements EventEmitter {

    /**
     * Logger for the class.
     */
    private static final Logger                         log = LoggerFactory.getLogger(SynchronousEventEmitter.class);

    /**
     * Listeners which can capture the events.
     */
    private final Map<Class<?>, List<EventListener<?>>> listeners;

    public SynchronousEventEmitter(final Collection<EventListener<?>> lsts) {
        super();

        listeners = lsts.stream()
            .collect(Collectors.groupingBy(EventListener::getEventType));
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <E extends AbstractEvent> void emit(final E event) {
        final List<EventListener<?>> found;

        log.debug("Emiting event of type {} to listeners", event.getClass());

        found = listeners.getOrDefault(event.getClass(), List.of());

        log.debug("Found listeners for event of type {}: {}", event.getClass(), found);

        found.stream()
            .map(l -> (EventListener<E>) l)
            .forEach(l -> l.handle(event));
    }

}
