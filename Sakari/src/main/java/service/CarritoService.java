package Sakari.service;

import Sakari.domain.*;
import Sakari.repository.CarritoRepository;
import Sakari.repository.ItemCarritoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final ItemCarritoRepository itemCarritoRepository;
    private final ProductoService productoService;

    public Carrito obtenerOCrearCarrito(Usuario usuario) {
        return carritoRepository.findByUsuario(usuario)
                .orElseGet(() -> {
                    Carrito nuevoCarrito = new Carrito();
                    nuevoCarrito.setUsuario(usuario);
                    return carritoRepository.save(nuevoCarrito);
                });
    }

    public Carrito obtenerCarritoPorUsuarioId(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));
    }

    public Carrito agregarProducto(Long usuarioId, Long productoId, Integer cantidad, String personalizacion) {
        Carrito carrito = carritoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));
        
        Producto producto = productoService.buscarPorId(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        
        if (!producto.tieneStockSuficiente(cantidad)) {
            throw new RuntimeException("Stock insuficiente para el producto");
        }

        Optional<ItemCarrito> itemExistente = itemCarritoRepository
                .findByCarritoAndProducto(carrito, producto);

        if (itemExistente.isPresent()) {
            ItemCarrito item = itemExistente.get();
            item.setCantidad(item.getCantidad() + cantidad);
            itemCarritoRepository.save(item);
        } else {
            ItemCarrito nuevoItem = new ItemCarrito();
            nuevoItem.setCarrito(carrito);
            nuevoItem.setProducto(producto);
            nuevoItem.setCantidad(cantidad);
            nuevoItem.setPrecioUnitario(producto.getPrecio());
            nuevoItem.setPersonalizacion(personalizacion);
            carrito.agregarItem(nuevoItem);
            itemCarritoRepository.save(nuevoItem);
        }

        return carritoRepository.save(carrito);
    }

    public Carrito actualizarCantidad(Long itemId, Integer nuevaCantidad) {
        ItemCarrito item = itemCarritoRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));
        
        if (!item.getProducto().tieneStockSuficiente(nuevaCantidad)) {
            throw new RuntimeException("Stock insuficiente");
        }
        
        item.setCantidad(nuevaCantidad);
        itemCarritoRepository.save(item);
        
        return carritoRepository.save(item.getCarrito());
    }

    public Carrito eliminarItem(Long itemId) {
        ItemCarrito item = itemCarritoRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));
        
        Carrito carrito = item.getCarrito();
        carrito.eliminarItem(item);
        itemCarritoRepository.delete(item);
        
        return carritoRepository.save(carrito);
    }

    public void limpiarCarrito(Long usuarioId) {
        Carrito carrito = obtenerCarritoPorUsuarioId(usuarioId);
        carrito.limpiarCarrito();
        carritoRepository.save(carrito);
    }

    public BigDecimal calcularTotal(Long usuarioId) {
        Carrito carrito = obtenerCarritoPorUsuarioId(usuarioId);
        return carrito.calcularTotal();
    }
}
