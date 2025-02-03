package br.ufrpe.poo.banco.negocio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import br.ufrpe.poo.banco.dados.IRepositorioContas;
import br.ufrpe.poo.banco.dados.RepositorioContasArquivoBin;
import br.ufrpe.poo.banco.dados.RepositorioContasArray;
import br.ufrpe.poo.banco.exceptions.AtualizacaoNaoRealizadaException;
import br.ufrpe.poo.banco.exceptions.ClienteJaCadastradoException;
import br.ufrpe.poo.banco.exceptions.ClienteJaPossuiContaException;
import br.ufrpe.poo.banco.exceptions.ClienteNaoCadastradoException;
import br.ufrpe.poo.banco.exceptions.ClienteNaoPossuiContaException;
import br.ufrpe.poo.banco.exceptions.ContaJaAssociadaException;
import br.ufrpe.poo.banco.exceptions.ContaJaCadastradaException;
import br.ufrpe.poo.banco.exceptions.ContaNaoEncontradaException;
import br.ufrpe.poo.banco.exceptions.InicializacaoSistemaException;
import br.ufrpe.poo.banco.exceptions.RenderBonusContaEspecialException;
import br.ufrpe.poo.banco.exceptions.RenderJurosPoupancaException;
import br.ufrpe.poo.banco.exceptions.RepositorioException;
import br.ufrpe.poo.banco.exceptions.SaldoInsuficienteException;
import br.ufrpe.poo.banco.exceptions.ValorInvalidoException;

public class TesteBanco {

	private static Banco banco;

	@Before
	public void apagarArquivos() throws IOException, RepositorioException,
			InicializacaoSistemaException, ClassNotFoundException {

		BufferedWriter bw = new BufferedWriter(new FileWriter("clientes.dat"));
		bw.close();
		bw = new BufferedWriter(new FileWriter("contas.dat"));
		bw.close();

		Banco.instance = null;
		TesteBanco.banco = Banco.getInstance();
	}

	/**
	 * Verifica o cadastramento de uma nova conta.
	 * @throws ClassNotFoundException 
	 * 
	 */
	@Test
	public void testeCadastarNovaConta() throws RepositorioException,
			ContaJaCadastradaException, ContaNaoEncontradaException,
			InicializacaoSistemaException, ClassNotFoundException {

		Banco banco = new Banco(null, new RepositorioContasArquivoBin());
		ContaAbstrata conta1 = new Conta("1", 100);
		banco.cadastrar(conta1);
		ContaAbstrata conta2 = banco.procurarConta("1");
		assertEquals(conta1.getNumero(), conta2.getNumero());
		assertEquals(conta1.getSaldo(), conta2.getSaldo(), 0);
	}

	@Test
	public void testeEstourarLimiteDeContasArray() throws RepositorioException,
			ContaJaCadastradaException, ContaNaoEncontradaException,
			InicializacaoSistemaException {

		Banco banco = new Banco(null, new RepositorioContasArray());
		
		List<ContaAbstrata> contas = new ArrayList<ContaAbstrata>();
		for(int i = 0;i < 100;i++) {
			contas.add(new Conta(String.valueOf(i),0));
			banco.cadastrar(contas.get(i));
		}
		ContaAbstrata conta = new Conta("100",0);
		banco.cadastrar(conta);
	}
	
	/**
	 * Verifica que nao e permitido cadastrar duas contas com o mesmo numero.
	 * 
	 */
	@Test(expected = ContaJaCadastradaException.class)
	public void testeCadastrarContaExistente() throws RepositorioException,
			ContaJaCadastradaException, ContaNaoEncontradaException,
			InicializacaoSistemaException {

		Conta c1 = new Conta("1", 200);
		Conta c2 = new Conta("1", 300);
		banco.cadastrar(c1);
		banco.cadastrar(c2);
		fail("Excecao ContaJaCadastradaException nao levantada");
	}

	/**
	 * Verifica se o credito esta sendo executado corretamente em uma conta
	 * corrente.
	 * 
	 */
	@Test
	public void testeCreditarContaExistente() throws RepositorioException,
			ContaNaoEncontradaException, InicializacaoSistemaException,
			ContaJaCadastradaException, ValorInvalidoException {

		ContaAbstrata conta = new Conta("1", 100);
		banco.cadastrar(conta);
		banco.creditar(conta, 100);
		conta = banco.procurarConta("1");
		assertEquals(200, conta.getSaldo(), 0);
	}

