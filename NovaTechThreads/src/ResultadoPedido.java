public class ResultadoPedido {

    public enum Estado {
        APROBADO,
        RECHAZADO,
        ERROR
    }

    private final Estado estado;
    private final String motivo;

    public ResultadoPedido(Estado estado, String motivo) {
        this.estado = estado;
        this.motivo = motivo;
    }

    public Estado getEstado() { return estado; }
    public String getMotivo() { return motivo; }

    public static ResultadoPedido aprobado() {
        return new ResultadoPedido(
                Estado.APROBADO,
                "Pedido aprobado correctamente."
        );
    }

    public static ResultadoPedido rechazado(String motivo) {
        return new ResultadoPedido(Estado.RECHAZADO, motivo);
    }

    public static ResultadoPedido error(String motivo) {
        return new ResultadoPedido(Estado.ERROR, motivo);
    }
}
