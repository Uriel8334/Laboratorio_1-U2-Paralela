import java.io.File;

public class ProcesarCarpeta {

    public static void main(String[] args) {

        String carpeta = "C:\\UNI\\Software\\7mo\\CP\\Programas\\Lab1U2\\ImagenesParalela";
        String carpetaSalida = carpeta + "\\salidaImagenes";
        int numeroHilos = 16; 

        if (args.length >= 1) {
            try {
                numeroHilos = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Argumento inválido, usando 8 hilos.");
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

        File[] archivos = dir.listFiles((d, name) -> {
            String l = name.toLowerCase();
            return l.endsWith(".png") || l.endsWith(".jpg")
                    || l.endsWith(".jpeg") || l.endsWith(".bmp")
                    || l.endsWith(".gif");
        });

        if (archivos == null || archivos.length == 0) {
            System.out.println("No se encontraron imágenes.");
            return;
        }

        System.out.println("Procesando " + archivos.length +
                " imágenes usando " + numeroHilos + " hilos por imagen...");

        ImagenesConcurrentes.procesar(archivos, outDir, numeroHilos);
    }
}

