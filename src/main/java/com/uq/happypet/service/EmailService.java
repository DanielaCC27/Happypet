package com.uq.happypet.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.uq.happypet.model.DetallePedido;
import com.uq.happypet.model.Pedido;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:}")
    private String fromAddress;

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    @Value("${app.base-url}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envía el correo de verificación con el token.
     *
     * @return true si el envío fue correcto, false si falló (el error queda en log)
     */
    public boolean sendVerificationEmail(String to, String token) {
        return sendVerificationEmail(to, token, null);
    }

    /**
     * Igual que {@link #sendVerificationEmail(String, String)} pero con nombre para el saludo.
     */
    public boolean sendVerificationEmail(String to, String token, String recipientName) {
        String link = buildEmailLink("/api/auth/verify?token=", token);
        String nombre = (recipientName != null && !recipientName.isBlank()) ? recipientName : "Usuario";
        String cuerpo = "Hola " + nombre + ",\n\n"
                + "Para activar tu cuenta en HappyPet, abre este enlace en el navegador:\n"
                + link + "\n\n"
                + "Si no creaste esta cuenta, ignora este mensaje.\n";

        return enviar(to, "Verifica tu cuenta - HappyPet", cuerpo);
    }

    public void enviarVerificacionCorreo(String destinatario, String nombre, String token) {
        sendVerificationEmail(destinatario, token, nombre);
    }

    public void enviarRecuperacionPassword(String destinatario, String nombre, String token) {
        String link = buildEmailLink("/reset-password?token=", token);
        String cuerpo = "Hola " + nombre + ",\n\n"
                + "Para restablecer tu contrase\u00f1a en HappyPet, abre este enlace (v\u00e1lido por tiempo limitado):\n"
                + link + "\n\n"
                + "Si no solicitaste el cambio, ignora este mensaje.\n";

        enviar(destinatario, "Restablecer contrase\u00f1a - HappyPet", cuerpo);
    }

    /**
     * baseUrl (from app.base-url) + endpoint path/query with token placeholder suffix + token.
     * Local: application.properties. AWS: application-aws.properties with profile aws.
     */
    private String buildEmailLink(String endpointWithTokenParam, String token) {
        String base = baseUrl == null ? "" : baseUrl.trim();
        if (base.isEmpty()) {
            throw new IllegalStateException("app.base-url must be set (e.g. in application.properties or application-aws.properties)");
        }
        base = base.replaceAll("/$", "");
        return base + endpointWithTokenParam + token;
    }

    private boolean enviar(String destinatario, String asunto, String texto) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            String from = resolveFromAddress();
            if (from != null && !from.isBlank()) {
                message.setFrom(from);
            }
            message.setTo(destinatario);
            message.setSubject(asunto);
            message.setText(texto);
            mailSender.send(message);
            log.info("Correo enviado correctamente a {}", destinatario);
            return true;
        } catch (Exception e) {
            log.error("ERROR AL ENVIAR CORREO a {} — asunto: '{}' — causa: {} — detalle:",
                    destinatario, asunto, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Con Gmail, el remitente debe coincidir con la cuenta autenticada ({@code spring.mail.username}).
     */
    private String resolveFromAddress() {
        if (fromAddress != null && !fromAddress.isBlank()) {
            return fromAddress;
        }
        if (smtpUsername != null && !smtpUsername.isBlank()) {
            return smtpUsername;
        }
        return null;
    }

    public void enviarPedidoRecibidoCliente(Pedido pedido) {
        String to = resolverCorreoClientePedido(pedido);
        String nombre = pedido.getUsuario().getNombre();
        StringBuilder cuerpo = new StringBuilder();
        cuerpo.append("Hola ").append(nombre != null ? nombre : "cliente").append(",\n\n");
        cuerpo.append("Pedido creado exitosamente. Numero de pedido: #").append(pedido.getId()).append(".\n\n");
        cuerpo.append("A continuacion encontraras los datos de ENVIO, FACTURACION y el detalle de productos.\n\n");
        appendResumenPedido(cuerpo, pedido);
        appendSeccionEnvio(cuerpo, pedido);
        appendSeccionFacturacion(cuerpo, pedido);
        appendDetalleProductos(cuerpo, pedido);
        cuerpo.append("\nGracias por comprar en HappyPet.\n");
        enviar(to, "Pedido #" + pedido.getId() + " registrado - HappyPet", cuerpo.toString());
    }

    public void enviarPedidoConfirmadoCliente(Pedido pedido) {
        String to = resolverCorreoClientePedido(pedido);
        String nombre = pedido.getUsuario().getNombre();
        StringBuilder cuerpo = new StringBuilder();
        cuerpo.append("Hola ").append(nombre != null ? nombre : "cliente").append(",\n\n");
        cuerpo.append("Tu pedido #").append(pedido.getId()).append(" ha sido CONFIRMADO por HappyPet.\n\n");
        appendResumenPedido(cuerpo, pedido);
        appendSeccionEnvio(cuerpo, pedido);
        appendSeccionFacturacion(cuerpo, pedido);
        appendDetalleProductos(cuerpo, pedido);
        cuerpo.append("\nEn breve prepararemos el envio. Gracias por tu compra.\n");
        enviar(to, "Pedido #" + pedido.getId() + " confirmado - HappyPet", cuerpo.toString());
    }

    public void enviarPedidoEnviadoCliente(Pedido pedido) {
        String to = resolverCorreoClientePedido(pedido);
        String nombre = pedido.getUsuario().getNombre();
        StringBuilder cuerpo = new StringBuilder();
        cuerpo.append("Hola ").append(nombre != null ? nombre : "cliente").append(",\n\n");
        cuerpo.append("Tu pedido #").append(pedido.getId()).append(" esta ENVIADO o listo para recogida segun lo acordado.\n\n");
        appendResumenPedido(cuerpo, pedido);
        appendSeccionEnvio(cuerpo, pedido);
        appendSeccionFacturacion(cuerpo, pedido);
        appendDetalleProductos(cuerpo, pedido);
        cuerpo.append("\nCualquier duda, responde a este correo o contacta a HappyPet.\n");
        enviar(to, "Pedido #" + pedido.getId() + " enviado - HappyPet", cuerpo.toString());
    }

    private String resolverCorreoClientePedido(Pedido pedido) {
        if (pedido.getFacturacionEmail() != null && !pedido.getFacturacionEmail().isBlank()) {
            return pedido.getFacturacionEmail().trim();
        }
        return pedido.getUsuario().getCorreo();
    }

    private void appendResumenPedido(StringBuilder cuerpo, Pedido pedido) {
        cuerpo.append("========== RESUMEN ==========\n");
        cuerpo.append("Total pedido: $").append(Math.round(pedido.getTotal())).append("\n");
        cuerpo.append("Estado actual: ").append(pedido.getEstado().name()).append("\n");
        cuerpo.append("Metodo de pago: ").append(pedido.getMetodoPago().getEtiqueta()).append("\n");
        cuerpo.append("Entrega preferida: ").append(pedido.getFechaEntregaPreferida())
                .append(" (franja: ").append(pedido.getHorarioEntrega().getEtiqueta()).append(")\n");
        cuerpo.append("Ventana de fechas posibles al realizar el pedido: ")
                .append(pedido.getVentanaEntregaDesde()).append(" a ")
                .append(pedido.getVentanaEntregaHasta()).append("\n\n");
    }

    private void appendSeccionEnvio(StringBuilder cuerpo, Pedido pedido) {
        cuerpo.append("========== ENVIO ==========\n");
        cuerpo.append("Direccion completa de entrega:\n").append(pedido.getDireccionEnvio()).append("\n\n");
    }

    private void appendSeccionFacturacion(StringBuilder cuerpo, Pedido pedido) {
        cuerpo.append("========== FACTURACION ==========\n");
        cuerpo.append("Tipo de documento: ").append(pedido.getFacturacionTipoDocumento() != null
                ? pedido.getFacturacionTipoDocumento() : "-").append("\n");
        cuerpo.append("Nombre y apellidos: ").append(pedido.getFacturacionNombre());
        if (pedido.getFacturacionApellidos() != null && !pedido.getFacturacionApellidos().isBlank()) {
            cuerpo.append(" ").append(pedido.getFacturacionApellidos());
        }
        cuerpo.append("\n");
        cuerpo.append("Numero de documento: ").append(pedido.getFacturacionDocumento()).append("\n");
        cuerpo.append("Direccion de facturacion / registro:\n").append(pedido.getFacturacionDireccion()).append("\n");
        cuerpo.append("Correo de contacto facturacion: ").append(pedido.getFacturacionEmail()).append("\n\n");
    }

    private void appendDetalleProductos(StringBuilder cuerpo, Pedido pedido) {
        cuerpo.append("========== PRODUCTOS ==========\n");
        for (DetallePedido d : pedido.getDetalles()) {
            cuerpo.append("- ").append(d.getProducto().getNombre())
                    .append("  |  Cantidad: ").append(d.getCantidad())
                    .append("  |  Precio unitario: $").append(Math.round(d.getPrecioUnitario()))
                    .append("  |  Subtotal: $").append(Math.round(d.getPrecioUnitario() * d.getCantidad()))
                    .append("\n");
        }
    }
}
