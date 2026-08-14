import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pedido {

    private final String id;
    private final String cliente;
    private final List<ItemPedido> items;

    public Pedido(String id, String cliente, ItemPedido item) {
        this(id, cliente, Collections.singletonList(item));
    }

    public Pedido(String id, String cliente, List<ItemPedido> items) {
        this.id = id;
        this.cliente = cliente;
        this.items = items == null
                ? new ArrayList<>()
                : new ArrayList<>(items);
    }

    public String getId() { return id; }
    public String getCliente() { return cliente; }
    public List<ItemPedido> getItems() {
        return Collections.unmodifiableList(items);
    }

    public boolean esValidoEstructuralmente() {
        if (id == null || id.isBlank()) return false;
        if (cliente == null || cliente.isBlank()) return false;
        if (items.isEmpty()) return false;

        for (ItemPedido item : items) {
            if (item == null || !item.esValido()) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return id + " | Cliente: " + cliente + " | Items: " + items;
    }
}
