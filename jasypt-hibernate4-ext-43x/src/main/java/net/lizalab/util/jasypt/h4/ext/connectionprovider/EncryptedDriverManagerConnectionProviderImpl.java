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
import org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.hibernate4.connectionprovider.ParameterNaming;
import org.jasypt.hibernate4.encryptor.HibernatePBEEncryptorRegistry;
import org.jasypt.properties.PropertyValueEncryptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncryptedDriverManagerConnectionProviderImpl extends
		DriverManagerConnectionProviderImpl {

	private static final long serialVersionUID = 8285207080294374437L;

	private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedDriverManagerConnectionProviderImpl.class);

	public EncryptedDriverManagerConnectionProviderImpl() {
		super();
	}

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
	
	private String decryptIfEncrypted(PBEStringEncryptor encryptor, String propName, String value) {
		final String methodName = "decryptIfEncrypted : ";
		
		if (PropertyValueEncryptionUtils.isEncryptedValue(value)) {
			LOGGER.info("{} Decrypting value for encrypted property: {}", methodName, propName);
			return PropertyValueEncryptionUtils.decrypt(value, encryptor);
		}
		return value;
	}

}
