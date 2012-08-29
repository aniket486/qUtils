package adcom.ddpm.monitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;

public class VelocityEMail
{
	public static final String configFileName = "ddpm.email.properties";
	public static final String velocityConfigPath = "velocity.properties.path";
	
	public static Logger logger = Logger.getLogger(VelocityEMail.class);
	
	private String testModeAddr = null;
	/**
	 * The default instance for the Velocity Mail class
	 */
	private static VelocityEMail defaultInstance;

	/**
	 * The configuration for Velocity Mail
	 */
	private ExtendedProperties properties;

	/**
	 * The list of email configurations that are available.
	 */
	private Map<String, ExtendedProperties> configurations;

	/**
	 * Create a new Velocity Mail object with a properties object
	 * 
	 * @param properties
	 */
	public VelocityEMail(ExtendedProperties properties)
	{
		this.configurations = new HashMap<String, ExtendedProperties>();
		this.properties = properties;
		
		testModeAddr = this.properties.getString("test.mode.address");
	}

	/**
	 * Return the default instance for VelocityMail. It attempts to load the
	 * resources "velocity.mail.properties" from the classpath.
	 * 
	 * @return @throws
	 *         IOException
	 */
	public static VelocityEMail getDefaultInstance() throws IOException
	{
		if (defaultInstance == null)
		{
			InputStream inputStream = VelocityEMail.class.getResourceAsStream("/" + configFileName);
            ExtendedProperties extendedProperties = new ExtendedProperties();
			extendedProperties.load(inputStream);
			defaultInstance = new VelocityEMail(extendedProperties);
			String velocityProp = extendedProperties.getString(velocityConfigPath);
			if(velocityProp != null) {
				Velocity.init(velocityProp);
			} else {
				Velocity.init();
			}
		}
		return defaultInstance;
	}
	
	/**
	 * Send an email message to configured address in config
	 * 
	 * @param config
	 *            The configuration to use (specified in the email properties)
	 * @param context
	 *            The velocity context used to merge the email template
	 * @throws Exception
	 */
	public void send(String config, Context context)
	throws Exception
	{
		send(config, null, context);
	}

	/**
	 * Send an email message to a list of addresses using the default
	 * configuration.
	 * 
	 * @param address
	 *            The receivers of the email
	 * @param context
	 *            The velocity context used to merge the email template
	 * 
	 * @throws Exception
	 */
	public void send(InternetAddress[] addresses, Context context)
	throws Exception
	{
		send("ddpm", addresses, context);
	}
	
	/**
	 * Send an email message to a list of addresses
	 * 
	 * @param config
	 *            The configuration to use (specified in the email properties)
	 * @param address
	 *            The receivers of the email
	 * @param context
	 *            The velocity context used to merge the email template
	 * @throws Exception
	 */
	public void send(String config, InternetAddress[] addresses, Context context)
	throws Exception
	{
		send(config, addresses, null, context);
	}
	
	public void send(String config, InternetAddress[] addresses, InternetAddress[] ccAddresses, Context context)
	throws Exception
	{
		ExtendedProperties configuration = getConfiguration(config);
		Message message = createMessage(configuration, context);
		setMessageContent(message, configuration, context);
		if(addresses != null) {
			message.addRecipients(Message.RecipientType.TO, addresses);
		}
		if(ccAddresses != null) {
			message.addRecipients(Message.RecipientType.CC, ccAddresses);
		}

		if(testModeAddr != null) {
			logger.info("Test mode is ON, sending email to: "+ testModeAddr);
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(testModeAddr));
			message.setRecipients(Message.RecipientType.CC, null);
			message.setRecipients(Message.RecipientType.BCC, null);
		}
		
		for(Address email : message.getAllRecipients()){
			logger.info("Email sent to: " + email);
		}
		
