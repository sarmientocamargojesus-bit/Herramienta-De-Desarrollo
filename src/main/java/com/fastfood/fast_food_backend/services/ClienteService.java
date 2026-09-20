package com.fastfood.fast_food_backend.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fastfood.fast_food_backend.entities.Cliente;
import com.fastfood.fast_food_backend.exceptions.RecursoNoEncontradoException;
import com.fastfood.fast_food_backend.exceptions.ReglaNegocioException;
import com.fastfood.fast_food_backend.repositories.ClienteRepository;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<Cliente> listar() {
        return clienteRepository.findAll();
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado: " + id));
    }

    public Cliente registrar(Cliente cliente) {
        validar(cliente);
        validarCorreoDisponible(cliente.getCorreo(), null);
        if (cliente.getActivo() == null) {
            cliente.setActivo(true);
        }
        return clienteRepository.save(cliente);
    }

    public Cliente actualizar(Long id, Cliente datosCliente) {
        Cliente cliente = buscarPorId(id);
        validar(datosCliente);
        validarCorreoDisponible(datosCliente.getCorreo(), id);

        cliente.setNombres(datosCliente.getNombres());
        cliente.setApellidos(datosCliente.getApellidos());
        cliente.setTelefono(datosCliente.getTelefono());
        cliente.setCorreo(datosCliente.getCorreo());
        cliente.setDireccion(datosCliente.getDireccion());
        if (datosCliente.getActivo() != null) {
            cliente.setActivo(datosCliente.getActivo());
        }
        return clienteRepository.save(cliente);
    }

    public void eliminar(Long id) {
        Cliente cliente = buscarPorId(id);
        clienteRepository.delete(cliente);
    }

    public List<Cliente> buscarPorApellidos(String apellidos) {
        if (apellidos == null || apellidos.isBlank()) {
            throw new IllegalArgumentException("El apellido de búsqueda es obligatorio");
        }
        return clienteRepository.findByApellidosContainingIgnoreCase(apellidos);
    }

    public Cliente buscarPorCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("El correo de búsqueda es obligatorio");
        }
        return clienteRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con correo: " + correo));
    }

    private void validar(Cliente cliente) {
        if (cliente.getNombres() == null || cliente.getNombres().isBlank()) {
            throw new IllegalArgumentException("Los nombres del cliente son obligatorios");
        }
        if (cliente.getApellidos() == null || cliente.getApellidos().isBlank()) {
            throw new IllegalArgumentException("Los apellidos del cliente son obligatorios");
        }
        if (cliente.getTelefono() == null || cliente.getTelefono().isBlank()) {
            throw new IllegalArgumentException("El teléfono del cliente es obligatorio");
        }
        if (cliente.getCorreo() == null || cliente.getCorreo().isBlank() || !cliente.getCorreo().contains("@")) {
            throw new IllegalArgumentException("El correo del cliente no es válido");
        }
    }

    private void validarCorreoDisponible(String correo, Long clienteIdActual) {
        clienteRepository.findByCorreoIgnoreCase(correo).ifPresent(clienteExistente -> {
            if (clienteIdActual == null || !clienteExistente.getId().equals(clienteIdActual)) {
                throw new ReglaNegocioException("Ya existe un cliente registrado con el correo: " + correo);
            }
        });
    }
}
