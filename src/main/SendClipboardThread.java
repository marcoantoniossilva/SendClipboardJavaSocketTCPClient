package main;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import javax.swing.JOptionPane;

public class SendClipboardThread extends Thread {

    private final Principal principal;
    private Socket clientSocket;

    SendClipboardThread(Principal principal) {
        this.principal = principal;
    }

    @Override
    public void run() {
        try {
            clientSocket = new Socket(principal.getHostText(), principal.getIntPort());
            principal.notifyConnection();
            while (clientSocket.isConnected()) {
                try {
                    Thread.sleep(500);
                    System.out.println(clientSocket.getInetAddress().isReachable(2000));
                    String clientClipboard = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
                    if (!clientClipboard.equals(principal.getLastClipboard())) {
                        principal.setLastClipboard(clientClipboard);
                        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                        outToServer.writeBytes(clientClipboard + '\n');
                    }
                } catch (IOException ex) {
                    principal.disconnect();
                    messageConnectError();
                } catch (UnsupportedFlavorException ex) {
                    System.err.println("Erro de UnsupportedFlavorException: " + ex.getMessage());
                } catch (InterruptedException ex) {
                    System.err.println("Erro de InterruptedException: " + ex.getMessage());
                }
            }
        } catch (IOException ex) {
            messageConnectError();
        }
    }

    public void messageConnectError() {
        JOptionPane.showMessageDialog(principal, "Não foi possível conectar ao servidor.\n"
                + "verifique se o servidor está ligado e ativo na porta e ip especificados!", "Erro", JOptionPane.ERROR_MESSAGE);
    }

    public void disconnect() {
        try {
            if (clientSocket.isConnected()) {
                clientSocket.close();
            }
        } catch (IOException ex) {
            System.err.println("Erro de IO: " + ex.getMessage());
        }
    }

}