	/**
	 * Verifica a excecao levantada na tentativa de creditar em uma conta que
	 * nao existe.
	 * 
	 */
	@Test(expected = ContaNaoEncontradaException.class)
	public void testeCreditarContaInexistente() throws RepositorioException,
			ContaNaoEncontradaException, InicializacaoSistemaException,
			ValorInvalidoException {

		banco.creditar(new Conta("", 0), 200);

		fail("Excecao ContaNaoEncontradaException nao levantada");
	}

	/**
	 * Verifica que a operacao de debito em conta corrente esta acontecendo
	 * corretamente.
	 * 
	 */
	@Test
	public void testeDebitarContaExistente() throws RepositorioException,
			ContaNaoEncontradaException, SaldoInsuficienteException,
			InicializacaoSistemaException, ContaJaCadastradaException,
			ValorInvalidoException {

		ContaAbstrata conta = new Conta("1", 50);
		banco.cadastrar(conta);
		banco.debitar(conta, 50);
		conta = banco.procurarConta("1");
		assertEquals(0, conta.getSaldo(), 0);
	}

	/**
	 * Verifica que tentantiva de debitar em uma conta que nao existe levante
	 * excecao.
	 * 
	 */
	@Test(expected = ContaNaoEncontradaException.class)
	public void testeDebitarContaInexistente() throws RepositorioException,
			ContaNaoEncontradaException, SaldoInsuficienteException,
			InicializacaoSistemaException, ValorInvalidoException {

		banco.debitar(new Conta("", 0), 50);
		fail("Excecao ContaNaoEncontradaException nao levantada");
	}

	/**
	 * Verifica que a transferencia entre contas correntes e realizada com
	 * sucesso.
	 * 
	 */
	@Test
	public void testeTransferirContaExistente() throws RepositorioException,
			ContaNaoEncontradaException, SaldoInsuficienteException,
			InicializacaoSistemaException, ContaJaCadastradaException,
			ValorInvalidoException {

		ContaAbstrata conta1 = new Conta("1", 100);
		ContaAbstrata conta2 = new Conta("2", 200);
		banco.cadastrar(conta1);
		banco.cadastrar(conta2);
		banco.transferir(conta1, conta2, 50);
		conta1 = banco.procurarConta("1");
		conta2 = banco.procurarConta("2");
		assertEquals(50, conta1.getSaldo(), 0);
		assertEquals(250, conta2.getSaldo(), 0);
	}

	/**
	 * Verifica que tentativa de transferir entre contas cujos numeros nao
	 * existe levanta excecao.
	 * 
	 */
	@Test(expected = ContaNaoEncontradaException.class)
	public void testeTransferirContaInexistente() throws RepositorioException,
			ContaNaoEncontradaException, SaldoInsuficienteException,
			InicializacaoSistemaException, ValorInvalidoException {
		Poupanca poupancaNumeroNaoExiste1 = new Poupanca("126", 10);
		Poupanca poupancaNumeroNaoExiste2 = new Poupanca("127", 10);
		banco.transferir(poupancaNumeroNaoExiste1, poupancaNumeroNaoExiste2, 5);
		fail("Excecao ContaNaoEncontradaException nao levantada)");
	}

	/**
	 * Verifica que render juros de uma conta poupanca funciona corretamente
	 * 
	 */

	@Test
	public void testeRenderJurosContaExistente() throws RepositorioException,
			ContaNaoEncontradaException, RenderJurosPoupancaException,
			InicializacaoSistemaException, ContaJaCadastradaException {

		Poupanca poupanca = new Poupanca("20", 100);
		banco.cadastrar(poupanca);
		double saldoSemJuros = poupanca.getSaldo();
		double saldoComJuros = saldoSemJuros + (saldoSemJuros * 0.008);
		poupanca.renderJuros(0.008);
		assertEquals(saldoComJuros, poupanca.getSaldo(), 0);
	}

	/**
	 * Verifica que tentativa de render juros em conta inexistente levanta
	 * excecao.
	 * 
	 */
	@Test(expected = ContaNaoEncontradaException.class)
	public void testeRenderJurosContaInexistente() throws RepositorioException,
			ContaNaoEncontradaException, RenderJurosPoupancaException,
			InicializacaoSistemaException {
		Poupanca poupancaInexistente = new Poupanca("12345", 10);
		banco.renderJuros(poupancaInexistente);
		// caso a exceção nao seja levantada
		fail("Excecao ContaNaoEncontradaException nao levantada");

	}

