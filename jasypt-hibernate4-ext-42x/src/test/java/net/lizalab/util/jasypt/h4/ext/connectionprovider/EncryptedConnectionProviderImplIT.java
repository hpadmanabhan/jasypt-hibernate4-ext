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

import static org.junit.Assert.assertNotNull;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.hibernate4.encryptor.HibernatePBEEncryptorRegistry;
import org.junit.Before;
import org.junit.Test;

public class EncryptedConnectionProviderImplIT {
	
	@Before
	public final void initialize() {
		// Setup the encryptor.
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		encryptor.setStringOutputType("hexadecimal");
		// Setting the encryption password directly here for the test.
		// In real usage this should be sourced from sys/env/web props.
		// Also, using the same password as the DB password, again not
		// a good idea for production systems ever!
		encryptor.setPassword("Test$Passwo73");
		HibernatePBEEncryptorRegistry encryptorRegistry = HibernatePBEEncryptorRegistry
				.getInstance();
		encryptorRegistry.registerPBEStringEncryptor("configHibernateEncrypter", encryptor);
	}

	@Test
	public final void testDriverManagerImpl() {
		// Load up the configuration, for this test we'll use the default
		// which is /hibernate.cfg.xml.
		Configuration configuration = new Configuration();
		configuration.configure();
		doTest(configuration);
	}
	
	@Test
	public final void testC3P0ConnectionProviderImpl() {
		// Load up the configuration, for this test we'll use the c3p0
		// specific cfg which is /hibernate.c3p0.cfg.xml.
		Configuration configuration = new Configuration();
		configuration.configure("hibernate.c3p0.cfg.xml");
		doTest(configuration);
	}
	
	private void doTest(Configuration configuration) {
		ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
		// Try to initialize session factory. Since we have db entity creation
		// configured it should happen here.
		SessionFactory sessionFactory = configuration.buildSessionFactory(serviceRegistry);
		// Test that we have a non-null session factory.
		assertNotNull("Expected a non-null session factory!", sessionFactory);
		// For some reason db entity creation errors don't get propagated so
		// we are still not guaranteed to be up and running, so lets do a 
		// simple test transaction to confirm everything worked and we are
		// indeed connected to the database.
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		try {
			TestEntity testEntity = new TestEntity("Encrypted c3p0 connection provider implementation Integration Test.");
			session.persist(testEntity);
			session.getTransaction().commit();
		} finally {
			try { session.getTransaction().rollback(); } catch (Exception e) {}
		}
	}

}
