# Local Setup

## Requirements

* **git**
* **A JDK** to run Gradle with. The build itself compiles on **Java 25**, but you
  do not have to install it: the Gradle toolchain support downloads a Java 25 JDK
  the first time you build, using the
  [foojay resolver](https://github.com/gradle/foojay-toolchains) configured in
  `settings.gradle`.
* **Network access** on the first build, to fetch Gradle 9.5.0, the Java 25
  toolchain, and the platform APIs from Maven Central, the SpigotMC repository,
  and the PaperMC repository.

Gradle itself does not need to be installed — use the wrapper (`./gradlew`)
checked into the repository.

**You do not need a copy of `MCAgents/core` to build this project.** It is
reached through a reflective bridge at runtime rather than compiled against, so
nothing has to be published or checked out alongside it. See
[`../information/modules.md`](../information/modules.md).

## Access

This repository is **proprietary**. Cloning it does not grant a license to use
the software — see [`../information/licensing.md`](../information/licensing.md).

## Get a working copy

```sh
git clone https://github.com/MCAgents/chat.git
cd chat
```

The default branch is `master`.

## Build

```sh
./gradlew build
```

That compiles every module, runs the tests, and produces one jar per module under
`{module}/build/libs/`, including the universal `MCAgentsChat-{version}.jar`
shaded by `platforms:engine`.

Useful variants:

```sh
./gradlew :api:build                   # one module only
./gradlew :platforms:engine:shadowJar  # the universal jar on its own
./gradlew clean                        # delete the root build directory
./gradlew javaToolchains               # show which JDK the build resolved
```

## Test

```sh
./gradlew test                        # every module
./gradlew :common:test                # one module
./gradlew test --tests '*ChatSettings*'    # one class
```

Tests are **JUnit 5**, wired once in the root `build.gradle` so every module has
a test source set without asking for one. A module with no `src/test/java`
reports `NO-SOURCE` and costs nothing.

Test sources live beside the code they cover, at
`{module}/src/test/java/{same package}`. Only what the module compiles against
is on the test classpath: modules declare `api` and `common` as `compileOnly`,
which does not reach tests, so a module with tests repeats those as
`testImplementation` in its own build file.

A run writes an HTML report to `{module}/build/reports/tests/test/index.html`
and the machine-readable results to `{module}/build/test-results/test/`.

There is no CI pipeline in this repository, so this command is the only thing
that runs the tests. Run it before opening a pull request.

## Run it on a server

The universal jar needs the **MCAgents core plugin installed alongside it** — the
bridge resolves against the loaded core plugin, and without it the chat commands
report that the backend is unavailable rather than working.

## Publish the library modules

`api` and `common` are the two published modules. They go to GitHub Packages as
`io.github.mcagents:mcagents-chat-api` and
`io.github.mcagents:mcagents-chat-common`:

```sh
./gradlew publish
```

Publishing reads the `GITHUB_ACTOR` and `GITHUB_TOKEN` environment variables for
credentials. To try a release locally without publishing anywhere public:

```sh
./gradlew publishToMavenLocal
```

## Start a piece of work

Never commit to `master`. Branch from it, named `{type}/{primary-noun}`:

```sh
git checkout master
git pull origin master
git checkout -b docs/my-change
```

When the work is done:

```sh
git push -u origin docs/my-change
```

Then open one pull request for the branch. The branch types, commit message
format, pull request title and body rules, and the merge procedure are defined in
`.agents/` — start from [`../../AGENTS.md`](../../AGENTS.md).

## Environment variables

| Variable | Read by | Purpose |
|---|---|---|
| `GITHUB_ACTOR` | `build.gradle` | Username for the GitHub Packages repository. |
| `GITHUB_TOKEN` | `build.gradle` | Token for the GitHub Packages repository. |

Both are only needed when running `./gradlew publish`. Nothing else in the
repository reads the environment.

**Never put an API token in an environment variable committed to this
repository**, or in any example on this page. Tokens are configured at runtime —
see [`../../.agents/security/token-handling.md`](../../.agents/security/token-handling.md).
