/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2023 the original author or authors.
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

package com.bernardomg.event.test.emitter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bernardomg.event.emitter.SynchronousEventEmitter;
import com.bernardomg.event.listener.EventListener;
import com.bernardomg.event.test.config.AlternativeTestEvent;
import com.bernardomg.event.test.config.TestEvent;

@ExtendWith(MockitoExtension.class)
@DisplayName("SynchronousEventEmitter")
class TestSynchronousEventEmitter {

    public TestSynchronousEventEmitter() {
        super();
    }

    @Test
    @DisplayName("When there are multiple listeners for an event, they all handle the event")
    @SuppressWarnings("unchecked")
    void testEmit_MultipleRegisteredListener() {
        final SynchronousEventEmitter      emitter;
        final Collection<EventListener<?>> listeners;
        final EventListener<TestEvent>     listenerA;
        final EventListener<TestEvent>     listenerB;
        final TestEvent                    event;

        // GIVEN
        listenerA = Mockito.mock(EventListener.class);
        given(listenerA.getEventType()).willReturn(TestEvent.class);

        listenerB = Mockito.mock(EventListener.class);
        given(listenerB.getEventType()).willReturn(TestEvent.class);

        listeners = List.of(listenerA, listenerB);

        emitter = new SynchronousEventEmitter(listeners);

        event = new TestEvent(this);

        // WHEN
        emitter.emit(event);

        // THEN
        verify(listenerA).handle(event);
        verify(listenerB).handle(event);
    }

    @Test
    @DisplayName("When there are no listeners, nothing is done")
    void testEmit_NoListeners() {
        final SynchronousEventEmitter      emitter;
        final Collection<EventListener<?>> listeners;

        // GIVEN
        listeners = List.of();

        emitter = new SynchronousEventEmitter(listeners);

        // WHEN
        emitter.emit(new TestEvent(this));

        // THEN
    }

    @Test
    @DisplayName("When there is a listener for an event, it handles the event")
    @SuppressWarnings("unchecked")
    void testEmit_RegisteredListener() {
        final SynchronousEventEmitter      emitter;
        final Collection<EventListener<?>> listeners;
        final EventListener<TestEvent>     listener;
        final TestEvent                    event;

        // GIVEN
        listener = Mockito.mock(EventListener.class);
        given(listener.getEventType()).willReturn(TestEvent.class);
        listeners = List.of(listener);

        emitter = new SynchronousEventEmitter(listeners);

        event = new TestEvent(this);

        // WHEN
        emitter.emit(event);

        // THEN
        verify(listener).handle(event);
    }

    @Test
    @DisplayName("When there is a listener for another event, it doesn't handle the event")
    @SuppressWarnings("unchecked")
    void testEmit_RegisteredListenerForAnother() {
        final SynchronousEventEmitter      emitter;
        final Collection<EventListener<?>> listeners;
        final EventListener<TestEvent>     listener;
        final AlternativeTestEvent         event;

        // GIVEN
        listener = Mockito.mock(EventListener.class);
        given(listener.getEventType()).willReturn(TestEvent.class);
        listeners = List.of(listener);

        emitter = new SynchronousEventEmitter(listeners);

        event = new AlternativeTestEvent(this);

        // WHEN
        emitter.emit(event);

        // THEN
        verify(listener, never()).handle(any());
    }

}
