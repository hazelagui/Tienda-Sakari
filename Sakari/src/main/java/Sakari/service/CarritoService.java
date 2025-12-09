package Sakari.service;

import Sakari.domain.*;
import Sakari.repository.CarritoRepository;
import Sakari.repository.ItemCarritoRepository;
import Sakari.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final ItemCarritoRepository itemCarritoRepository;
    private final ProductoService productoService;
    private final UsuarioRepository usuarioRepository;

    /**
     * Obtiene o crea un carrito para el usuario
     */
    public Carrito obtenerOCrearCarrito(Usuario usuario) {
        return carritoRepository.findByUsuario(usuario)
                .orElseGet(() -> {
                    log.info("Creando nuevo carrito para usuario: {}", usuario.getEmail());
                    Carrito nuevoCarrito = new Carrito();
                    nuevoCarrito.setUsuario(usuario);
                    return carritoRepository.save(nuevoCarrito);
                });
    }

    /**
     * Obtiene el carrito por ID de usuario, creándolo si no existe
     */
    public Carrito obtenerCarritoPorUsuarioId(Long usuarioId) {
        Optional<Carrito> carritoOpt = carritoRepository.findByUsuarioId(usuarioId);
        
        if (carritoOpt.isPresent()) {
            return carritoOpt.get();
        }
        
        // Si no existe, crear uno nuevo
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        log.info("Carrito no encontrado, creando uno nuevo para usuario: {}", usuario.getEmail());
        Carrito nuevoCarrito = new Carrito();
        nuevoCarrito.setUsuario(usuario);
        return carritoRepository.save(nuevoCarrito);
    }

    public Carrito agregarProducto(Long usuarioId, Long productoId, Integer cantidad, String personalizacion) {
        // Obtener o crear carrito
        Carrito carrito = obtenerCarritoPorUsuarioId(usuarioId);
        
        Producto producto = productoService.buscarPorId(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        
        // Verificar que el producto esté activo
        if (!producto.getActivo()) {
            throw new RuntimeException("El producto no está disponible");
        }
        
        // Verificar stock
        if (!producto.tieneStockSuficiente(cantidad)) {
            throw new RuntimeException("Stock insuficiente. Disponible: " + producto.getStock());
        }

        // Buscar si ya existe el producto en el carrito
        Optional<ItemCarrito> itemExistente = itemCarritoRepository
                .findByCarritoAndProducto(carrito, producto);

        if (itemExistente.isPresent()) {
            ItemCarrito item = itemExistente.get();
            int nuevaCantidad = item.getCantidad() + cantidad;
            
            // Verificar stock para la nueva cantidad total
            if (!producto.tieneStockSuficiente(nuevaCantidad)) {
                throw new RuntimeException("Stock insuficiente. Disponible: " + producto.getStock());
            }
            
            item.setCantidad(nuevaCantidad);
            // Actualizar personalización si se proporciona una nueva
            if (personalizacion != null && !personalizacion.isEmpty()) {
                item.setPersonalizacion(personalizacion);
            }
            itemCarritoRepository.save(item);
            log.info("Cantidad actualizada en carrito - Producto: {}, Nueva cantidad: {}", 
                    producto.getNombre(), nuevaCantidad);
        } else {
            ItemCarrito nuevoItem = new ItemCarrito();
            nuevoItem.setCarrito(carrito);
            nuevoItem.setProducto(producto);
            nuevoItem.setCantidad(cantidad);
            nuevoItem.setPrecioUnitario(producto.getPrecio());
            nuevoItem.setPersonalizacion(personalizacion);
            carrito.agregarItem(nuevoItem);
            itemCarritoRepository.save(nuevoItem);
            log.info("Producto agregado al carrito - Producto: {}, Cantidad: {}", 
                    producto.getNombre(), cantidad);
        }

        return carritoRepository.save(carrito);
    }

    public Carrito actualizarCantidad(Long itemId, Integer nuevaCantidad) {
        ItemCarrito item = itemCarritoRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado en el carrito"));
        
        if (nuevaCantidad <= 0) {
            // Si la cantidad es 0 o negativa, eliminar el item
            return eliminarItem(itemId);
        }
        
        if (!item.getProducto().tieneStockSuficiente(nuevaCantidad)) {
            throw new RuntimeException("Stock insuficiente. Disponible: " + item.getProducto().getStock());
        }
        
        item.setCantidad(nuevaCantidad);
        itemCarritoRepository.save(item);
        log.info("Cantidad actualizada - Item: {}, Nueva cantidad: {}", itemId, nuevaCantidad);
        
        return carritoRepository.save(item.getCarrito());
    }

    public Carrito eliminarItem(Long itemId) {
        ItemCarrito item = itemCarritoRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado en el carrito"));
        
        Carrito carrito = item.getCarrito();
        carrito.eliminarItem(item);
        itemCarritoRepository.delete(item);
        log.info("Item eliminado del carrito: {}", itemId);
        
        return carritoRepository.save(carrito);
    }

    public void limpiarCarrito(Long usuarioId) {
        Carrito carrito = obtenerCarritoPorUsuarioId(usuarioId);
        carrito.limpiarCarrito();
        carritoRepository.save(carrito);
        log.info("Carrito limpiado para usuario ID: {}", usuarioId);
    }

    public BigDecimal calcularTotal(Long usuarioId) {
        Carrito carrito = obtenerCarritoPorUsuarioId(usuarioId);
        return carrito.calcularTotal();
    }

    public int contarItems(Long usuarioId) {
        Carrito carrito = obtenerCarritoPorUsuarioId(usuarioId);
        return carrito.getCantidadTotalItems();
    }

    public boolean carritoEstaVacio(Long usuarioId) {
        Carrito carrito = obtenerCarritoPorUsuarioId(usuarioId);
        return carrito.getItems() == null || carrito.getItems().isEmpty();
    }

    // Métodos sobrecargados que aceptan Usuario directamente
    
    public Carrito agregarProducto(Usuario usuario, Long productoId, Integer cantidad, String personalizacion) {
        return agregarProducto(usuario.getId(), productoId, cantidad, personalizacion);
    }

    public Carrito actualizarCantidad(Usuario usuario, Long itemId, Integer nuevaCantidad) {
        return actualizarCantidad(itemId, nuevaCantidad);
    }

    public Carrito eliminarItem(Usuario usuario, Long itemId) {
        return eliminarItem(itemId);
    }

    public void limpiarCarrito(Usuario usuario) {
        limpiarCarrito(usuario.getId());
    }

    public BigDecimal calcularTotal(Carrito carrito) {
        if (carrito == null || carrito.getItems() == null) {
            return BigDecimal.ZERO;
        }
        return carrito.calcularTotal();
    }
}
