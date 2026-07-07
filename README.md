# ✈️ SkyQ - Portal B2C Móvil (Client Version)

Este proyecto constituye el portal B2C de venta de boletos y selección de asientos para los pasajeros de **SkyQ**, optimizado con un diseño responsive emulado para dispositivos móviles.

La aplicación ha sido refactorizada bajo un estricto patrón de arquitectura en capas (**MVC / N-Capas**), aislando completamente el acceso a datos en la base de datos de los componentes de presentación (UI).

---

## 🏗️ Arquitectura y Componentes del Proyecto

El proyecto está organizado en los siguientes paquetes bajo `skyq.cliente`:

```
skyq.cliente/
├── db/
│   └── ConexionBD.java         # Proveedor de conexión JDBC a SQL Server
├── model/
│   ├── PasajeroDTO.java        # DTO para transferir datos de pasajeros
│   └── VueloDTO.java           # DTO para transferir datos de vuelos programados
├── service/
│   └── ClienteService.java     # Capa de Servicio: Lógica transaccional (ACID) y consultas JDBC
├── view/
│   ├── PanelReserva.java       # Interfaz gráfica de reserva y mapa de asientos dinámico
│   └── VentanaCliente.java     # Frame principal móvil (450x800), cartelera de vuelos (acordeón)
└── MainCliente.java            # Punto de entrada de la aplicación
```

### 📋 Características Principales

1. **Separación de Responsabilidades (MVC):** 
   - El paquete `skyq.cliente.view` tiene **cero** imports de `java.sql.*`.
   - Toda interacción con la base de datos se delega a `ClienteService` utilizando DTOs (`VueloDTO`, `PasajeroDTO`).

2. **Diseño Mobile-First & Full Dark Mode:**
   - La interfaz está fijada en `450x800` píxeles, con la propiedad `setResizable(false)` y `setUndecorated(true)` para emular la apariencia nativa de un smartphone.
   - Colores oscuros premium implementados consistentemente: fondo principal `Color(13,17,23)` y tarjetas de componentes `Color(22,27,34)`.

3. **Navegación Fluida sin Diálogos Emergentes:**
   - Toda la navegación interna entre la cartelera, el formulario de pasajero y el mapa de asientos se realiza utilizando `CardLayout` en el mismo frame para evitar el uso de diálogos externos (`JDialog`).

4. **Tarjetas de Vuelo Expansibles (Acordeón):**
   - Los vuelos se visualizan mediante tarjetas personalizadas en un flujo vertical. Al hacer clic en detalles, la tarjeta seleccionada se expande y colapsa automáticamente a las demás en pantalla.

5. **Lógica de Asientos Agotados & Bloqueo de Clase:**
   - El ComboBox de clase de cabina valida en tiempo real la capacidad del avión contra los asientos ocupados en la BD. Si una sección está llena, se muestra con la etiqueta `AGOTADO` y tachada en color rojo en la lista. Se incluye un *reentrancy guard* para bloquear su selección física.

6. **Integridad Transaccional (ACID):**
   - Las reservas grupales se procesan de forma atómica en `ClienteService`. Si falla la inserción de algún pasajero, la transacción completa realiza un `rollback()`. En caso de éxito, se confirma mediante `commit()` y se devuelve el PNR alfanumérico único.

---


