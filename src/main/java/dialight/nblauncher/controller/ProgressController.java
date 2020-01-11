package dialight.nblauncher.controller;

import dialight.misc.SimpleService;
import dialight.misc.SimpleTask;
import dialight.mvc.Controller;
import javafx.beans.property.*;
import javafx.concurrent.Worker;

import java.util.LinkedList;
import java.util.Queue;

public class ProgressController extends Controller {

    private final SimpleService<?> service = new SimpleService<>(this::nextTask, this::onFinish);

    private final Queue<SimpleTask<?>> taskQueue = new LinkedList<>();

    private void onFinish() {
        if(!taskQueue.isEmpty()) {
            if (service.getState() == Worker.State.READY) {
                service.start();
            }
        }
    }

    private SimpleTask<?> nextTask() {
        return taskQueue.poll();
    }

    public boolean isBusy() {
        return service.isBusy();
    }

    public BooleanProperty busyProperty() {
        return service.busyProperty();
    }

    public double getProgress() {
        return service.getProgress();
    }

    public ReadOnlyDoubleProperty progressProperty() {
        return service.progressProperty();
    }

    public String getStatus() {
        return service.getMessage();
    }

    public ReadOnlyStringProperty statusProperty() {
        return service.messageProperty();
    }

    public void scheduleTask(SimpleTask<?> task) {
        taskQueue.add(task);
        if (service.getState() == Worker.State.READY) {
            service.start();
        }
    }

}
