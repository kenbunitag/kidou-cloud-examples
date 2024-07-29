package de.kenbun.kidou.cloud.examples;

import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.fluent.Response;
import org.apache.hc.core5.http.ContentType;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    private final static URI KIDOU_CLOUD_URI = URI.create("https://cloud.kidou.ai/v1/api/transcribe");
    private final static String API_KEY = ""; // <-- INSERT API KEY HERE (Receive free at https://cloud.kidou.ai)
    private final static Path WAV_FILE_PATH = Paths.get(""); // <-- INSERT WAV FILE PATH HERE

    public static void main(String[] args) throws Exception {
        final Response response = Request.post(KIDOU_CLOUD_URI)
                .addHeader("X-API-KEY", API_KEY)
                .bodyFile(WAV_FILE_PATH.toFile(), ContentType.create("audio/wave"))
                .execute();
        final String transcription = response.returnContent().asString();
        System.out.println(transcription);
    }
}