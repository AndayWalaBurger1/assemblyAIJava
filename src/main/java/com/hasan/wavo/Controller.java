package com.hasan.wavo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Controller {

    private static String API_TOKEN = "your_token";  // replace with your API token

    public static void main(String[] args) throws IOException {
        String transcribeUrl = "https://api.assemblyai.com/v2/transcript";

        String requestBody = "{\"audio_url\":\"https://s3-us-west-2.amazonaws.com/blog.assemblyai.com/audio/8-7-2018-post/7510.mp3\"}";

        // Make the POST request
        String response = sendPOST(transcribeUrl, requestBody);
        System.out.println("POST Response: " + response);

        // Extract the id from the response
        String id = extractIdFromResponse(response);

        // Keep checking the status of the transcript until it's done
        String transcriptStatus = "";
        while (!transcriptStatus.equals("completed")) {
            String transcriptUrl = "https://api.assemblyai.com/v2/transcript/" + id;
            String transcriptResponse = sendGET(transcriptUrl);
            System.out.println("GET Response: " + transcriptResponse);
            transcriptStatus = extractStatusFromResponse(transcriptResponse);
            if(transcriptStatus.contains("completed")) {
                System.out.println("Speech to Text: " +transcriptResponse.split(", \"text\": ")[1].split(", \"words")[0].replaceAll("\"",""));
            }
        }
    }

    private static String sendPOST(String targetURL, String requestBody) throws IOException {
        URL url = new URL(targetURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("authorization", API_TOKEN);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        return readResponse(conn);
    }

    private static String sendGET(String targetURL) throws IOException {
        URL url = new URL(targetURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("authorization", API_TOKEN);

        return readResponse(conn);
    }

    private static String readResponse(HttpURLConnection conn) throws IOException {
        try (Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0; )
                sb.append((char)c);
            return sb.toString();
        }
    }

    private static String extractIdFromResponse(String response) {
        if (response.contains("\"id\":")) {
            return response.split("\"id\": ")[1].split(",")[0].replaceAll("\"", "");
        } else {
            System.err.println("Unexpected response: " + response);
            return null;
        }
    }

    private static String extractStatusFromResponse(String response) {
        if (response.contains("\"status\":")) {
            return response.split("\"status\": ")[1].split(",")[0].replaceAll("\"","");
        } else {
            System.err.println("Unexpected response: " + response);
            return null;
        }
    }

}
