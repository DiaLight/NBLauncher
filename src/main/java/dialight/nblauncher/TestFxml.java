package dialight.nblauncher;

import dialight.mvc.ViewDebug;
import javafx.application.Application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestFxml {

    public static void main(String[] args) {
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        argsList.add("--fxml=/test.fxml");
        argsList.add("--debug");
        argsList.addAll(Arrays.asList("--width=400", "--height=200"));
        Application.launch(ViewDebug.class, argsList.toArray(new String[0]));
    }

}
