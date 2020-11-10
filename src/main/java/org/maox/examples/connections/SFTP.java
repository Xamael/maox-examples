package org.maox.examples.connections;

import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Properties;

/**
 * @author Alex
 */
@SuppressWarnings("ALL")
public class SFTP {

    private final static Logger logger = LoggerFactory.getLogger(SFTP.class);
    private final static int DIRECT = 0;
    private final static int HTTP = 1;
    private final static int SOCKS4 = 4;
    private final static int SOCKS5 = 5;

    private Session session = null;
    private Channel channel = null;
    private ChannelSftp channelSftp = null;

    /**
     * Constuctor
     */
    public SFTP() {
        super();
    }

    /**
     * Test
     *
     * @param args
     */
    public static void main(String[] args) {

        final String SFTP_HOST = "sftps.host.com";
        final int SFTP_PORT = 22;
        final String SFTP_USER = "user";
        final String SFTP_PASS = "pass";
        final String SFTP_WORKING_DIR = "directory/subdir/";
        final String PROXY_HOST = "proxy.domain.com";
        final int PROXY_PORT = 1080;

        SFTP sftp = new SFTP();

        try {
            // Conexion
            sftp.openSession(SFTP_HOST, SFTP_PORT, SFTP_USER, SFTP_PASS);
            sftp.setProxy(SFTP.SOCKS5, PROXY_HOST, PROXY_PORT);
            sftp.connect();
            sftp.openChannel("sftp");

            // Cambio directorio
            sftp.changeDir(SFTP_WORKING_DIR);

            // Listar todos los ficheros "*" del directorio actual "."
            List<LsEntry> list = sftp.list(".", "*");
            // Download
            for (LsEntry entry : list) {
                sftp.download(entry.getFilename(), "src\\main\\resources\\"/* destinationPath */ + entry.getFilename());
            }

            // Upload
            sftp.upload(new File("src\\main\\resources\\prueba1.txt"));
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            sftp.close();
        }

    }

    /**
     * changeDir
     *
     * @param dir
     * @throws SftpException
     */
    public void changeDir(String dir) throws SftpException {
        channelSftp.cd(dir);
        logger.debug("Directory is now {}", dir);
    }

    /**
     * close
     */
    public void close() {
        if (channelSftp != null)
            channelSftp.exit();
        if (channel != null)
            channel.disconnect();
        if (session != null)
            session.disconnect();
        logger.debug("Host Session disconnected.");
    }

    /**
     * connect
     *
     * @throws JSchException
     */
    public void connect() throws JSchException {
        session.connect();

    }

    /**
     * download
     *
     * @param sourceFileName
     * @param destPathName
     * @throws SftpException
     */
    public void download(String sourceFileName, String destPathName) throws SftpException {
        logger.debug("Downloading {} to {}", sourceFileName, destPathName);
        channelSftp.get(sourceFileName, destPathName);
        logger.debug("Download OK");

    }

    /**
     * list
     *
     * @param path
     * @param filter
     * @throws SftpException
     */
    @SuppressWarnings("unchecked")
    public List<ChannelSftp.LsEntry> list(String path, String filter) throws SftpException {
        return channelSftp.ls(new StringBuilder("").append(path).append("/").append(filter).toString());
    }

    /**
     * openChannel
     *
     * @param channelType
     * @throws JSchException
     */
    public void openChannel(String channelType) throws JSchException {
        if (channelType.equals("sftp")) {
            channel = session.openChannel(channelType);
            channel.connect();
            logger.debug("SFTP channel opened and connected.");
            channelSftp = (ChannelSftp) channel;
        }
    }

    /**
     * openSession
     *
     * @param host
     * @param port
     * @param user
     * @param password
     * @throws JSchException
     */
    public void openSession(String hostName, int port, String user, String password) throws JSchException {
        logger.debug("Open connection to {}:{}", hostName, port);
        JSch jsch = new JSch();
        session = jsch.getSession(user, hostName, port);
        session.setPassword(password);

        logger.debug("Setting properties");
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        logger.debug("Connection established");
    }

    /**
     * setProxy
     *
     * @param type
     * @param hostName
     * @param port
     */
    public void setProxy(int type, String hostName, int port) {
        Proxy proxy = null;

        switch (type) {
            case HTTP:
                break;
            case SOCKS4:
                proxy = (port >= 0) ? new ProxySOCKS4(hostName, port) : new ProxySOCKS4(hostName);
                break;
            case SOCKS5:
                proxy = (port >= 0) ? new ProxySOCKS5(hostName, port) : new ProxySOCKS5(hostName);
                break;
            case DIRECT:
                break;
        }

        if (proxy != null)
            session.setProxy(proxy);

        logger.debug("Proxy established");
    }

    /**
     * upload
     *
     * @param file
     * @throws FileNotFoundException
     * @throws SftpException
     */
    public void upload(File file) throws FileNotFoundException, SftpException {
        logger.debug("Uploading {}", file.getAbsolutePath());
        channelSftp.put(new FileInputStream(file), file.getName());
        logger.debug("Uploading OK");
    }

}