	/**
	 * Verifica que tentativa de render juros em conta que nao e poupanca
	 * levanta excecao.
	 * 
	 */
	@Test(expected = RenderJurosPoupancaException.class)
	public void testeRenderJurosContaNaoEhPoupanca()
			throws RepositorioException, ContaNaoEncontradaException,
			RenderJurosPoupancaException, InicializacaoSistemaException,
			ContaJaCadastradaException {
		ContaEspecial contaNaoPoupanca = new ContaEspecial("10100", 100);
		banco.cadastrar(contaNaoPoupanca);
		banco.renderJuros(contaNaoPoupanca);
		// caso a excecao nao seja levantada
		fail("Excecao RenderJurosPoupancaException nao levantada");
	}

	/**
	 * Verifica que render bonus de uma conta especial funciona corretamente.
	 * 
	 */
	@Test
	public void testeRenderBonusContaEspecialExistente()
			throws RepositorioException, ContaNaoEncontradaException,
			RenderBonusContaEspecialException, InicializacaoSistemaException,
			RenderJurosPoupancaException, ContaJaCadastradaException {
		ContaEspecial contaEspecialRenderBonus = new ContaEspecial("123", 100);
		banco.cadastrar(contaEspecialRenderBonus);
		contaEspecialRenderBonus.creditar(200);// bonus de 2
		contaEspecialRenderBonus.creditar(100);// bonus de 1
		banco.renderBonus(contaEspecialRenderBonus);

		ContaEspecial contaVerificada = (ContaEspecial) banco.procurarConta("123");
		double saldoEsperado = 100 + 200 + 100 + 3;
		assertEquals(saldoEsperado, contaVerificada.getSaldo(), 0);
		assertEquals(0, contaVerificada.getBonus(), 0);

	}

	/**
	 * Verifica que a tentativa de render bonus em conta especial inexistente
	 * levanta excecao.
	 * 
	 */

	@Test(expected = ContaNaoEncontradaException.class)
	public void testeRenderBonusContaEspecialNaoInexistente()
			throws RepositorioException, ContaNaoEncontradaException,
			RenderBonusContaEspecialException, InicializacaoSistemaException,
			RenderJurosPoupancaException {
		ContaEspecial contaEspecialNaoExistente = new ContaEspecial("124", 4);
		banco.renderBonus(contaEspecialNaoExistente);

		fail("Excecao ContaNaoEncontradaException nao levantada");
	}

	/**
	 * Verifica que tentativa de render bonus em conta que nao e especial
	 * levante excecao.
	 */

	@Test(expected = RenderBonusContaEspecialException.class)
	public void testeRenderBonusContaNaoEspecial1() throws RepositorioException,
			ContaNaoEncontradaException, RenderBonusContaEspecialException,
			InicializacaoSistemaException, RenderJurosPoupancaException,
			ContaJaCadastradaException {
		Poupanca contaNaoEspecial = new Poupanca("125", 100);
		banco.cadastrar(contaNaoEspecial);
		banco.renderBonus(contaNaoEspecial);
		fail("Excecao RenderBonusContaEspecialException nao levantada");
	}
	
	@Test
    public void testeAtualizarCliente() throws RepositorioException, AtualizacaoNaoRealizadaException, ClienteJaCadastradoException, InicializacaoSistemaException {
        Cliente cliente = new Cliente("12345678901", "João");
        banco.cadastrarCliente(cliente);
        cliente.setNome("João da Silva");
        banco.atualizarCliente(cliente);

        Cliente clienteAtualizado = banco.procurarCliente("12345678901");
        assertEquals("João da Silva", clienteAtualizado.getNome());
    }

    @Test(expected = AtualizacaoNaoRealizadaException.class)
    public void testeAtualizarClienteNaoExistente() throws RepositorioException, AtualizacaoNaoRealizadaException {
        Cliente cliente = new Cliente("00000000000", "Desconhecido");
        banco.atualizarCliente(cliente);
        fail("Excecao AtualizacaoNaoRealizadaException nao levantada");
    }

    @Test
    public void testeAssociarContaClienteExistente() throws RepositorioException, ClienteJaPossuiContaException, ContaJaAssociadaException, ClienteNaoCadastradoException, ContaJaCadastradaException, InicializacaoSistemaException, ClienteJaCadastradoException {
        Cliente cliente = new Cliente("12345678901", "Maria");
        ContaAbstrata conta = new Conta("1", 200);

        banco.cadastrarCliente(cliente);
        banco.cadastrar(conta);
        banco.associarConta("12345678901", "1");

        Cliente clienteAtualizado = banco.procurarCliente("12345678901");
        assertEquals(1, clienteAtualizado.getContas().size());
        assertEquals("1", clienteAtualizado.consultarNumeroConta(0));
    }

