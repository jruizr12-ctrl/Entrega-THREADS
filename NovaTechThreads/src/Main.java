import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {

    private static final int TRABAJADORES_POR_DEFECTO = 3;

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("--test")) {
            ejecutarPrueba(args.length > 1 ? args[1] : "all");
            return;
        }

        int trabajadores = leerTrabajadores(args);
        ejecutarEscenario(
                "NOVATECHT PRUEBA DE PROCESAMIENTO DE PEDIDOS",
                crearInventarioDemo(),
                crearPedidosDemo(),
                trabajadores
        );
    }

    private static int leerTrabajadores(String[] args) {
        if (args.length == 0) return TRABAJADORES_POR_DEFECTO;

        if (args.length >= 2 && args[0].equalsIgnoreCase("--workers")) {
            return convertirTrabajadores(args[1]);
        }

        if (args.length >= 2) {
            return convertirTrabajadores(args[1]);
        }

        return TRABAJADORES_POR_DEFECTO;
    }

    private static int convertirTrabajadores(String texto) {
        try {
            int valor = Integer.parseInt(texto);
            if (valor < 1) throw new NumberFormatException();
            return valor;
        } catch (NumberFormatException e) {
            System.out.println("Cantidad de trabajadores inválida. Se usarán 3.");
            return TRABAJADORES_POR_DEFECTO;
        }
    }

    private static void ejecutarPrueba(String nombre) {
        if (nombre.equalsIgnoreCase("all")) {
            ejecutarPrueba("CP-01");
            ejecutarPrueba("CP-02");
            ejecutarPrueba("CP-03");
            ejecutarPrueba("CP-04");
            ejecutarPrueba("CP-05");
            return;
        }

        switch (nombre.toUpperCase()) {
            case "CP-01" -> ejecutarEscenario(
                    "CP-01 | FLUJO NORMAL",
                    inventario("P001", "Teclado mecánico", 20),
                    pedidos(
                            new Pedido("CP01-001", "Ana", new ItemPedido("P001", 2)),
                            new Pedido("CP01-002", "Luis", new ItemPedido("P001", 3)),
                            new Pedido("CP01-003", "María", new ItemPedido("P001", 4)),
                            new Pedido("CP01-004", "Carlos", new ItemPedido("P001", 1)),
                            new Pedido("CP01-005", "Sofía", new ItemPedido("P001", 2))
                    ), 3
            );
            case "CP-02" -> ejecutarEscenario(
                    "CP-02 | CONTENCIÓN",
                    inventario("P005", "Monitor de 24 pulgadas", 5),
                    pedidos(
                            new Pedido("CP02-001", "Cliente A", new ItemPedido("P005", 4)),
                            new Pedido("CP02-002", "Cliente B", new ItemPedido("P005", 4)),
                            new Pedido("CP02-003", "Cliente C", new ItemPedido("P005", 1))
                    ), 3
            );
            case "CP-03" -> ejecutarEscenario(
                    "CP-03 | STOCK INSUFICIENTE",
                    inventario("P003", "Audífonos USB", 10),
                    pedidos(
                            new Pedido("CP03-001", "Cliente A", new ItemPedido("P003", 20)),
                            new Pedido("CP03-002", "Cliente B", new ItemPedido("P003", 3))
                    ), 3
            );
            case "CP-04" -> ejecutarEscenario(
                    "CP-04 | PEDIDO INVÁLIDO",
                    inventario("P001", "Teclado mecánico", 10),
                    pedidos(
                            new Pedido("CP04-001", "Cliente válido", new ItemPedido("P001", 2)),
                            new Pedido("CP04-002", "Cliente cero", new ItemPedido("P001", 0)),
                            new Pedido("CP04-003", "Cliente sin código", new ItemPedido("", 2)),
                            new Pedido("CP04-004", "Cliente posterior", new ItemPedido("P001", 2))
                    ), 3
            );
            case "CP-05" -> ejecutarEscenario(
                    "CP-05 | CIERRE LIMPIO",
                    crearInventarioDemo(),
                    pedidos(
                            new Pedido("CP05-001", "Ana", new ItemPedido("P001", 1)),
                            new Pedido("CP05-002", "Luis", new ItemPedido("P002", 1)),
                            new Pedido("CP05-003", "María", new ItemPedido("P003", 1))
                    ), 3
            );
            default -> System.out.println(
                    "Prueba desconocida. Use CP-01, CP-02, CP-03, CP-04, CP-05 o all."
            );
        }
    }

    private static void ejecutarEscenario(
            String nombre,
            Inventario inventario,
            List<Pedido> pedidos,
            int cantidadTrabajadores
    ) {
        System.out.println("\n==============================================================");
        System.out.println(" " + nombre);
        System.out.println("==============================================================");
        System.out.println("Pedidos cargados: " + pedidos.size());
        validarIdsUnicos(pedidos);

        BlockingQueue<Pedido> cola = new LinkedBlockingQueue<>(pedidos);
        Estadisticas estadisticas = new Estadisticas();
        Monitor monitor = new Monitor(cola, estadisticas, cantidadTrabajadores);

        List<Thread> trabajadores = new ArrayList<>();
        for (int i = 1; i <= cantidadTrabajadores; i++) {
            Thread hilo = new Thread(
                    new ProcesadorPedidos(
                            cola, inventario, estadisticas, "WORKER-" + i
                    ),
                    "WORKER-" + i
            );
            trabajadores.add(hilo);
        }

        Thread hiloMonitor = new Thread(monitor, "MONITOR");
        long inicio = System.currentTimeMillis();

        hiloMonitor.start();
        for (Thread trabajador : trabajadores) trabajador.start();

        for (Thread trabajador : trabajadores) {
            try {
                trabajador.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[MAIN] Interrupción durante join de " + trabajador.getName());
            }
        }

        monitor.detener();

        try {
            hiloMonitor.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[MAIN] Interrupción durante join del monitor.");
        }

        double segundos = (System.currentTimeMillis() - inicio) / 1000.0;
        mostrarResumen(inventario, estadisticas, cola, segundos, trabajadores, hiloMonitor);
    }

    private static void mostrarResumen(
            Inventario inventario,
            Estadisticas estadisticas,
            BlockingQueue<Pedido> cola,
            double segundos,
            List<Thread> trabajadores,
            Thread monitor
    ) {
        System.out.println("\n---------------------- RESUMEN FINAL -------------------------");
        System.out.println("Procesados: " + estadisticas.getProcesados());
        System.out.println("Aprobados: " + estadisticas.getAprobados());
        System.out.println("Rechazados: " + estadisticas.getRechazados());
        System.out.println("Errores: " + estadisticas.getErrores());
        System.out.println("Pendientes: " + cola.size());
        System.out.println("Tiempo total: " + String.format("%.2f", segundos) + " s");

        System.out.println("\nInventario final:");
        inventario.mostrarInventario();

        int finalizados = 0;
        for (Thread trabajador : trabajadores) {
            boolean terminado = trabajador.getState() == Thread.State.TERMINATED;
            System.out.println(trabajador.getName() + " -> " + (terminado ? "FINALIZADO" : "ACTIVO"));
            if (terminado) finalizados++;
        }

        boolean monitorFinalizado = monitor.getState() == Thread.State.TERMINATED;
        System.out.println("MONITOR -> " + (monitorFinalizado ? "FINALIZADO" : "ACTIVO"));
        if (monitorFinalizado) finalizados++;
        System.out.println("Hilos finalizados correctamente: " + finalizados + "/" + (trabajadores.size() + 1));
    }

    private static Inventario crearInventarioDemo() {
        Inventario inventario = new Inventario();
        inventario.agregarProducto(new Producto("P001", "Teclado mecánico", 12));
        inventario.agregarProducto(new Producto("P002", "Mouse inalámbrico", 18));
        inventario.agregarProducto(new Producto("P003", "Audífonos USB", 10));
        inventario.agregarProducto(new Producto("P004", "Cámara web", 8));
        inventario.agregarProducto(new Producto("P005", "Monitor de 24 pulgadas", 6));
        return inventario;
    }

    private static Inventario inventario(String codigo, String nombre, int stock) {
        Inventario inventario = new Inventario();
        inventario.agregarProducto(new Producto(codigo, nombre, stock));
        return inventario;
    }

    private static List<Pedido> pedidos(Pedido... pedidos) {
        return new ArrayList<>(List.of(pedidos));
    }

    private static List<Pedido> crearPedidosDemo() {
        List<Pedido> pedidos = new ArrayList<>();
        pedidos.add(new Pedido("ORD-001", "Carlos López", new ItemPedido("P001", 2)));
        pedidos.add(new Pedido("ORD-002", "Ana Martínez", new ItemPedido("P002", 3)));
        pedidos.add(new Pedido("ORD-003", "Mario Pérez", new ItemPedido("P003", 2)));
        pedidos.add(new Pedido("ORD-004", "Laura García", new ItemPedido("P004", 2)));
        pedidos.add(new Pedido("ORD-005", "Pedro Ramírez", new ItemPedido("P005", 2)));
        pedidos.add(new Pedido("ORD-006", "Sofía Hernández", new ItemPedido("P001", 3)));
        pedidos.add(new Pedido("ORD-007", "José Castillo", new ItemPedido("P002", 4)));
        pedidos.add(new Pedido("ORD-008", "Daniela Morales", new ItemPedido("P003", 3)));
        pedidos.add(new Pedido("ORD-009", "Roberto Díaz", new ItemPedido("P004", 3)));
        pedidos.add(new Pedido("ORD-010", "Gabriela Flores", new ItemPedido("P005", 2)));
        pedidos.add(new Pedido("ORD-011", "Luis Gómez", new ItemPedido("P001", 4)));
        pedidos.add(new Pedido("ORD-012", "Valeria Torres", new ItemPedido("P002", 5)));
        pedidos.add(new Pedido("ORD-013", "Fernando Ruiz", new ItemPedido("P003", 4)));
        pedidos.add(new Pedido("ORD-014", "María Sánchez", new ItemPedido("P004", 3)));
        pedidos.add(new Pedido("ORD-015", "Andrés Vargas", new ItemPedido("P005", 3)));
        pedidos.add(new Pedido("ORD-016", "Patricia Cruz", new ItemPedido("P001", 5)));
        pedidos.add(new Pedido("ORD-017", "Miguel Herrera", new ItemPedido("P002", 6)));
        pedidos.add(new Pedido("ORD-018", "Carolina Mendoza", new ItemPedido("P003", 5)));
        pedidos.add(new Pedido("ORD-019", "Ricardo Ortiz", new ItemPedido("P004", 4)));
        pedidos.add(new Pedido("ORD-020", "Elena Navarro", new ItemPedido("P005", 4)));
        pedidos.add(new Pedido("ORD-021", "Cliente Inválido 1", new ItemPedido("P001", 0)));
        pedidos.add(new Pedido("ORD-022", "Cliente Inválido 2", new ItemPedido("P999", 2)));
        pedidos.add(new Pedido("ORD-023", "Cliente Inválido 3", new ItemPedido("P002", -5)));
        pedidos.add(new Pedido("ORD-024", "Cliente Stock Insuficiente", new ItemPedido("P005", 100)));
        pedidos.add(new Pedido("ORD-025", "Cliente Contención", new ItemPedido("P005", 5)));
        return pedidos;
    }

    private static void validarIdsUnicos(List<Pedido> pedidos) {
        Set<String> ids = new HashSet<>();
        for (Pedido pedido : pedidos) {
            if (!ids.add(pedido.getId())) {
                throw new IllegalStateException("ID de pedido duplicado: " + pedido.getId());
            }
        }
    }
}
