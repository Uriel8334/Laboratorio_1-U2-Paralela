import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ProcesarCarpeta {

    public static void main(String[] args) {
        String carpeta = "imagenes\\ImagenesParalela";
        String carpetaSalida = carpeta + "\\salidaImagenes";
        int numeroHilos = 16;

        // Permitir pasar número de hilos como argumento opcional
        if (args.length >= 1) {
            try {
                numeroHilos = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Argumento inválido para número de hilos, usando 16 por defecto.");
            }
        }

        File dir = new File(carpeta);
        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("Directorio no encontrado: " + carpeta);
            return;
        }

        File outDir = new File(carpetaSalida);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        // Filtrar por extensiones comunes de imagen
        File[] archivos = dir.listFiles((d, name) -> {
            String l = name.toLowerCase();
            return l.endsWith(".png") || l.endsWith(".jpg") || l.endsWith(".jpeg") || l.endsWith(".bmp") || l.endsWith(".gif");
        });

        if (archivos == null || archivos.length == 0) {
            System.out.println("No se encontraron imágenes en: " + carpeta);
            return;
        }

        System.out.println("Procesando " + archivos.length + " imágenes con " + numeroHilos + " hilos...");

        ExecutorService pool = Executors.newFixedThreadPool(numeroHilos);

        long inicioTotal = System.nanoTime();

        for (File archivo : archivos) {
            pool.submit(() -> {
                try {
                    BufferedImage imagen = ImageIO.read(archivo);
                    if (imagen == null) {
                        System.err.println("No se pudo leer (no es imagen válida): " + archivo.getName());
                        return;
                    }

                    // Usar FiltroGris para procesar la imagen completa (todas las filas)
                    FiltroGris filtro = new FiltroGris(imagen, 0, imagen.getHeight());
                    filtro.run(); // ejecutar en este hilo del pool

                    File salida = new File(outDir, "gris_" + archivo.getName());
                    ImageIO.write(imagen, "png", salida);

                    System.out.println("Procesada: " + archivo.getName());
                } catch (Exception e) {
                    System.err.println("Error procesando " + archivo.getName() + ": " + e.getMessage());
                }
            });
        }

        pool.shutdown();
        try {
            // Esperar hasta 1 hora como máximo
            if (!pool.awaitTermination(1, TimeUnit.HOURS)) {
                System.err.println("Timeout esperando tareas. Terminando.");
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Procesamiento interrumpido.");
            pool.shutdownNow();
        }

        long finTotal = System.nanoTime();
        System.out.println("Todas las imágenes procesadas en " + ((finTotal - inicioTotal) / 1_000_000) + " ms");
        System.out.println("Salida guardada en: " + outDir.getPath());
    }
}
