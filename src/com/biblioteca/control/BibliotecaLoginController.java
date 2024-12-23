package com.biblioteca.control;

import com.biblioteca.dao.GestionUsuariosDAO;
import com.biblioteca.ui.MenuAdministrador;
import com.biblioteca.ui.MenuProfesor;
import com.biblioteca.ui.MenuAlumno;
import com.biblioteca.ui.BibliotecaLogin;

import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.*;
import java.util.Properties;
import java.util.UUID;

public class BibliotecaLoginController {

    private BibliotecaLogin vista;
    private GestionUsuariosDAO dao;

    public BibliotecaLoginController(BibliotecaLogin vista) {
        this.vista = vista;
        this.dao = new GestionUsuariosDAO();
    }

    public void iniciarSesion() {
        String correo = vista.getCorreo();
        String contrasena = vista.getContrasena();

        if (correo.isEmpty() || contrasena.isEmpty()) {
            vista.mostrarMensaje("Por favor, ingrese su correo y contraseña.");
            return;
        }

        if (dao.validarCredenciales(correo, contrasena)) {
            String tipoUsuario = dao.obtenerTipoUsuario(correo);
            String nombreCompleto = dao.obtenerNombreCompletoPorCorreo(correo); // Obtener nombre completo

            vista.mostrarMensaje("Login exitoso!");
            vista.mostrarDialogo("Bienvenido, " + nombreCompleto); // Mostrar nombre y apellido
            vista.dispose();

            switch (tipoUsuario) {
                case "Administrador":
                    new MenuAdministrador().setVisible(true);
                    break;
                case "Profesor":
                    new MenuProfesor().setVisible(true);
                    break;
                case "Alumno":
                    new MenuAlumno().setVisible(true);
                    break;
                default:
                    vista.mostrarMensaje("Tipo de usuario no válido");
                    System.err.println("Error: Tipo de usuario desconocido: " + tipoUsuario);
            }
        } else {
            vista.mostrarMensaje("Correo o contraseña incorrectos");
        }
    }

    public void recuperarContrasena() {
        // Solicitar el correo electrónico del usuario
        String correo = JOptionPane.showInputDialog(vista, "Ingrese su correo electrónico para recuperar su contraseña:", 
                                                    "Recuperar Contraseña", JOptionPane.INFORMATION_MESSAGE);

        // Verificar si el usuario ingresó un correo
        if (correo == null || correo.trim().isEmpty()) {
            JOptionPane.showMessageDialog(vista, "Por favor, ingrese un correo electrónico válido.", 
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Verificar si el correo está registrado en la base de datos
        if (!dao.correoRegistrado(correo)) {
            JOptionPane.showMessageDialog(vista, "El correo electrónico no está registrado.", 
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Generar un token de recuperación único
        String token = UUID.randomUUID().toString();

        // Enviar el correo de recuperación
        enviarCorreoRecuperacion(correo, token);

        JOptionPane.showMessageDialog(vista, "Si el correo ingresado está registrado, recibirá un enlace para restablecer su contraseña.", 
                                      "Recuperación de Contraseña", JOptionPane.INFORMATION_MESSAGE);
    }

    // Método para enviar el correo electrónico con el enlace de recuperación
    private void enviarCorreoRecuperacion(String correo, String token) {
        String host = "smtp.udb.edu.sv"; // Servidor SMTP de Outlook
        String from = ""; // Tu correo de Outlook
        String pass = ""; // Contraseña de aplicación de Outlook (si tienes autenticación en 2 pasos)

        // Enlace para restablecer contraseña
        String linkRecuperacion = "http://tuservidor.com/restablecer?token=" + token;

        // Configuración de las propiedades SMTP de Outlook
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.udb.edu.sv");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true"); 

        // Crear la autenticación con el correo y la contraseña de aplicación
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, pass); // Autenticación con tu correo y contraseña
            }
        };

        // Crear una sesión de correo
        Session session = Session.getInstance(properties, authenticator);

        // Crear el mensaje de correo
        MimeMessage message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(correo));
            message.setSubject("Recuperación de Contraseña");
            message.setText("Para restablecer su contraseña, haga clic en el siguiente enlace:\n" + linkRecuperacion);

            // Enviar el correo
            Transport.send(message);
            System.out.println("Correo enviado con éxito.");
        } catch (MessagingException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(vista, "Error al enviar el correo de recuperación.", 
                                          "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
