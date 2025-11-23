
import java.awt.image.BufferedImage; // clase para representar imágenes en memoria
import java.io.File; // clase para trabajar con rutas y ficheros
import javax.imageio.ImageIO; // utilidades para leer/escribir imágenes

public class ImagenesConcurrentes {

    /**
     * Procesa un arreglo de archivos en paralelo usando varios hilos.
     *
     * @param archivos arreglo de archivos de imagen a procesar
     * @param outDir directorio donde se guardarán las imágenes procesadas
     * @param numeroHilos número máximo de hilos a usar
     */
    public static void procesar(File[] archivos, File outDir, int numeroHilos) {
        if (archivos == null || archivos.length == 0) { // si no hay archivos, salir
            System.out.println("No hay imágenes para procesar."); // informar
            return; // terminar ejecución del método
        }
        int hilosActivos = Math.min(numeroHilos, archivos.length); // limitar hilos a cantidad de archivos
        Thread[] hilos = new Thread[hilosActivos]; // arreglo para guardar referencias a los hilos
        long inicioTotal = System.nanoTime(); // marca de tiempo inicial para medir duración
        // calcula cuántos archivos tocará procesar a cada hilo
        int archivosPorHilo = archivos.length / hilosActivos; // parte entera
        int resto = archivos.length % hilosActivos; // sobrante que repartiremos uno a uno
        for (int i = 0; i < hilosActivos; i++) { // para cada hilo a crear
            final int inicioIdx = i * archivosPorHilo + Math.min(i, resto); // índice inicial (incluido)
            final int finIdx = inicioIdx + archivosPorHilo + (i < resto ? 1 : 0); // índice final (excluido)
            // Crear el hilo que procesará el rango [inicioIdx, finIdx)
            hilos[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int j = inicioIdx; j < finIdx; j++) { // recorrer cada archivo asignado
                        File archivo = archivos[j]; // obtener referencia al archivo actual
                        try {
                            // ---> Punto de ejecución: lectura del archivo de imagen
                            BufferedImage imagen = ImageIO.read(archivo); // lee el fichero a memoria
                            if (imagen == null) { // si no se leyó como imagen válida
                                System.err.println("No se pudo leer (no es imagen válida): " + archivo.getName()); // aviso
                                continue; // saltar a la siguiente iteración
                            }
                            // ---> Punto de ejecución: aplicación del filtro (procesamiento de la imagen)
                            FiltroGris filtro = new FiltroGris(imagen, 0, imagen.getHeight()); // crear runnable del filtro
                            filtro.run(); // Ejecuta el trabajo de conversión a escala de grises
                            // ---> Punto de ejecución: escritura del resultado a disco
                            File salida = new File(outDir, "gris_" + archivo.getName()); // preparar ruta de salida
                            ImageIO.write(imagen, "png", salida); // escribe la imagen transformada
                            System.out.println("Procesada: " + archivo.getName()); // informar archivo procesado
                        } catch (java.io.IOException e) { // captura errores de lectura/escritura
                            System.err.println("I/O error procesando " + archivo.getName() + ": " + e.getMessage());
                        } catch (RuntimeException e) { // captura errores inesperados en tiempo de ejecución
                            System.err.println("Error inesperado procesando " + archivo.getName() + ": " + e.getMessage());
                        }
                    }
                }
            });
            // ---> Punto de ejecución: arranque del hilo (inicia procesamiento en background)
            hilos[i].start(); // arranca el hilo
        }
        // Esperar a que todos los hilos terminen
        for (int i = 0; i < hilosActivos; i++) {
            try {
                hilos[i].join(); // bloquea hasta que el hilo i termine
            } catch (InterruptedException e) { // si se interrumpe la espera
                Thread.currentThread().interrupt(); // restablece el estado de interrupción
                System.err.println("Esperando hilos interrumpido."); // informa
            }
        }
        long finTotal = System.nanoTime(); // marca de tiempo final
        System.out.println("Todas las imágenes procesadas en " + ((finTotal - inicioTotal) / 1_000_000) + " ms"); // imprime duración en ms
        System.out.println("Salida guardada en: " + outDir.getPath()); // imprime ruta de salida
    }
}
