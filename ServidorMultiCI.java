import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.Base64;

public class ServidorMultiCI {
    private static final int PORTA = 10001;
    private static Set<ClienteHandlerCI> clientesConectados = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        try {
            ServerSocket servidorSocket = new ServerSocket(PORTA);
            System.out.println("Servidor iniciado na porta " + PORTA);
            System.out.println("Aguardando conexões...");

            while (true) {
                Socket clienteSocket = servidorSocket.accept();
                System.out.println("Novo cliente conectado: " + clienteSocket.getInetAddress().getHostAddress());

                ClienteHandlerCI clienteHandler = new ClienteHandlerCI(clienteSocket);
                clientesConectados.add(clienteHandler);
                new Thread(clienteHandler).start();
            }
        } catch (IOException e) {
            System.out.println("Erro no servidor: " + e.getMessage());
        }
    }

    public static void broadcast(String mensagem, ClienteHandlerCI remetente) {
        for (ClienteHandlerCI cliente : clientesConectados) {
            if (cliente != remetente) {
                cliente.enviarMensagem(mensagem);
            }
        }
    }

    static class ClienteHandlerCI implements Runnable {
        private Socket clienteSocket;
        private BufferedReader entrada;
        private PrintWriter saida;
        private static int contadorClientes = 0;
        private int clienteId;
        private String nome = "Cliente";

        public ClienteHandlerCI(Socket socket) {
            this.clienteSocket = socket;
            this.clienteId = ++contadorClientes;

            try {
                this.entrada = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
                this.saida = new PrintWriter(clienteSocket.getOutputStream(), true);
            } catch (IOException e) {
                System.out.println("Erro ao configurar streams: " + e.getMessage());
            }
        }

        public void enviarMensagem(String mensagem) {
            saida.println(mensagem);
        }

        @Override
        public void run() {
            try {
                String linha = entrada.readLine();
                if (linha != null && linha.startsWith("NOME:")) {
                    nome = linha.substring(5).trim();
                } else {
                    nome += "-" + clienteId;
                }

                saida.println("Você está conectado como " + nome);
                ServidorMultiCI.broadcast(nome + " entrou no chat!", this);

                String mensagem;
                while ((mensagem = entrada.readLine()) != null) {
                    if (mensagem.equalsIgnoreCase("sair")) break;

                    if (mensagem.startsWith("IMAGEM:")) {
                        ServidorMultiCI.broadcast(nome + " enviou uma imagem.", this);
                        ServidorMultiCI.broadcast("IMAGEMDATA:" + nome + ":" + mensagem.substring(7), this);
                    } else {
                        System.out.println(nome + ": " + mensagem);
                        ServidorMultiCI.broadcast(nome + ": " + mensagem, this);
                    }
                }
            } catch (IOException e) {
                System.out.println("Erro com " + nome + ": " + e.getMessage());
            } finally {
                try {
                    entrada.close();
                    saida.close();
                    clienteSocket.close();
                    clientesConectados.remove(this);
                    ServidorMultiCI.broadcast(nome + " saiu do chat!", this);
                    System.out.println(nome + " desconectado.");
                } catch (IOException e) {
                    System.out.println("Erro ao fechar conexão: " + e.getMessage());
                }
            }
        }
    }
}
