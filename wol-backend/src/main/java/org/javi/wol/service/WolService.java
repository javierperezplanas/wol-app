package org.javi.wol.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

@ApplicationScoped
public class WolService {

    @ConfigProperty(name = "wol.host")
    String targetHost; // Sacado de application.yaml

    public void sendMagicPacket(String mac) throws Exception {
        byte[] macBytes = parseMac(mac);
        byte[] bytes = new byte[6 + 16 * macBytes.length];

        Arrays.fill(bytes, 0, 6, (byte) 0xff);
        for (int i = 6; i < bytes.length; i += macBytes.length) {
            System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
        }

        InetAddress address = InetAddress.getByName(targetHost);
        try (DatagramSocket socket = new DatagramSocket()) {
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, 9);
            socket.send(packet);
        }
    }

    public PingResult checkStatus() {
        try {
            long start = System.currentTimeMillis();
            boolean online = InetAddress.getByName(targetHost).isReachable(2000);
            long latencyMs = online ? (System.currentTimeMillis() - start) : -1;
            return new PingResult(online, latencyMs);
        } catch (java.io.IOException e) {
            return new PingResult(false, -1);
        }
    }

    public record PingResult(boolean online, long latencyMs) {}

    private byte[] parseMac(String mac) {
        String[] hex = mac.split("(\\:|\\-)");
        byte[] bytes = new byte[6];
        for (int i = 0; i < 6; i++) {
            bytes[i] = (byte) Integer.parseInt(hex[i], 16);
        }
        return bytes;
    }
}