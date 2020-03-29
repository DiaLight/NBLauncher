package dialight.misc;

import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class ProcessIO implements AutoCloseable {

    private final Process process;
    private final ExecutorService ioExecutor;
    private final Future<?> stdoutFuture;
    private final Future<?> stderrFuture;
    private final Object event = new Object();
    private final ReentrantLock lock = new ReentrantLock();
    private final LinkedList<String> stdout = new LinkedList<>();
    private final LinkedList<String> stderr = new LinkedList<>();

    public ProcessIO(Process process) {
        this.process = process;
        ioExecutor = Executors.newFixedThreadPool(2);
        stdoutFuture = ioExecutor.submit(this::readStdout);
        stderrFuture = ioExecutor.submit(this::readStderr);
    }

    private void readStdout() {
        try {
            try(BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while((line = br.readLine()) != null) {
                    try {
                        lock.lock();
                        stdout.addLast(line);
                    } finally {
                        lock.unlock();
                    }
                    signalIO();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            signalIO();
        }
    }

    private void readStderr() {
        try {
            try(BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while((line = br.readLine()) != null) {
                    try {
                        lock.lock();
                        stderr.addLast(line);
                    } finally {
                        lock.unlock();
                    }
                    signalIO();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            signalIO();
        }
    }

    public List<String> popAllStdout() {
        try {
            lock.lock();
            return new ArrayList<>(stdout);  // copy
        } finally {
            lock.unlock();
        }
    }

    public List<String> popAllStderr() {
        try {
            lock.lock();
            return new ArrayList<>(stderr);  // copy
        } finally {
            lock.unlock();
        }
    }

    @Nullable public String popStdout() {
        try {
            lock.lock();
            if(stdout.isEmpty()) return null;
            return stdout.removeFirst();
        } finally {
            lock.unlock();
        }
    }

    @Nullable public String popStderr() {
        try {
            lock.lock();
            if(stderr.isEmpty()) return null;
            return stderr.removeFirst();
        } finally {
            lock.unlock();
        }
    }

    private void signalIO() {
        synchronized (event) {
            event.notifyAll();
        }
    }
    public boolean hasIO() {
        try {
            lock.lock();
            if(!stdout.isEmpty()) return true;
            if(!stderr.isEmpty()) return true;
            return false;
        } finally {
            lock.unlock();
        }
    }
    public boolean waitForIO() {
        if(stdoutFuture.isDone() && stderrFuture.isDone()) return false;
        synchronized (event) {
            try {
                event.wait();
            } catch (InterruptedException ignore) {}
        }
        return true;
    }

    @Override public void close() throws Exception {
        process.waitFor();
        ioExecutor.shutdown();
        ioExecutor.awaitTermination(1, TimeUnit.HOURS);
    }

}
