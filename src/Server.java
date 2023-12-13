import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

public class Server {
    static int port = 1235;
    static byte [] senddata;
    static byte [] receivedata;
    static DatagramPacket outdata = null;
    static DatagramPacket indata = null;
    static DatagramSocket socket;

    static {
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    static String diretorio = "C:/Users/Rodrigo/Downloads/";
    static boolean isConnected = false;
    public static void main(String[] args) throws IOException, InterruptedException {
        String hash = "";
        String resposta = "";
        while (true){
            receivedata = new byte[10000];
            indata = new DatagramPacket(receivedata, receivedata.length);
            socket.receive(indata);
            InetAddress clientAddress = indata.getAddress();
            int clientPort = indata.getPort();
            String rawData = new String(indata.getData());
            String clientData = rawData.substring(0, (indata.getLength()));
            System.out.println(clientData);

            if (clientData.contains("Endereço")){
                resposta = "Olá cliente!";
                senddata = resposta.getBytes();
                outdata = new DatagramPacket(senddata, 0, senddata.length, clientAddress, clientPort);
                socket.send(outdata);
            }

            if(clientData.contains("Arquivo")) {
                // TODO: transformar em função
                String[] nomeArquivo = clientData.split(" ", 2);
                diretorio = diretorio.concat(nomeArquivo[1]);
                File f = new File(diretorio);
                if(!f.exists()) {
                    resposta = "Arquivo não existe, digite um arquivo válido!";
                    System.out.println(resposta.length());
                    senddata = resposta.getBytes();
                    outdata = new DatagramPacket(senddata, senddata.length, clientAddress, clientPort);
                    socket.send(outdata);
                    continue;
                }
                final Path path = Paths.get(diretorio);
                int numeroPacotes = 0;
                int srcPos = 0;
                int tamanhoArquivo = (int) Files.size(path);
                String infosArquivo2 = "Pacote " + numeroPacotes;
                int tamanhoPacote = 10000 + infosArquivo2.length();
                String infosArquivo = "Tamanho: " + tamanhoArquivo + " bytes e Pacotes: " + tamanhoPacote + " bytes com cabeçalho de " + infosArquivo2.length() + " bytes";
                senddata = infosArquivo.getBytes();
                outdata = new DatagramPacket(senddata, senddata.length, clientAddress, clientPort);
                socket.send(outdata);
                byte[] bytes = new byte[tamanhoPacote];
                try {
                    // arquivo para bytes
                    senddata = bytes;
                    bytes = Files.readAllBytes(path);
                    while(tamanhoArquivo > 0){
                        System.arraycopy(infosArquivo2.getBytes(), 0, senddata, 0, (infosArquivo2.length()));
                        System.arraycopy(bytes, srcPos, senddata, infosArquivo2.length(), (Math.min(tamanhoArquivo, (tamanhoPacote - infosArquivo2.length()))));
                        outdata = new DatagramPacket(senddata, senddata.length, clientAddress, clientPort);
                        socket.send(outdata);
                        try { Thread.sleep (10000); } catch (InterruptedException ex) {}
                        receivedata = new byte[10000];
                        indata = new DatagramPacket(receivedata, receivedata.length);
                        socket.receive(indata);
                        String rawACK = new String(indata.getData());
                        String clientACK = rawACK.substring(0, (indata.getLength()));
                        System.out.println(clientACK);
                        if (!clientACK.contains("ACK")) {
                            while (!clientACK.contains("ACK")) {
                                System.arraycopy(infosArquivo2.getBytes(), 0, senddata, 0, (infosArquivo2.length()));
                                System.arraycopy(bytes, srcPos, senddata, infosArquivo2.length(), (Math.min(tamanhoArquivo, (tamanhoPacote - infosArquivo2.length()))));
                                outdata = new DatagramPacket(senddata, senddata.length, clientAddress, clientPort);
                                socket.send(outdata);
                                try {
                                    Thread.sleep(10000);
                                } catch (InterruptedException ex) {
                                }
                                receivedata = new byte[10000];
                                indata = new DatagramPacket(receivedata, receivedata.length);
                                socket.receive(indata);
                                rawACK = new String(indata.getData());
                                clientACK = rawACK.substring(0, (indata.getLength()));
                                System.out.println(clientACK);
                            }
                        }
                        numeroPacotes++;
                        infosArquivo2 = "Pacote " + numeroPacotes;
                        srcPos += tamanhoPacote - infosArquivo2.length();
                        tamanhoArquivo -= tamanhoPacote - infosArquivo2.length();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                diretorio = "C:/Users/Rodrigo/Downloads/";
                hash = stringHexa(Objects.requireNonNull(gerarHash(Arrays.toString(bytes), "SHA-256")));
                infosArquivo = "Checksum: " + hash;
                senddata = infosArquivo.getBytes();
                outdata = new DatagramPacket(senddata, senddata.length, clientAddress, clientPort);
                socket.send(outdata);
            }
        }

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
