package dialight.misc;

import dialight.javafx.ErrorDialog;
import javafx.concurrent.Task;
import org.jetbrains.annotations.Nullable;

public abstract class SimpleTask<T> extends Task<T> {

    public SimpleTask() {

    }

    public void uiInit() {};

    public void uiDone(@Nullable T value) {}

}
