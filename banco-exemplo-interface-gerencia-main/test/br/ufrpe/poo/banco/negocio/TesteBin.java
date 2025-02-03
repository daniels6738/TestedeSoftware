package br.ufrpe.poo.banco.negocio;

import br.ufrpe.poo.banco.dados.RepositorioContasArquivoBin;
import br.ufrpe.poo.banco.exceptions.RepositorioException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import static org.junit.Assert.*;

public class TesteBin {

    private RepositorioContasArquivoBin repositorio;

    @Before
    public void setUp() throws RepositorioException, ClassNotFoundException {
        repositorio = new RepositorioContasArquivoBin();
    }

    @After
    public void tearDown() {
        File file = new File("contas.dat");
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void testLerArquivoIOException() throws IOException {
        // Simula um erro de IOException ao tentar ler o arquivo.
        File file = new File("contas.dat");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("corrupted content");
        }

        RepositorioException exception = assertThrows(RepositorioException.class, () -> {
            repositorio = new RepositorioContasArquivoBin(); // O construtor chama o método lerArquivo()
        });

        assertTrue(exception.getCause() instanceof IOException);
    }

    @Test
    public void testGravarArquivoIOException() {
        // Simula um erro de IOException ao tentar gravar no arquivo.
        File file = new File("contas.dat");
        file.setWritable(false);

        ContaAbstrata conta = new Conta("12345", 100.0);

        RepositorioException exception = assertThrows(RepositorioException.class, () -> {
            repositorio.inserir(conta); // O método inserir chama o método gravarArquivo()
        });

        assertTrue(exception.getCause() instanceof IOException);

        // Restaurar a permissão de escrita
        file.setWritable(true);
    }

    @Test
    public void testLerArquivoClassNotFoundException() throws IOException {
        // Simula um erro de ClassNotFoundException ao tentar ler o arquivo.

        File file = new File("contas.dat");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("corrupted content");
        }

        RepositorioException exception = assertThrows(RepositorioException.class, () -> {
            repositorio = new RepositorioContasArquivoBin(); // O construtor chama o método lerArquivo()
        });

        assertTrue(exception.getCause() instanceof ClassNotFoundException);
    }
    
  
    @Test
    public void testGravarArquivoFileNotFoundException() {
        // Simula um erro de FileNotFoundException ao tentar gravar no arquivo.
        File file = new File("contas.dat");
        file.setWritable(false);

        ContaAbstrata conta = new Conta("12345", 100.0);

        RepositorioException exception = assertThrows(RepositorioException.class, () -> {
            repositorio.inserir(conta); // O método inserir chama o método gravarArquivo()
        });

        assertTrue(exception.getCause() instanceof FileNotFoundException);

        // Restaurar a permissão de escrita
        file.setWritable(true);
    }
}
