package br.ufrpe.poo.banco.negocio;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import br.ufrpe.poo.banco.exceptions.ClienteJaPossuiContaException;
import br.ufrpe.poo.banco.exceptions.ClienteNaoPossuiContaException;

public class TesteCliente {

    private Cliente cliente;

    @Before
    public void setUp() {
        cliente = new Cliente("João Silva", "123.456.789-00");
    }

    @Test
    public void testGetNome() {
        assertEquals("João Silva", cliente.getNome());
    }

    @Test
    public void testSetNome() {
        cliente.setNome("Maria Silva");
        assertEquals("Maria Silva", cliente.getNome());
    }

    @Test
    public void testGetCpf() {
        assertEquals("123.456.789-00", cliente.getCpf());
    }

    @Test
    public void testSetCpf() {
        cliente.setCpf("987.654.321-00");
        assertEquals("987.654.321-00", cliente.getCpf());
    }

    @Test
    public void testAdicionarConta() throws ClienteJaPossuiContaException {
        cliente.adicionarConta("1234-5");
        assertEquals(1, cliente.getContas().size());
        assertEquals("1234-5", cliente.getContas().get(0));
    }

    @Test(expected = ClienteJaPossuiContaException.class)
    public void testAdicionarContaJaExistente() throws ClienteJaPossuiContaException {
        cliente.adicionarConta("1234-5");
        cliente.adicionarConta("1234-5");
    }

    @Test
    public void testRemoverConta() throws ClienteJaPossuiContaException, ClienteNaoPossuiContaException {
        cliente.adicionarConta("1234-5");
        cliente.removerConta("1234-5");
        assertEquals(0, cliente.getContas().size());
    }

    @Test(expected = ClienteNaoPossuiContaException.class)
    public void testRemoverContaNaoExistente() throws ClienteNaoPossuiContaException {
        cliente.removerConta("1234-5");
    }

    @Test
    public void testRemoverTodasAsContas() throws ClienteJaPossuiContaException {
        cliente.adicionarConta("1234-5");
        cliente.adicionarConta("6789-0");
        cliente.removerTodasAsContas();
        assertNull(cliente.getContas());
    }

    @Test
    public void testProcurarConta() throws ClienteJaPossuiContaException {
        cliente.adicionarConta("1234-5");
        assertEquals(0, cliente.procurarConta("1234-5"));
        assertEquals(-1, cliente.procurarConta("6789-0"));
    }

    @Test
    public void testConsultarNumeroConta() throws ClienteJaPossuiContaException {
        cliente.adicionarConta("1234-5");
        assertEquals("1234-5", cliente.consultarNumeroConta(0));
    }

    @Test
    public void testEquals() {
        Cliente outroCliente = new Cliente("Maria Silva", "123.456.789-00");
        assertTrue(cliente.equals(outroCliente));
        Cliente clienteDiferente = new Cliente("Maria Silva", "987.654.321-00");
        assertFalse(cliente.equals(clienteDiferente));
    }

    @Test
    public void testToString() {
        assertEquals("Nome: João Silva\nCPF: 123.456.789-00\nContas: []", cliente.toString());
    }
}
