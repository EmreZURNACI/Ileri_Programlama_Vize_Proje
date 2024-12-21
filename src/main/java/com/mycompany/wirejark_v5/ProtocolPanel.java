package com.mycompany._wirejark;

import java.awt.GridLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class ProtocolPanel {

    private final JCheckBox tcpCheckBox;
    private final JCheckBox udpCheckBox;
    private final JCheckBox icmpCheckBox;
    private final JCheckBox arpCheckBox;
    private final JCheckBox dnsCheckBox;
    private final JCheckBox httpCheckBox;
    private final JCheckBox httpsCheckBox;
    private final JCheckBox ftpCheckBox;
    private final JCheckBox sshCheckBox;
    private final JCheckBox smtpCheckBox;
    private final JCheckBox pop3CheckBox;
    private final JCheckBox imapCheckBox;
    private final JCheckBox telnetCheckBox;
    private final JCheckBox smbCheckBox;
    private final JCheckBox rtspCheckBox;
    private final JCheckBox dhcpCheckBox;
    private final JCheckBox sslCheckBox;
    private final JCheckBox grpcCheckBox;

    public ProtocolPanel() {
        // Initialize the JCheckBox components
        tcpCheckBox = new JCheckBox("TCP", true);   // true = selected
        udpCheckBox = new JCheckBox("UDP", true);
        icmpCheckBox = new JCheckBox("ICMP", true);
        dnsCheckBox = new JCheckBox("DNS", true);
        arpCheckBox = new JCheckBox("ARP", true);
        httpCheckBox = new JCheckBox("HTTP", true);
        httpsCheckBox = new JCheckBox("HTTPS", true);
        ftpCheckBox = new JCheckBox("FTP", true);
        sshCheckBox = new JCheckBox("SSH", true);
        smtpCheckBox = new JCheckBox("SMTP", true);
        pop3CheckBox = new JCheckBox("POP3", true);
        imapCheckBox = new JCheckBox("IMAP", true);
        telnetCheckBox = new JCheckBox("TELNET", true);
        smbCheckBox = new JCheckBox("SMB", true);
        rtspCheckBox = new JCheckBox("RSTP", true);
        dhcpCheckBox = new JCheckBox("DHCP", true);
        sslCheckBox = new JCheckBox("SSL", true);
        grpcCheckBox = new JCheckBox("GRPC", true);
    }

    // Return the panel containing all checkboxes
    public JPanel getProtocolPanel() {
        JPanel protocolPanel = new JPanel(new GridLayout(3, 5, 5, 5));

        // Add checkboxes to the panel
        protocolPanel.add(tcpCheckBox);
        protocolPanel.add(udpCheckBox);
        protocolPanel.add(icmpCheckBox);
        protocolPanel.add(dnsCheckBox);
        protocolPanel.add(arpCheckBox);
        protocolPanel.add(httpCheckBox);
        protocolPanel.add(httpsCheckBox);
        protocolPanel.add(ftpCheckBox);
        protocolPanel.add(sshCheckBox);
        protocolPanel.add(smtpCheckBox);
        protocolPanel.add(pop3CheckBox);
        protocolPanel.add(imapCheckBox);
        protocolPanel.add(telnetCheckBox);
        protocolPanel.add(smbCheckBox);
        protocolPanel.add(rtspCheckBox);
        protocolPanel.add(dhcpCheckBox);
        protocolPanel.add(sslCheckBox);
        protocolPanel.add(grpcCheckBox);

        return protocolPanel;
    }
}
