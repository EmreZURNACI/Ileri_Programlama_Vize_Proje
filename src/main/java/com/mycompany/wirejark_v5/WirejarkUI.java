package com.mycompany.wirejark_v5;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class WirejarkUI extends JFrame {

    private DefaultListModel<String> listModel;
    private JList<String> portList;
    private JButton showPortsButton;
    private JButton startMonitoringButton;
    private JButton closePortsButton;
    private JButton showNetworkInterfacesButton; // Ağ arayüzlerini listeleme butonu
    private JTextArea packetArea;
    private PortScan portScanner;

    public WirejarkUI(PortScan portScanner) {
        this.portScanner = portScanner;

        setTitle("Wirejark");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Liste Modeli ve JList
        listModel = new DefaultListModel<>();
        portList = new JList<>(listModel);
        portList.setBorder(BorderFactory.createTitledBorder("Açık Portlar"));
        portList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Portları Göster Butonu
        showPortsButton = new JButton("Portları Göster");
        showPortsButton.addActionListener(new ShowPortsAction());

        // Paketleri İzleme Butonu
        startMonitoringButton = new JButton("Paketleri İzlemeye Başla");
        startMonitoringButton.addActionListener(new StartMonitoringAction());

        // Seçili Portları Kapat Butonu
        closePortsButton = new JButton("Seçili Portları Kapat");
        closePortsButton.addActionListener(new ClosePortsAction());

        // Ağ Arayüzlerini Listele Butonu
        showNetworkInterfacesButton = new JButton("Ağ Arayüzlerini Listele");
        showNetworkInterfacesButton.addActionListener(new ShowNetworkInterfacesAction());

        // Paket Gösterim Alanı
        packetArea = new JTextArea();
        packetArea.setEditable(false);
        packetArea.setBorder(BorderFactory.createTitledBorder("Gelen/Giden Paketler ve Ağ Arayüzleri"));
        JScrollPane packetScrollPane = new JScrollPane(packetArea);

        // Panel düzenleme
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(showPortsButton, BorderLayout.NORTH);
        panel.add(new JScrollPane(portList), BorderLayout.CENTER);

        // Butonları bir alt panelde düzenleme
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        buttonPanel.add(startMonitoringButton);
        buttonPanel.add(closePortsButton);
        buttonPanel.add(showNetworkInterfacesButton); // Ağ arayüzleri listeleme butonu eklendi
        panel.add(buttonPanel, BorderLayout.SOUTH); // Alt butonları panelin altına ekliyoruz

        add(panel, BorderLayout.WEST);
        add(packetScrollPane, BorderLayout.CENTER);
    }

    // Portları tarayan iç sınıf
    private class ShowPortsAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            listModel.clear();
            showPortsButton.setEnabled(false);
            new Thread(() -> {
                try {
                    List<Integer> openPorts = portScanner.findOpenPorts(36); // Örneğin 36 thread kullanarak tarama
                    //1 tane thread açınca 13.28dk bekledi amk
                    //36 tane açınca 4.48s
                    openPorts.sort(Integer::compareTo);

                    SwingUtilities.invokeLater(() -> {
                        for (int port : openPorts) {
                            listModel.addElement("Port " + port);
                        }
                        showPortsButton.setEnabled(true);
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> showPortsButton.setEnabled(true));
                }
            }).start();
        }
    }

    // Ağ arayüzlerini listeleyen iç sınıf
    private class ShowNetworkInterfacesAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            NetworkInterfaces lister = new NetworkInterfaces();
            packetArea.setText(""); // Önceki verileri temizle
            packetArea.append("Ağ Arayüzleri:\n----------------\n");
            lister.listNetworkInterfaces(packetArea); // Ağ arayüzlerini listele ve packetArea'ya yaz
        }
    }

    // Seçili portları dinlemeye başlayan iç sınıf
    private class StartMonitoringAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int[] selectedIndices = portList.getSelectedIndices();
            if (selectedIndices.length == 0) {
                JOptionPane.showMessageDialog(WirejarkUI.this, "Lütfen en az bir port seçin.", "Uyarı", JOptionPane.WARNING_MESSAGE);
                return;
            }

            packetArea.setText(""); // Eski verileri temizliyoruz
            for (int index : selectedIndices) {
                String portString = listModel.getElementAt(index).replaceAll("\\D+", ""); // Port numarasını alıyoruz
                int port = Integer.parseInt(portString);

                // Her port için yeni bir PortListener thread'i başlatıyoruz
                NetworkPackets listener = new NetworkPackets(port, packetArea);
                new Thread(listener).start();
            }
        }
    }

    private class ClosePortsAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int[] selectedIndices = portList.getSelectedIndices();
            if (selectedIndices.length == 0) {
                JOptionPane.showMessageDialog(WirejarkUI.this, "Lütfen kapatmak için en az bir port seçin.", "Uyarı", JOptionPane.WARNING_MESSAGE);
                return;
            }

            for (int index : selectedIndices) {
                String portString = listModel.getElementAt(index).replaceAll("\\D+", ""); // Port numarasını alıyoruz
                int port = Integer.parseInt(portString);

                // Gelen ve giden trafiği engellemek için komutlar
                String commandIn = "cmd /c netsh advfirewall firewall add rule name=\"ClosePort" + port + "_In\" protocol=TCP dir=in localport=" + port + " action=block";
                String commandOut = "cmd /c netsh advfirewall firewall add rule name=\"ClosePort" + port + "_Out\" protocol=TCP dir=out localport=" + port + " action=block";

                try {
                    // Gelen trafiği engelle
                    Process processIn = Runtime.getRuntime().exec(commandIn);
                    processIn.waitFor();

                    try (BufferedReader readerIn = new BufferedReader(new InputStreamReader(processIn.getErrorStream()))) {
                        String line;
                        while ((line = readerIn.readLine()) != null) {
                            System.out.println("Error (IN): " + line); // Gelen trafik engelleme hatası
                        }
                    }

                    // Giden trafiği engelle
                    Process processOut = Runtime.getRuntime().exec(commandOut);
                    processOut.waitFor();

                    try (BufferedReader readerOut = new BufferedReader(new InputStreamReader(processOut.getErrorStream()))) {
                        String line;
                        while ((line = readerOut.readLine()) != null) {
                            System.out.println("Error (OUT): " + line); // Giden trafik engelleme hatası
                        }
                    }

                    JOptionPane.showMessageDialog(null, "Port " + port + " gelen ve giden trafik için başarıyla kapatıldı.");
                } catch (IOException | InterruptedException ex) {
                    JOptionPane.showMessageDialog(null, "Port kapatılamadı: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
