package com.mycompany._wirejark;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class _Wirejark {

    public static void main(String[] args) {
        try {
            SwingUtilities.invokeLater(() -> {
                try {
                    // Create the UI and make it visible
                    UI wirejarkUI = new UI();
                    wirejarkUI.setVisible(true);
                } catch (Exception e) {
                    // Log exception if UI creation fails
                    JOptionPane.showMessageDialog(null, "UI olusturulurken bir hata meydana geldi.", "Hata", JOptionPane.ERROR_MESSAGE);
                }
            });
        } catch (Exception e) {
            // Log any exception thrown during PortScan or UI initialization
            JOptionPane.showMessageDialog(null, "Port tarayici baslatilirken bir hata olustu.", "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
}
