package dialight.misc;

import dialight.javafx.ErrorDialog;
import javafx.concurrent.Task;
import org.jetbrains.annotations.Nullable;

public abstract class SimpleTask<T> extends Task<T> {

    public void uiInit() {};

    public void uiDone(@Nullable T value) {}

    public void uiError(@Nullable Throwable e) {
        ErrorDialog.show(e);
    }

}
