package sample;

import com.jcraft.jsch.*;

import java.net.ConnectException;

public class SFTPClient {

    private Session session = null;

    private String privateKeyPath;

    public void connect() throws JSchException, ConnectException {
        JSch jsch = new JSch();

        // Uncomment the line below if the FTP server requires certificate
//        jsch.addIdentity("private-key-path");
//        session = jsch.getSession("192.168.8.1");

        // Uncomment the two lines below if the FTP server requires password
        session = jsch.getSession("debian", "192.168.8.1", 22);
        session.setPassword("temppwd");
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(3000);
    }

    public void upload(String source, String destination) throws JSchException, SftpException {
        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;
        sftpChannel.put(source, destination);
        sftpChannel.exit();
    }

    public void download(String source, String destination) throws JSchException, SftpException {
        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;
        sftpChannel.get(source, destination);
        sftpChannel.exit();
    }

    public void disconnect() {
        if (session != null) {
            session.disconnect();
        }
    }
}