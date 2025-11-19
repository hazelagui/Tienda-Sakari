package Sakari.service;

import Sakari.domain.*;
import Sakari.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final CarritoService carritoService;
    private final ProductoService productoService;
    private final UsuarioService usuarioService;

    public Pedido crearPedidoDesdeCarrito(Long usuarioId, String direccionEnvio, String telefonoContacto) {
        Usuario usuario = usuarioService.buscarPorId(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Carrito carrito = carritoService.obtenerCarritoPorUsuarioId(usuarioId);
        
        if (carrito.getItems().isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setDireccionEnvio(direccionEnvio);
        pedido.setTelefonoContacto(telefonoContacto);
        pedido.setEstado(Pedido.EstadoPedido.PENDIENTE);

        BigDecimal total = BigDecimal.ZERO;

        for (ItemCarrito itemCarrito : carrito.getItems()) {
            Producto producto = itemCarrito.getProducto();
            
            if (!producto.tieneStockSuficiente(itemCarrito.getCantidad())) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }

            ItemPedido itemPedido = new ItemPedido();
            itemPedido.setPedido(pedido);
            itemPedido.setProducto(producto);
            itemPedido.setCantidad(itemCarrito.getCantidad());
            itemPedido.setPrecioUnitario(itemCarrito.getPrecioUnitario());
            itemPedido.setPersonalizacion(itemCarrito.getPersonalizacion());
            
            pedido.getItems().add(itemPedido);
            
            productoService.reducirStock(producto.getId(), itemCarrito.getCantidad());
            
            total = total.add(itemPedido.getSubtotal());
        }

        pedido.setTotal(total);
        Pedido pedidoGuardado = pedidoRepository.save(pedido);
        
        carritoService.limpiarCarrito(usuarioId);
        
        return pedidoGuardado;
    }

    public Optional<Pedido> buscarPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    public Optional<Pedido> buscarPorNumeroPedido(String numeroPedido) {
        return pedidoRepository.findByNumeroPedido(numeroPedido);
    }

    public List<Pedido> listarPedidosUsuario(Long usuarioId) {
        return pedidoRepository.findByUsuarioIdOrderByFechaPedidoDesc(usuarioId);
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    public Pedido actualizarEstado(Long pedidoId, Pedido.EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        
        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }

    public void cancelarPedido(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        
        if (pedido.getEstado() == Pedido.EstadoPedido.ENTREGADO) {
            throw new RuntimeException("No se puede cancelar un pedido ya entregado");
        }
        
        for (ItemPedido item : pedido.getItems()) {
            Producto producto = item.getProducto();
            producto.setStock(producto.getStock() + item.getCantidad());
            productoService.actualizarProducto(producto);
        }
        
        pedido.setEstado(Pedido.EstadoPedido.CANCELADO);
        pedidoRepository.save(pedido);
    }
}
