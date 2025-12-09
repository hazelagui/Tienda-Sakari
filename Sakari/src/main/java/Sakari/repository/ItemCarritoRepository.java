package Sakari.repository;

import Sakari.domain.Carrito;
import Sakari.domain.ItemCarrito;
import Sakari.domain.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {
    
    List<ItemCarrito> findByCarrito(Carrito carrito);
    
    List<ItemCarrito> findByCarritoId(Long carritoId);
    
    Optional<ItemCarrito> findByCarritoAndProducto(Carrito carrito, Producto producto);
    
    void deleteByCarrito(Carrito carrito);
}
