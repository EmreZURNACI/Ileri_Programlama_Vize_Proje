package com.mycompany.wirejark_v5;

import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.Packet;


import javax.swing.*;
import java.util.List;

public class NetworkPackets implements Runnable {

    private final int port;
    private final JTextArea packetArea;

    public NetworkPackets(int port, JTextArea packetArea) {
        this.port = port;
        this.packetArea = packetArea;
    }

    @Override
    public void run() {
        try {
            // Tüm ağ arayüzlerini alıyoruz
            List<PcapNetworkInterface> allDevs = Pcaps.findAllDevs();
            if (allDevs.isEmpty()) {
                SwingUtilities.invokeLater(() -> packetArea.append("Ağ arayüzü bulunamadı.\n"));
                return;
            }
            
            

            // İlk ağ arayüzünü seçiyoruz (ya da belirli bir tanesini seçebilirsiniz)
            PcapNetworkInterface nif = allDevs.get(0);

            // PcapHandle ile paket yakalamayı başlatıyoruz
            int snapLen = 65536;
            PcapHandle handle = nif.openLive(snapLen, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10);

            // Belirli bir port üzerinden paketleri filtrelemek için BPF filtresi ayarlıyoruz
            //String filter = "port " + port;
            //handle.setFilter(filter, PcapHandle.PcapCompileMode.OPTIMIZE);

            SwingUtilities.invokeLater(() -> packetArea.append("Port " + port + " üzerinde tüm paketler dinleniyor...\n"));

            // Paketleri sürekli dinlemek için döngü
            while (true) {
                Packet packet = handle.getNextPacket();
                if (packet != null) {
                    SwingUtilities.invokeLater(() -> packetArea.append("Paket yakalandı: " + packet + "\n"));
                }
            }

        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> packetArea.append("Dinleme hatası: " + e.getMessage() + "\n"));
        }
    }
}
