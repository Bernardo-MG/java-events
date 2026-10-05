
package com.bernardomg.event.test.emitter;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bernardomg.event.emitter.SynchronousEventEmitter;
import com.bernardomg.event.listener.EventListener;
import com.bernardomg.event.test.config.AlternativeTestEvent;
import com.bernardomg.event.test.config.TestEvent;

@ExtendWith(MockitoExtension.class)
@DisplayName("SynchronousEventEmitter - emit")
class TestSynchronousEventEmitter {

    public TestSynchronousEventEmitter() {
        super();
    }

    @Test
    @DisplayName("When an event is emitted and there are multiple listeners for it, then all the listeners handle the event")
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

        event = new TestEvent("abc");

        // WHEN
        emitter.emit(event);

        // THEN
        assertAll(() -> verify(listenerA).handle(event), () -> verify(listenerB).handle(event));
    }

    @Test
    @DisplayName("When an event is emitted and there are no listeners, then no exception is thrown")
    void testEmit_NoListeners() {
        final SynchronousEventEmitter      emitter;
        final Collection<EventListener<?>> listeners;
        final TestEvent                    event;
        final Executable                   action;

        // GIVEN
        listeners = List.of();

        emitter = new SynchronousEventEmitter(listeners);

        event = new TestEvent("abc");

        // WHEN
        action = () -> emitter.emit(event);

        // THEN
        assertDoesNotThrow(action);
    }

    @Test
    @DisplayName("When an event is emitted and there is a listener for it, then the listener handles the event")
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

        event = new TestEvent("abc");

        // WHEN
        emitter.emit(event);

        // THEN
        verify(listener).handle(event);
    }

    @Test
    @DisplayName("When an event is emitted and the listener is for another event type, then the listener is not called")
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

        event = new AlternativeTestEvent("abc");

        // WHEN
        emitter.emit(event);

        // THEN
        verify(listener, never()).handle(any());
    }

}
