import java.util.concurrent.atomic.AtomicInteger;

public class Estadisticas {

    private final AtomicInteger procesados = new AtomicInteger();
    private final AtomicInteger aprobados = new AtomicInteger();
    private final AtomicInteger rechazados = new AtomicInteger();
    private final AtomicInteger errores = new AtomicInteger();
    private final AtomicInteger trabajadoresActivos = new AtomicInteger();

    public void trabajadorInicio() {
        trabajadoresActivos.incrementAndGet();
    }

    public void trabajadorFin() {
        trabajadoresActivos.decrementAndGet();
    }

    public void registrarResultado(ResultadoPedido resultado) {
        procesados.incrementAndGet();

        switch (resultado.getEstado()) {
            case APROBADO -> aprobados.incrementAndGet();
            case RECHAZADO -> rechazados.incrementAndGet();
            case ERROR -> errores.incrementAndGet();
        }
    }

    public int getProcesados() { return procesados.get(); }
    public int getAprobados() { return aprobados.get(); }
    public int getRechazados() { return rechazados.get(); }
    public int getErrores() { return errores.get(); }
    public int getTrabajadoresActivos() { return trabajadoresActivos.get(); }
}