		Transport.send(message);

	}

	/**
	 * Create a JavaMail message object from a specified configuration
	 * 
	 * @param configuration
	 *            A configuration object that was created from VelocityMail
	 *            properties
	 * 
	 * @param context
	 *            The velocity context to use to merge the email template
	 * 
	 * @throws Exception
	 */
	protected Message createMessage(ExtendedProperties configuration,
			Context context) throws Exception
	{
		String host = configuration.getString("host");
		Properties properties = new Properties();
		properties.put("mail.smtp.host", host);
		Session session = Session.getInstance(properties);

		MimeMessage result = new MimeMessage(session);

		String subject = evaluate(configuration, "subject", context);
		result.setSubject(subject);

		String fromAddr = evaluate(configuration, "from.address", context);
		String fromName = evaluate(configuration, "from.name", context);
		InternetAddress fromAddress = new InternetAddress(fromAddr, fromName);
		result.setFrom(fromAddress);

		String bccAddr = evaluate(configuration, "bcc.address", context);
		if(bccAddr != null) {
			logger.info("Adding bcc address from configuration: " + bccAddr);
			result.addRecipients(Message.RecipientType.BCC, InternetAddress.parse(bccAddr));
		}

		String ccAddr = evaluate(configuration, "cc.address", context);
		if(ccAddr != null) {
			logger.info("Adding cc address from configuration: " + ccAddr);
			result.addRecipients(Message.RecipientType.CC, InternetAddress.parse(ccAddr));
		}
		
		String toAddr = evaluate(configuration, "to.address", context);
		if(toAddr != null) {
			logger.info("Adding to address from configuration: " + toAddr);
			result.addRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddr));
		}

		return result;
	}

	/**
	 * Merge the email template(s) using the velocity context and add the
	 * content to the JavaMail message object
	 * 
	 * @param message
	 *            The message that will receive the message content
	 * 
	 * @param configuration
	 *            The configuration to use
	 * 
	 * @param context
	 *            The velocity context that will be used to merge the mail
	 *            templates
	 * 
	 * @throws Exception
	 */
	protected void setMessageContent(Message message, ExtendedProperties configuration, Context context) throws Exception
	{
		Multipart multipart = new MimeMultipart();
		
		ExtendedProperties content = configuration.subset("message");
		if(content != null) {
			@SuppressWarnings("rawtypes")
			Enumeration keys = content.keys();
	
			while (keys.hasMoreElements())
			{
				BodyPart bodyPart = new MimeBodyPart();
				String key = (String)keys.nextElement();
				String templateName = content.getString(key);
				StringWriter writer = new StringWriter();
				Template template = Velocity.getTemplate(templateName);
				template.merge(context, writer);
				writer.close();
				bodyPart.setContent(writer.toString(), key);
				multipart.addBodyPart(bodyPart);
			}
		}

		content = configuration.subset("attachment");
		if(content != null) {
			@SuppressWarnings("rawtypes")
			Enumeration keys = content.keys();
			keys = content.keys();
	
			while (keys.hasMoreElements())
			{
				BodyPart bodyPart = new MimeBodyPart();
				String key = (String)keys.nextElement();
				String[] attachmentNamePath = evaluate(content, key, context).split("@@");
				String attachmentName = attachmentNamePath[0];
				String attachmentPath = attachmentNamePath[1];
				// attach the file to the message
				FileDataSource fds = new FileDataSource(attachmentPath);
				bodyPart.setHeader("Content-Type", key);
				bodyPart.setDataHandler(new DataHandler(fds));
				bodyPart.setFileName(attachmentName);
				multipart.addBodyPart(bodyPart);
			}
		}

		message.setContent(multipart);
	}

	/**
	 * Helper method to evaluate a string
	 * 
	 * @param configuration
	 * @param property
	 * @param context
	 * @return @throws
	 *         Exception
	 */
	protected String evaluate(ExtendedProperties configuration,
			String property, Context context) throws Exception
	{
		String subjectTemplate = configuration.getString(property);
		if(subjectTemplate == null) {
			return null;
		}
		StringWriter stringWriter = new StringWriter();
		Velocity.evaluate(context, stringWriter, "email", subjectTemplate);
		stringWriter.close();
		return stringWriter.toString();
	}

	/**
	 * Get the configuration for a given configuration name
	 * 
	 * @param name
	 * @return
	 */
	protected ExtendedProperties getConfiguration(String name)
	{
		ExtendedProperties config = configurations.get(name);
		if (config == null)
		{
			config = properties.subset(name);
			if(config == null) {
				throw new IllegalStateException("Configuration "+ name + " is missing in "+ configFileName);
			}
			configurations.put(name, config);
		}
		return config;
	}
}