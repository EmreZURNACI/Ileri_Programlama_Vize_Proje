package com.mycompany._wirejark;

import org.pcap4j.core.*;
import org.pcap4j.packet.Packet;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkPackets implements Runnable {

    private final JTextArea packetArea;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final int snapLen = 65536; // Maksimum paket boyutu
    private final int timeout = 50; // Timeout (ms)
    private final List<Integer> ports;
    private final List<PcapHandle> handles = new ArrayList<>(); // Handle'ları sakla
    private final ExecutorService executorService = Executors.newCachedThreadPool(); // Thread havuzu

    // Yapıcı metot
    public NetworkPackets(List<Integer> ports, JTextArea packetArea) {
        this.ports = ports;
        this.packetArea = packetArea;
    }

    @Override
    public void run() {
        try {
            running.set(true);

            // Ağ arayüzlerini listele
            List<PcapNetworkInterface> allDevs = Pcaps.findAllDevs();
            if (allDevs.isEmpty()) {
                SwingUtilities.invokeLater(() -> packetArea.append("Ağ arayüzü bulunamadı.\n"));
                return;
            }

            // Tüm arayüzlerde paralel paket dinleme işlemi başlat
            for (PcapNetworkInterface nif : allDevs) {
                executorService.submit(() -> startPacketCapture(nif)); // Thread havuzuna görev ekle
            }

        } catch (PcapNativeException e) {
            SwingUtilities.invokeLater(() -> packetArea.append("Dinleme hatası: " + e.getMessage() + "\n"));
        }
    }

    private void startPacketCapture(PcapNetworkInterface nif) {
        PcapHandle handle = null;
        try {
            // Paket yakalamayı başlat
            handle = nif.openLive(snapLen, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, timeout);

            // Handle'ı listeye ekle
            synchronized (handles) {
                handles.add(handle);
            }

            // BPF filtresi oluştur ve uygula
            String bpfFilter = createBpfFilter(ports);
            if (!bpfFilter.isEmpty()) {
                try {
                    handle.setFilter(bpfFilter, BpfProgram.BpfCompileMode.OPTIMIZE);
                } catch (PcapNativeException | NotOpenException e) {
                    SwingUtilities.invokeLater(() -> packetArea.append("Filtre uygulama hatası: " + e.getMessage() + "\n"));
                }
            }

            // Paket yakalama döngüsü
            while (running.get() && !Thread.currentThread().isInterrupted()) {
                Packet packet = handle.getNextPacket();
                if (packet != null) {
                    SwingUtilities.invokeLater(() -> packetArea.append("Paket: " + packet + "\n"));
                }
            }

        } catch (NotOpenException | PcapNativeException e) {
            SwingUtilities.invokeLater(() -> packetArea.append("Dinleme hatası: " + e.getMessage() + "\n"));
        } finally {
            // İşlemi tamamladıktan sonra handle'ı kapat
            if (handle != null && handle.isOpen()) {
                handle.close();
                SwingUtilities.invokeLater(() -> packetArea.append("Paket yakalama işlemi durduruldu.\n"));
            }
        }
    }

    // Dinlemeyi durdur
    public void stop() {
        running.set(false); // Dinlemeyi hemen durdur

        // Tüm handle'ları kapat
        synchronized (handles) {
            for (PcapHandle handle : handles) {
                if (handle.isOpen()) {
                    try {
                        handle.breakLoop(); // Döngüyü hemen kır
                    } catch (NotOpenException e) {
                        // Zaten kapalıysa işlem yapma
                    }
                    handle.close(); // Handle'ı kapat
                }
            }
            handles.clear(); // Handle listesine dair veriyi temizle
        }

        // ExecutorService'i kapat
        executorService.shutdownNow(); // Tüm görevleri sonlandır
        executorService.shutdown();

        SwingUtilities.invokeLater(() -> packetArea.append("Dinleme işlemi tamamen durduruldu.\n"));
    }

    // Port listesine göre BPF filtresi oluşturma
    private String createBpfFilter(List<Integer> ports) {
        StringBuilder filter = new StringBuilder();
        if (ports != null && !ports.isEmpty()) {
            filter.append("(tcp or udp) and (");
            for (int i = 0; i < ports.size(); i++) {
                filter.append("port ").append(ports.get(i));
                if (i < ports.size() - 1) {
                    filter.append(" or ");
                }
            }
            filter.append(")");
        }
        return filter.toString();
    }
}
