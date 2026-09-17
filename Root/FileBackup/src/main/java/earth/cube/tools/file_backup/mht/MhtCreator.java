package earth.cube.tools.file_backup.mht;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
 
/**
 * Example how to construct a MHT (AKA MHTML) file in Java using JavaMail
 * API. Creates a simple HTML page (from a String) with an image, which is
 * pulled from a file. Requires mail.jar in the classpath (and also Java
 * Activation Framework if JDK version is less than 1.6).
 *
 * The program expects a file named <em>pooh.jpg</em> in the current directory
 * and produces a very simple MHT archive to stdout.
 */
public class MhtCreator
{
    public static void main(String argv[]) throws Exception
    {
        // get system properties
        Properties props = new Properties();
        // System.getProperties();
        // create a new session
        Session session = Session.getInstance(props, null);
        // construct a MIME message message
        MimeMessage message = new MimeMessage(session);
        MimeMultipart mpart = new MimeMultipart("related");
        String htmlPage =
            "<html><head><title>Example</title></head>" +
            "<body><p>ExampleExampleSome text..." +
            "<img src='pooh.jpg'></p></body>";
        mpart.addBodyPart(bodyPart(
            new StringSource("text/html", "index.html", htmlPage) ));
        mpart.addBodyPart(bodyPart(new FileDataSource("pooh.jpg")));
        message.setContent(mpart);
        // the subject is displayed as the window title in the browser
        message.setSubject("MHTML example");
        // one can set the URL of the original page:
        message.addHeader("Content-Location", "index.html");
 
        // Save to example.mhtml
        FileOutputStream out = new FileOutputStream("example.mhtml");
        message.writeTo(out);
        out.close();
    }
    static BodyPart bodyPart(DataSource ds) throws MessagingException
    {
        MimeBodyPart body = new MimeBodyPart();
        DataHandler dh = new DataHandler(ds);
        body.setDisposition("inline");
        body.setDataHandler(dh);
        body.setFileName(dh.getName());
        // the URL of the file; we set it simply to its name
        body.addHeader("Content-Location", dh.getName());
        return body;
    }
 
    /**
     * A simple in-memory implementation of {@link DataSource}.
     */
    static final class StringSource implements DataSource
    {
        private final String contentType;
        private final String name;
        private final byte[] data;
        public StringSource(String contentType, String name, String data)
        {
            this.contentType = contentType;
            this.data = data.getBytes();
            this.name = name;
        }
        public String getContentType()
        {
            return contentType;
        }
        public OutputStream getOutputStream() throws IOException
        {
            throw new IOException();
        }
        public InputStream getInputStream() throws IOException
        {
            return new ByteArrayInputStream(data);
        }
        public String getName()
        {
            return name;
        }
    }
}