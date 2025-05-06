import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Base64;

public class ClienteMultiCI {
    public static void main(String[] args) {
        try {
            String servidorIP = "10.10.3.203";
            int porta = 10001;

            Socket socket = new Socket(servidorIP, porta);
            System.out.println("Conectado ao servidor: " + servidorIP + ":" + porta);

            PrintWriter saida = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Digite seu nome: ");
            String nome = teclado.readLine();
            saida.println("NOME:" + nome);

            new Thread(new MensagemReceptorCI(socket)).start();

            String mensagem;
            while (true) {
                mensagem = teclado.readLine();

                if (mensagem.startsWith("/imagem ")) {
                    String caminho = mensagem.substring(8);
                    try {
                        byte[] imagemBytes = Files.readAllBytes(Paths.get(caminho));
                        String base64 = Base64.getEncoder().encodeToString(imagemBytes);
                        saida.println("IMAGEM:" + base64);
                    } catch (IOException e) {
                        System.out.println("Erro ao ler imagem: " + e.getMessage());
                    }
                } else {
                    saida.println(mensagem);
                }

                if (mensagem.equalsIgnoreCase("sair")) break;
            }

            socket.close();
            System.exit(0);

        } catch (IOException e) {
            System.out.println("Erro no cliente: " + e.getMessage());
        }
    }

    static class MensagemReceptorCI implements Runnable {
        private Socket socket;
        private BufferedReader entrada;

        public MensagemReceptorCI(Socket socket) {
            this.socket = socket;
            try {
                this.entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                System.out.println("Erro no receptor: " + e.getMessage());
            }
        }

        @Override
        public void run() {
            try {
                String mensagemServidor;
                while ((mensagemServidor = entrada.readLine()) != null) {
                    if (mensagemServidor.startsWith("IMAGEMDATA:")) {
                        String[] partes = mensagemServidor.split(":", 3);
                        String remetente = partes[1];
                        String base64 = partes[2];

                        byte[] imagemBytes = Base64.getDecoder().decode(base64);
                        String nomeArquivo = "imagem_recebida_" + System.currentTimeMillis() + ".jpg";
                        Files.write(Paths.get(nomeArquivo), imagemBytes);
                        System.out.println(remetente + " enviou uma imagem. Salva como: " + nomeArquivo);
                    } else {
                        System.out.println(mensagemServidor);
                    }
                }
            } catch (IOException e) {
                if (!socket.isClosed()) {
                    System.out.println("Conexão perdida: " + e.getMessage());
                }
            }
        }
    }
}
