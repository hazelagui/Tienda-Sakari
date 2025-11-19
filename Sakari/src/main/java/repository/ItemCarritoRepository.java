package Sakari.repository;

import Sakari.domain.Carrito;
import Sakari.domain.ItemCarrito;
import Sakari.domain.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {
    
    Optional<ItemCarrito> findByCarritoAndProducto(Carrito carrito, Producto producto);
}
