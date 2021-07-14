package org.heigit.ors.fastisochrones.partitioning;

/**
 * Based on https://stackoverflow.com/questions/4958330/java-executorservice-awaittermination-of-all-recursively-created-tasks/4958416#4958416
 */
public class InverseSemaphore {
    private int value = 0;
    private final Object lock = new Object();

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
