import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ServidorMultiCliente {
    private static final int PORTA = 10001;
    private static Set<ClienteHandler> clientesConectados = ConcurrentHashMap.newKeySet();
    
    public static void main(String[] args) {
        try {
            ServerSocket servidorSocket = new ServerSocket(PORTA);
            System.out.println("Servidor iniciado na porta " + PORTA);
            System.out.println("Aguardando conexões...");
            
            while (true) {
                Socket clienteSocket = servidorSocket.accept();
                System.out.println("Novo cliente conectado: " + clienteSocket.getInetAddress().getHostAddress());
                
                // Criar um handler para o cliente
                ClienteHandler clienteHandler = new ClienteHandler(clienteSocket);
                clientesConectados.add(clienteHandler);
                
                // Iniciar uma thread para cuidar deste cliente
                new Thread(clienteHandler).start();
            }
        } catch (IOException e) {
            System.out.println("Erro no servidor: " + e.getMessage());
        }
    }
    
    // Método para enviar mensagem para todos os clientes (broadcast)
    public static void broadcast(String mensagem, ClienteHandler remetente) {
        for (ClienteHandler cliente : clientesConectados) {
            if (cliente != remetente) { // Não enviamos de volta para o remetente
                cliente.enviarMensagem("Cliente-" + remetente.getClienteId() + ": " + mensagem);
            }
        }
    }
    
    // Handler de cliente em uma thread separada
    static class ClienteHandler implements Runnable {
        private Socket clienteSocket;
        private BufferedReader entrada;
        private PrintWriter saida;
        private static int contadorClientes = 0;
        private int clienteId;
        
        public ClienteHandler(Socket socket) {
            this.clienteSocket = socket;
            this.clienteId = ++contadorClientes; // ID único para cada cliente
            
            try {
                this.entrada = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
                this.saida = new PrintWriter(clienteSocket.getOutputStream(), true);
            } catch (IOException e) {
                System.out.println("Erro ao configurar streams para cliente: " + e.getMessage());
            }
        }
        
        public int getClienteId() {
            return clienteId;
        }
        
        public void enviarMensagem(String mensagem) {
            saida.println(mensagem);
        }
        
        @Override
        public void run() {
            try {
                // Informando ao cliente seu ID
                saida.println("Você está conectado como Cliente-" + clienteId);
                
                // Notificando outros clientes sobre esta nova conexão
                broadcast("Cliente-" + clienteId + " entrou no chat!", this);
                
                String mensagem;
                while ((mensagem = entrada.readLine()) != null) {
                    // Se o cliente enviar "sair", encerramos a conexão
                    if (mensagem.equalsIgnoreCase("sair")) {
                        break;
                    }
                    
                    // Exibir mensagem no console do servidor
                    System.out.println("Cliente-" + clienteId + ": " + mensagem);
                    
                    // Enviar a mensagem para todos os outros clientes
                    broadcast(mensagem, this);
                }
            } catch (IOException e) {
                System.out.println("Erro na comunicação com Cliente-" + clienteId + ": " + e.getMessage());
            } finally {
                try {
                    // Fechar recursos
                    if (entrada != null) entrada.close();
                    if (saida != null) saida.close();
                    if (clienteSocket != null) clienteSocket.close();
                    
                    // Remover cliente da lista de conectados
                    clientesConectados.remove(this);
                    
                    // Notificar outros que este cliente saiu
                    broadcast("Cliente-" + clienteId + " saiu do chat!", this);
                    System.out.println("Cliente-" + clienteId + " desconectado.");
                } catch (IOException e) {
                    System.out.println("Erro ao fechar conexão: " + e.getMessage());
                }
            }
        }
    }
}