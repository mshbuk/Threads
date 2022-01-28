package threads;

import java.util.ArrayList;
import java.util.List;

public class Task1 {
    private static final int NUM_THREADS = 10;

    public static List<String> generate(final int from, final int to, final int count) {
        if (from < 0 || to < 0 || !isInRange(count, 0, to - from + 1)) throw new IllegalArgumentException();

        List<String> generated = new ArrayList<>(count);

        Thread[] threads = new Thread[NUM_THREADS];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                while (true) {
                    int number = (int) (Math.random() * (to - from + 1) + from);

                    String kanji = KanjiLib.convert(number);

                    String candidate = String.format("%d,%s", number, kanji);

                    synchronized (generated) {
                        if (generated.size() < count) {

                            if (!generated.contains(candidate)) {
                                generated.add(candidate);
                            }

                        } else {
                            break;
                        }
                    }
                }
            });

            threads[i].start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return generated;
    }

    private static boolean isInRange(int count, int from, int to) {
        return from <= count && count <= to;
    }
}