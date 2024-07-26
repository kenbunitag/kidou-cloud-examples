package de.kenbun.kidou.cloud.examples;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    private final static URI KIDOU_CLOUD_URI = URI.create("https://cloud.kidou.ai/v1/api/transcribe");
    private final static String API_KEY = ""; // <-- INSERT API KEY HERE (Receive free at https://cloud.kidou.ai)
    private final static Path WAV_FILE_PATH = Paths.get(""); // <-- INSERT WAV FILE PATH HERE

    public static void main(String[] args) throws Exception {
        final HttpRequest fileUpload = HttpRequest.newBuilder()
                .uri(KIDOU_CLOUD_URI)
                .header("X-API-KEY", API_KEY)
                .header("content-type", "audio/wave")
                .POST(HttpRequest.BodyPublishers.ofFile(WAV_FILE_PATH))
                .build();
        final HttpClient httpClient = HttpClient.newHttpClient();
        final String transcription = httpClient.send(fileUpload, HttpResponse.BodyHandlers.ofString()).body();
        System.out.println(transcription);
    }
}
