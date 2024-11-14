package com.mycompany.wirejark_v5;

import javax.swing.SwingUtilities;

public class Wirejark_v5 {

    public static void main(String[] args) {
        PortScan portScanner = new PortScan();
        SwingUtilities.invokeLater(() -> {
            WirejarkUI wirejarkUI = new WirejarkUI(portScanner);
            wirejarkUI.setVisible(true);
        });
    }
}
