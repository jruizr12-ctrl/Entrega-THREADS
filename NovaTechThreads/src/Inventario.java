import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class Inventario {

    private final Map<String, Producto> productos = new LinkedHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public void agregarProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser null.");
        }

        lock.lock();
        try {
            if (productos.containsKey(producto.getCodigo())) {
                throw new IllegalArgumentException(
                        "Código de producto duplicado: " + producto.getCodigo()
                );
            }
            productos.put(producto.getCodigo(), producto);
        } finally {
            lock.unlock();
        }
    }

    public ResultadoPedido procesarPedido(Pedido pedido) {
        if (pedido == null) {
            return ResultadoPedido.error("Pedido nulo.");
        }

        if (!pedido.esValidoEstructuralmente()) {
            return ResultadoPedido.error(
                    "Pedido mal formado: código, cliente, producto o cantidad inválida."
            );
        }

        lock.lock();
        try {
            // Fase 1: validar TODOS los ítems sin modificar stock.
            for (ItemPedido item : pedido.getItems()) {
                Producto producto = productos.get(item.getCodigoProducto());

                if (producto == null) {
                    return ResultadoPedido.rechazado(
                            "Producto inexistente: " + item.getCodigoProducto()
                    );
                }

                if (item.getCantidad() > producto.getExistencia()) {
                    return ResultadoPedido.rechazado(
                            "Stock insuficiente " + item.getCodigoProducto()
                                    + " | Disponible: " + producto.getExistencia()
                                    + " | Solicitado: " + item.getCantidad()
                    );
                }
            }

            // Fase 2: descontar solamente después de validar todo.
            for (ItemPedido item : pedido.getItems()) {
                productos.get(item.getCodigoProducto()).descontar(item.getCantidad());
            }

            return ResultadoPedido.aprobado();
        } finally {
            lock.unlock();
        }
    }

    public Map<String, Integer> obtenerExistencias() {
        lock.lock();
        try {
            Map<String, Integer> copia = new LinkedHashMap<>();
            for (Producto producto : productos.values()) {
                copia.put(producto.getCodigo(), producto.getExistencia());
            }
            return copia;
        } finally {
            lock.unlock();
        }
    }

    public void mostrarInventario() {
        for (Map.Entry<String, Integer> entry : obtenerExistencias().entrySet()) {
            System.out.println(entry.getKey() + " | Existencia final: " + entry.getValue());
        }
    }
}
