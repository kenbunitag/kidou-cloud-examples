package de.kenbun.kidou.cloud.examples;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.concurrent.CompletionStage;

public class Main {
    private final static URI KIDOU_CLOUD_URI = URI.create("wss://cloud.kidou.ai/v1/api/transcribe");
    private final static String API_KEY = ""; // <-- INSERT API KEY HERE (Receive free at https://cloud.kidou.ai)
    private final static int SAMPLE_RATE = 16000;

    public static void main(String[] args) throws Exception {
        // Open WebSocket and register callback for transcriptions
        final WebSocket kidouCloudWebSocket = HttpClient.newHttpClient()
                .newWebSocketBuilder()
                .buildAsync(KIDOU_CLOUD_URI, new WebSocket.Listener() {
                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence message, boolean last) {
                        System.out.println(message);
                        webSocket.request(1);
                        return null;
                    }
                }).get();
        // Send API config: API key and sample rate
        kidouCloudWebSocket.sendText(String.format("{ \"api_key\": \"%s\", \"sample_rate\": %s }", API_KEY, SAMPLE_RATE), true);

        // Configure microphone input
        final AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        final TargetDataLine line = (TargetDataLine) AudioSystem.getLine(new DataLine.Info(TargetDataLine.class, format));
        line.open(format, line.getBufferSize());
        line.start();

        final byte[] data = new byte[format.getFrameSize() * line.getBufferSize() / 8];
        final Instant end = Instant.now().plusSeconds(10);
        // Open microphone for 10 seconds
        while (Instant.now().isBefore(end)) {
            // Read microphone input
            int numBytesRead = line.read(data, 0, data.length);
            if (numBytesRead > 0) {
                // Send microphone input to API
                kidouCloudWebSocket.sendBinary(ByteBuffer.wrap(data, 0, numBytesRead), true).get();
            }
        }
        kidouCloudWebSocket.sendClose(WebSocket.NORMAL_CLOSURE, "").get();
    }
}
