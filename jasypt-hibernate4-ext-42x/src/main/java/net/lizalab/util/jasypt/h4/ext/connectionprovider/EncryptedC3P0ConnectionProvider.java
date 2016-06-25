/*
 * Copyright 2015 Hemant Padmanabhan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.lizalab.util.jasypt.h4.ext.connectionprovider;

import java.util.Map;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.jdbc.connections.internal.C3P0ConnectionProvider;
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.hibernate4.connectionprovider.ParameterNaming;
import org.jasypt.hibernate4.encryptor.HibernatePBEEncryptorRegistry;
import org.jasypt.properties.PropertyValueEncryptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * <p>
 * Extension of Hibernate 4.2.x API version of {@link C3P0ConnectionProvider}
 * that allows the user to write the datasource configuration parameters in an
 * encrypted manner in the <tt>hibernate.cfg.xml</tt> or
 * <tt>hibernate.properties</tt> files.
 * </p>
 * The parameters that can be encrypted are:
 * <ul>
 * <li><tt>hibernate.connection.driver_class</tt></li>
 * <li><tt>hibernate.connection.url</tt></li>
 * <li><tt>hibernate.connection.username</tt></li>
 * <li><tt>hibernate.connection.password</tt></li>
 * </ul>
 * <p>
 * The name of the PBE encryptor (decryptor, in fact) must be set in the
 * property <tt>hibernate.connection.encryptor_registered_name</tt> in
 * <tt>hibernate.cfg.xml</tt>. Its value must be the name of a
 * {@link PBEStringEncryptor} object previously registered with
 * {@link HibernatePBEEncryptorRegistry}. <b>NOTE:</b> Encryptor registration
 * needs to happen BEFORE the Hibernate configuration gets loaded to bootstrap a
 * {@link org.hibernate.SessionFactory}.
 * </p>
 * <p>
 * An example <tt>hibernate.cfg.xml</tt> file configured to use this connection
 * provider implementation:
 * </p>
 * 
 * <pre>
 *  &lt;hibernate-configuration&gt;
 * 
 *    &lt;session-factory&gt;
 * 
 *      <!-- Database connection settings -->
 *      &lt;property name="<b>hibernate.connection.provider_class</b>"&gt;net.lizalab.util.jasypt.h4.ext.connectionprovider.EncryptedC3P0ConnectionProvider&lt;/property&gt;
 *      &lt;property name="<b>hibernate.connection.encryptor_registered_name</b>"&gt;stringEncryptor&lt;/property&gt;
 *      &lt;property name="hibernate.connection.driver_class"&gt;org.postgresql.Driver&lt;/property&gt;
 *      &lt;property name="hibernate.connection.url"&gt;jdbc:postgresql://localhost/mydatabase&lt;/property&gt;
 *      &lt;property name="hibernate.connection.username"&gt;myuser&lt;/property&gt;
 *      &lt;property name="hibernate.connection.password"&gt;ENC(T6DAe34NasW==)&lt;/property&gt;
 *      &lt;property name="hibernate.c3p0.min_size"&gt;5&lt;/property&gt;
 *      &lt;property name="hibernate.c3p0.max_size"&gt;20&lt;/property&gt;
 *      &lt;property name="hibernate.c3p0.timeout"&gt;1800&lt;/property&gt;
 *      &lt;property name="hibernate.c3p0.max_statements"&gt;50&lt;/property&gt;
 *      ...
 *      
 *    &lt;/session-factory&gt;
 *    
 *    ...
 *    
 *  &lt;/hibernate-configuration&gt;
 * </pre>
 * 
 * <p>
 * <b>IMPORTANT:</b> Encrypted values specified must be enclosed in 'ENC(...)'
 * as shown above for them to be picked up and decrypted.
 * </p>
 * 
 * @author Hemant Padmanabhan
 * @since 1.0.0
 */
public class EncryptedC3P0ConnectionProvider extends C3P0ConnectionProvider {

