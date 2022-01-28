package threads;

import java.util.List;
import java.util.Set;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


public class Task3 {
    private static final int NUM_THREADS = 10;
    private static final int CHANNEL_CAPACITY = 100;
    private static final int POISON_PILL = -1;

    private List<Thread> a;
    private List<Thread> b;

    private List<String> generated;
    private Deque<Integer> container;

    public List<String> get() throws InterruptedException {
        for (Thread i : a) {
            if (!i.isInterrupted()) {
                i.join();
            }
        }

        for (Thread i : b) {
            if (!i.isInterrupted()) {
                i.join();
            }
        }

        return new ArrayList<>(generated);
    }

    public List<Thread> getThreads() {
        return b;
    }

    public void interrupt() {
        for (Thread i : a) {
            i.interrupt();
        }

        for (Thread i : b) {
            i.interrupt();
        }
    }

    public Task3(final int from, final int to, final int count) {
        if (from < 0 || to < 0 || !isInRange(count, 0, to - from + 1)) throw new IllegalArgumentException();

        a = new ArrayList<>();
        b = new ArrayList<>();

        generated = new ArrayList<>(NUM_THREADS);
        Thread buffer;

        List<Integer> used = new ArrayList<>(CHANNEL_CAPACITY);
        container = new ArrayDeque<>(CHANNEL_CAPACITY);

        AtomicInteger currentCount = new AtomicInteger();


        for (int i = 0; i < NUM_THREADS; i++) {
             buffer = new Thread(() -> {
                while (true) {
                    int number = (int) (Math.random() * (to - from + 1) + from);

                    synchronized (container) {
                        if (container.size() < CHANNEL_CAPACITY) {
                            boolean isUsed = true;

                            synchronized (used) {
                                if (!used.contains(number)) {
                                    isUsed = false;
                                }
                            }

                            if (!isUsed) {
                                synchronized (currentCount) {
                                    if (currentCount.get() < count) {
                                        synchronized (used) {
                                            used.add(number);
                                        }
                                        container.add(number);
                                        currentCount.getAndIncrement();
                                    } else {
                                        container.add(POISON_PILL);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            });

            buffer.start();
            a.add(buffer);

            buffer = new Thread(() -> {
                boolean gotPoison = false;

                while (!gotPoison) {
                    int number;

                    synchronized (container) {
                        if (container.isEmpty()) {
                            continue;
                        }
                        number = container.poll();
                    }

                    if (number != POISON_PILL) {
                        String kanjii = KanjiLib.convert(number);
                        String candidate = String.format("%d,%s", number, kanjii);

                        synchronized (generated) {
                            generated.add(candidate);
                        }
                    } else {
                        gotPoison = true;
                    }
                }
            });

            buffer.start();
            b.add(buffer);
        }
    }

    private static boolean isInRange(int count, int from, int to) {
        return from <= count && count <= to;
    }
}
