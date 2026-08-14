# NovaTech — Procesamiento Concurrente de Pedidos

## 1. Lenguaje y versión

- Lenguaje: Java
- Versión: JDK 17 o superior
- Entorno recomendado: Visual Studio Code
- Extensión recomendada: Extension Pack for Java

## 2. Dependencias

El proyecto utiliza únicamente las clases incluidas en el JDK estándar de Java.

No requiere librerías externas ni frameworks.

Principales clases utilizadas:

- `Thread`
- `Runnable`
- `BlockingQueue`
- `LinkedBlockingQueue`
- `ReentrantLock`
- `AtomicInteger`
- `AtomicBoolean`

## 3. Comandos de instalación y ejecución

### Compilación — Windows PowerShell

Desde la carpeta raíz del proyecto:

```powershell
javac -d out src\*.java
```

### Ejecución

```powershell
java -cp out Main
```

### Linux/macOS

```bash
javac -d out src/*.java
```

```bash
java -cp out Main
```

No es necesario instalar paquetes adicionales.

## 4. Estructura de hilos

La aplicación utiliza:

- **1 hilo principal (`main`)**
  - Inicializa los recursos.
  - Crea e inicia los trabajadores y el monitor.
  - Espera mediante `join()`.
  - Coordina la finalización.

- **3 hilos trabajadores**
  - `WORKER-1`
  - `WORKER-2`
  - `WORKER-3`

Cada trabajador toma pedidos de la cola compartida, simula su procesamiento y actualiza el inventario cuando corresponde.

- **1 hilo monitor**
  - `MONITOR`
  - Supervisa periódicamente el estado de la cola y las estadísticas.
  - Finaliza mediante una señal de detención.

## 5. Decisiones de sincronización

### Cola de pedidos

Se utiliza:

```java
BlockingQueue<Pedido>
```

con implementación:

```java
LinkedBlockingQueue<Pedido>
```

La cola es thread-safe y permite que los trabajadores extraigan pedidos concurrentemente sin duplicarlos.

### Inventario

El inventario es un recurso compartido y se protege mediante:

```java
ReentrantLock
```

La sección crítica incluye conjuntamente:

1. Verificación de existencia.
2. Descuento del inventario.

Esto evita condiciones de carrera y sobreventa.

La simulación de procesamiento de cada pedido se realiza **fuera del lock**, evitando mantener bloqueado el inventario durante `Thread.sleep()`.

### Estadísticas

Los contadores compartidos se implementan mediante:

```java
AtomicInteger
```

para garantizar actualizaciones seguras desde múltiples trabajadores.

### Monitor

El estado de finalización se controla mediante:

```java
AtomicBoolean
```

y la coordinación de espera/despertar mediante:

```java
wait()
notifyAll()
```

### Finalización

El hilo principal utiliza:

```java
join()
```

para esperar a que terminen los trabajadores y posteriormente al monitor.

De esta manera, todos los hilos finalizan de forma coordinada antes de terminar el programa.