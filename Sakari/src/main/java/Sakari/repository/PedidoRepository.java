package Sakari.repository;

import Sakari.domain.Pedido;
import Sakari.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    
    List<Pedido> findByUsuarioOrderByFechaPedidoDesc(Usuario usuario);
    
    List<Pedido> findByUsuarioIdOrderByFechaPedidoDesc(Long usuarioId);
    
    Optional<Pedido> findByNumeroPedido(String numeroPedido);
    
    List<Pedido> findByEstado(Pedido.EstadoPedido estado);
    
    List<Pedido> findAllByOrderByFechaPedidoDesc();
    
    long countByEstado(Pedido.EstadoPedido estado);
}
