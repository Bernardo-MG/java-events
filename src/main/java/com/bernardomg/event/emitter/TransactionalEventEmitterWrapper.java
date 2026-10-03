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

import java.util.Objects;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.bernardomg.event.domain.AbstractEvent;

/**
 * Event emitter wrapper which works with Spring transactions.
 */
public final class TransactionalEventEmitterWrapper implements EventEmitter {

    private final EventEmitter emitter;

    /**
     * @param emitter
     *            emitter to invoke after commit
     */
    public TransactionalEventEmitterWrapper(final EventEmitter emitter) {
        this.emitter = Objects.requireNonNull(emitter);
    }

    @Override
    public <E extends AbstractEvent> void emit(final E event) {
        Objects.requireNonNull(event);

        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            // Outside a transaction
            emitter.emit(event);
        } else if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("Transaction synchronization is unavailable");
        } else {
            // Inside a transaction
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

                @Override
                public void afterCommit() {
                    emitter.emit(event);
                }
            });
        }
    }
}
