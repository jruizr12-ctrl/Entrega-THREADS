public class Producto {

    private final String codigo;
    private final String nombre;
    private int existencia;

    public Producto(String codigo, String nombre, int existencia) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.existencia = existencia;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public int getExistencia() {
        return existencia;
    }

    public void descontar(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero."
            );
        }

        if (cantidad > existencia) {
            throw new IllegalArgumentException(
                    "No hay existencia suficiente."
            );
        }

        existencia -= cantidad;
    }

    @Override
    public String toString() {
        return codigo + " | " + nombre
                + " | Existencia: " + existencia;
    }
}
