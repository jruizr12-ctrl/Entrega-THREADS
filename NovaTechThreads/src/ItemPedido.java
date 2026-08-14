public class ItemPedido {

    private final String codigoProducto;
    private final int cantidad;

    public ItemPedido(String codigoProducto, int cantidad) {
        this.codigoProducto = codigoProducto;
        this.cantidad = cantidad;
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public boolean esValido() {
        return codigoProducto != null
                && !codigoProducto.trim().isEmpty()
                && cantidad > 0;
    }

    @Override
    public String toString() {
        return codigoProducto + " x " + cantidad;
    }
}
