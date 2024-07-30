Kotlin Android live transcription example
=

# Usage
Just open in Android Studio and start the app in the emulator or on a remote device. Don't forget to insert the API key into the variable before.
After the app started, press the button and speak into your connected microphone a german text. The transcription will be visible shortly. You can speak multiple times. Press the button again to stop the transcription.

Remember that this is an example project without exception handling. If something does not work, take a look into logcat.

# Dependencies
As Kotlin (and Java before 11) does not contain a websocket implementation, we use [nv-websocket-client](https://github.com/TakahikoKawasaki/nv-websocket-client).