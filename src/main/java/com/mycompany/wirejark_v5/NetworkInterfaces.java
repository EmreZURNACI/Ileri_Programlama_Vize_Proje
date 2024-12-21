package com.mycompany.wirejark_v5;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import javax.swing.JTextArea;

public class NetworkInterfaces {

    // Ağ arayüzlerini JTextArea'ya yazdıran metot
    public void listNetworkInterfaces(JTextArea outputArea) {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();

                if (networkInterface.isUp()) {
                    outputArea.append("Arayüz Adı: " + networkInterface.getDisplayName() + "\n");
                    outputArea.append("Arayüz Kısa Adı: " + networkInterface.getName() + "\n");
                    outputArea.append("MAC Adresi: " + getMacAddress(networkInterface) + "\n");
                    outputArea.append("MTU: " + networkInterface.getMTU() + "\n");
                    outputArea.append("Loopback: " + eng_to_turk(networkInterface.isLoopback()) + "\n");
                    outputArea.append("Sanal Arayüz: " + eng_to_turk(networkInterface.isVirtual()) + "\n");
                    outputArea.append("Point-to-Point: " + eng_to_turk(networkInterface.isPointToPoint()) + "\n");
                    outputArea.append("Multicast Desteği: " + eng_to_turk(networkInterface.supportsMulticast()) + "\n");

                    // IPv4 ve IPv6 Adresleri
                    networkInterface.getInterfaceAddresses().forEach(interfaceAddress -> {
                        outputArea.append("IP Adresi: " + interfaceAddress.getAddress().getHostAddress() + "\n");
                        if (interfaceAddress.getBroadcast() != null) {
                            outputArea.append("Broadcast Adresi: " + interfaceAddress.getBroadcast().getHostAddress() + "\n");
                        }
                        outputArea.append("Alt Ağ Uzunluğu (Prefix Length): " + interfaceAddress.getNetworkPrefixLength() + "\n");
                    });

                    // Multicast Adresleri
                    Enumeration<InetAddress> multicastAddresses = networkInterface.getInetAddresses();
                    while (multicastAddresses.hasMoreElements()) {
                        outputArea.append("Multicast Adresi: " + multicastAddresses.nextElement().getHostAddress() + "\n");
                    }

                    outputArea.append("----------------------------\n");

                }
            }
        } catch (SocketException e) {
            outputArea.append("Hata oluştu: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    // MAC adresini okunabilir bir biçime çeviren yardımcı metot
    private String getMacAddress(NetworkInterface networkInterface) {
        try {
            byte[] mac = networkInterface.getHardwareAddress();
            if (mac == null) {
                return "MAC adresi bulunamadı";
            }

            StringBuilder macAddress = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                macAddress.append(String.format("%02X", mac[i]));
                if (i < mac.length - 1) {
                    macAddress.append("-");
                }
            }
            return macAddress.toString();
        } catch (SocketException e) {
            return "Hata oluştu";
        }
    }

    private String eng_to_turk(Boolean text) {
        if (text) {
            return "Evet";
        }
        return "Hayır";
    }
}
