import java.io.*;
import java.net.*;

public class Cliente {
    public static void main(String[] args) {
        try {
            // Endereço IP do servidor
            String servidorIP = "10.10.3.203"; // Altere para o IP do servidor se estiver em outra máquina
            
            // Conectando ao servidor na porta 12345
            Socket socket = new Socket(servidorIP, 10001);
            System.out.println("Conectado ao servidor: " + servidorIP);
            
            // Criando streams de entrada e saída
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter saida = new PrintWriter(socket.getOutputStream(), true);
            
            // Criando um BufferedReader para ler do teclado
            BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
            
            String mensagemServidor;
            String mensagemCliente;
            
            // Loop de comunicação
            while (true) {
                // Enviando mensagem para o servidor
                System.out.print("Cliente: ");
                mensagemCliente = teclado.readLine();
                saida.println(mensagemCliente);
                
                if (mensagemCliente.equalsIgnoreCase("sair")) {
                    System.out.println("Encerrando cliente...");
                    break;
                }
                
                // Recebendo resposta do servidor
                mensagemServidor = entrada.readLine();
                if (mensagemServidor == null) {
                    System.out.println("Servidor desconectou.");
                    break;
                }
                System.out.println("Servidor: " + mensagemServidor);
                
                if (mensagemServidor.equalsIgnoreCase("sair")) {
                    System.out.println("Servidor encerrou a conexão.");
                    break;
                }
            }
            
            // Fechando recursos
            entrada.close();
            saida.close();
            teclado.close();
            socket.close();
            
        } catch (IOException e) {
            System.out.println("Erro no cliente: " + e.getMessage());
        }
    }
}