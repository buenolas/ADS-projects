package server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class PontoColeta implements Serializable {
    private static final long serialVersionUID = 1L;
    private static AtomicLong idCounter = new AtomicLong(3);
    private long id;
    private String nome;
    private String endereco;
    private List<String> tiposResiduos;

    public PontoColeta(String nome, String endereco, List<String> tiposResiduos) {
        this.id = idCounter.incrementAndGet();
        this.nome = nome;
        this.endereco = endereco;
        this.tiposResiduos = (tiposResiduos != null) ? new ArrayList<>(tiposResiduos) : new ArrayList<>();
    }

    // Construtor para atualização, onde o ID é fornecido
    public PontoColeta(long id, String nome, String endereco, List<String> tiposResiduos) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.tiposResiduos = (tiposResiduos != null) ? new ArrayList<>(tiposResiduos) : new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public List<String> getTiposResiduos() {
        return tiposResiduos;
    }

    public void setTiposResiduos(List<String> tiposResiduos) {
        this.tiposResiduos = (tiposResiduos != null) ? new ArrayList<>(tiposResiduos) : new ArrayList<>();
    }

    public boolean aceitaResiduo(String tipo) {
        return tiposResiduos.stream().anyMatch(t -> t.equalsIgnoreCase(tipo));
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Nome: " + nome + ", Endereço: " + endereco + ", Tipos de Resíduos: " + String.join(", ", tiposResiduos);
    }

    // Método para resetar o contador de IDs, útil para testes ou reinícios do servidor
    public static void resetIdCounter() {
        idCounter.set(0);
    }
}

