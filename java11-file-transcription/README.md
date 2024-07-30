Java 11 file transcription example
=

File transcription for a given WAV-file. Uploads it via HTTP and prints the transcription to stdout.

# Usage
Just open the project inside IntelliJ or any other IDE supporting Apache Maven and start the main method. Don't forget to insert the API key and the path to the wav file (german) to transcribe into the marked variables. The transcription will be visible shortly on the console.

Remember that this is an example project without exception handling. If something does not work, take a look at the console.

# Dependencies
Since Java 11 the JDK contains a HTTP client implementation, so we don't have any dependencies.
