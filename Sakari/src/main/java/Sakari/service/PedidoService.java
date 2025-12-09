package Sakari.service;

import Sakari.domain.*;
import Sakari.repository.PedidoRepository;
import Sakari.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final CarritoService carritoService;
    private final ProductoService productoService;
    private final UsuarioRepository usuarioRepository;

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAllByOrderByFechaPedidoDesc();
    }

    public List<Pedido> listarPedidosUsuario(Long usuarioId) {
        return pedidoRepository.findByUsuarioIdOrderByFechaPedidoDesc(usuarioId);
    }

    public Optional<Pedido> buscarPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    public Optional<Pedido> buscarPorNumeroPedido(String numeroPedido) {
        return pedidoRepository.findByNumeroPedido(numeroPedido);
    }

    public List<Pedido> listarPorEstado(Pedido.EstadoPedido estado) {
        return pedidoRepository.findByEstado(estado);
    }

    public Pedido crearPedidoDesdeCarrito(Long usuarioId, String direccionEnvio, String telefonoContacto) {
        // Obtener el carrito del usuario
        Carrito carrito = carritoService.obtenerCarritoPorUsuarioId(usuarioId);
        
        if (carrito.getItems() == null || carrito.getItems().isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        // Obtener usuario
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Crear el pedido
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setDireccionEnvio(direccionEnvio);
        pedido.setTelefonoContacto(telefonoContacto);
        pedido.setEstado(Pedido.EstadoPedido.PENDIENTE);
        pedido.setFechaPedido(LocalDateTime.now());

        // Convertir items del carrito a items del pedido
        BigDecimal total = BigDecimal.ZERO;
        
        for (ItemCarrito itemCarrito : carrito.getItems()) {
            Producto producto = itemCarrito.getProducto();
            
            // Verificar stock
            if (!producto.tieneStockSuficiente(itemCarrito.getCantidad())) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }

            ItemPedido itemPedido = new ItemPedido();
            itemPedido.setProducto(producto);
            itemPedido.setCantidad(itemCarrito.getCantidad());
            itemPedido.setPrecioUnitario(itemCarrito.getPrecioUnitario());
            itemPedido.setPersonalizacion(itemCarrito.getPersonalizacion());
            
            pedido.agregarItem(itemPedido);
            total = total.add(itemPedido.getSubtotal());

            // Reducir stock
            productoService.reducirStock(producto.getId(), itemCarrito.getCantidad());
        }

        pedido.setTotal(total);

        // Guardar el pedido
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        // Limpiar el carrito
        carritoService.limpiarCarrito(usuarioId);

        log.info("Pedido creado: {} - Total: {} - Usuario: {}", 
                pedidoGuardado.getNumeroPedido(), total, usuario.getEmail());

        return pedidoGuardado;
    }

    public Pedido actualizarEstado(Long pedidoId, Pedido.EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        
        pedido.setEstado(nuevoEstado);
        pedido.setFechaActualizacion(LocalDateTime.now());
        
        if (nuevoEstado == Pedido.EstadoPedido.ENTREGADO) {
            pedido.setFechaEntrega(LocalDateTime.now());
        }

        Pedido actualizado = pedidoRepository.save(pedido);
        log.info("Pedido {} actualizado a estado: {}", pedido.getNumeroPedido(), nuevoEstado);
        
        return actualizado;
    }

    public void cancelarPedido(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        
        if (!pedido.puedeSerCancelado()) {
            throw new RuntimeException("Este pedido no puede ser cancelado");
        }

        // Restaurar el stock de los productos
        for (ItemPedido item : pedido.getItems()) {
            productoService.aumentarStock(item.getProducto().getId(), item.getCantidad());
        }

        pedido.setEstado(Pedido.EstadoPedido.CANCELADO);
        pedido.setFechaActualizacion(LocalDateTime.now());
        pedidoRepository.save(pedido);

        log.info("Pedido cancelado: {}", pedido.getNumeroPedido());
    }

    public long contarPedidosPendientes() {
        return pedidoRepository.countByEstado(Pedido.EstadoPedido.PENDIENTE);
    }
}
