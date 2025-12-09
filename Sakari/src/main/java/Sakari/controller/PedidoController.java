package Sakari.controller;

import Sakari.domain.Pedido;
import Sakari.domain.Usuario;
import Sakari.security.CustomUserDetails;
import Sakari.service.CarritoService;
import Sakari.service.PedidoService;
import Sakari.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pedidos")
@RequiredArgsConstructor
@Slf4j
public class PedidoController {

    private final PedidoService pedidoService;
    private final CarritoService carritoService;
    private final UsuarioService usuarioService;

    @GetMapping("/checkout")
    public String mostrarCheckout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        try {
            var carrito = carritoService.obtenerCarritoPorUsuarioId(userDetails.getId());
            
            if (carrito.getItems() == null || carrito.getItems().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Tu carrito está vacío");
                return "redirect:/carrito";
            }
            
            Usuario usuario = usuarioService.buscarPorId(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            model.addAttribute("carrito", carrito);
            model.addAttribute("total", carritoService.calcularTotal(userDetails.getId()));
            model.addAttribute("usuario", usuario);
            
            return "pedido/checkout";
            
        } catch (Exception e) {
            log.error("Error al mostrar checkout", e);
            redirectAttributes.addFlashAttribute("error", "Error al cargar el checkout");
            return "redirect:/carrito";
        }
    }

    @PostMapping("/procesar")
    public String procesarPedido(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String direccionEnvio,
            @RequestParam String telefonoContacto,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Validaciones
            if (direccionEnvio == null || direccionEnvio.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "La dirección de envío es obligatoria");
                return "redirect:/pedidos/checkout";
            }
            
            if (telefonoContacto == null || telefonoContacto.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El teléfono de contacto es obligatorio");
                return "redirect:/pedidos/checkout";
            }
            
            // Crear pedido
            Pedido pedido = pedidoService.crearPedidoDesdeCarrito(
                    userDetails.getId(),
                    direccionEnvio.trim(),
                    telefonoContacto.trim()
            );
            
            log.info("Pedido creado exitosamente: {} para usuario: {}", 
                    pedido.getNumeroPedido(), userDetails.getUsername());
            
            redirectAttributes.addFlashAttribute("pedido", pedido);
            return "redirect:/pedidos/confirmacion/" + pedido.getId();
            
        } catch (RuntimeException e) {
            log.error("Error al procesar pedido", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/pedidos/checkout";
        }
    }

    @GetMapping("/confirmacion/{id}")
    public String mostrarConfirmacion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        try {
            Pedido pedido = pedidoService.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
            
            // Verificar que el pedido pertenece al usuario
            if (!pedido.getUsuario().getId().equals(userDetails.getId())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para ver este pedido");
                return "redirect:/perfil/pedidos";
            }
            
            model.addAttribute("pedido", pedido);
            return "pedido/confirmacion";
            
        } catch (Exception e) {
            log.error("Error al mostrar confirmación", e);
            redirectAttributes.addFlashAttribute("error", "Error al cargar la confirmación");
            return "redirect:/perfil/pedidos";
        }
    }

    @GetMapping("/{id}")
    public String verDetallePedido(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        try {
            Pedido pedido = pedidoService.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
            
            // Verificar que el pedido pertenece al usuario (o es admin)
            boolean esAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            if (!esAdmin && !pedido.getUsuario().getId().equals(userDetails.getId())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para ver este pedido");
                return "redirect:/perfil/pedidos";
            }
            
            model.addAttribute("pedido", pedido);
            return "pedido/detalle";
            
        } catch (Exception e) {
            log.error("Error al ver detalle del pedido", e);
            redirectAttributes.addFlashAttribute("error", "Error al cargar el pedido");
            return "redirect:/perfil/pedidos";
        }
    }

    @PostMapping("/{id}/cancelar")
    public String cancelarPedido(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            Pedido pedido = pedidoService.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
            
            // Verificar permisos
            boolean esAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            if (!esAdmin && !pedido.getUsuario().getId().equals(userDetails.getId())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para cancelar este pedido");
                return "redirect:/perfil/pedidos";
            }
            
            // Solo se puede cancelar si está pendiente o procesando
            if (pedido.getEstado() != Pedido.EstadoPedido.PENDIENTE && 
                pedido.getEstado() != Pedido.EstadoPedido.PROCESANDO) {
                redirectAttributes.addFlashAttribute("error", 
                    "No se puede cancelar un pedido que ya ha sido enviado o entregado");
                return "redirect:/pedidos/" + id;
            }
            
            pedidoService.cancelarPedido(id);
            redirectAttributes.addFlashAttribute("mensaje", "Pedido cancelado exitosamente");
            log.info("Pedido {} cancelado por usuario: {}", id, userDetails.getUsername());
            
        } catch (Exception e) {
            log.error("Error al cancelar pedido", e);
            redirectAttributes.addFlashAttribute("error", "Error al cancelar el pedido: " + e.getMessage());
        }
        
        return "redirect:/perfil/pedidos";
    }
}
