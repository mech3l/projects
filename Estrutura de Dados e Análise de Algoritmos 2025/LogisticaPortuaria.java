import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LogisticaPortuaria extends JFrame {

    private JTextField volumesField;
    private JTextField capacidadeArmazemField;
    private JTextField capacidadeCaminhaoField;
    private JTextArea resultadoArea;
    private GraficoArmazemPanel graficoPanel;

    public LogisticaPortuaria() {
        setTitle("Logística Portuária");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));

        inputPanel.add(new JLabel("Volumes dos containers (separados por vírgula):"));
        volumesField = new JTextField("50,30,20,60,80,55,45");
        inputPanel.add(volumesField);

        inputPanel.add(new JLabel("Capacidade do armazém:"));
        capacidadeArmazemField = new JTextField("100");
        inputPanel.add(capacidadeArmazemField);

        inputPanel.add(new JLabel("Capacidade do caminhão:"));
        capacidadeCaminhaoField = new JTextField("150");
        inputPanel.add(capacidadeCaminhaoField);

        JButton calcularButton = new JButton("Calcular");
        inputPanel.add(calcularButton);

        add(inputPanel, BorderLayout.NORTH);

        resultadoArea = new JTextArea();
        resultadoArea.setEditable(false);
        add(new JScrollPane(resultadoArea), BorderLayout.CENTER);

        graficoPanel = new GraficoArmazemPanel();
        graficoPanel.setPreferredSize(new Dimension(800, 200));
        add(graficoPanel, BorderLayout.SOUTH);

        calcularButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                realizarCalculos();
            }
        });
    }

    private void realizarCalculos() {
        try {
            String[] partes = volumesField.getText().split(",");
            int[] volumes = Arrays.stream(partes).mapToInt(s -> Integer.parseInt(s.trim())).toArray();
            int capacidadeArmazem = Integer.parseInt(capacidadeArmazemField.getText().trim());
            int capacidadeCaminhao = Integer.parseInt(capacidadeCaminhaoField.getText().trim());

            List<List<Integer>> armazens = alocarEmArmazens(volumes, capacidadeArmazem);

            int[] volumesArmazenados = armazens.stream()
                    .mapToInt(lista -> lista.stream().mapToInt(Integer::intValue).sum())
                    .toArray();

            int volumeTotalArmazenado = Arrays.stream(volumesArmazenados).sum();
            int numCaminhoes = (volumeTotalArmazenado + capacidadeCaminhao - 1) / capacidadeCaminhao;

            StringBuilder sb = new StringBuilder();
            sb.append("Volumes dos containers: ").append(Arrays.toString(volumes)).append("\n");
            sb.append("Capacidade de cada armazém: ").append(capacidadeArmazem).append("\n");
            sb.append("Capacidade de cada caminhão: ").append(capacidadeCaminhao).append("\n\n");
            sb.append("Número mínimo de armazéns: ").append(armazens.size()).append("\n");
            sb.append("Número mínimo de caminhões: ").append(numCaminhoes).append("\n\n");

            for (int i = 0; i < armazens.size(); i++) {
                sb.append("Armazém ").append(i + 1).append(": ")
                        .append(armazens.get(i)).append("\n");
            }

            resultadoArea.setText(sb.toString());
            graficoPanel.setData(armazens, capacidadeArmazem);

        } catch (NumberFormatException ex) {
            resultadoArea.setText("Erro: Verifique se os campos estão preenchidos corretamente.");
        }
    }

    private List<List<Integer>> alocarEmArmazens(int[] volumes, int capacidadeArmazem) {
        Integer[] volumesDesc = Arrays.stream(volumes).boxed().toArray(Integer[]::new);
        Arrays.sort(volumesDesc, Collections.reverseOrder());

        List<List<Integer>> armazens = new ArrayList<>();
        List<Integer> capacidades = new ArrayList<>();

        for (int volume : volumesDesc) {
            boolean alocado = false;
            for (int i = 0; i < armazens.size(); i++) {
                if (capacidades.get(i) + volume <= capacidadeArmazem) {
                    armazens.get(i).add(volume);
                    capacidades.set(i, capacidades.get(i) + volume);
                    alocado = true;
                    break;
                }
            }
            if (!alocado) {
                List<Integer> novoArmazem = new ArrayList<>();
                novoArmazem.add(volume);
                armazens.add(novoArmazem);
                capacidades.add(volume);
            }
        }
        return armazens;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LogisticaPortuaria().setVisible(true));
    }

    // Classe interna para exibir graficamente os armazéns
    static class GraficoArmazemPanel extends JPanel {
        private List<List<Integer>> armazens;
        private int capacidadeMaxima;

        public void setData(List<List<Integer>> armazens, int capacidadeMaxima) {
            this.armazens = armazens;
            this.capacidadeMaxima = capacidadeMaxima;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (armazens == null || armazens.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g;
            int larguraArmazem = 100;
            int espacamento = 20;
            int alturaPainel = getHeight();
            int x = espacamento;

            for (List<Integer> armazem : armazens) {
                int y = alturaPainel;
                for (int volume : armazem) {
                    int altura = (int) ((double) volume / capacidadeMaxima * alturaPainel);
                    y -= altura;
                    g2.setColor(new Color(100 + volume * 2 % 155, 100, 255 - volume * 2 % 155));
                    g2.fillRect(x, y, larguraArmazem, altura);
                    g2.setColor(Color.BLACK);
                    g2.drawRect(x, y, larguraArmazem, altura);
                }
                x += larguraArmazem + espacamento;
            }
        }
    }
}

