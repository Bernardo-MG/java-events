# Basic event handling

A small event library for Java projects.

[![Maven Central](https://img.shields.io/maven-central/v/com.bernardomg.framework/event.svg)][maven-repo]

## Usage

The application is coded in Java, using Maven to manage the project.

It is a Java library, meant to be included as a dependency on any project which may want to make use of it.

### Installing

The recommended way to install the project is by setting it up as a dependency. To get the configuration information for this check  the [Maven Central Repository][maven-repo].

It is always possible installing it by using the usual Maven command:

```
mvn install
```

## Collaborate

Any kind of help with the project will be well received, and there are two main ways to give such help:

- Reporting errors and asking for extensions through the issues management
- or forking the repository and extending the project

### Issues management

Issues are managed at the GitHub [project issues tracker][issues], where any Github user may report bugs or ask for new features.

### Getting the code

If you wish to fork or modify the code, visit the [GitHub project page][scm], where the latest versions are always kept. Check the 'master' branch for the latest release, and the 'develop' for the current, and stable, development version.

## License

The project has been released under the [MIT License][license].

[maven-repo]: https://mvnrepository.com/artifact/com.bernardomg.framework/event
[issues]: https://github.com/bernardo-mg/java-event/issues
[license]: https://www.opensource.org/licenses/mit-license.php
[scm]: https://github.com/bernardo-mg/java-event

### Event metadata migration

Events now provide immutable UUID identity, String source, stable type, positive
payload schema version, and occurrence timestamp. Subclasses call
`super(source, "fee.paid", 1)` for new events, or
`super(id, source, type, schemaVersion, timestamp)` when restoring existing events.
Retries reuse the same event identity and timestamp. Subclass payloads should be
immutable. Transport adapters handle JSON serialization and bus-specific mapping.

This replaces the Serializable-source constructor and requires downstream
subclasses to migrate. Null and blank sources/types are rejected. The Java
serialization UID changes deliberately: serialized events from the old model
are not compatible with this model. This change does not alter emitter interfaces.
