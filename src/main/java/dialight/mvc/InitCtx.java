package dialight.mvc;

import java.util.Iterator;
import java.util.List;

public class InitCtx {

    private final Iterator<Controller> it;

    public InitCtx(List<Controller> controllers) {
        this.it = controllers.iterator();
    }

    public void fireInit(MVCApplication app, Runnable done) {
        if(!it.hasNext()) {
            done.run();
            return;
        }
        Controller controller = it.next();
        controller.init(this, app, done);
    }

}
