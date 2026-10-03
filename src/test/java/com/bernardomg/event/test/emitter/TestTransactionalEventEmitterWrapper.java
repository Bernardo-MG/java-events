
package com.bernardomg.event.test.emitter;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.bernardomg.event.emitter.EventEmitter;
import com.bernardomg.event.emitter.TransactionalEventEmitterWrapper;
import com.bernardomg.event.test.config.TestEvent;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionalEventEmitterWrapper - emit")
class TestTransactionalEventEmitterWrapper {

    /**
     * Exercises Spring's real commit and rollback synchronization lifecycle without requiring a database.
     */
    private static final class TestTransactionManager extends AbstractPlatformTransactionManager {

        private static final long serialVersionUID = 1L;

        @Override
        protected void doBegin(final Object transaction, final TransactionDefinition definition) {
            // No external resource is needed for synchronization tests.
        }

        @Override
        protected void doCommit(final DefaultTransactionStatus status) {
            // AbstractPlatformTransactionManager invokes afterCommit callbacks.
        }

        @Override
        protected Object doGetTransaction() {
            return new Object();
        }

        @Override
        protected void doRollback(final DefaultTransactionStatus status) {
            // AbstractPlatformTransactionManager discards afterCommit callbacks.
        }
    }

    private TransactionalEventEmitterWrapper emitter;

    private TestTransactionManager           manager;

    private TransactionStatus                openStatus;

    private TransactionTemplate              transaction;

    @Mock
    private EventEmitter                     wrappedEmitter;

    @AfterEach
    void cleanUp() {
        if ((openStatus != null) && (!openStatus.isCompleted())) {
            manager.rollback(openStatus);
        }
        TransactionSynchronizationManager.clear();
    }

    @Test
    @DisplayName("When an event is emitted before the transaction commits, then the wrapped emitter is not called")
    void testEmit_BeforeCommit() {
        final TestEvent event;

        // GIVEN
        emitter = new TransactionalEventEmitterWrapper(wrappedEmitter);
        manager = new TestTransactionManager();
        openStatus = manager.getTransaction(new DefaultTransactionDefinition());
        event = new TestEvent("abc");

        // WHEN
        emitter.emit(event);

        // THEN
        verifyNoInteractions(wrappedEmitter);
    }

    @Test
    @DisplayName("When an event is emitted in a transaction which is committed, then the wrapped emitter emits the event")
    void testEmit_Commit() {
        final TestEvent event;

        // GIVEN
        emitter = new TransactionalEventEmitterWrapper(wrappedEmitter);
        transaction = new TransactionTemplate(new TestTransactionManager());
        event = new TestEvent("abc");

        // WHEN
        transaction.executeWithoutResult(status -> emitter.emit(event));

        // THEN
        verify(wrappedEmitter).emit(event);
    }

    @Test
    @DisplayName("When the wrapped emitter fails after the commit, then the failure is propagated")
    void testEmit_DispatchFailure() {
        final TestEvent        event;
        final RuntimeException failure;
        final Executable       action;

        // GIVEN
        emitter = new TransactionalEventEmitterWrapper(wrappedEmitter);
        transaction = new TransactionTemplate(new TestTransactionManager());
        event = new TestEvent("abc");
        failure = new IllegalStateException("dispatch failed");
        doThrow(failure).when(wrappedEmitter)
            .emit(event);

        // WHEN
        action = () -> transaction.executeWithoutResult(status -> emitter.emit(event));

        // THEN
        assertThrows(IllegalStateException.class, action);
    }

    @Test
    @DisplayName("When an exception rolls back the transaction, then the exception is propagated and the wrapped emitter is not called")
    void testEmit_ExceptionRollback() {
        final TestEvent        event;
        final RuntimeException failure;
        final Executable       action;

        // GIVEN
        emitter = new TransactionalEventEmitterWrapper(wrappedEmitter);
        transaction = new TransactionTemplate(new TestTransactionManager());
        event = new TestEvent("abc");
        failure = new IllegalStateException("rollback");

        // WHEN
        action = () -> transaction.executeWithoutResult(status -> {
            emitter.emit(event);
            throw failure;
        });

        // THEN
        assertAll(() -> assertSame(failure, assertThrows(IllegalStateException.class, action)),
            () -> verifyNoInteractions(wrappedEmitter));
    }

    @Test
    @DisplayName("When an event is emitted without a transaction, then the wrapped emitter emits the event immediately")
    void testEmit_NoTransaction() {
        final TestEvent event;

        // GIVEN
        emitter = new TransactionalEventEmitterWrapper(wrappedEmitter);
        event = new TestEvent("abc");

        // WHEN
        emitter.emit(event);

        // THEN
        verify(wrappedEmitter).emit(event);
    }

    @Test
    @DisplayName("When a null event is emitted, then a null pointer exception is thrown")
    void testEmit_Null() {
        final Executable action;

        // GIVEN
        emitter = new TransactionalEventEmitterWrapper(wrappedEmitter);

        // WHEN
        action = () -> emitter.emit(null);

        // THEN
        assertThrows(NullPointerException.class, action);
    }

    @Test
    @DisplayName("When an event is emitted in a transaction which is rolled back, then the wrapped emitter is not called")
    void testEmit_Rollback() {
        final TestEvent event;

        // GIVEN
        emitter = new TransactionalEventEmitterWrapper(wrappedEmitter);
        transaction = new TransactionTemplate(new TestTransactionManager());
        event = new TestEvent("abc");

        // WHEN
        transaction.executeWithoutResult(status -> {
            emitter.emit(event);
            status.setRollbackOnly();
        });

        // THEN
        verifyNoInteractions(wrappedEmitter);
    }

    @Test
    @DisplayName("When an event is emitted in an active transaction without synchronization, then an illegal state exception is thrown")
    void testEmit_UnsupportedSynchronization() {
        final TestEvent  event;
        final Executable action;

        // GIVEN
        emitter = new TransactionalEventEmitterWrapper(wrappedEmitter);
        event = new TestEvent("abc");
        TransactionSynchronizationManager.setActualTransactionActive(true);

        // WHEN
        action = () -> emitter.emit(event);

        // THEN
        assertThrows(IllegalStateException.class, action);
    }

}
