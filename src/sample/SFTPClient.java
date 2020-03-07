package sample;

import com.jcraft.jsch.*;

import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class SFTPClient {

    private Session session = null;

    public void connect(String text) throws JSchException, ConnectException {
        JSch jsch = new JSch();

        // Uncomment the line below if the FTP server requires certificate
//        jsch.addIdentity("private-key-path");
//        session = jsch.getSession("192.168.8.1");

        // Uncomment the two lines below if the FTP server requires password
        session = jsch.getSession("debian", text, 22);
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
    public ArrayList<String> listDataFiles() throws JSchException, SftpException {
        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;
        sftpChannel.cd("/home/debian/stratus/build/datafiles");
        Vector filelist = sftpChannel.ls("/home/debian/stratus/build/datafiles");
        ArrayList<String> listOfDirectories = new ArrayList<String>();
        for(int i = 0; i < filelist.size(); i++){
            ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) filelist.get(i);
            if(entry.getFilename().contains("-")) {
                listOfDirectories.add(entry.getFilename());
            }
        }
        return listOfDirectories;
    }

    public void disconnect() {
        if (session != null) {
            session.disconnect();
        }
    }
}