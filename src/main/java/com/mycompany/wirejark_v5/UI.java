package com.mycompany._wirejark;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UI extends JFrame {

    private final JPanel portlar;
    private final JButton showPortsButton;
    private final JButton startMonitoringButton;
    private final JButton closePortsButton;
    private final JButton showNetworkInterfacesButton;
    private final JButton stopMonitoringButton;
    private final PortScanner portScanner;
    private final JTextArea packetArea;
    private final JTextField minPortField;
    private final JTextField maxPortField;

    private final List<NetworkPackets> networkPacketsList;

    public UI() {
        portScanner = new PortScanner();
        setTitle("Wirejark");

        // Ekran boyutlarını alıyoruz
        int screenWidth = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
        int screenHeight = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;

        // Pencereyi ekran boyutuna göre ayarlıyoruz
        setSize(screenWidth, screenHeight);

        // Pencereyi tam ekran yapıyoruz
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Pencerenin ekranın ortasında olmasını sağlıyoruz
        setLocationRelativeTo(null);  // Bu, pencereyi ekranda ortalar

        // Pencereyi kapatıldığında uygulamayı sonlandırmak için
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        packetArea = new JTextArea();

        portlar = new JPanel();
        portlar.setLayout(new BoxLayout(portlar, BoxLayout.Y_AXIS));  // Dikey düzen ayarı

        showPortsButton = new JButton("Portları Göster");
        showPortsButton.addActionListener(new ShowPortsAction());

        startMonitoringButton = new JButton("Paketleri İzlemeye Başla");
        startMonitoringButton.addActionListener(new StartMonitoringAction());

        closePortsButton = new JButton("Seçili Portları Kapat");
        closePortsButton.addActionListener(new ClosePortsAction(portScanner));

        showNetworkInterfacesButton = new JButton("Ağ Arayüzlerini Listele");
        showNetworkInterfacesButton.addActionListener(new ShowNetworkInterfacesAction());

        stopMonitoringButton = new JButton("Dinlemeyi Durdur");
        NetworkPackets networkPackets = new NetworkPackets(selectedPorts, packetArea);
        stopMonitoringButton.addActionListener(new StopMonitoringAction(networkPackets));

        packetArea.setEditable(false);
        packetArea.setBorder(BorderFactory.createTitledBorder("Gelen/Giden Paketler ve Ağ Arayüzleri"));
        JScrollPane packetScrollPane = new JScrollPane(packetArea);

        minPortField = new JTextField(8);
        maxPortField = new JTextField(8);

        JPanel portRangePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        portRangePanel.add(new JLabel("Min Port:"));
        portRangePanel.add(minPortField);
        portRangePanel.add(new JLabel("Max Port:"));
        portRangePanel.add(maxPortField);

        ProtocolPanel pp = new ProtocolPanel();

        JPanel topPanel = new JPanel(new BorderLayout());

        topPanel.add(portRangePanel, BorderLayout.NORTH);

        topPanel.add(pp.getProtocolPanel(), BorderLayout.CENTER);

        topPanel.add(showPortsButton, BorderLayout.SOUTH);

        JPanel panel = new JPanel(new BorderLayout());

        panel.add(topPanel, BorderLayout.NORTH);

        panel.add(new JScrollPane(portlar), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 5, 5));

        buttonPanel.add(startMonitoringButton);

        buttonPanel.add(closePortsButton);

        buttonPanel.add(showNetworkInterfacesButton);

        buttonPanel.add(stopMonitoringButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel, BorderLayout.WEST);

        add(packetScrollPane, BorderLayout.CENTER);

        networkPacketsList = new ArrayList<>();
    }

    private final List<Integer> selectedPorts = new ArrayList<>();

    private class ShowPortsAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            showPortsButton.setEnabled(false);
            new Thread(() -> {
                try {
                    int minPort = 1;
                    int maxPort = 65535;

                    // Parse min and max port numbers
                    String minPortText = minPortField.getText();
                    String maxPortText = maxPortField.getText();

                    if (!minPortText.isEmpty()) {
                        try {
                            minPort = Integer.parseInt(minPortText);
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(UI.this, "Geçersiz minimum port numarası!", "Hata", JOptionPane.ERROR_MESSAGE);
                            SwingUtilities.invokeLater(() -> showPortsButton.setEnabled(true));
                            return;
                        }
                    }

                    if (!maxPortText.isEmpty()) {
                        try {
                            maxPort = Integer.parseInt(maxPortText);
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(UI.this, "Geçersiz maksimum port numarası!", "Hata", JOptionPane.ERROR_MESSAGE);
                            SwingUtilities.invokeLater(() -> showPortsButton.setEnabled(true));
                            return;
                        }
                    }

                    // Find open ports
                    List<Integer> openPorts = portScanner.findOpenPorts(36, "127.0.0.1", minPort, maxPort);
                    // Sort open ports
                    Collections.sort(openPorts);

                    // Update UI with open ports
                    SwingUtilities.invokeLater(() -> {
                        portlar.removeAll();  // Eski butonları temizliyoruz
                        portlar.setLayout(new BoxLayout(portlar, BoxLayout.Y_AXIS));  // Dikey düzenleme

                        for (int port : openPorts) {
                            JButton portButton = new JButton("Port " + port);
                            portButton.setAlignmentX(Component.CENTER_ALIGNMENT);  // Ortalamak için
                            portButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30)); // Tam genişlik, sabit yükseklik
                            portButton.setBackground(Color.LIGHT_GRAY);  // Varsayılan arka plan rengi (gri)
                            portButton.setOpaque(true);
                            portButton.setBorderPainted(false);
                            portButton.setForeground(Color.BLACK);  // Varsayılan yazı rengi siyah

                            // MouseListener ekliyoruz
                            portButton.addMouseListener(new java.awt.event.MouseAdapter() {
                                @Override
                                public void mouseEntered(java.awt.event.MouseEvent evt) {
                                    portButton.setBackground(Color.BLACK);  // Koyu yeşil arka plan
                                    portButton.setForeground(Color.WHITE);  // Yazı rengini beyaz yap
                                    portButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));  // Fareyi el işaretine değiştir
                                }

                                @Override
                                public void mouseExited(java.awt.event.MouseEvent evt) {
                                    // Eğer seçilmişse yeşil kalmasını sağlıyoruz, değilse varsayılan gri renge dönüyoruz
                                    if (selectedPorts.contains(port)) {
                                        portButton.setBackground(new Color(0, 128, 0));  // Seçili ise yeşil
                                        portButton.setForeground(Color.WHITE);  // Yazı rengi beyaz
                                    } else {
                                        portButton.setBackground(Color.LIGHT_GRAY);  // Eski gri arka plana dön
                                        portButton.setForeground(Color.BLACK);  // Yazı rengini siyah yap
                                    }
                                    portButton.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));  // Fareyi varsayılan ok işaretine geri getir
                                }
                            });

                            portButton.addActionListener(e1 -> {
                                if (selectedPorts.contains(port)) {
                                    selectedPorts.remove(port);  // Portu seçili listeden kaldır
                                    portButton.setBackground(Color.LIGHT_GRAY);  // Varsayılan gri renge geri döndür
                                    portButton.setForeground(Color.BLACK);  // Yazı rengini siyah yap
                                } else {
                                    selectedPorts.add(port);  // Portu seçili listesine ekle
                                    portButton.setBackground(new Color(0, 128, 0));  // Seçili durumu için renk değiştir
                                    portButton.setForeground(Color.WHITE);  // Yazı rengini beyaz yap
                                }
                            });

                            portlar.add(portButton);  // Butonları portlar paneline ekliyoruz
                        }

                        portlar.revalidate(); //Panele eklenen butonları göstermiyor.Revalidate iile tekrar kontrol edilerek yeniden hesap edilerek düzenlenir.
                        showPortsButton.setEnabled(true);  // "Portları Göster" butonunu etkinleştiriyoruz
                    });

                    showPortsButton.setEnabled(true);

                } catch (HeadlessException | InterruptedException ex) {
                    SwingUtilities.invokeLater(() -> showPortsButton.setEnabled(true));
                }
            }).start();
        }
    }

    private class ShowNetworkInterfacesAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                NetworkInterfaces ni = new NetworkInterfaces();
                ni.listNetworkInterfaces(packetArea);
            } catch (Exception ex) {
                packetArea.append("Hata: " + ex.getMessage() + "\n");
            }
        }
    }

    private class StartMonitoringAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                // Port seçimi yapılmamışsa, dinlemeyi başlatma
                if (selectedPorts == null || selectedPorts.isEmpty()) {
                    JOptionPane.showMessageDialog(UI.this, "Lütfen portları seçin.", "Hata", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                NetworkPackets networkPackets = new NetworkPackets(selectedPorts, packetArea);
                networkPacketsList.add(networkPackets);

                networkPacketsList.clear();

                Thread monitoringThread = new Thread(networkPackets);
                monitoringThread.start();
                //Swiing utilities EDT üeirnden çalıştığı için UI tarafında herhangi bir donma ve karışıklık söz konusu olmaması için
                //farklı bir thread il eçalıştırılır.

                startMonitoringButton.setEnabled(false);
                stopMonitoringButton.setEnabled(true);

            } catch (HeadlessException ex) {
                JOptionPane.showMessageDialog(UI.this, "Hata oluştu: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class StopMonitoringAction implements ActionListener {

        private final NetworkPackets networkPackets;

        // Yapıcı metot, NetworkPackets nesnesini alır
        public StopMonitoringAction(NetworkPackets networkPackets) {
            this.networkPackets = networkPackets;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Dinleme işlemini durdur
            if (networkPackets != null) {
                networkPackets.stop(); // NetworkPackets'teki thread'leri durdurur
            }

            // UI düğmelerini güncelle
            startMonitoringButton.setEnabled(true);
            stopMonitoringButton.setEnabled(false);
        }
    }

    private class ClosePortsAction implements ActionListener {

        public ClosePortsAction(PortScanner portScanner) {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(UI.this,
                    "Seçilen portlar: " + selectedPorts.toString() + " başarıyla kapatıldı.",
                    "Başarılı",
                    JOptionPane.INFORMATION_MESSAGE);

        }
    }
}
