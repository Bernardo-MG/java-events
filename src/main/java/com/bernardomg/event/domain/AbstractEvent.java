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
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

/**
 * Immutable metadata shared by domain events. Subclasses define immutable payload fields. Transport adapters serialize
 * the payload and map this metadata to their envelopes; this class does not depend on any transport or JSON library.
 */
public abstract class AbstractEvent implements Serializable {

    private static final long serialVersionUID = 2L;

    private final UUID        id;

    private final int         schemaVersion;

    private final String      source;

    private final Instant     timestamp;

    private final String      type;

    protected AbstractEvent(final String source, final String type, final int schemaVersion) {
        this(UUID.randomUUID(), source, type, schemaVersion, Instant.now());
    }

    protected AbstractEvent(final UUID id, final String source, final String type, final int schemaVersion,
            final Instant timestamp) {
        Objects.requireNonNull(id, "Received null id");
        Objects.requireNonNull(source, "Received null source");
        Objects.requireNonNull(type, "Received null type");
        Objects.requireNonNull(timestamp, "Received null timestamp");

        this.id = id;
        this.source = StringUtils.trim(source);
        this.type = StringUtils.trim(type);
        this.schemaVersion = schemaVersion;
        this.timestamp = timestamp;
    }

    public final UUID getId() {
        return id;
    }

    public final int getSchemaVersion() {
        return schemaVersion;
    }

    public final String getSource() {
        return source;
    }

    public final Instant getTimestamp() {
        return timestamp;
    }

    public final String getType() {
        return type;
    }

}
