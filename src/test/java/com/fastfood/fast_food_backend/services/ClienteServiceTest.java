package com.fastfood.fast_food_backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fastfood.fast_food_backend.entities.Cliente;
import com.fastfood.fast_food_backend.exceptions.RecursoNoEncontradoException;
import com.fastfood.fast_food_backend.exceptions.ReglaNegocioException;
import com.fastfood.fast_food_backend.repositories.ClienteRepository;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void debeRegistrarUnClienteValido() {
        Cliente cliente = cliente("ana@correo.com");
        when(clienteRepository.findByCorreoIgnoreCase(cliente.getCorreo())).thenReturn(Optional.empty());
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        Cliente resultado = clienteService.registrar(cliente);

        assertSame(cliente, resultado);
        assertTrue(resultado.getActivo());
        verify(clienteRepository).save(cliente);
    }

    @Test
    void debeRechazarUnCorreoDuplicado() {
        Cliente existente = cliente("ana@correo.com");
        existente.setId(1L);
        Cliente nuevo = cliente("ana@correo.com");
        when(clienteRepository.findByCorreoIgnoreCase(nuevo.getCorreo())).thenReturn(Optional.of(existente));

        assertThrows(ReglaNegocioException.class, () -> clienteService.registrar(nuevo));

        verify(clienteRepository, never()).save(any());
    }

    @Test
    void debeBuscarClientesPorApellido() {
        Cliente cliente = cliente("ana@correo.com");
        when(clienteRepository.findByApellidosContainingIgnoreCase("Pérez")).thenReturn(List.of(cliente));

        List<Cliente> resultado = clienteService.buscarPorApellidos("Pérez");

        assertEquals(1, resultado.size());
        assertSame(cliente, resultado.get(0));
    }

    @Test
    void debeInformarCuandoElClienteNoExiste() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> clienteService.buscarPorId(99L));
    }

    private Cliente cliente(String correo) {
        Cliente cliente = new Cliente();
        cliente.setNombres("Ana");
        cliente.setApellidos("Pérez");
        cliente.setTelefono("999111222");
        cliente.setCorreo(correo);
        cliente.setDireccion("Av. Principal 123");
        return cliente;
    }
}
