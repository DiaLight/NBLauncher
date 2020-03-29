package dialight.misc;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Worker;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SimpleService<T> extends Service<T> {

    private static Field f_service_task;

    static {
        try {
            f_service_task = Service.class.getDeclaredField("task");
            f_service_task.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
    private final Supplier<SimpleTask<T>> createTask;
    private final Runnable onFinish;
    private final BooleanProperty done = new SimpleBooleanProperty(false);
    private final BooleanProperty busy = new SimpleBooleanProperty(false);
    private final ObjectProperty<Consumer<T>> onDone = new SimpleObjectProperty<>();
    private final ObjectProperty<Runnable> onBusy = new SimpleObjectProperty<>();

    public SimpleService(Supplier<SimpleTask<T>> createTask, Runnable onFinish) {
        this.createTask = createTask;
        this.onFinish = onFinish;
        stateProperty().addListener((observable, oldValue, newValue) -> {
            boolean busy = newValue == Worker.State.SCHEDULED || newValue == Worker.State.RUNNING;
            this.busy.setValue(busy);

            boolean done = newValue == State.SUCCEEDED || newValue == State.CANCELLED || newValue == State.FAILED;
            this.done.setValue(done);
        });

        this.done.addListener((observable, oldValue, newValue) -> {
            if(newValue) {
                Consumer<T> consumer = onDone.get();
                if(consumer != null) consumer.accept(getValue());
                getTask().uiDone(getValue());
//                Throwable exception = getException();
//                if(exception != null) getTask().uiError(exception);
                reset();
                Platform.runLater(onFinish);
            }
        });

        this.busy.addListener((observable, oldValue, newValue) -> {
            if(newValue) {
                Runnable runnable = onBusy.get();
                if(runnable != null) runnable.run();
                getTask().uiInit();
            }
        });
        this.setOnFailed(event -> {
            getException().printStackTrace();
        });
    }

    protected SimpleTask<T> getTask() {
        try {
            return (SimpleTask<T>) f_service_task.get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override protected SimpleTask<T> createTask() {
        return createTask.get();
    }

    public boolean isDone() {
        return done.get();
    }
    public BooleanProperty doneProperty() {
        return done;
    }
    public ObjectProperty<Consumer<T>> onDoneProperty() { return onDone; }
    public void setOnDone(Consumer<T> value) { onDone.set(value); }

    public boolean isBusy() {
        return busy.get();
    }
    public BooleanProperty busyProperty() {
        return busy;
    }
    public ObjectProperty<Runnable> onBusyProperty() {
        return onBusy;
    }
    public void setOnBusy(Runnable onBusy) {
        this.onBusy.set(onBusy);
    }

}
