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

package com.bernardomg.event.test.emitter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
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
@DisplayName("TransactionalEventEmitterWrapper")
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

    private TransactionTemplate       transaction;

    @Mock
    private EventEmitter              wrappedEmitter;

    @BeforeEach
    void setUp() {
        emitter = new TransactionalEventEmitterWrapper(wrappedEmitter);
        transaction = new TransactionTemplate(new TestTransactionManager());
    }

    @Test
    @DisplayName("Null wrapped emitters are rejected")
    void testConstructor_Null() {
        final Executable action;

        // GIVEN
        // No wrapped emitter is supplied.

        // WHEN
        action = () -> new TransactionalEventEmitterWrapper(null);

        // THEN
        assertThrows(NullPointerException.class, action);
    }

    @Test
    @DisplayName("Events are not emitted before commit")
    void testEmit_BeforeCommit() {
        final TestEvent              event;
        final TestTransactionManager manager;
        final TransactionStatus      status;

        // GIVEN
        event = new TestEvent("abc");
        manager = new TestTransactionManager();
        status = manager.getTransaction(new DefaultTransactionDefinition());
        try {
            // WHEN
            emitter.emit(event);

            // THEN
            verifyNoInteractions(wrappedEmitter);
        } finally {
            manager.rollback(status);
        }
    }

    @Test
    @DisplayName("Committed transactions emit their events")
    void testEmit_Commit() {
        final TestEvent event;

        // GIVEN
        event = new TestEvent("abc");

        // WHEN
        transaction.executeWithoutResult(status -> emitter.emit(event));

        // THEN
        verify(wrappedEmitter).emit(event);
    }

    @Test
    @DisplayName("Dispatch failures propagate after commit")
    void testEmit_DispatchFailure() {
        final TestEvent        event;
        final RuntimeException failure;
        final Executable       action;

        // GIVEN
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
    @DisplayName("Transactions rolled back by exceptions do not emit events")
    void testEmit_ExceptionRollback() {
        final TestEvent        event;
        final RuntimeException failure;

        // GIVEN
        event = new TestEvent("abc");
        failure = new IllegalStateException("rollback");

        // WHEN
        try {
            transaction.executeWithoutResult(status -> {
                emitter.emit(event);
                throw failure;
            });
        } catch (final IllegalStateException exception) {
            if (exception != failure) {
                throw exception;
            }
        }

        // THEN
        verifyNoInteractions(wrappedEmitter);
    }

    @Test
    @DisplayName("Without a transaction, emission is immediate")
    void testEmit_NoTransaction() {
        final TestEvent event;

        // GIVEN
        event = new TestEvent("abc");

        // WHEN
        emitter.emit(event);

        // THEN
        verify(wrappedEmitter).emit(event);
    }

    @Test
    @DisplayName("Null events are rejected")
    void testEmit_Null() {
        final Executable action;

        // GIVEN
        // The emitter is initialized by the fixture.

        // WHEN
        action = () -> emitter.emit(null);

        // THEN
        assertThrows(NullPointerException.class, action);
    }

    @Test
    @DisplayName("Rolled back transactions do not emit events")
    void testEmit_Rollback() {
        final TestEvent event;

        // GIVEN
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
    @DisplayName("Active transactions without synchronization fail")
    void testEmit_UnsupportedSynchronization() {
        final TestEvent  event;
        final Executable action;

        // GIVEN
        event = new TestEvent("abc");

        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            // WHEN
            action = () -> emitter.emit(event);

            // THEN
            assertThrows(IllegalStateException.class, action);
        } finally {
            TransactionSynchronizationManager.clear();
        }

    }
}
