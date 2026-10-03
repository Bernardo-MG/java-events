
package com.bernardomg.event.test.emitter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bernardomg.event.domain.AbstractEvent;
import com.bernardomg.event.emitter.AsynchronousEventEmitter;
import com.bernardomg.event.listener.EventListener;
import com.bernardomg.event.test.config.AlternativeTestEvent;
import com.bernardomg.event.test.config.TestEvent;

@ExtendWith(MockitoExtension.class)
@DisplayName("AsynchronousEventEmitter - emit")
@Timeout(10)
class TestAsynchronousEventEmitter {

    private AsynchronousEventEmitter                    emitter;

    @Mock
    private BiConsumer<AbstractEvent, RuntimeException> errorHandler;

    @Mock
    private Executor                                    executor;

    @Mock
    private EventListener<TestEvent>                    listener;

    private AsynchronousEventEmitter getEmitterWithListeners() {
        given(listener.getEventType()).willReturn(TestEvent.class);

        return new AsynchronousEventEmitter(List.of(listener), executor, errorHandler);
    }

    private AsynchronousEventEmitter getEmitterWithoutListeners() {
        return new AsynchronousEventEmitter(List.of(), executor, errorHandler);
    }

    @Test
    @DisplayName("When a listener fails, then the error handler receives the event and the failure")
    void testEmit_Failure() {
        final TestEvent        event;
        final RuntimeException failure;

        // GIVEN
        emitter = getEmitterWithListeners();
        event = new TestEvent("abc");
        failure = new IllegalStateException("failed");

        doThrow(failure).when(listener)
            .handle(event);

        doAnswer(invocation -> {
            final Runnable task;

            task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(executor)
            .execute(any(Runnable.class));

        // WHEN
        emitter.emit(event);

        // THEN
        verify(errorHandler).accept(event, failure);
    }

    @Test
    @DisplayName("When an event is emitted and there are no listeners, then the error handler is not called")
    void testEmit_NoListeners() {
        final TestEvent event;

        // GIVEN
        emitter = getEmitterWithoutListeners();
        event = new TestEvent("abc");

        doAnswer(invocation -> {
            final Runnable task;

            task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(executor)
            .execute(any(Runnable.class));

        // WHEN
        emitter.emit(event);

        // THEN
        verify(errorHandler, never()).accept(any(), any());
    }

    @Test
    @DisplayName("When a null event is emitted, then a null pointer exception is thrown")
    void testEmit_Null() {
        final Executable action;

        // GIVEN
        emitter = getEmitterWithoutListeners();

        // WHEN
        action = () -> emitter.emit(null);

        // THEN
        assertThrows(NullPointerException.class, action);
    }

    @Test
    @DisplayName("When an event is emitted and the listener is for another event type, then the listener is not called")
    void testEmit_OtherType() {
        final AlternativeTestEvent event;

        // GIVEN
        emitter = getEmitterWithListeners();
        event = new AlternativeTestEvent("abc");

        doAnswer(invocation -> {
            final Runnable task;

            task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(executor)
            .execute(any(Runnable.class));

        // WHEN
        emitter.emit(event);

        // THEN
        verify(listener, never()).handle(any());
    }

    @Test
    @DisplayName("When an event is emitted and the task is only queued, then the listener is not called")
    void testEmit_Queued() {
        final TestEvent event;

        // GIVEN
        emitter = getEmitterWithListeners();
        event = new TestEvent("abc");

        // WHEN
        emitter.emit(event);

        // THEN
        verify(listener, never()).handle(any());
    }

    @Test
    @DisplayName("When a queued task is run, then the matching listener handles the event")
    void testEmit_QueuedDispatch() {
        final AtomicReference<Runnable> task;
        final TestEvent                 event;

        // GIVEN
        emitter = getEmitterWithListeners();
        task = new AtomicReference<>();
        event = new TestEvent("abc");

        doAnswer(invocation -> {
            task.set(invocation.getArgument(0));
            return null;
        }).when(executor)
            .execute(any(Runnable.class));

        // WHEN
        emitter.emit(event);
        task.get()
            .run();

        // THEN
        verify(listener).handle(event);
    }

    @Test
    @DisplayName("When an event is emitted and there is a matching listener, then the listener handles the event")
    void testEmit_RegisteredListener() {
        final TestEvent event;

        // GIVEN
        emitter = getEmitterWithListeners();
        event = new TestEvent("abc");

        doAnswer(invocation -> {
            final Runnable task;

            task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(executor)
            .execute(any(Runnable.class));

        // WHEN
        emitter.emit(event);

        // THEN
        verify(listener).handle(event);
    }

    @Test
    @DisplayName("When the executor rejects the task, then a rejected execution exception is thrown")
    void testEmit_Rejection() {
        final TestEvent                  event;
        final RejectedExecutionException failure;
        final Executable                 action;

        // GIVEN
        emitter = getEmitterWithListeners();
        event = new TestEvent("abc");
        failure = new RejectedExecutionException("full");

        doThrow(failure).when(executor)
            .execute(any(Runnable.class));

        // WHEN
        action = () -> emitter.emit(event);

        // THEN
        assertThrows(RejectedExecutionException.class, action);
    }

}