    @Test(expected = ClienteJaPossuiContaException.class)
    public void testeAssociarContaJaAssociada() throws RepositorioException, ClienteJaPossuiContaException, ContaJaAssociadaException, ClienteNaoCadastradoException, ContaJaCadastradaException, InicializacaoSistemaException, ClienteJaCadastradoException {
        Cliente cliente = new Cliente("12345678901", "Carlos");
        ContaAbstrata conta = new Conta("1", 300);

        banco.cadastrarCliente(cliente);
        banco.cadastrar(conta);
        banco.associarConta("12345678901", "1");
        banco.associarConta("12345678901", "1");

        fail("Excecao ClienteJaPossuiContaException nao levantada");
    }

    @Test(expected = ClienteNaoCadastradoException.class)
    public void testeAssociarContaClienteNaoExistente() throws RepositorioException, ClienteJaPossuiContaException, ContaJaAssociadaException, ClienteNaoCadastradoException, ContaJaCadastradaException {
        ContaAbstrata conta = new Conta("1", 400);
        banco.cadastrar(conta);
        banco.associarConta("99999999999", "1");

        fail("Excecao ClienteNaoCadastradoException nao levantada");
    }

    @Test
    public void testeRemoverClienteComContas() throws RepositorioException, ClienteNaoCadastradoException, ContaNaoEncontradaException, ClienteNaoPossuiContaException, InicializacaoSistemaException, ContaJaCadastradaException, ClienteJaPossuiContaException, ContaJaAssociadaException, ClienteJaCadastradoException {
        Cliente cliente = new Cliente("12345678901", "Ana");
        ContaAbstrata conta = new Conta("1", 500);

        banco.cadastrarCliente(cliente);
        banco.cadastrar(conta);
        banco.associarConta("12345678901", "1");

        banco.removerCliente("12345678901");

        Cliente clienteRemovido = banco.procurarCliente("12345678901");
        assertEquals(null, clienteRemovido);
    }

    @Test(expected = ClienteNaoCadastradoException.class)
    public void testeRemoverClienteNaoExistente() throws RepositorioException, ClienteNaoCadastradoException, ContaNaoEncontradaException, ClienteNaoPossuiContaException {
        banco.removerCliente("00000000000");
        fail("Excecao ClienteNaoCadastradoException nao levantada");
    }

    @Test
    public void testeRenderBonusContaEspecial() throws RepositorioException, ContaNaoEncontradaException, RenderBonusContaEspecialException, InicializacaoSistemaException, ContaJaCadastradaException, ValorInvalidoException {
        ContaEspecial contaEspecial = new ContaEspecial("2", 100);
        banco.cadastrar(contaEspecial);
        banco.creditar(contaEspecial, 500); // bonus de 5
        banco.renderBonus(contaEspecial);

        ContaEspecial contaVerificada = (ContaEspecial) banco.procurarConta("2");
        double saldoEsperado = 100 + 500 + 5;
        assertEquals(saldoEsperado, contaVerificada.getSaldo(), 0);
        assertEquals(0, contaVerificada.getBonus(), 0);
    }

    @Test(expected = RenderBonusContaEspecialException.class)
    public void testeRenderBonusContaNaoEspecial() throws RepositorioException, ContaNaoEncontradaException, RenderBonusContaEspecialException, InicializacaoSistemaException, ContaJaCadastradaException {
        Poupanca poupanca = new Poupanca("3", 200);
        banco.cadastrar(poupanca);
        banco.renderBonus(poupanca);
        fail("Excecao RenderBonusContaEspecialException nao levantada");
    }

    @Test
    public void testeRenderJurosContaPoupanca() throws RepositorioException, ContaNaoEncontradaException, RenderJurosPoupancaException, InicializacaoSistemaException, ContaJaCadastradaException {
        Poupanca poupanca = new Poupanca("4", 300);
        banco.cadastrar(poupanca);
        banco.renderJuros(poupanca);

        Poupanca poupancaVerificada = (Poupanca) banco.procurarConta("4");
        double saldoEsperado = 300 + (300 * 50 / 100);
        assertEquals(saldoEsperado, poupancaVerificada.getSaldo(), 0);
    }

