import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class ProcesadorPedidos implements Runnable {

    private static final DateTimeFormatter FORMATO_HORA =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final BlockingQueue<Pedido> colaPedidos;
    private final Inventario inventario;
    private final Estadisticas estadisticas;
    private final String nombreTrabajador;

    public ProcesadorPedidos(
            BlockingQueue<Pedido> colaPedidos,
            Inventario inventario,
            Estadisticas estadisticas,
            String nombreTrabajador
    ) {
        this.colaPedidos = colaPedidos;
        this.inventario = inventario;
        this.estadisticas = estadisticas;
        this.nombreTrabajador = nombreTrabajador;
    }

    @Override
    public void run() {
        estadisticas.trabajadorInicio();
        try {
            while (true) {
                Pedido pedido = colaPedidos.poll();
                if (pedido == null) break;
                procesarPedido(pedido);
            }
        } finally {
            estadisticas.trabajadorFin();
            log("Finaliza hilo trabajador.");
        }
    }

    private void procesarPedido(Pedido pedido) {
        log("Inicia pedido " + pedido.getId()
                + " | Cliente: " + pedido.getCliente());

        simularProcesamiento();

        ResultadoPedido resultado;
        try {
            resultado = inventario.procesarPedido(pedido);
        } catch (RuntimeException e) {
            resultado = ResultadoPedido.error(
                    "Error controlado: " + e.getMessage()
            );
        }

        estadisticas.registrarResultado(resultado);
        registrarResultado(pedido, resultado);
    }

    private void simularProcesamiento() {
        int milisegundos = ThreadLocalRandom.current().nextInt(500, 2001);
        try {
            Thread.sleep(milisegundos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log("Procesamiento interrumpido; se continúa con el cierre del trabajador.");
        }
    }

    private void registrarResultado(Pedido pedido, ResultadoPedido resultado) {
        String tiempo = LocalTime.now().format(FORMATO_HORA);
        String mensaje;

        switch (resultado.getEstado()) {
            case APROBADO -> mensaje = "APROBADO | " + pedido.getId()
                    + " | " + obtenerDetalleProductos(pedido);
            case RECHAZADO -> mensaje = "RECHAZADO | " + pedido.getId()
                    + " | " + resultado.getMotivo();
            case ERROR -> mensaje = "ERROR | " + pedido.getId()
                    + " | " + resultado.getMotivo();
            default -> mensaje = pedido.getId() + " | Estado desconocido.";
        }

        System.out.println("[" + tiempo + "] [" + nombreTrabajador + "] " + mensaje);
    }

    private String obtenerDetalleProductos(Pedido pedido) {
        StringBuilder detalle = new StringBuilder();
        for (ItemPedido item : pedido.getItems()) {
            if (detalle.length() > 0) detalle.append(", ");
            detalle.append(item.getCodigoProducto())
                    .append(": -")
                    .append(item.getCantidad())
                    .append(" unidades");
        }
        return detalle.toString();
    }

    private void log(String mensaje) {
        String tiempo = LocalTime.now().format(FORMATO_HORA);
        System.out.println("[" + tiempo + "] [" + nombreTrabajador + "] " + mensaje);
    }
}
