package no.nilsen.sftp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

public class SftpClient {
    private final JSch jsch = new JSch();
    private final int port = 22999;

    public SftpClient() {
        Hashtable config = new Hashtable();
        config.put("StrictHostKeyChecking", "no");
        JSch.setConfig(config);

    }

    public void send(String remoteFilename, ByteArrayInputStream inputStream) {
        try {
            ChannelSftp sftpChannel = kobleTilServer();
            sftpChannel.put(inputStream, remoteFilename);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String hent(String downloadedFileName, String remoteFilename) {
        try {
            ChannelSftp sftpChannel = kobleTilServer();

            sftpChannel.get(downloadedFileName, remoteFilename);

            String fileData = getFileContents(downloadedFileName);

            stengServer(sftpChannel);
            return fileData;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ChannelSftp kobleTilServer() throws JSchException {
        com.jcraft.jsch.Session session = jsch.getSession("remote-username", "localhost", port);
        session.setPassword("remote-password");

        session.connect();

        Channel channel = session.openChannel("sftp");
        channel.connect();

        return (ChannelSftp) channel;
    }

    private void stengServer(final ChannelSftp sftpChannel) {
        if (sftpChannel.isConnected()) {
            sftpChannel.exit();
        }

        if (sftpChannel.isConnected()) {
            sftpChannel.disconnect();
        }
    }

    private String getFileContents(final String downloadedFileName) {
        try {
            return new String(Files.readAllBytes(Paths.get(downloadedFileName)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
