package Calendar.ui;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

final class DialogShortcuts {

    private DialogShortcuts() {
    }

    static void installCloseShortcut(JDialog dialog) {
        int shortcutKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        KeyStroke closeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_W, shortcutKey);

        dialog.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(closeKeyStroke, "closeDialog");
        dialog.getRootPane()
                .getActionMap()
                .put("closeDialog", new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        dialog.dispose();
                    }
                });
    }
}
