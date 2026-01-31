
PACKAGE_NAME="com.projectapp.tempus"

if adb shell pm list packages | grep -q "$PACKAGE_NAME"; then
    echo "Removing old app installation..."
    adb uninstall "$PACKAGE_NAME"
else
    echo "App not installed. Skipping uninstall."
fi

echo "Building and installing Debug APK..."
./gradlew installDebug

if [ $? -eq 0 ]; then
    echo "Build successful. Launching app..."
    adb shell am start -n "$PACKAGE_NAME/$PACKAGE_NAME.ui.onboarding.OnboardingActivity"
else
    echo "Build failed. App will not be launched."
    exit 1
fi