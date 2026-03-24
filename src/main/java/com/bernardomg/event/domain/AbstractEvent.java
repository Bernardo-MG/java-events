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

package com.bernardomg.event.domain;

import java.io.Serializable;
import java.time.Instant;

/**
 * Abstract event, which should be extended to create new events for the
 * {@link com.bernardomg.event.emitter.EventEmitter EventEmitter}.
 */
public abstract class AbstractEvent implements Serializable {

    private static final long serialVersionUID = 4530889924666987059L;

    /**
     * Event source.
     */
    private transient Object  source;

    /**
     * Event creation time.
     */
    private final Instant     timestamp;

    public AbstractEvent(final Object src) {
        super();

        source = src;
        timestamp = Instant.now();
    }

    /**
     * Returns the event source.
     *
     * @return the event source
     */
    public final Object getSource() {
        return source;
    }

    /**
     * Returns the event creation time.
     *
     * @return the event creation time
     */
    public final Instant getTimestamp() {
        return timestamp;
    }

}
