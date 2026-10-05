
package com.bernardomg.event.test.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.bernardomg.event.test.config.TestEvent;

@DisplayName("AbstractEvent - constructor")
class TestAbstractEvent {

    @Test
    @DisplayName("When an event is created, then it has an identity")
    void testCreate_Id() {
        final TestEvent event;

        // GIVEN
        // Identity is generated at creation.

        // WHEN
        event = new TestEvent("producer");

        // THEN
        assertNotNull(event.getId());
    }

    @Test
    @DisplayName("When an event is created, then it keeps the schema version")
    void testCreate_SchemaVersion() {
        final TestEvent event;

        // GIVEN
        // Metadata is provided by the concrete event constructor.

        // WHEN
        event = new TestEvent("producer");

        // THEN
        assertEquals(1, event.getSchemaVersion());
    }

    @Test
    @DisplayName("When an event is created, then it keeps the source")
    void testCreate_Source() {
        final TestEvent event;

        // GIVEN
        // Metadata is provided by the concrete event constructor.

        // WHEN
        event = new TestEvent("producer");

        // THEN
        assertEquals("producer", event.getSource());
    }

    @Test
    @DisplayName("When an event is created, then its timestamp is within the creation interval")
    void testCreate_Timestamp() {
        final Instant   before;
        final Instant   after;
        final TestEvent event;

        // GIVEN
        before = Instant.now();

        // WHEN
        event = new TestEvent("producer");
        after = Instant.now();

        // THEN
        assertAll(() -> assertFalse(event.getTimestamp()
            .isBefore(before)), () -> assertFalse(
                event.getTimestamp()
                    .isAfter(after)));
    }

    @Test
    @DisplayName("When an event is created, then it keeps the type")
    void testCreate_Type() {
        final TestEvent event;

        // GIVEN
        // Metadata is provided by the concrete event constructor.

        // WHEN
        event = new TestEvent("producer");

        // THEN
        assertEquals("test.event", event.getType());
    }

    @Test
    @DisplayName("When two events are created, then they have distinct identities")
    void testCreate_UniqueId() {
        final TestEvent other;
        final TestEvent event;

        // GIVEN
        other = new TestEvent("producer");

        // WHEN
        event = new TestEvent("producer");

        // THEN
        assertNotEquals(other.getId(), event.getId());
    }

    @Test
    @DisplayName("When an event is restored, then it keeps the id")
    void testRestore_Id() {
        final UUID      id;
        final Instant   timestamp;
        final TestEvent event;

        // GIVEN
        id = UUID.randomUUID();
        timestamp = Instant.parse("2026-01-01T00:00:00Z");

        // WHEN
        event = new TestEvent(id, "producer", "fee.paid", 3, timestamp);

        // THEN
        assertEquals(id, event.getId());
    }

    @Test
    @DisplayName("When an event is restored with a null id, then a null pointer exception is thrown")
    void testRestore_NullId() {
        final Executable action;

        // GIVEN
        // Invalid metadata is supplied through the restoring constructor.

        // WHEN
        action = () -> new TestEvent(null, "producer", "fee.paid", 1, Instant.EPOCH);

        // THEN
        assertThrows(NullPointerException.class, action);
    }

    @Test
    @DisplayName("When an event is restored with a null source, then a null pointer exception is thrown")
    void testRestore_NullSource() {
        final Executable action;

        // GIVEN
        // Invalid metadata is supplied through the restoring constructor.

        // WHEN
        action = () -> new TestEvent(UUID.randomUUID(), null, "fee.paid", 1, Instant.EPOCH);

        // THEN
        assertThrows(NullPointerException.class, action);
    }

    @Test
    @DisplayName("When an event is restored with a null timestamp, then a null pointer exception is thrown")
    void testRestore_NullTimestamp() {
        final Executable action;

        // GIVEN
        // Invalid metadata is supplied through the restoring constructor.

        // WHEN
        action = () -> new TestEvent(UUID.randomUUID(), "producer", "fee.paid", 1, null);

        // THEN
        assertThrows(NullPointerException.class, action);
    }

    @Test
    @DisplayName("When an event is restored with a null type, then a null pointer exception is thrown")
    void testRestore_NullType() {
        final Executable action;

        // GIVEN
        // Invalid metadata is supplied through the restoring constructor.

        // WHEN
        action = () -> new TestEvent(UUID.randomUUID(), "producer", null, 1, Instant.EPOCH);

        // THEN
        assertThrows(NullPointerException.class, action);
    }

    @Test
    @DisplayName("When an event is restored, then it keeps the schema version")
    void testRestore_SchemaVersion() {
        final UUID      id;
        final Instant   timestamp;
        final TestEvent event;

        // GIVEN
        id = UUID.randomUUID();
        timestamp = Instant.parse("2026-01-01T00:00:00Z");

        // WHEN
        event = new TestEvent(id, "producer", "fee.paid", 3, timestamp);

        // THEN
        assertEquals(3, event.getSchemaVersion());
    }

    @Test
    @DisplayName("When an event is restored, then it keeps the source")
    void testRestore_Source() {
        final UUID      id;
        final Instant   timestamp;
        final TestEvent event;

        // GIVEN
        id = UUID.randomUUID();
        timestamp = Instant.parse("2026-01-01T00:00:00Z");

        // WHEN
        event = new TestEvent(id, "producer", "fee.paid", 3, timestamp);

        // THEN
        assertEquals("producer", event.getSource());
    }

    @Test
    @DisplayName("When an event is restored, then it keeps the timestamp")
    void testRestore_Timestamp() {
        final UUID      id;
        final Instant   timestamp;
        final TestEvent event;

        // GIVEN
        id = UUID.randomUUID();
        timestamp = Instant.parse("2026-01-01T00:00:00Z");

        // WHEN
        event = new TestEvent(id, "producer", "fee.paid", 3, timestamp);

        // THEN
        assertEquals(timestamp, event.getTimestamp());
    }

    @Test
    @DisplayName("When an event is restored, then it keeps the type")
    void testRestore_Type() {
        final UUID      id;
        final Instant   timestamp;
        final TestEvent event;

        // GIVEN
        id = UUID.randomUUID();
        timestamp = Instant.parse("2026-01-01T00:00:00Z");

        // WHEN
        event = new TestEvent(id, "producer", "fee.paid", 3, timestamp);

        // THEN
        assertEquals("fee.paid", event.getType());
    }

    @Test
    @DisplayName("When an event is serialized and deserialized, then all the metadata is preserved")
    void testSerialization_Metadata() throws Exception {
        final ByteArrayOutputStream bytes;
        final TestEvent             event;
        final TestEvent             restored;

        // GIVEN
        event = new TestEvent("producer");
        bytes = new ByteArrayOutputStream();

        // WHEN
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(event);
        }
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            restored = (TestEvent) input.readObject();
        }

        // THEN
        assertAll(() -> assertEquals(event.getId(), restored.getId()),
            () -> assertEquals(event.getSource(), restored.getSource()),
            () -> assertEquals(event.getType(), restored.getType()),
            () -> assertEquals(event.getSchemaVersion(), restored.getSchemaVersion()),
            () -> assertEquals(event.getTimestamp(), restored.getTimestamp()));
    }

}
