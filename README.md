# chartboost-core-android-sdk-1

![badge](https://img.shields.io/endpoint?url=https%3A%2F%2Fchartboost.s3.amazonaws.com%2Fchartboost-core%2Fsdk%2Fandroid%2Fcode-coverage%2Fcoverage-percent.json)

## Introduction

ChartboostCore SDK is a modular Android SDK designed as an entry point to manage and facilitate different modules for your Android application/game. Each module can be individually initialized and has its metrics collected and reported, offering detailed insights into module performance and potential issues.

The main functionalities provided by the SDK are:

1. Initialization of individual or a set of modules.
2. Performance metrics collection during the module initialization process.
3. Detailed error tracking and reporting with categorized error codes.
4. Centralized logging system with multiple log levels and output options.

## Repository Structure

The main components of the repository are:

- The `app` directory contains a sample app that demonstrates the usage of ChartboostCore SDK.
- The `chartboostcore` directory contains the source code for the ChartboostCore SDK.

## Getting Started

To integrate the SDK into your project, you will need to add the following to your app's `build.gradle` file:

```groovy
dependencies {
    implementation project(path: ':chartboostcore')
}
```

Then, you can initialize the SDK in your application as follows. Note that this code snippet is for demonstration purposes only and should not be used in production.

```kotlin
    ChartboostCore.debug = true
    ChartboostCore.initializeSdk(
        context,
        sdkConfiguration,
        setOf(
            ModuleAlpha(appId = "alpha_app_id", someOtherId = "alpha_other_id"),
            ModuleBeta(
                appId = "beta_app_id",
                someOtherIds = listOf("beta_other_id_1", "beta_other_id_2")
            )
        ), object : InitializableModuleObserver {
            override fun onModuleInitializationCompleted(result: ModuleInitializationResult) {
                Log.d("[ChartboostCore]", "Module initialized with result: $result")
            }
        }
    )
```

For more detailed information on the usage of the SDK, refer to the app module in this repository.

## Contribution

See CONTRIBUTING.md

## License

TBD
