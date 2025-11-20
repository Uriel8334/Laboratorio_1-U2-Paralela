import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ImagenesConcurrentes {
    /**
     * Clase auxiliar que procesa un arreglo de archivos de imagen en paralelo.
     * Cada `Thread` recibe un rango de archivos a procesar.
     *
     * Acciones de ejecución importantes dentro de este método:
     * - Lectura de archivo de imagen: `ImageIO.read(archivo)`
     * - Procesamiento (ejecución del filtro): `filtro.run()`
     * - Escritura del archivo de salida: `ImageIO.write(...)`
     * - Creación/arranque de hilos: `new Thread(...).start()`
     * - Espera de finalización de hilos: `thread.join()`
     */
    public static void procesar(File[] archivos, File outDir, int numeroHilos) {
        if (archivos == null || archivos.length == 0) {
            System.out.println("No hay imágenes para procesar.");
            return;
        }

        int hilosActivos = Math.min(numeroHilos, archivos.length);
        Thread[] hilos = new Thread[hilosActivos];

        long inicioTotal = System.nanoTime();

        int archivosPorHilo = archivos.length / hilosActivos;
        int resto = archivos.length % hilosActivos;

        for (int i = 0; i < hilosActivos; i++) {
            final int inicioIdx = i * archivosPorHilo + Math.min(i, resto);
            final int finIdx = inicioIdx + archivosPorHilo + (i < resto ? 1 : 0);
            // Crear el hilo que procesará el rango [inicioIdx, finIdx)
            hilos[i] = new Thread(() -> {
                for (int j = inicioIdx; j < finIdx; j++) {
                    File archivo = archivos[j];
                    try {
                        // ---> Punto de ejecución: lectura del archivo de imagen
                        BufferedImage imagen = ImageIO.read(archivo);
                        if (imagen == null) {
                            System.err.println("No se pudo leer (no es imagen válida): " + archivo.getName());
                            continue;
                        }

                        // ---> Punto de ejecución: aplicación del filtro (procesamiento de la imagen)
                        FiltroGris filtro = new FiltroGris(imagen, 0, imagen.getHeight());
                        filtro.run(); // Ejecuta el trabajo de conversión a escala de grises

                        // ---> Punto de ejecución: escritura del resultado a disco
                        File salida = new File(outDir, "gris_" + archivo.getName());
                        ImageIO.write(imagen, "png", salida);

                        System.out.println("Procesada: " + archivo.getName());
                    } catch (java.io.IOException e) {
                        System.err.println("I/O error procesando " + archivo.getName() + ": " + e.getMessage());
                    } catch (RuntimeException e) {
                        System.err.println("Error inesperado procesando " + archivo.getName() + ": " + e.getMessage());
                    }
                }
            });

            // ---> Punto de ejecución: arranque del hilo (inicia procesamiento en background)
            hilos[i].start();
        }

        // Esperar a que todos los hilos terminen
        for (int i = 0; i < hilosActivos; i++) {
            try {
                hilos[i].join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Esperando hilos interrumpido.");
            }
        }

        long finTotal = System.nanoTime();
        System.out.println("Todas las imágenes procesadas en " + ((finTotal - inicioTotal) / 1_000_000) + " ms");
        System.out.println("Salida guardada en: " + outDir.getPath());
    }
}
