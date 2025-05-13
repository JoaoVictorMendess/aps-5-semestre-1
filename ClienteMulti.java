import java.io.*;
import java.net.*;

public class ClienteMulti {
    public static void main(String[] args) {
        try {
            // Endereço IP do servidor
            String servidorIP = "10.10.3.203"; // Altere para o IP do servidor se estiver em outra máquina
            int porta = 10001;
            
            // Conectando ao servidor
            Socket socket = new Socket(servidorIP, porta);
            System.out.println("Conectado ao servidor: " + servidorIP + ":" + porta);
            
            // Thread para receber mensagens do servidor continuamente
            Thread receptorThread = new Thread(new MensagemReceptor(socket));
            receptorThread.start();
            
            // Enviando mensagens para o servidor
            PrintWriter saida = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
            
            String mensagem;
            while (true) {
                mensagem = teclado.readLine();
                saida.println(mensagem);
                
                if (mensagem.equalsIgnoreCase("sair")) {
                    break;
                }
            }
            
            // Encerrando recursos
            socket.close();
            System.exit(0);
            
        } catch (IOException e) {
            System.out.println("Erro no cliente: " + e.getMessage());
        }
    }
    
    // Classe para receber mensagens do servidor continuamente
    static class MensagemReceptor implements Runnable {
        private Socket socket;
        private BufferedReader entrada;
        
        public MensagemReceptor(Socket socket) {
            this.socket = socket;
            try {
                this.entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                System.out.println("Erro ao criar receptor de mensagens: " + e.getMessage());
            }
        }
        
        @Override
        public void run() {
            try {
                String mensagemServidor;
                while ((mensagemServidor = entrada.readLine()) != null) {
                    System.out.println(mensagemServidor);
                }
            } catch (IOException e) {
                if (!socket.isClosed()) {
                    System.out.println("Conexão com o servidor perdida: " + e.getMessage());
                }
            }
        }
    }
}