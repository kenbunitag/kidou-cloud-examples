Java 8 Android live transcription example
=

Live transcription using the default microphone and a third party WebSocket implementation.

Opens default microphone for 10 seconds and prints the live transcription on stdout.

# Usage
Just open the project inside IntelliJ or any other IDE supporting Apache Maven and start the main method. Don't forget to insert the API key into the marked variable.

Remember that this is an example project without exception handling. If something does not work, take a look into the console.

# Dependencies
As Java before 11 does not contain a WebSocket implementation, we use [nv-websocket-client](https://github.com/TakahikoKawasaki/nv-websocket-client).
