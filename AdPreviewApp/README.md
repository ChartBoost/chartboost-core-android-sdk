# AdPreviewApp - Android Setup 🚀

This guide provides instructions on how to set up and run the `AdPreviewApp` on Android within the monorepo structure.

***

## 📋 Prerequisites

Before you begin, ensure you have the following set up:
* **JDK 17**: The project requires Java Development Kit version 17. Make sure your Android Studio's Gradle JDK is set to this version.

***

## ⚙️ Setup Instructions

To run the app correctly, you **must open the entire monorepo project** in Android Studio. Opening the `AdPreviewApp` directory directly will cause build errors.

1.  Launch Android Studio.
2.  Select **File > Open**.
3.  Navigate to and select the **root directory of the monorepo** (the parent folder containing `AdPreviewApp` and other projects).
4.  Add secrets.properties file inside the AdPreviewApp with appropriate AUTH0_CLIENT_ID property
5.  Wait for the Gradle synchronization to complete successfully.
6.  Select the `AdPreviewApp` run configuration from the dropdown menu.
7.  Click the 'Run' button (▶️). The app should now build and install on your selected device or emulator.

To build locally the release build:
1.  Add keystore.properties file inside the AdPreviewApp with appropriate properties: 
   - storePassword,
   - keyPassword, 
   - keyAlias,
   - storeFile
2.  Add ad_preview_upload_keystore.jks inside AdPreviewApp

***

## ⚠️ Troubleshooting

### Common Errors

A frequent error you might encounter is:

```
Task 'wrapper' not found in project ':AdPreviewApp'.
```

This error occurs if you open the `adPreviewApp` folder as a standalone project in Android Studio.

**Solution**: To fix this, close the project and re-open it by selecting the **root folder of the monorepo**, as described in the setup instructions above. This ensures that the Gradle wrapper and all necessary configurations from the parent project are correctly loaded.
