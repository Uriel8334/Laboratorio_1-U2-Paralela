import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.imageio.ImageIO;

public class ImagenesConcurrentes {

    public static void procesar(File[] archivos, File outDir, int numeroHilos) {

        if (archivos == null || archivos.length == 0) {
            System.out.println("No hay imágenes para procesar.");
            return;
        }

        ExecutorService pool = Executors.newFixedThreadPool(numeroHilos);

        long inicioTotal = System.nanoTime();

        for (File archivo : archivos) {
            try {

                // Tiempo individual por imagen
                long inicioImg = System.nanoTime();

                BufferedImage imagen = ImageIO.read(archivo);
                if (imagen == null) {
                    System.err.println("No es una imagen válida: " + archivo.getName());
                    continue;
                }

                int width = imagen.getWidth();
                int height = imagen.getHeight();

                int altura = imagen.getHeight();
                int filasPorTarea = altura / numeroHilos;
                int resto = altura % numeroHilos;

                List<Callable<Void>> tareas = new ArrayList<>();
                int inicio = 0;

                for (int i = 0; i < numeroHilos; i++) {
                    int extra = (i < resto) ? 1 : 0;
                    int fin = inicio + filasPorTarea + extra;

                    tareas.add(new FiltroGrisConcurrente(imagen, inicio, fin));
                    inicio = fin;
                }

                List<Future<Void>> resultados = pool.invokeAll(tareas);

                for (Future<Void> f : resultados) {
                    f.get();
                }

                File salida = new File(outDir, "gris_" + archivo.getName());
                ImageIO.write(imagen, "png", salida);

                long finImg = System.nanoTime();
                long tiempoMs = (finImg - inicioImg) / 1_000_000;

                System.out.println("Procesada: " + archivo.getName() +
                        " (" + width + "x" + height + ") - " +
                        tiempoMs + " ms");

            } catch (Exception e) {
                System.err.println("Error procesando " + archivo.getName() + ": " + e.getMessage());
            }
        }

        pool.shutdown();

        long finTotal = System.nanoTime();
        System.out.println("Todas las imágenes procesadas en " +
                ((finTotal - inicioTotal) / 1_000_000) + " ms");
    }
}


