package Sakari.service;

import Sakari.domain.Categoria;
import Sakari.domain.Producto;
import Sakari.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;

    public Producto crearProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    public Optional<Producto> buscarPorId(Long id) {
        return productoRepository.findById(id);
    }

    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    public List<Producto> listarActivos() {
        return productoRepository.findByActivoTrue();
    }

    public List<Producto> listarPorCategoria(Categoria categoria) {
        return productoRepository.findByCategoriaAndActivoTrue(categoria);
    }

    public List<Producto> listarPersonalizables() {
        return productoRepository.findByPersonalizableTrue();
    }

    public List<Producto> listarDisponibles() {
        return productoRepository.findProductosDisponibles();
    }

    public List<Producto> buscarPorPalabraClave(String keyword) {
        return productoRepository.buscarPorPalabraClave(keyword);
    }

    public Producto actualizarProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    public void eliminarProducto(Long id) {
        productoRepository.deleteById(id);
    }

    public boolean verificarStock(Long productoId, int cantidad) {
        Optional<Producto> producto = productoRepository.findById(productoId);
        return producto.isPresent() && producto.get().tieneStockSuficiente(cantidad);
    }

    public void reducirStock(Long productoId, int cantidad) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        
        if (!producto.tieneStockSuficiente(cantidad)) {
            throw new RuntimeException("Stock insuficiente");
        }
        
        producto.setStock(producto.getStock() - cantidad);
        productoRepository.save(producto);
    }
}
