package com.mycompany._wirejark;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class PortScanner {

    private final List<Integer> openPorts = new ArrayList<>();

    // Belirtilen aralıktaki portları bulmak için metod
    public List<Integer> findOpenPorts(int threadCount, String targetIp, int startPort, int endPort) throws InterruptedException {
        openPorts.clear();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Callable<Void>> tasks = new ArrayList<>(); //Havuza atanmak üzere olan görevlerin tutludugu generic arayüzdür.
        int range = (endPort - startPort + 1) / threadCount;

        // Her bir thread için port aralığı belirliyoruz
        for (int i = 0; i < threadCount; i++) {
            int fromPort = startPort + i * range;
            int toPort = (i == threadCount - 1) ? endPort : fromPort + range - 1;
            tasks.add(() -> {
                scanPortsInRange(targetIp, fromPort, toPort);
                return null;
            });
        }

        // Tüm görevleri çalıştır
        executor.invokeAll(tasks);
        executor.shutdown(); //güvenli şekilde processleri durdurur.
        executor.awaitTermination(1, TimeUnit.MINUTES);

        return new ArrayList<>(openPorts); // Açık portları döndürüyoruz
    }

    // Belirli bir port aralığını tarayan metod
    private void scanPortsInRange(String targetIp, int startPort, int endPort) {
        for (int port = startPort; port <= endPort; port++) {
            if (isPortOpen(targetIp, port)) {
                openPorts.add(port); // Port açıksa listeye ekle
            }
        }
    }

    // Belirli bir portun açık olup olmadığını kontrol eden metod
    private boolean isPortOpen(String targetIp, int port) {
        try (Socket socket = new Socket()) {
            // Timeout süresi 150 ms olarak ayarlanmış
            socket.connect(new InetSocketAddress(targetIp, port), 1000); // Timeout süresini 1000 ms (1 saniye) yapın
            return true; // Eğer port açıksa true döner
        } catch (Exception e) {
            return false; // Eğer port kapalıysa false döner
        }
    }
}
