/*
 * ADS 471 - Fernanda Cristina Oliveira Pinheiro
 * Letícia Amaral Xavier
 *
 * TRABALHO PRÁTICO 04
 *
 * Desenvolver uma interface gráfica em Java para consultar
 * funcionários cadastrados em um banco SQL Server.
 *
 * A aplicação utiliza JDBC, PreparedStatement e ResultSet
 * para pesquisar funcionários pelo nome e navegar entre
 * os registros utilizando os botões Anterior e Próximo.
 */

package trabalhopratico04;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Locale;

public class TelaFuncionarios extends JFrame {
    private final JTextField txtPesquisa = new JTextField(23);
    private final JTextField txtNome = new JTextField(22);
    private final JTextField txtSalario = new JTextField(22);
    private final JTextField txtCargo = new JTextField(22);
    private final JButton btnPesquisar = new JButton("Pesquisar");
    private final JButton btnAnterior = new JButton("Anterior");
    private final JButton btnProximo = new JButton("Próximo");
    private final JLabel lblStatus = new JLabel("Digite um nome ou parte dele e clique em Pesquisar.");

    private Connection conexao;
    private PreparedStatement comando;
    private ResultSet registros;
    private int posicao = 0;
    private int total = 0;

    public TelaFuncionarios() {
        super("TRABALHO PRÁTICO 04");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createEmptyBorder(12, 14, 4, 14));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 5, 4, 5);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx=0;g.gridy=0; painel.add(new JLabel("Nome:"),g);
        g.gridx=1;g.weightx=1; painel.add(txtPesquisa,g);
        g.gridx=1;g.gridy=1;g.weightx=0;g.fill=GridBagConstraints.NONE;g.anchor=GridBagConstraints.CENTER;
        painel.add(btnPesquisar,g);

        g.fill=GridBagConstraints.HORIZONTAL;g.anchor=GridBagConstraints.WEST;
        addCampo(painel,g,2,"Nome:",txtNome);
        addCampo(painel,g,3,"Salário:",txtSalario);
        addCampo(painel,g,4,"Cargo:",txtCargo);
        for (JTextField campo : new JTextField[]{txtNome,txtSalario,txtCargo}) {
            campo.setEditable(false);
            campo.setBackground(Color.WHITE);
        }
        add(painel,BorderLayout.CENTER);

        JPanel rodape = new JPanel(new BorderLayout(0, 5));
        JPanel botoes = new JPanel(new GridLayout(1,2,0,0));
        botoes.add(btnAnterior); botoes.add(btnProximo);
        rodape.add(botoes,BorderLayout.NORTH);
        lblStatus.setBorder(BorderFactory.createEmptyBorder(4,8,6,8));
        rodape.add(lblStatus,BorderLayout.SOUTH);
        add(rodape,BorderLayout.SOUTH);

        btnPesquisar.addActionListener(e -> pesquisar());
        txtPesquisa.addActionListener(e -> pesquisar());
        btnAnterior.addActionListener(e -> navegar(-1));
        btnProximo.addActionListener(e -> navegar(1));
        atualizarBotoes();
        pack(); setMinimumSize(new Dimension(440, 270));
        setLocationRelativeTo(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { fecharRecursos(); }
        });
    }

    private void addCampo(JPanel p, GridBagConstraints g, int linha, String rotulo, JTextField campo) {
        g.gridy=linha;g.gridx=0;g.weightx=0;p.add(new JLabel(rotulo),g);
        g.gridx=1;g.weightx=1;p.add(campo,g);
    }

    private Connection conectar() throws SQLException {
        String servidor = System.getenv().getOrDefault("SQL_SERVER_HOST", "localhost");
        String porta = System.getenv().getOrDefault("SQL_SERVER_PORT", "1433");
        String usuario = System.getenv().getOrDefault("SQL_SERVER_USER", "sa");
        String senha = System.getenv("SQL_SERVER_PASSWORD");
        if (senha == null || senha.isBlank()) {
            throw new SQLException("Configure a variável SQL_SERVER_PASSWORD antes de executar. Veja o LEIA-ME.");
        }
        String url = "jdbc:sqlserver://" + servidor + ":" + porta
                + ";databaseName=aulajava;encrypt=true;trustServerCertificate=true;loginTimeout=8";
        return DriverManager.getConnection(url, usuario, senha);
    }

    private void pesquisar() {
        fecharRecursos();
        limparCampos();
        try {
            conexao = conectar();
            String sql = "SELECT f.nome_func, f.sal_func, c.ds_cargo "
                    + "FROM dbo.tb_func f INNER JOIN dbo.tb_cargo c ON f.cod_cargo = c.id_cargo "
                    + "WHERE f.nome_func LIKE ? ORDER BY f.nome_func, f.cod_func";
            comando = conexao.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            String termo = txtPesquisa.getText().trim();
            comando.setString(1, "%" + termo + "%");
            registros = comando.executeQuery();
            registros.last();
            total = registros.getRow();
            if (total > 0) {
                registros.first();
                posicao = 1;
                mostrarRegistro();
            } else {
                lblStatus.setText("Nenhum funcionário encontrado.");
            }
            atualizarBotoes();
        } catch (SQLException ex) {
            fecharRecursos();
            lblStatus.setText("Erro na consulta ou conexão.");
            JOptionPane.showMessageDialog(this,
                    "Não foi possível consultar o SQL Server:\n" + ex.getMessage()
                    + "\n\nConfira o LEIA-ME: serviço, TCP/IP, porta, usuário e senha.",
                    "Erro de banco de dados", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void navegar(int direcao) {
        if (registros == null || total == 0) return;
        int destino = posicao + direcao;
        if (destino < 1 || destino > total) return;
        try {
            if (registros.absolute(destino)) {
                posicao = destino;
                mostrarRegistro();
                atualizarBotoes();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro ao navegar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarRegistro() throws SQLException {
        txtNome.setText(registros.getString("nome_func"));
        BigDecimal salario = registros.getBigDecimal("sal_func");
        txtSalario.setText(String.format(Locale.US,"%.2f",salario));
        txtCargo.setText(registros.getString("ds_cargo"));
        lblStatus.setText("Registro " + posicao + " de " + total);
    }

    private void limparCampos() {
        txtNome.setText("");txtSalario.setText("");txtCargo.setText("");
        posicao=0;total=0;atualizarBotoes();
    }

    private void atualizarBotoes() {
        btnAnterior.setEnabled(posicao > 1);
        btnProximo.setEnabled(posicao > 0 && posicao < total);
    }

    private void fecharRecursos() {
        try { if (registros != null) registros.close(); } catch(SQLException ignored) {}
        try { if (comando != null) comando.close(); } catch(SQLException ignored) {}
        try { if (conexao != null) conexao.close(); } catch(SQLException ignored) {}
        registros=null;comando=null;conexao=null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TelaFuncionarios().setVisible(true));
    }
}