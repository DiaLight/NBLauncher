package dialight.mvc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void registerDone() {
        for (Controller controller : controllers) {
            controller.init(this);
        }
    }


}
