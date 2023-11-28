import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class Server {
    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 1235;
        byte [] senddata, receivedata;
        DatagramPacket outdata = null;
        DatagramPacket indata = null;
        DatagramSocket socket = new DatagramSocket(port);
        String diretorio = "C:/Users/Rodrigo/Downloads/";
        boolean isConnected = false;
        while (true){
            receivedata = new byte[10000];
            indata = new DatagramPacket(receivedata, receivedata.length);
            socket.receive(indata);
            InetAddress clientAddress = indata.getAddress();
            int clientPort = indata.getPort();
            String rawData = new String(indata.getData());
            String clientData = rawData.substring(0, (indata.getLength()));

            if (!isConnected){
                String resposta = "Olá cliente!";
                senddata = resposta.getBytes();
                outdata = new DatagramPacket(senddata, 0, senddata.length, clientAddress, clientPort);
                socket.send(outdata);
                isConnected = true;
            }

            if(clientData.contains("Arquivo")) {
                // TODO: transformar em função
                String[] nomeArquivo = (clientData.split(" "));
                diretorio = diretorio.concat(nomeArquivo[1]);
                final Path path = Paths.get(diretorio);
                long tamanhoArquivo = Files.size(path);
                String infosArquivo = "Tamanho: " + tamanhoArquivo + " KB e Pacotes: 10 KB";
                byte[] bytes = new byte[10000];
                bytes = infosArquivo.getBytes();
                senddata = bytes;
                outdata = new DatagramPacket(senddata, senddata.length, clientAddress, clientPort);
                socket.send(outdata);
                diretorio = "C:/Users/Rodrigo/Downloads/";
            }
        }

    }
}
