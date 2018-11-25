import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.logging.*;

public class MailFile {

    private static final Logger LOG = Logger.getLogger(MailFile.class.getName());
    private SSLSocketFactory factory;
    private SSLSocket clientSocket;
    private DataOutputStream os;
    private BufferedReader is;

    /**
     * Constructor for MailFile class
     *
     * @param hostName name of mailserver
     * @param port     port of mailserver
     */
    public MailFile(String hostName, int port) {
        factory =
                (SSLSocketFactory) SSLSocketFactory.getDefault();
        // create socket, dataoutputstream, bufferedreader, logger
        try {
            clientSocket = (SSLSocket) factory.createSocket(hostName, port);
            os = new DataOutputStream(clientSocket.getOutputStream());
            is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            Handler handler = new FileHandler("log.txt");
            SimpleFormatter formatter = new SimpleFormatter();
            handler.setFormatter(formatter);
            LOG.addHandler(handler);
        } catch (
                IOException e) {
            LOG.info("IOException MailFile Constructor: " + e);
        }

    }

    /**
     * Method to send an E-Mail
     *
     * @param userName HAW user name used for authentification
     * @param clAdress sender adress
     * @param subj     email subject
     * @param mesBody  message body of email
     * @param attPath  path to the attachment
     */
    public void sendMail(String userName, String clAdress, String subj, String mesBody, String attPath) {
        //ask for password
        String password = new String(System.console().readPassword("Passwort eingeben: "));
        //build base64 encryption for authentification
        String usernamePassword = "\0" + "ach444" + "\0" + password;
        byte[] usernamePasswordBytes = usernamePassword.getBytes();
        String usernamePasswordEncoded = Base64.getEncoder().encodeToString(usernamePasswordBytes);

        printServerMessage();
        //handshake
        sendToServer("EHLO example.client.de");
        printServerMessage();
        //authentification
        sendToServer("AUTH PLAIN " + usernamePasswordEncoded);

        printServerMessage();
        //prepare attachment
        File attachment = new File(attPath);
        Path attachmentPath = Paths.get(attPath);
        byte[] attachmentByte = null;
        try {
            attachmentByte = Files.readAllBytes(attachmentPath);
        } catch (IOException e) {
            LOG.info("IOException read attachment: " + e);
        }
        //send mail content
        sendToServer("MAIL FROM: <" + userName + ">");
        printServerMessage();
        sendToServer("RCPT TO: " + "<" + clAdress + ">");
        sendToServer("RCPT TO: <akram.askar@haw-hamburg.de>");
        //sendToServer("RCPT TO: <magnus.hatlauf@haw-hamburg.de>";
        printServerMessage();
        sendToServer("DATA");
        printServerMessage();

        sendToServer("From: <Donald Duck@blabla.de>");
        sendToServer("To: <" + clAdress + ">");
        sendToServer("SUBJECT: " + subj);
        sendToServer("MIME-Version: 1.0");
        sendToServer("Content-Type: multipart/mixed; boundary=frontier");
        sendToServer("--frontier");
        //message text body
        sendToServer("Content-Type: text/plain");
        sendToServer("Content-Transfer-Encoding: quoted-printable");
        sendToServer("");
        sendToServer(mesBody);
        sendToServer("--frontier");
        //base64 encrypted attachment
        sendToServer("Content-Transfer-Encoding: base64");
        sendToServer("Content-Type: application/pdf");
        sendToServer("Content-Disposition: attachment; filename=" + attachment.getName());
        sendToServer("");
        sendToServer(Base64.getEncoder().encodeToString(attachmentByte));
        //end of mail content
        sendToServer(".");
        printServerMessage();
        //quit connection to mail server
        sendToServer("QUIT");
        printServerMessage();
        //close socket connection
        try {
            clientSocket.close();
        } catch (IOException e) {
            LOG.info("IOException: " + e);
        }
    }

    public void printServerMessage() {
        while (true) {
            try {
                String serverMessage = is.readLine();
                //quit if pattern doesnt match, else readline is not terminating
                if (serverMessage.matches("[0-9][0-9][0-9]-.*")) {
                    LOG.info("SERVER: " + serverMessage);
                } else if (serverMessage.matches("250.*")){
                    LOG.info("SERVER: " + serverMessage);
                    break;
                } else if (serverMessage.matches("235 2.7.0 Authentication successful")){
                    LOG.info("SERVER: " + serverMessage);
                    break;
                } else if (serverMessage.matches("535.*")){
                    LOG.info("SERVER: " + serverMessage);
                    System.exit(0);
                } else {
                    break;
                }
            } catch (Exception e) {
                break;
            }

        }
    }

    public void sendToServer(String message) {
        try {
            // add <CR><LF>
            os.writeBytes(message + "\r" + "\n");
            LOG.info("CLIENT: " + message);
        } catch (IOException e) {
            LOG.info("IOException sendToServer: " + e);
        }
    }

    public static void main(String[] args) {
        //instanciate properties object
        // File Path: /home/magnshatlauf/Schreibtisch/Uni/Rechnernetze/Praktikum/RN_WS18_Aufgabe1.pdf
        Properties prop = new Properties(args[0], args[1]);
        //instanciate MailFile object
        MailFile mail = new MailFile(prop.getHostName(), prop.getPort());
        //send E-Mail
        mail.sendMail(prop.getUserName(), prop.getClientAdress(), prop.getSubject(), prop.getMessageBody(), prop.getAttachmentPath());
    }


}



//235 2.7.0 Authentication successful