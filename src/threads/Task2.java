package threads;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;

public class Task2 {
    private static final int NUM_THREADS = 10;
    private static final int CHANNEL_CAPACITY = 100;
    private static final int POISON_PILL = -1;

    public static List<String> generate(final int from, final int to, final int count) {
        if (from < 0 || to < 0 || !isInRange(count, 0, to - from + 1)) throw new IllegalArgumentException();

        List<String> generated = new ArrayList<>(count);

        List<Integer> used = new ArrayList<>(CHANNEL_CAPACITY);
        Deque<Integer> container = new ArrayDeque<>(CHANNEL_CAPACITY); //because we are going to change its size later
        AtomicInteger currentCount = new AtomicInteger();

        Thread[] a = new Thread[NUM_THREADS];
        Thread[] b = new Thread[NUM_THREADS];

        for (int i = 0; i < NUM_THREADS; i++) {//creating 10 thread to generate 10 random numbers
            a[i] = new Thread(() -> {
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
                                synchronized (used) {
                                    if (currentCount.get() < count) {
                                        synchronized (used) {
                                            used.add(number);
                                        }
                                        container.add(number);
                                        currentCount.getAndIncrement();
                                    } else {
                                        container.add(POISON_PILL);
                                        break;//stop
                                    }
                                }
                            }
                        }
                    }
                }
            });
            a[i].start();

            b[i] = new Thread(() -> {//creating 10 threads to convert them into kanji
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
                            String kanji = KanjiLib.convert(number);
                            String candidate = String.format("%d,%s", number, kanji);
                            synchronized (generated) {
                                generated.add(candidate);
                            }
                        } else {
                            gotPoison = true; //stop working
                        }
                    }
                });
                b[i].start();
            }

            for (Thread i : a) {
                try {
                    i.join();
                } catch (Exception e) {
                }
            }
            for (Thread i : b) {
                try {
                    i.join();
                } catch (Exception e) {
                }
            }
            return generated;
        }

        private static boolean isInRange ( int count, int from, int to){
            return from <= count && count <= to;
        }
    }
