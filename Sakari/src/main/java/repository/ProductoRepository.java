package Sakari.repository;

import Sakari.domain.Categoria;
import Sakari.domain.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    
    List<Producto> findByActivoTrue();
    
    List<Producto> findByCategoria(Categoria categoria);
    
    List<Producto> findByCategoriaAndActivoTrue(Categoria categoria);
    
    List<Producto> findByPersonalizableTrue();
    
    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.stock > 0")
    List<Producto> findProductosDisponibles();
    
    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Producto> buscarPorPalabraClave(String keyword);
}
