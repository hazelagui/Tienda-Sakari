package Sakari.repository;

import Sakari.domain.Carrito;
import Sakari.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {
    
    Optional<Carrito> findByUsuario(Usuario usuario);
    
    Optional<Carrito> findByUsuarioId(Long usuarioId);
}
