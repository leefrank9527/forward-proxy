package com.neat.util;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class IOHelper {
    private static final int STREAM_BUFFER_LENGTH = 1024;

    public static String readln(InputStream inputStream) {
        ByteArrayOutputStream bufLine = new ByteArrayOutputStream();
        String line = null;
        while (true) {
            int b;
            try {
                b = inputStream.read();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            if (b == -1) {
                line = new String(bufLine.toByteArray());
                break;
            }

            if (b == '\n') {
                line = new String(bufLine.toByteArray());
                break;
            } else {
                bufLine.write(b);
            }
        }

        MultiThreadsPrint.putFinished(line);

        return line == null ? null : line.trim();
    }

    public static void writeln(OutputStream outputStream, String s) {
        MultiThreadsPrint.putFinished(s);

        try {
            outputStream.write(s.getBytes());
            outputStream.write("\r\n".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isNull(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static void copy(String prefix, InputStream inputStream, OutputStream outputStream) throws IOException {
        final byte[] buffer = new byte[STREAM_BUFFER_LENGTH];

        String msg = String.format("%s, read=[0]", prefix);
        MultiThreadsPrint.putFinished(msg);

        long countRead = 0, previousCountRead = 0;
        try {
            int read = inputStream.read(buffer, 0, STREAM_BUFFER_LENGTH);
            while (read > -1) {
                outputStream.write(buffer, 0, read);
                System.out.print(new String(buffer, 0, read));
                countRead += read;

                if ((countRead - previousCountRead) >= STREAM_BUFFER_LENGTH * 1024) {
                    msg = String.format("%s, read=[%d]", prefix, countRead);
                    MultiThreadsPrint.putFinished(msg);
                    previousCountRead = countRead;
                }
                read = inputStream.read(buffer, 0, STREAM_BUFFER_LENGTH);
//                System.out.println(new String(buffer));
            }
        } finally {
            msg = String.format("%s, read=[%d]", prefix, countRead);
            MultiThreadsPrint.putFinished(msg);
            System.out.println();
        }
    }

    public static boolean isConnective(String urlLinkName) {
        try {
            URL url = new URL(urlLinkName);
            URLConnection originalConnection = url.openConnection();
            originalConnection.setConnectTimeout(3 * 1000);
            if (urlLinkName.startsWith("https")) { //HTTPS
                HttpsURLConnection conn = (HttpsURLConnection) originalConnection;
                return conn.getResponseCode() != HttpsURLConnection.HTTP_INTERNAL_ERROR;
            } else { //HTTPS
                HttpURLConnection conn = (HttpURLConnection) originalConnection;
                return conn.getResponseCode() != HttpURLConnection.HTTP_INTERNAL_ERROR;
            }
        } catch (IOException e) {
            return false;
        }
    }

//    public static void debug(String s) {
//        System.out.println(s);
//    }
}
