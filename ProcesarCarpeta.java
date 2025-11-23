
import java.io.File; // clase para representar archivos y directorios

public class ProcesarCarpeta {

    public static void main(String[] args) {
        String carpeta = "\\ImagenesParalela\\Imagenes8K"; // ruta de la carpeta que contiene imágenes
        String carpetaSalida = carpeta + "\\salidaImagenes8k"; // ruta donde se guardarán las imágenes procesadas
        int numeroHilos = 16; // número por defecto de hilos a usar
        // Permitir pasar número de hilos como argumento opcional
        if (args.length >= 1) { // si se recibió al menos un argumento
            try {
                numeroHilos = Integer.parseInt(args[0]); // parsear el primer argumento como entero
            } catch (NumberFormatException e) { // si no es un número válido
                System.err.println("Argumento inválido para número de hilos, usando 16 por defecto."); // avisar y mantener el valor por defecto
            }
        }
        File dir = new File(carpeta); // objeto File para la carpeta de entrada
        if (!dir.exists() || !dir.isDirectory()) { // comprobar que existe y es un directorio
            System.err.println("Directorio no encontrado: " + carpeta); // informar si no existe
            return; // terminar la ejecución
        }
        File outDir = new File(carpetaSalida); // objeto File para la carpeta de salida
        if (!outDir.exists()) { // si la carpeta de salida no existe
            outDir.mkdirs(); // crear la carpeta (y padres si es necesario)
        }
        // Filtrar por extensiones comunes de imagen
        File[] archivos = dir.listFiles((d, name) -> { // listar archivos que cumplan el filtro
            String l = name.toLowerCase(); // convertir nombre a minúsculas para comparar
            return l.endsWith(".png") || l.endsWith(".jpg") || l.endsWith(".jpeg") || l.endsWith(".bmp") || l.endsWith(".gif"); // true si tiene extensión de imagen común
        });
        if (archivos == null || archivos.length == 0) { // si no hay archivos encontrados
            System.out.println("No se encontraron imágenes en: " + carpeta); // informar
            return; // terminar
        }
        System.out.println("Procesando " + archivos.length + " imágenes con " + numeroHilos + " hilos..."); // mostrar resumen
        // ---> Punto de ejecución: aquí se inicia el procesamiento concurrente de imágenes.
        // Llama a la clase `ImagenesConcurrentes` que crea y arranca los `Thread` (cada hilo
        // procesa un rango de archivos), ejecuta el filtro y guarda las imágenes.
        ImagenesConcurrentes.procesar(archivos, outDir, numeroHilos); // delega la ejecución concurrente
    }
}
