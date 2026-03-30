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
import org.jboss.logging.Logger;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class WolService {

    private static final Logger LOG = Logger.getLogger(WolService.class);

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
            
            // --- RÁFAGA DE 5 PAQUETES ---
            // Enviamos 5 veces para asegurar que el router y el PC se enteren a la primera
            for (int i = 0; i < 5; i++) {
                socket.send(packet);
                Thread.sleep(100); // 100 milisegundos de pausa entre envíos
            }
        }

        // Email de notificación: lo enviamos de forma ASÍNCRONA (en segundo plano)
        // para que la petición HTTP termine inmediatamente y Nginx no dé "504 Gateway Time-out"
        CompletableFuture.runAsync(() -> {
            long startTime = System.currentTimeMillis();
            long timeoutMs = 4 * 60 * 1000; // 4 minutos
            boolean emailSent = false;
            
            while (!emailSent && (System.currentTimeMillis() - startTime) < timeoutMs) {
                try {
                    mailer.send(Mail.withText("viercadaver@gmail.com", 
                        "WOL: Despertando PC", 
                        "Se ha enviado una ráfaga de Magic Packets para despertar el PC de Javi con MAC: (" + mac + ")."));
                    emailSent = true;
                    LOG.info("Notification email sent successfully for MAC: " + mac);
                } catch (Exception e) {
                    LOG.error("Fallo enviando correo para MAC " + mac + ". Reintentando en 5 segundos... Error: " + e.getMessage());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Hilo interrumpido", ie);
                    }
                }
            }
            
            if (!emailSent) {
                LOG.error("No se pudo enviar el correo de notificación tras 4 minutos.");
            }
        });
    }

    public PingResult checkStatus() {
        try {
            long start = System.currentTimeMillis();
            // Mantenemos el comando ping que ya arreglamos con la imagen de Docker
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
