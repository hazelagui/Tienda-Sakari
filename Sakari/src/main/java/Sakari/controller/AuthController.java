package Sakari.controller;

import Sakari.domain.Usuario;
import Sakari.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UsuarioService usuarioService;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "auth/registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(
            @ModelAttribute Usuario usuario,
            @RequestParam("confirmarPassword") String confirmarPassword,
            RedirectAttributes redirectAttributes) {

        log.info("Intentando registrar usuario: {}", usuario.getEmail());

        // Validar que las contraseñas coincidan
        if (!usuario.getPassword().equals(confirmarPassword)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
            redirectAttributes.addFlashAttribute("usuario", usuario);
            return "redirect:/registro";
        }

        // Validar longitud mínima de contraseña
        if (usuario.getPassword().length() < 6) {
            redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 6 caracteres");
            redirectAttributes.addFlashAttribute("usuario", usuario);
            return "redirect:/registro";
        }

        // Validar email no vacío
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El correo electrónico es obligatorio");
            redirectAttributes.addFlashAttribute("usuario", usuario);
            return "redirect:/registro";
        }

        try {
            usuarioService.registrarUsuario(usuario);

            log.info("Usuario registrado exitosamente: {}", usuario.getEmail());
            redirectAttributes.addFlashAttribute("mensaje", 
                "¡Registro exitoso! Ya puedes iniciar sesión con tu cuenta.");
            return "redirect:/login";

        } catch (RuntimeException e) {
            log.error("Error en registro: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("usuario", usuario);
            return "redirect:/registro";
        }
    }
}
