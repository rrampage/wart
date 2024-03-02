package rrampage.wasp.examples;

import rrampage.wasp.data.Memory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class ProgramUtils {
    public static String loadString(Memory memory, int addr, int len) {
        return new String(memory.load(addr, len), StandardCharsets.UTF_8);
    }

    public static int saveString(String s, Memory memory, int addr) {
        byte[] data = s.getBytes(StandardCharsets.UTF_8);
        var len = data.length;
        memory.store(addr, data);
        return len;
    }

    public static byte[] downloadAsBytes(String url) {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return response.body();
        } catch (Exception e) {
            System.out.println(STR."HTTP_EXCEPTION: \{e.getMessage()}");
            return null;
        }
    }
}