	private static final long serialVersionUID = -5712348739957144201L;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedC3P0ConnectionProvider.class);

	/**
	 * Default constructor, simply invokes parent no args constructor.
	 */
	public EncryptedC3P0ConnectionProvider() {
		super();
	}

	/**
	 * Overrides parent method to scan and replace encrypted configuration values
	 * with their corresponding decrypted values before invoking the parent with
	 * them.
	 * <p>
	 * Looks in the configuration for the name with which the PBE encryptor has been
	 * registered with {@link HibernatePBEEncryptorRegistry} using the key 
	 * {@link ParameterNaming#ENCRYPTOR_REGISTERED_NAME}.
	 * Scans the configuration for supported connection configuration values, checks
	 * to see if they are encrypted and replaces them with the decrypted values if
	 * they are.
	 * </p>
	 * @throws EncryptionInitializationException If the PBE encryptor registered name
	 * configuration cannot be found or if lookup by the registered name does not return
	 * a valid encryptor.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void configure(Map configurationValues) {
		final String methodName = "configure : ";
		
		// Find encryptor registered for config.
		final String encryptorRegisteredName = ConfigurationHelper.getString(
				ParameterNaming.ENCRYPTOR_REGISTERED_NAME, configurationValues);
		if (encryptorRegisteredName == null) {
			throw new EncryptionInitializationException(
					"No encryptor registered in configuration! Encryptor must be registered with property name: "
							+ ParameterNaming.ENCRYPTOR_REGISTERED_NAME);
		}
		LOGGER.info("{} Using Encryptor registered with name: {}", methodName, encryptorRegisteredName);
		
		final HibernatePBEEncryptorRegistry encryptorRegistry = HibernatePBEEncryptorRegistry
				.getInstance();
		final PBEStringEncryptor encryptor = encryptorRegistry
				.getPBEStringEncryptor(encryptorRegisteredName);

		if (encryptor == null) {
			throw new EncryptionInitializationException(
					"No string encryptor registered for hibernate "
							+ "with name: " + encryptorRegisteredName);
		}
		LOGGER.info("{} Found registered Encryptor, checking connection properties ..", methodName);
		
		configurationValues.put(AvailableSettings.DRIVER, decryptIfEncrypted(encryptor, AvailableSettings.DRIVER, ConfigurationHelper.getString(AvailableSettings.DRIVER, configurationValues)));
		configurationValues.put(AvailableSettings.URL, decryptIfEncrypted(encryptor, AvailableSettings.URL, ConfigurationHelper.getString(AvailableSettings.URL, configurationValues)));
		configurationValues.put(AvailableSettings.USER, decryptIfEncrypted(encryptor, AvailableSettings.USER, ConfigurationHelper.getString(AvailableSettings.USER, configurationValues)));
		configurationValues.put(AvailableSettings.PASS, decryptIfEncrypted(encryptor, AvailableSettings.PASS, ConfigurationHelper.getString(AvailableSettings.PASS, configurationValues)));
		// Let Hibernate do the rest.
		super.configure(configurationValues);
	}

	/**
	 * Checks if the provided value is encrypted, if yes, uses the provided 
	 * encryptor to decrypt it.
	 * 
	 * @param encryptor - The encryptor to use to decrypt an encrypted value.
	 * @param propName - Name of the configuration property the value belongs to. 
	 * @param value - The configuration value to be decrypted if encrypted.
	 * @return The value unchanged if not encrypted, else its decrypted equivalent.
	 */
	private String decryptIfEncrypted(PBEStringEncryptor encryptor, String propName, String value) {
		final String methodName = "decryptIfEncrypted : ";
		
		if (PropertyValueEncryptionUtils.isEncryptedValue(value)) {
			LOGGER.info("{} Decrypting value for encrypted property: {}", methodName, propName);
			return PropertyValueEncryptionUtils.decrypt(value, encryptor);
		}
		return value;
	}
	
}
