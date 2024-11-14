package com.mycompany.wirejark_v5;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class PortScan {

    private final List<Integer> openPorts = new CopyOnWriteArrayList<>();

    public List<Integer> findOpenPorts(int threadCount,int startPort,int endPort) throws InterruptedException {
        // Tarama öncesinde listeyi temizliyoruz
        openPorts.clear();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        List<Callable<Void>> tasks = new ArrayList<>();
        int range = (endPort - startPort + 1) / threadCount;

        for (int i = 0; i < threadCount; i++) {
            int fromPort = startPort + i * range;
            int toPort = (i == threadCount - 1) ? endPort : fromPort + range - 1;
            tasks.add(() -> {
                scanPortsInRange(fromPort, toPort);
                return null;
            });
        }

        executor.invokeAll(tasks); // Tüm görevleri çalıştır
        executor.shutdown();       // Executor'ı kapat
        executor.awaitTermination(1, TimeUnit.MINUTES);

        return new ArrayList<>(openPorts);
    }

    private void scanPortsInRange(int startPort, int endPort) {
        for (int port = startPort; port <= endPort; port++) {
            if (isPortOpen(port)) {
                openPorts.add(port);
            }
        }
    }

    private boolean isPortOpen(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", port), 150); // Timeout'u 100 ms yaptık
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}