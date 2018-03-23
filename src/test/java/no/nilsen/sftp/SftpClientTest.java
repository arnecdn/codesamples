package no.nilsen.sftp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class SftpClientTest {
   SshServer sshd;

   @Test
   public void testPutAndGetFile() throws Exception {
       SftpClient sftpClient = new SftpClient();
       final String testFileContents = "some file contents";
       String uploadedFileName = "uploadFile";
       sftpClient.send(uploadedFileName, new ByteArrayInputStream(testFileContents.getBytes()));

       String downloadedFileName = "downLoadFile";
       sftpClient.hent(uploadedFileName, downloadedFileName);

       File downloadedFile = new File(downloadedFileName);
       assertTrue(downloadedFile.exists());

       String fileData = getFileContents(downloadedFileName);

       assertEquals(testFileContents, fileData);
   }

   private String getFileContents(final String downloadedFileName) {
       try {
           return new String(Files.readAllBytes(Paths.get(downloadedFileName)));
       } catch (IOException e) {
           e.printStackTrace();
       }
       return "";
   }

   @Before
   public void beforeTestSetup() throws Exception {
       sshd = SshServer.setUpDefaultServer();
       sshd.setPort(22999);

       sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"));
       sshd.setPasswordAuthenticator(new PasswordAuthenticator() {

           public boolean authenticate(String username, String password, ServerSession session) {
               return true;
           }
       });

       CommandFactory myCommandFactory = new CommandFactory() {

           public Command createCommand(String command) {
               System.out.println("Command: " + command);
               return null;
           }
       };
       sshd.setCommandFactory(new ScpCommandFactory(myCommandFactory));

       List<NamedFactory<Command>> namedFactoryList = new ArrayList<>();
       namedFactoryList.add(new SftpSubsystem.Factory());
       sshd.setSubsystemFactories(namedFactoryList);
       sshd.start();
   }

   @After
   public void teardown() throws Exception { sshd.stop(); }
}