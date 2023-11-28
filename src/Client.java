import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Client {
    public static void main(String[] args) throws IOException {
        int port = 1235; // porta que o servidor mandará os dados
        Scanner sc = new Scanner(System.in);
        DatagramSocket ds = new DatagramSocket(); // socket para enviar ou receber pacotes
        DatagramPacket outdata = null; // envia pacotes para o servidor
        DatagramPacket indata = null; // recebe pacotes do servidor
        byte[] senddata, receivedata;
        String nomeArquivo = "";
        InetAddress address = InetAddress.getLocalHost();

        // envia o endereço e porta do cliente para o servidor
        String ola = "Endereço:" + address + ", Porta:" + port;
        senddata = ola.getBytes();
        outdata = new DatagramPacket(senddata, senddata.length, address, port);
        ds.send(outdata);

        while (true){
            // recebe resposta do servidor
            receivedata = new byte[10000];
            indata = new DatagramPacket(receivedata, receivedata.length);
            ds.receive(indata);
            String rawData = new String(indata.getData());
            String serverResponse = rawData.substring(0, (indata.getLength()));
            System.out.println(serverResponse);

            if (nomeArquivo.isEmpty()){
                // envia o nome do arquivo
                System.out.println("Insira a palavra Arquivo e logo em seguida, o nome do arquivo ou digite 'a' para sair:");
                nomeArquivo = sc.nextLine();
                senddata = nomeArquivo.getBytes();
                outdata = new DatagramPacket(senddata, senddata.length, address, port);
                ds.send(outdata);
            }

            if (serverResponse.contains("Tamanho")){
                String[] infos = (serverResponse.split(" "));
                System.out.println(infos[1]);
            }

            if (nomeArquivo.equalsIgnoreCase("a")){
                break;
            }
        }
        ds.close();
    }
}
