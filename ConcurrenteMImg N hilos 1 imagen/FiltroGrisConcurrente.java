import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;

public class FiltroGrisConcurrente implements Callable<Void> {

    private final BufferedImage imagen;
    private final int inicioFila;
    private final int finFila;

    public FiltroGrisConcurrente(BufferedImage imagen, int inicioFila, int finFila) {
        this.imagen = imagen;
        this.inicioFila = inicioFila;
        this.finFila = finFila;
    }

    @Override
    public Void call() throws Exception {
        int width = imagen.getWidth();

        for (int y = inicioFila; y < finFila; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = imagen.getRGB(x, y);

                int rojo  = (pixel >> 16) & 0xff;
                int verde = (pixel >> 8) & 0xff;
                int azul  = pixel & 0xff;

                int gris = (rojo + verde + azul) / 3;

                int nuevoPixel = (gris << 16) | (gris << 8) | gris;
                imagen.setRGB(x, y, nuevoPixel);
            }
        }

        return null;
    }
}
