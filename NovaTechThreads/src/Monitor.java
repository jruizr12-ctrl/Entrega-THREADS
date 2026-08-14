import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Monitor implements Runnable {

    private static final long INTERVALO_MONITOR_MS = 1500;
    private static final DateTimeFormatter FORMATO_HORA =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final BlockingQueue<Pedido> colaPedidos;
    private final Estadisticas estadisticas;
    private final int totalTrabajadores;
    private final AtomicBoolean detener;
    private volatile Thread hilo;

    public Monitor(
            BlockingQueue<Pedido> colaPedidos,
            Estadisticas estadisticas,
            int totalTrabajadores
    ) {
        this.colaPedidos = colaPedidos;
        this.estadisticas = estadisticas;
        this.totalTrabajadores = totalTrabajadores;
        this.detener = new AtomicBoolean(false);
    }

    @Override
    public void run() {
        hilo = Thread.currentThread();
        log("Monitor iniciado.");

        while (!detener.get()) {
            mostrarEstado();

            try {
                Thread.sleep(INTERVALO_MONITOR_MS);
            } catch (InterruptedException e) {
                
                if (detener.get()) {
                    break;
                }
                Thread.currentThread().interrupt();
                log("Monitor interrumpido inesperadamente.");
                break;
            }
        }

        mostrarEstado();
        log("Monitor detenido correctamente.");
    }

    public void detener() {
        detener.set(true);
        Thread hiloActual = hilo;
        if (hiloActual != null) {
            hiloActual.interrupt();
        }
    }

    private void mostrarEstado() {
        System.out.println(
                "[MONITOR] Pendientes: " + colaPedidos.size()
                        + " | Aprobados: " + estadisticas.getAprobados()
                        + " | Rechazados: " + estadisticas.getRechazados()
                        + " | Errores: " + estadisticas.getErrores()
                        + " | Procesados: " + estadisticas.getProcesados()
                        + " | Activos: " + estadisticas.getTrabajadoresActivos()
                        + "/" + totalTrabajadores
        );
    }

    private void log(String mensaje) {
        String tiempo = LocalTime.now().format(FORMATO_HORA);
        System.out.println("[" + tiempo + "] [MONITOR] " + mensaje);
    }
}
