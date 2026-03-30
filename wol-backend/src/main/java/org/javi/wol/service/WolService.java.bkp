package org.javi.wol.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.inject.Inject;

@ApplicationScoped
public class WolService {

    @ConfigProperty(name = "wol.host")
    String targetHost; // Sacado de application.yaml

    @Inject
    Mailer mailer;

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

        // Enviar email de notificación. Si falla, lanzará una excepción que será capturada por el WolResource
        // y devolverá un error 500 al frontend.
        mailer.send(Mail.withText("viercadaver@gmail.com", "test", "Se ha enviado el magic packet para despertar el PC de Javi con MAC: (" + mac + ")."));
    }

    public PingResult checkStatus() {
        try {
            long start = System.currentTimeMillis();
            // Utilizar el comando ping del sistema porque isReachable en Linux requiere root para usar ICMP.
            ProcessBuilder pb = new ProcessBuilder("ping", "-c", "1", "-W", "2", targetHost);
            Process process = pb.start();
            boolean online = process.waitFor() == 0;
            long latencyMs = online ? (System.currentTimeMillis() - start) : -1;
            return new PingResult(online, latencyMs);
        } catch (Exception e) {
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