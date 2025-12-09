package Sakari.controller;

import Sakari.domain.MensajeContacto;
import Sakari.service.ContactoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ContactoController {

    private final ContactoService contactoService;

    @GetMapping("/contacto")
    public String mostrarFormularioContacto(Model model) {
        model.addAttribute("mensajeContacto", new MensajeContacto());
        return "publico/contacto";
    }

    @PostMapping("/contacto")
    public String enviarMensajeContacto(
            @Valid @ModelAttribute("mensajeContacto") MensajeContacto mensaje,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("error", "Por favor corrige los errores en el formulario");
            return "publico/contacto";
        }

        try {
            contactoService.enviarMensaje(mensaje);
            redirectAttributes.addFlashAttribute("mensaje", 
                "¡Gracias por contactarnos! Tu mensaje ha sido enviado exitosamente. " +
                "Te responderemos lo antes posible.");
            log.info("Mensaje de contacto enviado por: {}", mensaje.getEmail());
            return "redirect:/contacto";
        } catch (Exception e) {
            log.error("Error al enviar mensaje de contacto", e);
            model.addAttribute("error", 
                "Ocurrió un error al enviar tu mensaje. Por favor intenta de nuevo más tarde.");
            return "publico/contacto";
        }
    }
}
