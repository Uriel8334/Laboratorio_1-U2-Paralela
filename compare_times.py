#!/usr/bin/env python3
"""
compare_times.py

Script simple para comparar dos tiempos de procesamiento (ms) mediante
una tabla por consola y un gráfico de barras guardado como PNG.

Uso:
  python compare_times.py            # usa los valores por defecto
  python compare_times.py 216534 91043
  python compare_times.py --no-show  # no abre la ventana gráfica
"""
import sys
import csv
import matplotlib.pyplot as plt

# Valores por defecto (ms) proporcionados por el usuario
DEFAULTS = [216534, 91043]
LABELS = ["Hilos_en_imagen", "Hilo_por_imagen"]

def parse_args(argv):
    # permite recibir dos tiempos como argumentos posicionales
    show = True
    times = DEFAULTS.copy()
    args = [a for a in argv[1:] if a is not None]
    if "--no-show" in args:
        show = False
        args = [a for a in args if a != "--no-show"]
    if len(args) >= 2:
        try:
            t0 = int(args[0])
            t1 = int(args[1])
            times = [t0, t1]
        except ValueError:
            print("Argumentos inválidos: se esperan dos enteros en ms. Usando valores por defecto.")
    return times, show

def print_table(labels, times):
    # Imprime una tabla sencilla alineada
    print("\nComparación de tiempos (ms):")
    print(f"{'Metodo':<22}{'Tiempo (ms)':>12}")
    print('-' * 36)
    for lab, t in zip(labels, times):
        print(f"{lab:<22}{t:>12,}")

def save_csv(filename, labels, times):
    with open(filename, 'w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f)
        writer.writerow(["Metodo", "Tiempo_ms"])
        for lab, t in zip(labels, times):
            writer.writerow([lab, t])

def plot_bar(labels, times, out_png='tiempos_comparacion.png'):
    fig, ax = plt.subplots(figsize=(6,4))
    bars = ax.bar(labels, times, color=['#1f77b4', '#ff7f0e'])
    ax.set_ylabel('Tiempo (ms)')
    ax.set_title('Comparación de tiempos de procesamiento')
    ax.grid(axis='y', linestyle='--', alpha=0.4)
    # Anotar valores encima de cada barra
    for bar, t in zip(bars, times):
        h = bar.get_height()
        ax.text(bar.get_x() + bar.get_width()/2, h + max(times)*0.02, f"{t:,}", ha='center', va='bottom')
    plt.tight_layout()
    plt.savefig(out_png, dpi=150)
    return out_png

def main(argv):
    times, show = parse_args(argv)
    print_table(LABELS, times)
    csvfile = 'tiempos_comparacion.csv'
    save_csv(csvfile, LABELS, times)
    out_png = plot_bar(LABELS, times)
    print(f"\nCSV guardado en: {csvfile}")
    print(f"Gráfico guardado en: {out_png}")
    if show:
        plt.show()

if __name__ == '__main__':
    main(sys.argv)
