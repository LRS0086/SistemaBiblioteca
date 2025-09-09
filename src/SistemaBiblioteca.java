import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// Classe Livro
class Livro {
    private String titulo;
    private String autor;
    private boolean disponivel;

    public Livro(String titulo, String autor) {
        this.titulo = titulo;
        this.autor = autor;
        this.disponivel = true;
    }

    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public boolean isDisponivel() { return disponivel; }
    public void setDisponivel(boolean disponivel) { this.disponivel = disponivel; }

    @Override
    public String toString() {
        return titulo + " - " + autor + (disponivel ? " (Disponível)" : " (Emprestado)");
    }
}

// Classe Usuário (abstrata)
abstract class Usuario {
    protected String nome;
    protected int diasSuspensao;

    public Usuario(String nome) {
        this.nome = nome;
        this.diasSuspensao = 0;
    }

    public String getNome() { return nome; }
    public int getDiasSuspensao() { return diasSuspensao; }
    public void setDiasSuspensao(int diasSuspensao) { this.diasSuspensao = diasSuspensao; }

    public abstract int getPrazoEmprestimo();
}

// Aluno
class Aluno extends Usuario {
    public Aluno(String nome) { super(nome); }
    @Override
    public int getPrazoEmprestimo() { return 10; } // 10 dias úteis
}

// Professor
class Professor extends Usuario {
    public Professor(String nome) { super(nome); }
    @Override
    public int getPrazoEmprestimo() { return 20; } // 20 dias úteis
}

// Classe Empréstimo
class Emprestimo {
    private Usuario usuario;
    private Livro livro;
    private LocalDate dataEmprestimo;
    private LocalDate dataDevolucao;

    public Emprestimo(Usuario usuario, Livro livro) {
        this.usuario = usuario;
        this.livro = livro;
        this.dataEmprestimo = LocalDate.now();
        this.dataDevolucao = dataEmprestimo.plusDays(usuario.getPrazoEmprestimo());
        livro.setDisponivel(false);
    }

    public Usuario getUsuario() { return usuario; }
    public Livro getLivro() { return livro; }
    public LocalDate getDataDevolucao() { return dataDevolucao; }

    public boolean verificarAtraso() {
        LocalDate hoje = LocalDate.now();
        if (hoje.isAfter(dataDevolucao)) {
            long diasAtraso = ChronoUnit.DAYS.between(dataDevolucao, hoje);
            usuario.setDiasSuspensao(usuario.getDiasSuspensao() + (int)diasAtraso);
            return true;
        }
        return false;
    }
}

// Classe principal do sistema
public class SistemaBiblioteca {
    private static List<Usuario> usuarios = new ArrayList<>();
    private static List<Livro> livros = new ArrayList<>();
    private static List<Emprestimo> emprestimos = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean sair = false;

        // Exemplo de livros iniciais
        livros.add(new Livro("O Senhor dos Anéis", "J.R.R. Tolkien"));
        livros.add(new Livro("Harry Potter", "J.K. Rowling"));

        while(!sair) {
            System.out.println("\n--- Sistema de Biblioteca Escolar ---");
            System.out.println("1. Cadastrar usuário");
            System.out.println("2. Listar usuários");
            System.out.println("3. Listar livros");
            System.out.println("4. Realizar empréstimo");
            System.out.println("5. Devolver livro");
            System.out.println("6. Verificar atrasos");
            System.out.println("0. Sair");
            System.out.print("Escolha uma opção: ");
            int opcao = scanner.nextInt();
            scanner.nextLine(); // Consumir \n

            switch(opcao) {
                case 1: cadastrarUsuario(); break;
                case 2: listarUsuarios(); break;
                case 3: listarLivros(); break;
                case 4: realizarEmprestimo(); break;
                case 5: devolverLivro(); break;
                case 6: verificarAtrasos(); break;
                case 0: sair = true; break;
                default: System.out.println("Opção inválida!"); break;
            }
        }
        System.out.println("Encerrando sistema...");
    }

    private static void cadastrarUsuario() {
        System.out.print("Nome do usuário: ");
        String nome = scanner.nextLine();
        System.out.print("Tipo (1-Aluno, 2-Professor): ");
        int tipo = scanner.nextInt();
        scanner.nextLine();
        if (tipo == 1) usuarios.add(new Aluno(nome));
        else if (tipo == 2) usuarios.add(new Professor(nome));
        else System.out.println("Tipo inválido!");
    }

    private static void listarUsuarios() {
        System.out.println("\n--- Usuários ---");
        for (Usuario u : usuarios) {
            System.out.println(u.getNome() + " | Dias de suspensão: " + u.getDiasSuspensao());
        }
    }

    private static void listarLivros() {
        System.out.println("\n--- Livros ---");
        for (Livro l : livros) {
            System.out.println(l);
        }
    }

    private static void realizarEmprestimo() {
        System.out.print("Nome do usuário: ");
        String nome = scanner.nextLine();
        Usuario usuario = usuarios.stream().filter(u -> u.getNome().equalsIgnoreCase(nome)).findFirst().orElse(null);
        if (usuario == null) { System.out.println("Usuário não encontrado!"); return; }
        if (usuario.getDiasSuspensao() > 0) { System.out.println("Usuário está suspenso por " + usuario.getDiasSuspensao() + " dias."); return; }

        System.out.print("Título do livro: ");
        String titulo = scanner.nextLine();
        Livro livro = livros.stream().filter(l -> l.getTitulo().equalsIgnoreCase(titulo) && l.isDisponivel()).findFirst().orElse(null);
        if (livro == null) { System.out.println("Livro não disponível!"); return; }

        Emprestimo emprestimo = new Emprestimo(usuario, livro);
        emprestimos.add(emprestimo);
        System.out.println("Empréstimo realizado. Devolver até: " + emprestimo.getDataDevolucao());
    }

    private static void devolverLivro() {
        System.out.print("Nome do usuário: ");
        String nome = scanner.nextLine();
        Emprestimo emprestimo = emprestimos.stream()
                .filter(e -> e.getUsuario().getNome().equalsIgnoreCase(nome))
                .findFirst().orElse(null);
        if (emprestimo == null) { System.out.println("Nenhum empréstimo encontrado!"); return; }

        emprestimo.getLivro().setDisponivel(true);
        if (emprestimo.verificarAtraso()) {
            System.out.println("Livro devolvido com atraso. Suspensão aplicada: " + emprestimo.getUsuario().getDiasSuspensao() + " dias.");
        } else {
            System.out.println("Livro devolvido no prazo!");
        }
        emprestimos.remove(emprestimo);
    }

    private static void verificarAtrasos() {
        System.out.println("\n--- Verificação de atrasos ---");
        for (Emprestimo e : emprestimos) {
            if (e.verificarAtraso()) {
                System.out.println(e.getUsuario().getNome() + " atrasou a devolução de " + e.getLivro().getTitulo() +
                        ". Suspensão total: " + e.getUsuario().getDiasSuspensao() + " dias.");
            }
        }
        System.out.println("Verificação concluída.");
    }
}