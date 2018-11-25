public class Properties {


    private String clientAdress;
    private String userName = "ach444";
    private String hostName = "mailgate.informatik.haw-hamburg.de";
    private int port = 465;
    private String messageBody = "This is a test mail";
    private String subject = "TestBetreff1";
    private String attachmentPath;

    public Properties(String clAdress, String attPath) {
        //Client Adress and attachment path read from terminal
        clientAdress = clAdress;
        attachmentPath = attPath;
    }

    public String getClientAdress() {
        return clientAdress;
    }

    public String getUserName() {
        return userName;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public String getSubject() {
        return subject;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

}
