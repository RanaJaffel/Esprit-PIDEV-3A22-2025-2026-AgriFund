package com.agrifund;

import view.gui.MainFrame;
import javax.swing.*;

public class AgrifundApp {
    public static void main(String[] args) {
        // Configurer le look and feel
        try {
            // Pour un look moderne, vous pouvez utiliser Nimbus
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

            // Ou utiliser le look du système
            // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Lancer l'interface dans le thread EDT (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}