package de.kenbun.kidou.cloud.examples;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketCloseCode;
import com.neovisionaries.ws.client.WebSocketFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.net.URI;
import java.time.Instant;

public class Main {
    private final static URI KIDOU_CLOUD_URI = URI.create("wss://cloud.kidou.ai/v1/api/transcribe");
    private final static String API_KEY = ""; // <-- INSERT API KEY HERE (Receive free at https://cloud.kidou.ai)
    private final static int SAMPLE_RATE = 16000;

    public static void main(String[] args) throws Exception {
        // Open WebSocket and register callback for transcriptions
        final WebSocket kidouCloudWebSocket = new WebSocketFactory().createSocket(KIDOU_CLOUD_URI);
        kidouCloudWebSocket.addListener(new WebSocketAdapter() {
            @Override
            public void onTextMessage(WebSocket websocket, String text) {
                if (text != null) {
                    System.out.println(text);
                }
            }
        });
        kidouCloudWebSocket.connect();
        // Send API key and sample rate to KIDOU CLOUD websocket
        kidouCloudWebSocket.sendText("{ \"api_key\": \"" + API_KEY + "\", \"sample_rate\": " + SAMPLE_RATE + " }");

        // Configure microphone input
        final AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
        final TargetDataLine line = (TargetDataLine) AudioSystem.getLine(new DataLine.Info(TargetDataLine.class, format));
        line.open(format, line.getBufferSize());
        line.start();

        final byte[] microphoneData = new byte[format.getFrameSize() * line.getBufferSize() / 8];
        final Instant end = Instant.now().plusSeconds(10);
        // Open microphone for 10 seconds
        while (Instant.now().isBefore(end)) {
            // Read microphone input
            int numBytesRead = line.read(microphoneData, 0, microphoneData.length);
            if (numBytesRead > 0) {
                // Send microphone input to API
                kidouCloudWebSocket.sendBinary(microphoneData, true);
            }
        }
        kidouCloudWebSocket.sendClose(WebSocketCloseCode.NORMAL, "");
    }
}
