package dialight.mvc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ViewDebug extends Application {

    public static void tryRunScenicView(Scene scene) {
        Class<?> class_ScenicView = null;
        try {
            class_ScenicView = Class.forName("org.scenicview.ScenicView");
        } catch (ClassNotFoundException ignore) {}
        if(class_ScenicView == null) return;
        try {
            Method m_ScenicView_show = class_ScenicView.getDeclaredMethod("show", Scene.class);
            m_ScenicView_show.invoke(null, scene);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void tryRunCssfx(Scene scene) {
        Class<?> class_CSSFX = null;
        try {
            class_CSSFX = Class.forName("org.fxmisc.cssfx.CSSFX");
        } catch (ClassNotFoundException ignore) {}
        if(class_CSSFX == null) return;
        try {
            Method m_CSSFX_start = class_CSSFX.getDeclaredMethod("start", Scene.class);
            m_CSSFX_start.invoke(null, scene);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private Parent tryResolveFromView() throws Exception {
        String viewClassName = this.getParameters().getNamed().get("view-class");
        if(viewClassName == null) return null;
        Class<?> viewClass = Class.forName(viewClassName);
        Constructor<?> viewClassConstructor = viewClass.getConstructor();
        View view = (View) viewClassConstructor.newInstance();
        return view.getRoot();
    }
    private Parent tryResolveFromFxml() throws Exception {
        String fxmlFile = this.getParameters().getNamed().get("fxml");
        if(fxmlFile == null) return null;
        Parent root = FXMLLoader.load(ViewDebug.class.getResource(fxmlFile));
        if(root == null) throw new Exception(fxmlFile + " is not found");
        return root;
    }

    private Parent resolveRoot() throws Exception {
        Parent root;
        if((root = tryResolveFromView()) != null) return root;
        if((root = tryResolveFromFxml()) != null) return root;
        System.err.println("You have to specify --view-class=<class> or --fxml=<file> parameter to args");
        throw new Exception("Bad args");
    }

    @Override public void start(Stage primaryStage) throws Exception {
        Parent root = resolveRoot();

        int width = Integer.parseInt(getParameters().getNamed().getOrDefault("width", "600"));
        int height = Integer.parseInt(getParameters().getNamed().getOrDefault("height", "400"));

        Scene scene = new Scene(root, width, height);
        primaryStage.setScene(scene);
        primaryStage.show();
        if(getParameters().getUnnamed().contains("--debug")) {
            tryRunScenicView(scene);
        }
    }

}
