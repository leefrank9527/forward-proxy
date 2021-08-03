package com.neat.util;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MultiThreadsPrint {
    private static final ScheduledThreadPoolExecutor _schedule_executor = new ScheduledThreadPoolExecutor(256);
    private static final String lineSeparator = System.lineSeparator();
    private static final Map<String, Message> msgQueue = new HashMap<>();
    private static int countPreviousUnfinished = 0;
    private static ScheduledFuture<?> future;

    public static void init() {
        Runnable handler = MultiThreadsPrint::print;
        future = _schedule_executor.scheduleWithFixedDelay(handler, 500, 250, TimeUnit.MILLISECONDS);
    }

    public static void close() {
        future.cancel(true);
        _schedule_executor.shutdown();
        print();
    }

    synchronized private static void print() {
//        System.out.print("\b".repeat(countPreviousUnfinished));
        if (countPreviousUnfinished > 0) {
            System.out.printf("\033[%dA", countPreviousUnfinished);
            System.out.printf("\033[2K");
        }
        StringBuilder unfinishedBuf = new StringBuilder();
        StringBuilder finishedBuf = new StringBuilder();
        List<String> finishedKey = new ArrayList<>();
        msgQueue.values().stream().sorted(new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                return (int) (o1.getTimestamp() - o2.getTimestamp());
            }
        }).forEach(msg -> {
            if (msg.isFinished()) {
                finishedKey.add(msg.getKey());
                finishedBuf.append(msg.getInfo()).append(lineSeparator);
            } else {
                unfinishedBuf.append(msg.getInfo()).append(lineSeparator);
            }
        });
        countPreviousUnfinished = msgQueue.size() - finishedKey.size();
//        countPreviousUnfinished=unfinishedBuf.length();
        finishedKey.forEach(msgQueue::remove);
        System.out.print(finishedBuf.toString());
        System.out.print(unfinishedBuf.toString());
    }

    synchronized private static void put(String key, String info, boolean finished) {
        Message msg;
        if (msgQueue.containsKey(key)) {
            msg = msgQueue.get(key);
        } else {
            msg = new Message();
        }
        msg.setKey(key);
        msg.setFinished(finished);
        msg.setInfo(info);
        msgQueue.put(key, msg);
    }


    public static void putFinished(String info) {
        String key = UUID.randomUUID().toString();
        put(key, info, true);
    }

    public static void putFinished(String key, String info) {
        put(key, info, true);
    }

    public static String putUnFinished(String info) {
        String key = UUID.randomUUID().toString();
        put(key, info, false);
        return key;
    }

    public static void putUnFinished(String key, String info) {
        put(key, info, false);
    }

    static class Message {
        private long timestamp = System.currentTimeMillis();
        private boolean finished;
        private String info;
        private String key;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public boolean isFinished() {
            return finished;
        }

        public void setFinished(boolean finished) {
            this.finished = finished;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}