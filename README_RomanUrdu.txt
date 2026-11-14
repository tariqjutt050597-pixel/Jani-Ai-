JAANI AI - Android starter project (v0.1)

Included:
- Jaani AI branding and generated splash/logo artwork
- Basic Android Compose interface
- Speech recognition using the phone's speech-recognition service
- Roman Urdu / English / Hindi / Urdu language selector
- Editable message draft and confirmation dialog
- WhatsApp share intent, SMS draft, and phone dialer launch
- Text-to-speech starter response

Important limitations:
- This is a starter prototype, NOT a finished production app or APK.
- It does not yet implement the always-listening “Oye Jaani” hotword, reliable locked-screen listening,
  real language translation/transliteration, AI conversation backend, or automatic message sending.
- Speech recognition quality and available languages depend on the Android device and installed speech services.
- WhatsApp/SMS actions open the relevant app with a draft; the user completes sending.
- Phone calls open the dialer; the user confirms the call.

Build:
1. Install Android Studio on a computer.
2. Open this folder in Android Studio.
3. Allow Gradle sync and install Android SDK Platform 35 if prompted.
4. Connect an Android phone with USB debugging enabled, or use an emulator.
5. Select Build > Build Bundle(s) / APK(s) > Build APK(s).
6. The debug APK will be under app/build/outputs/apk/debug/.

This environment did not have Android SDK/Gradle installed, so no APK was built or tested here.


PHONE-ONLY CLOUD BUILD OPTION:
- This project includes .github/workflows/build-apk.yml.
- Upload the extracted project files to a GitHub repository.
- Open the repository in a browser, choose Actions, select "Build Jaani AI APK", and tap Run workflow.
- When the workflow finishes successfully, open its run and download the "JaaniAI-debug-apk" artifact.
- Extract the downloaded artifact ZIP; install app-debug.apk on the Android phone.
- GitHub account sign-in and internet are required. Android may ask permission to install apps from that browser/file manager.
