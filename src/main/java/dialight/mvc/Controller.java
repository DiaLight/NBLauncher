package dialight.mvc;

public class Controller {

    protected void init(InitCtx ctx, MVCApplication app, Runnable done) {
        ctx.fireInit(app, done);
    }

}
