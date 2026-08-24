package icons;

import com.intellij.ui.AnimatedIcon;
import com.intellij.ui.IconManager;

import javax.swing.*;
import java.util.List;

public interface ShurikenIcons {
    Icon SHURIKEN_STEP_1 = IconManager.getInstance().getIcon("/com/lawmillenium/intellij/plugins/narutoprogress/icons/shuriken16.svg",
            ShurikenIcons.class.getClassLoader());
    Icon SHURIKEN_STEP_2 = IconManager.getInstance().getIcon("/com/lawmillenium/intellij/plugins/narutoprogress/icons/shuriken16-rot45.svg",
            ShurikenIcons.class.getClassLoader());
    Icon SPINNING_SHURIKENS = new AnimatedIcon(130, getShurikensIcons().toArray(new Icon[0]));

    static List<Icon> getShurikensIcons() {
        return List.of(SHURIKEN_STEP_1, SHURIKEN_STEP_2);
    }
}