    @Test(expected = RenderJurosPoupancaException.class)
    public void testeRenderJurosContaNaoPoupanca() throws RepositorioException, ContaNaoEncontradaException, RenderJurosPoupancaException, InicializacaoSistemaException, ContaJaCadastradaException {
        ContaEspecial contaEspecial = new ContaEspecial("5", 400);
        banco.cadastrar(contaEspecial);
        banco.renderJuros(contaEspecial);
        fail("Excecao RenderJurosPoupancaException nao levantada");
    }

	@Test(expected = ClienteJaCadastradoException.class)
	public void testeCadastroCliente() throws RepositorioException, InicializacaoSistemaException, ClienteJaCadastradoException, ClassNotFoundException{
		Cliente c = new Cliente("Anderson", "12345678900");
		Banco b = Banco.getInstance();
		b.cadastrarCliente(c);
		assertEquals("Cliente não cadastrado","Anderson", b.procurarCliente("12345678900").getNome());
		b.cadastrarCliente(c);
	}

	@Test
	public void testeAssociarConta() throws RepositorioException, ClienteJaCadastradoException, ClienteJaPossuiContaException, ContaJaAssociadaException, ClienteNaoCadastradoException, ContaJaCadastradaException{
		Cliente c = new Cliente("Anderson", "12345678900");
		banco.cadastrarCliente(c);
		banco.associarConta("12345678900", "240");
	}

	@Test
	public void testeRemoverCliente() throws RepositorioException, ClienteNaoCadastradoException, ContaNaoEncontradaException, ClienteNaoPossuiContaException, ClienteJaCadastradoException, ContaJaCadastradaException, ClienteJaPossuiContaException, ContaJaAssociadaException{
		Cliente c = new Cliente("Boris", "12345678901");
		banco.cadastrarCliente(c);
		Conta conta = new Conta("240", 0);
		banco.cadastrar(conta);
		banco.associarConta("12345678901", "240");
		banco.removerCliente("12345678901");
		assertEquals("Cliente não removido", null, banco.procurarCliente("12345678900"));
	}
	
	@Test(expected = ClienteNaoPossuiContaException.class)
	public void testeRemoverConta() throws RepositorioException, ClienteJaCadastradoException, ClienteJaPossuiContaException, ContaNaoEncontradaException, ClienteNaoPossuiContaException, ContaJaCadastradaException{
		Cliente c = new Cliente("Boris", "12345678901");
		banco.cadastrarCliente(c);
		Conta conta = new Conta("240", 0);
		c.adicionarConta("240");
		banco.cadastrar(conta);
		banco.removerConta(c, "240");
		banco.removerConta(c, "240");
	}
	
	@Test(expected = ClienteNaoCadastradoException.class)
	public void testeRemoverClienteSemConta() throws RepositorioException, ClienteJaCadastradoException, ClienteNaoCadastradoException, ContaNaoEncontradaException, ClienteNaoPossuiContaException {
		Cliente c = new Cliente("Kamina", "101010");
		banco.cadastrarCliente(c);
		banco.removerCliente("101010");
	}

	@Test(expected = ValorInvalidoException.class)
	public void testeCreditarNegativo() throws RepositorioException, ContaJaCadastradaException, ValorInvalidoException{
		Conta conta = new Conta("240", 0);
		banco.cadastrar(conta);
		banco.creditar(conta, -20);
	}

	@Test
	public void testeIndiceAlcancadoRepoArray() throws RepositorioException, ContaJaCadastradaException{
		for(int i = 0; i < 20; i++){
			banco.cadastrar((new Conta(String.valueOf(i), 0)));
		}
	}

	@Test
	public void testeLerArquivo() throws RepositorioException, ContaJaCadastradaException, ClassNotFoundException{
		for(int i = 0; i < 20; i++){
			banco.cadastrar((new Conta(String.valueOf(i), 0)));
		}
		RepositorioContasArquivoBin repo = new RepositorioContasArquivoBin();
	}
	
	@Test
	public void testeGetIterator() throws RepositorioException, ContaJaCadastradaException, ClassNotFoundException{
		RepositorioContasArquivoBin repo = new RepositorioContasArquivoBin();
		repo.getIterator();
	}
	
	@Test
	public void testeRemoverContaInexistente() throws RepositorioException, ClassNotFoundException {
		RepositorioContasArquivoBin repo = new RepositorioContasArquivoBin();
		repo.remover("69420");
	}
	
	@Test
	public void testeAtaulizarContaInexistente() throws RepositorioException, ClassNotFoundException {
		RepositorioContasArquivoBin repo = new RepositorioContasArquivoBin();
		ContaAbstrata conta = new Conta("123",0);
		repo.atualizar(conta);
	}
}