package org.jets3t.service.mx;

final class LongCounter {
    private final Object lock = new Object();
    private long value = 0;

    void increment() {
        synchronized (this.lock) {
            this.value++;
        }
    }

    long getValue() {
        synchronized (this.lock) {
            return this.value;
        }
    }
}