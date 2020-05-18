package org.heigit.ors.fastisochrones.partitioning;

public class InverseSemaphore {
    private int value = 0;
    private Object lock = new Object();

    public void beforeSubmit() {
        synchronized (lock) {
            value++;
        }
    }

    public void taskCompleted() {
        synchronized (lock) {
            value--;
            if (value == 0) lock.notifyAll();
        }
    }

    public void awaitCompletion() throws InterruptedException {
        synchronized (lock) {
            while (value > 0) lock.wait();
        }
    }
}
