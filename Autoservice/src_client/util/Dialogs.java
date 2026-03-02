package util;

import javax.swing.*;

public final class Dialogs {
    private Dialogs() {}

    public static void info(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(String msg) {
        return JOptionPane.showConfirmDialog(null, msg, "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}