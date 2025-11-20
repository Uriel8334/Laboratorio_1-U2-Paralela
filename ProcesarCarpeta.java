
import java.io.File;


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

        // ---> Punto de ejecución: aquí se inicia el procesamiento concurrente de imágenes.
        // Llama a la clase `ImagenesConcurrentes` que crea y arranca los `Thread` (cada hilo
        // procesa un rango de archivos), ejecuta el filtro y guarda las imágenes.
        ImagenesConcurrentes.procesar(archivos, outDir, numeroHilos);
    }
}
