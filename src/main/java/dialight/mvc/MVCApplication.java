package dialight.mvc;

import java.util.ArrayList;
import java.util.List;

public class MVCApplication {

    private final List<Controller> controllers = new ArrayList<>();

    public void registerController(Controller controller) {
        controllers.add(controller);
    }

    public <T extends Controller> T findController(Class<T> cls) {
        for (Controller controller : controllers) {
            if(controller.getClass().isAssignableFrom(cls)) return (T) controller;
        }
        return null;
    }

    public void fireInit(Runnable done) {
        InitCtx ctx = new InitCtx(controllers);
        ctx.fireInit(this, done);
        for (Controller controller : controllers) {
            controller.init(ctx, this, done);
        }
    }


}
