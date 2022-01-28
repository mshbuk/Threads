package threads;

import java.util.ArrayList;
import java.util.List;

public class Task1 {
    private static final int NUM_THREADS = 10;

    public static List<String> generate(final int from, final int to, final int count) {
        if (from < 0 || to < 0 || !isInRange(count, 0, to - from + 1)) throw new IllegalArgumentException();

        List<String> generated = new ArrayList<>(count);

        Thread[] threads = new Thread[NUM_THREADS];

        for (int i = 0; i < threads.length; i++) { //creating all 10 threads
            threads[i] = new Thread(() -> {
                while (true) {
                    int number = (int) (Math.random() * (to - from + 1) + from); //generating random numbers

                    String kanji = KanjiLib.convert(number); //convert them to kanji

                    String candidate = String.format("%d,%s", number, kanji);

                    synchronized (generated) {
                        if (generated.size() < count) {

                            if (!generated.contains(candidate)) { //checking is it is already present
                                generated.add(candidate);
                            }

                        } else {
                            break; //stop
                        }
                    }
                }
            });

            threads[i].start(); //lets get threads running
        }

        for (Thread t : threads) {
            try {
                t.join(); // allowing one thread to wait for the completion of another
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