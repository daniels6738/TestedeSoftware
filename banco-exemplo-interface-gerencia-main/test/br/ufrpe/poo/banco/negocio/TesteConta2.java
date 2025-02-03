package br.ufrpe.poo.banco.negocio;

import static org.junit.Assert.*;

import org.junit.Test;

import br.ufrpe.poo.banco.exceptions.SaldoInsuficienteException;

public class TesteConta2 {

	@Test
	public final void testDebitar() throws SaldoInsuficienteException {
		Conta c = new Conta("1", 100);
		c.debitar(50);

		assertEquals("Erro ao debitar", 50, c.getSaldo(), 0);
	}

	@Test(expected = SaldoInsuficienteException.class)
	public final void testDebitarInsuficiente() throws SaldoInsuficienteException{
		Conta c = new Conta("1", 100);
		c.debitar(101);
		fail("Exceção de saldo insuficiente não levantada");
	}

	@Test
	public final void testCreditar() {
		fail("Not yet implemented"); // TODO
	}
	

}
