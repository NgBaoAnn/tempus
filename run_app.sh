#!/bin/bash

# Uninstall old app if exists
echo "Removing old app installation..."
adb uninstall com.projectapp.tempus

# Build and install the Debug APK
echo "Building and installing Debug APK..."
./gradlew installDebug

# Check if build was successful (gradlew installDebug usually returns 0 on success)
if [ $? -eq 0 ]; then
    echo "Build successful. Launching app..."
    # Launch the app
    adb shell am start -n com.projectapp.tempus/com.projectapp.tempus.ui.onboarding.OnboardingActivity
else
    echo "Build failed. App will not be launched."
    exit 1
fi