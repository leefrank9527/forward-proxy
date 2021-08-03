package com.neat.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

public class IOHelper {
    private static final int STREAM_BUFFER_LENGTH = 1024 * 64;

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
        //MultiThreadsPrint.putFinished(s);

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

    public static void copy(String prefixFormat, InputStream inputStream, OutputStream outputStream) throws IOException, InterruptedException {
        final byte[] buffer = new byte[STREAM_BUFFER_LENGTH];

        String key = MultiThreadsPrint.putUnFinished(String.format(prefixFormat, 0));
        long countRead = 0;
        try {
            long timeUsed = 0;
            while (timeUsed < 3000) {
                int read = inputStream.read(buffer, 0, STREAM_BUFFER_LENGTH);
                if (read < 0) {
                    break;
                } else if (read == 0) {
                    TimeUnit.MILLISECONDS.sleep(200);
                    timeUsed += 200;
                    continue;
                } else {
                    timeUsed = 0; //restart
                    outputStream.write(buffer, 0, read);
                    //String recvMsg=new String(buffer, 0, read);
                    //System.out.println(recvMsg);
                }

                countRead += read;
                MultiThreadsPrint.putUnFinished(key, String.format(prefixFormat, countRead));
            }
        } finally {
            try {
                inputStream.close();
                outputStream.close();
            } finally {
                MultiThreadsPrint.putFinished(key, String.format(prefixFormat, countRead) + " [DONE]");
            }
        }
    }
}
