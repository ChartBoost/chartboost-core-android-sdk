![badge](https://img.shields.io/endpoint?url=https%3A%2F%2Fchartboost.s3.amazonaws.com%2Fchartboost-mediation%2Fsdk%2Fandroid%2Fcode-coverage%2Fcoverage-percent.json)
![badge](https://img.shields.io/endpoint?url=https%3A%2F%2Fchartboost.s3.amazonaws.com%2Fchartboost-core%2Fsdk%2Fandroid%2Fcode-coverage%2Fcoverage-percent.json)
![badge](https://img.shields.io/endpoint?url=https%3A%2F%2Fchartboost.s3.amazonaws.com%2Fchartboost-monetization%2Fsdk%2Fandroid%2Fcode-coverage%2Fcoverage-percent.json)

# Chartboost Android SDKs

This repository contains the source code for the Chartboost Android SDKs, including the Core, Mediation, and Monetization products. This guide is intended for internal developers and provides information on building, testing, and releasing the SDKs.

## Repository Structure

The repository is a multi-module monorepository. The main components are:

| Module / Directory                  | Description                                                                                                                              |
| ----------------------------------- |------------------------------------------------------------------------------------------------------------------------------------------|
| `ChartboostCore/`                   | The Core SDK, providing common functionalities like networking, logging, and module management used by other Chartboost SDKs.            |
| `ChartboostMediation/`              | The Chartboost Mediation SDK, which integrates multiple ad network partners.                                                             |
| `ChartboostMonetization/`           | The Chartboost Monetization SDK.                                                                                                         |
| `ChartboostMediationCanary/`        | An internal testing application for the Mediation SDK, used for development, QA, and canary releases.                                    |
| `ChartboostCoreJavaValidator/`      | A small project to ensure the Core SDK has proper Java interoperability.                                                                 |
| `ChartboostMediationJavaValidator/` | A small project to ensure the Mediation SDK has proper Java interoperability.                                                            |
| `chartboost-mediation-android-adapter-*` | Git submodules for each third-party mediation adapter (e.g., AdMob, AppLovin). These are separate repositories.                          |
| `chartboost-core-android-consent-adapter-*` | Git submodules for consent management platform adapters (e.g., Usercentrics, Google UMP).                                                |
| `buildSrc/`                         | Contains build logic, dependencies, and version information shared across modules.                                                       |
| `scripts/`                          | Contains various scripts for CI/CD, releasing, and other automation tasks.                                                               |
| `.github/workflows/`                | GitHub Actions workflows for CI, nightly builds, releases, etc.                                                                          |

## Getting Started

### Prerequisites

1.  **Android Studio:** Use the version specified in the root `build.gradle.kts`.
2.  **Java Development Kit (JDK):** Ensure you have JDK 17 installed.
3.  **Artifactory Credentials:** To build the project, you need credentials for Chartboost's Artifactory instance. They can be found in the team's 1Password vault. Set the following environment variables:
    ```bash
    export JFROG_USER="<artifactory-username>"
    export JFROG_PASS="<artifactory-password>"
    ```
4.  **Git Submodules:** The adapter dependencies are managed as git submodules. After cloning the repository, you must initialize and update the submodules:
    ```bash
    git submodule update --init --recursive
    ```
    To pull the latest changes for all submodules, run:
    ```bash
    git submodule foreach git pull origin main
    ```

### Building the Source

You can build the entire project and all its modules by running the following command from the root directory:

```bash
./gradlew build
```

To build a specific module, you can run the `build` task for that module. For example, to build the Mediation SDK:

```bash
./gradlew :ChartboostMediation:build
```

### Running the Canary App

The `ChartboostMediationCanary` app is the primary tool for testing and debugging the SDKs. To run it:

1.  Open the project root in Android Studio.
2.  Select the `ChartboostMediationCanary` run configuration.
3.  Choose an emulator or connected device.
4.  Click "Run".

## Development Workflow & CI

The CI process is handled by GitHub Actions. The main workflow is defined in `ci-tests.yml`. It runs on every pull request (see below for details).

### Local CI Checks

Before pushing your changes, you should run the same high-level CI tasks that are used in GitHub Actions. These tasks lint, test, and build the main SDKs.

-   For Core SDK changes: `./gradlew chartboostcore-ci`
-   For Mediation SDK changes: `./gradlew chartboostmediation-ci`
-   For Monetization SDK changes: `./gradlew chartboostmonetization-ci`

### Pull Request CI

When you open a pull request, the `ci-tests.yml` workflow will automatically run. It performs the following steps:

1.  **Determines Changed Modules:** It intelligently inspects the changed files to determine which SDK modules are affected (Core, Mediation, Monetization).
2.  **Automated Fixes:** It automatically checks for and adds missing copyright headers. It also runs `ktlint` and `black` to format Kotlin and Python code. Any changes are automatically committed and pushed to your PR branch.
3.  **Static Analysis:** It runs `detekt` on the changed Kotlin files and posts a report as a comment on the PR. This comment is updated on subsequent pushes.
4.  **Build & Test:** It runs the appropriate `*-ci` Gradle task (e.g., `chartboostmediation-ci`) for the modules that were changed. This ensures the code builds and all tests pass.

## Releasing

The release process is highly automated through GitHub Actions. **Do not create and push tags manually.** The entire process is managed by a script to ensure consistency and safety.

### How to Create a Release

The release process is initiated through the **Manual Release** (odd name, we know) workflow in the GitHub Actions tab.

1.  **Navigate to the Actions tab** in the GitHub repository.
2.  Select the **Manual Release** workflow from the list on the left.
3.  Click the **Run workflow** dropdown button.
4.  **Fill in the required parameters:**
    *   **`version`**: The version to release in `MAJOR.MINOR.PATCH` format (e.g., `5.3.0`).
    *   **`sdk_module`**: The SDK module to release (choose from `ChartboostCore`, `ChartboostMediation`, or `ChartboostMonetization`).
    *   **`dry_run`**: Select `true` to run a test release or `false` for a production release.
5.  Click the **Run workflow** button.

### The Release Automation Flow

Here is what happens after you trigger the manual release:

1.  **`manual-release.yml` is triggered:** This workflow takes your inputs.
2.  **`scripts/release.sh` is executed:** The workflow calls this shell script, passing your inputs as parameters. The script checks out the correct release branch (e.g., `release/5.3.0-ChartboostMediation`), then creates and pushes the corresponding git tag (e.g., `5.3.0-ChartboostMediation` or `5.3.0-ChartboostMediation-dryrun`).
3.  **`release.yml` is triggered:** The push of the new tag automatically triggers the main `release.yml` workflow. This workflow is responsible for the actual release process:
    *   It runs precondition checks.
    *   It builds the AAR and publishes it to Artifactory.
    *   It creates a GitHub Release with the AAR as an artifact.
    *   It automatically creates PRs to merge the release branch back into `develop` and `main`.
    *   It publishes the documentation to S3.
    *   It syncs the code to the public open-source repository (for Core and Mediation).

### Dry Run Releases

It is highly recommended to perform a **dry run** before a production release. To do this, simply set the `dry_run` input parameter to `true` when running the **Manual Release** workflow.

The dry run will execute all the same steps as a real release, but it will not publish to the public Artifactory repositories. It will create a temporary GitHub release and PRs, and then automatically delete them at the end of the workflow. This allows you to safely verify that the entire release process is working correctly.

### Nightly Builds and Release Candidates

-   **Nightly Builds:** The `nightly.yml` workflow runs daily, building the latest `develop` branch and publishing it to a private Artifactory repository.
-   **Release Candidates:** The `generate-release-candidate.yml` workflow can be run manually to create and publish a release candidate for testing.
