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
    private JButton showNetworkInterfacesButton;
    private JTextArea packetArea;
    private PortScan portScanner;

    private JTextField minPortField; // Minimum port numarası için JTextField
    private JTextField maxPortField; // Maksimum port numarası için JTextField

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

        // Min ve Max Port numaraları için JTextField'ler
        minPortField = new JTextField(5);
        maxPortField = new JTextField(5);

        // Port aralığı için panel
        JPanel portRangePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        portRangePanel.add(new JLabel("Min Port:"));
        portRangePanel.add(minPortField);
        portRangePanel.add(new JLabel("Max Port:"));
        portRangePanel.add(maxPortField);

        // Panel düzenleme
        JPanel panel = new JPanel(new BorderLayout());
        
        // Üst paneli, butonu ve port aralığı için alanları ekliyoruz
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(portRangePanel, BorderLayout.NORTH);
        topPanel.add(showPortsButton, BorderLayout.SOUTH); // "Portları Göster" butonunu buraya ekliyoruz
        
        panel.add(topPanel, BorderLayout.NORTH); // Üst paneli yerleştiriyoruz
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
        showPortsButton.setEnabled(false);  // Buton başlangıçta devre dışı
        new Thread(() -> {
            try {
                int minPort = 1;
                int maxPort = 65535;

                // Min ve Max portları al
                String minPortText = minPortField.getText();
                String maxPortText = maxPortField.getText();

                // Min port kontrolü
                if (!minPortText.isEmpty()) {
                    try {
                        minPort = Integer.parseInt(minPortText);
                        if (minPort < 1) {
                            JOptionPane.showMessageDialog(WirejarkUI.this, "Minimum port numarası 1'den küçük olamaz!", "Hata", JOptionPane.ERROR_MESSAGE);
                            SwingUtilities.invokeLater(() -> showPortsButton.setEnabled(true));  // Butonu tekrar etkinleştiriyoruz
                            return;
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(WirejarkUI.this, "Geçersiz minimum port numarası!", "Hata", JOptionPane.ERROR_MESSAGE);
                        SwingUtilities.invokeLater(() -> showPortsButton.setEnabled(true));  // Butonu tekrar etkinleştiriyoruz
                        return;
                    }
                }

                // Max port kontrolü
                if (!maxPortText.isEmpty()) {
                    try {
                        maxPort = Integer.parseInt(maxPortText);
                        if (maxPort > 65535) {
                            JOptionPane.showMessageDialog(WirejarkUI.this, "Maksimum port numarası 65535'den büyük olamaz!", "Hata", JOptionPane.ERROR_MESSAGE);
                            SwingUtilities.invokeLater(() -> showPortsButton.setEnabled(true));  // Butonu tekrar etkinleştiriyoruz
                            return;
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(WirejarkUI.this, "Geçersiz maksimum port numarası!", "Hata", JOptionPane.ERROR_MESSAGE);
                        SwingUtilities.invokeLater(() -> showPortsButton.setEnabled(true));  // Butonu tekrar etkinleştiriyoruz
                        return;
                    }
                }

                // Min port'un Max port'tan büyük olmaması kontrolü
                if (minPort > maxPort) {
                    JOptionPane.showMessageDialog(WirejarkUI.this, "Minimum port numarası maksimum port numarasından büyük olamaz!", "Hata", JOptionPane.ERROR_MESSAGE);
                    SwingUtilities.invokeLater(() -> showPortsButton.setEnabled(true));  // Butonu tekrar etkinleştiriyoruz
                    return;
                }

                // Port taramasını başlat
                List<Integer> openPorts = portScanner.findOpenPorts(36,minPort, maxPort); // Min ve max portlarla tarama yap
                openPorts.sort(Integer::compareTo);

                SwingUtilities.invokeLater(() -> {
                    for (int port : openPorts) {
                        listModel.addElement("Port " + port);
                    }
                    showPortsButton.setEnabled(true);  // Taramadan sonra butonu tekrar etkinleştiriyoruz
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> showPortsButton.setEnabled(true));  // Hata sonrası da buton etkinleştirilmeli
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
                String commandIn = "sudo ufw deny in to any port " + port + " proto tcp";
                String commandOut = "sudo ufw deny out to any port " + port + " proto tcp";

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