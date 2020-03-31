package dialight.nblauncher.view.launcher;

import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;

public class ModifierCell {

    private final CheckBox checkBox;
    private final String displayName;
    private final String id;

    public ModifierCell(String id, String displayName) {
        this.id = id;
        this.checkBox = new CheckBox(displayName);
        this.displayName = displayName;
        this.checkBox.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
    }

    public boolean isSelected() {
        return checkBox.isSelected();
    }

    public void setSelected(boolean selected) {
        checkBox.setSelected(selected);
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Node getGraphic() {
        return checkBox;
    }
}
