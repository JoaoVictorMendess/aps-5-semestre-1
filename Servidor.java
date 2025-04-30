import java.io.*;
import java.net.*;

public class Servidor {
    public static void main(String[] args) {
        try {
            // Criando o servidor socket na porta 10001
            ServerSocket servidorSocket = new ServerSocket(10001);
            System.out.println("Servidor iniciado na porta 10001");
            System.out.println("Aguardando conexão do cliente...");
            
            // Aguardando conexão do cliente
            Socket clienteSocket = servidorSocket.accept();
            System.out.println("Cliente conectado: " + clienteSocket.getInetAddress().getHostAddress());
            
            // Criando streams de entrada e saída
            BufferedReader entrada = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
            PrintWriter saida = new PrintWriter(clienteSocket.getOutputStream(), true);
            
            // Criando um BufferedReader para ler do teclado
            BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
            
            String mensagemCliente;
            String mensagemServidor;
            
            // Loop de comunicação
            while (true) {
                // Recebendo mensagem do cliente
                mensagemCliente = entrada.readLine();
                if (mensagemCliente == null || mensagemCliente.equalsIgnoreCase("sair")) {
                    System.out.println("Cliente desconectou.");
                    break;
                }
                System.out.println("Cliente: " + mensagemCliente);
                
                // Enviando resposta para o cliente
                System.out.print("Servidor: ");
                mensagemServidor = teclado.readLine();
                saida.println(mensagemServidor);
                
                if (mensagemServidor.equalsIgnoreCase("sair")) {
                    System.out.println("Encerrando servidor...");
                    break;
                }
            }
            
            // Fechando recursos
            entrada.close();
            saida.close();
            teclado.close();
            clienteSocket.close();
            servidorSocket.close();
            
        } catch (IOException e) {
            System.out.println("Erro no servidor: " + e.getMessage());
        }
    }
}
