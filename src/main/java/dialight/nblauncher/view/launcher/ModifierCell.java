package dialight.nblauncher.view.launcher;

import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;

public class ModifierCell {

    private final CheckBox checkBox;
    private final String modifier;

    public ModifierCell(String modifier) {
        this.modifier = modifier;
        this.checkBox = new CheckBox(modifier);
        this.checkBox.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
    }

    public boolean isSelected() {
        return checkBox.isSelected();
    }

    public void setSelected(boolean selected) {
        checkBox.setSelected(selected);
    }

    public String getModifier() {
        return modifier;
    }

    public Node getGraphic() {
        return checkBox;
    }
}
