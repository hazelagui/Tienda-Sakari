package Sakari.service;

import Sakari.domain.Producto;
import Sakari.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductoService {

    private final ProductoRepository productoRepository;

    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    public List<Producto> listarActivos() {
        return productoRepository.findByActivoTrue();
    }

    public List<Producto> listarDisponibles() {
        return productoRepository.findProductosDisponibles();
    }

    public List<Producto> listarPorCategoria(Long categoriaId) {
        return productoRepository.findByCategoriaIdAndActivoTrue(categoriaId);
    }

    public List<Producto> listarPersonalizables() {
        return productoRepository.findByPersonalizableTrue();
    }

    public Optional<Producto> buscarPorId(Long id) {
        return productoRepository.findById(id);
    }

    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.buscarPorNombre(nombre);
    }

    public Producto crearProducto(Producto producto) {
        Producto guardado = productoRepository.save(producto);
        log.info("Producto creado: {}", guardado.getNombre());
        return guardado;
    }

    public Producto actualizarProducto(Producto producto) {
        Producto actualizado = productoRepository.save(producto);
        log.info("Producto actualizado: {}", actualizado.getNombre());
        return actualizado;
    }

    public void eliminarProducto(Long id) {
        productoRepository.deleteById(id);
        log.info("Producto eliminado: {}", id);
    }

    public void reducirStock(Long productoId, int cantidad) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        producto.reducirStock(cantidad);
        productoRepository.save(producto);
        log.info("Stock reducido para producto {}: -{}", producto.getNombre(), cantidad);
    }

    public void aumentarStock(Long productoId, int cantidad) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        producto.aumentarStock(cantidad);
        productoRepository.save(producto);
        log.info("Stock aumentado para producto {}: +{}", producto.getNombre(), cantidad);
    }
}
