import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
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
        String ackArquivo = "";
        String hash = "";
        String hashOriginal = "";
        String rawData = "";
        String serverResponse = "";
        Boolean perdeuPacote = false;

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
            rawData = new String(indata.getData());
            serverResponse = rawData.substring(0, (indata.getLength()));
            System.out.println(serverResponse);

            if (serverResponse.contains("Tamanho")){
                String[] infos = (serverResponse.split(" "));
                // System.out.println(Arrays.toString(infos));
                int tamanhoArquivo = (int) Long.parseLong(infos[1]);
                int tamanhoPacote = Integer.parseInt(infos[5]);
                int tamanhoCabecalho = Integer.parseInt(infos[10]);
                int numeroPacotes = 0;
                int destPos = 0;
                // TODO: tratar separadamente o envio dos pacotes
                receivedata = new byte[tamanhoPacote];
                byte [] fileComplete = new byte[(int) tamanhoArquivo];
                while(tamanhoArquivo > 0) {
                    indata = new DatagramPacket(receivedata, receivedata.length);
                    ds.receive(indata);
                    String cabecalho = new String(indata.getData()).substring(0,tamanhoCabecalho);
                    System.out.println(cabecalho);
                    if (cabecalho.contains("1") && !perdeuPacote){
                        ackArquivo = "";
                        perdeuPacote = true;
                    }
                    else{
                        System.arraycopy(indata.getData(), tamanhoCabecalho, fileComplete, destPos, (Math.min(tamanhoArquivo, (tamanhoPacote - tamanhoCabecalho))));
                        ackArquivo = "ACK " + numeroPacotes;
                        numeroPacotes++;
                        destPos += tamanhoPacote - tamanhoCabecalho;
                        tamanhoArquivo -= tamanhoPacote - tamanhoCabecalho;
                    }
                    senddata = ackArquivo.getBytes();
                    outdata = new DatagramPacket(senddata, senddata.length, address, port);
                    ds.send(outdata);
                }
                Path path = Paths.get("C:/Users/Rodrigo/Downloads/UDP-/novoArquivo4.png");
                Files.write(path, fileComplete);
                hash = stringHexa(Objects.requireNonNull(gerarHash(Arrays.toString(fileComplete), "SHA-256")));
            }

            if (serverResponse.contains("não existe")){
                nomeArquivo = "";
            }

            if(serverResponse.contains("Checksum")){
                String[] checksum = serverResponse.split(" ");
                hashOriginal = checksum[1];
                System.out.println(hashOriginal);
                System.out.println(hash);
                if(hashOriginal.equalsIgnoreCase(hash)){
                    System.out.println("Arquivo igual!");
                }
                else{
                    System.out.println("Houve perdas");
                }
                nomeArquivo = "";
            }

            if (nomeArquivo.equalsIgnoreCase("sair")){
                break;
            }

            if (nomeArquivo.isEmpty()){
                // envia o nome do arquivo
                System.out.println("Insira a palavra Arquivo e logo em seguida, o nome do arquivo ou digite 'Sair' para sair:");
                nomeArquivo = sc.nextLine();
                senddata = nomeArquivo.getBytes();
                outdata = new DatagramPacket(senddata, senddata.length, address, port);
                ds.send(outdata);
            }
        }
        ds.close();
    }

    public static byte[] gerarHash(String frase, String algoritmo) {
        try {
            MessageDigest md = MessageDigest.getInstance(algoritmo);
            md.update(frase.getBytes());
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private static String stringHexa(byte[] bytes) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            int parteAlta = ((bytes[i] >> 4) & 0xf) << 4;
            int parteBaixa = bytes[i] & 0xf;
            if (parteAlta == 0) s.append('0');
            s.append(Integer.toHexString(parteAlta | parteBaixa));
        }
        return s.toString();
    }
}
