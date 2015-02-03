package net.lizalab.util.jasypt.h4.ext.connectionprovider;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.hibernate4.connectionprovider.ParameterNaming;
import org.jasypt.hibernate4.encryptor.HibernatePBEEncryptorRegistry;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class EncryptedC3P0ConnectionProviderTest {

	@Rule
	public JUnitRuleMockery context = new JUnitRuleMockery();
	
	@Mock
	private PBEStringEncryptor mockEncryptor;
	
	@Mock
	private ClassLoaderService mockCLService;
	
	@Mock
	private ServiceRegistryImplementor mockSRImpl;
	
	private String testEncryptedCfgDriver = "ENC(0D347740A70CA9E28117EA25D4692D538C053B57E5125331CB9F7238C59BD967)";
	
	private String testEncryptedDriverText = "0D347740A70CA9E28117EA25D4692D538C053B57E5125331CB9F7238C59BD967";
	
	private String testDriver = "org.hsqldb.jdbcDriver";
	
	private String testEncryptedCfgUrl = "ENC(FC3970FF1E22528BD9B3FEF4F103C1A388E9B212D158766F5A989C79B1FF364F9116342E9A41413D4A605EB819C41E35AE8A6FCF7AB7FFEFEDF671C758ECBDCD)";
	
	private String testEncryptedUrlText = "FC3970FF1E22528BD9B3FEF4F103C1A388E9B212D158766F5A989C79B1FF364F9116342E9A41413D4A605EB819C41E35AE8A6FCF7AB7FFEFEDF671C758ECBDCD";
	
	private String testUrl = "jdbc:hsqldb:file:target/data/testdb;shutdown=true";
	
	private String testEncryptedCfgUser = "ENC(4BB1376E8D44E1DFE39F253F5390E7A0D68C1D7FA36E876A)";
	
	private String testEncryptedUserText = "4BB1376E8D44E1DFE39F253F5390E7A0D68C1D7FA36E876A";
	
	private String testUser = "TESTUSER";
	
	private String testEncryptedCfgPass = "ENC(05E8878D8EACD8B63F1E4AEC09649BAE7E9ED3093A8A4998)";
	
	private String testEncryptedPassText = "05E8878D8EACD8B63F1E4AEC09649BAE7E9ED3093A8A4998";
	
	private String testPassword = "Test$Passwo73";
	
	private String testEncryptorCfgName = "configHibernateEncrypter";
	
	private EncryptedC3P0ConnectionProvider testCPImpl;

	/**
	 * Registers mock encryptor.
	 */
	@Before
	public final void initialize() {
		HibernatePBEEncryptorRegistry encryptorRegistry = HibernatePBEEncryptorRegistry
				.getInstance();
		encryptorRegistry.registerPBEStringEncryptor(testEncryptorCfgName, mockEncryptor);
	}
	
	/**
	 * Verifies requirement for configuration of name encryptor is registered
	 * with is enforced.
	 */
	@Test(expected=EncryptionInitializationException.class)
	public final void testEncryptorRegisteredNameReqd() {
		// Initialize config values without encryptor registered name.
		Map<String, String> testConfigValues = new HashMap<String, String>();
		testConfigValues.put(AvailableSettings.DRIVER, testEncryptedCfgDriver);
		testConfigValues.put(AvailableSettings.URL, testEncryptedCfgUrl);
		testConfigValues.put(AvailableSettings.USER, testEncryptedCfgUser);
		testConfigValues.put(AvailableSettings.PASS, testEncryptedCfgPass);
		// Initialize test instance and invoke target method which should 
		// fail with the expected exception.
		testCPImpl = new EncryptedC3P0ConnectionProvider();
		testCPImpl.configure(testConfigValues);
	}

	/**
	 * Verifies decryption of encrypted configuration values for supported
	 * configuration parameters and confirms there are no other side effects.
	 */
	@Test
	public final void testConfigureDecryption() {
		// Set mock expectations to be auto-validated.
		final Sequence decryptSequence = context.sequence("decryptSequence");
		context.checking(new Expectations(){{
			oneOf(mockEncryptor).decrypt(testEncryptedDriverText); inSequence(decryptSequence); will(returnValue(testDriver));
			oneOf(mockEncryptor).decrypt(testEncryptedUrlText); inSequence(decryptSequence); will(returnValue(testUrl));
			oneOf(mockEncryptor).decrypt(testEncryptedUserText); inSequence(decryptSequence); will(returnValue(testUser));
			oneOf(mockEncryptor).decrypt(testEncryptedPassText); inSequence(decryptSequence); will(returnValue(testPassword));
			oneOf(mockSRImpl).getService(ClassLoaderService.class); inSequence(decryptSequence); will(returnValue(mockCLService));
			oneOf(mockCLService).classForName(testDriver); inSequence(decryptSequence);
		}});
		// Setup test configuration.
		Map<String, String> testConfigValues = new HashMap<String, String>();
		testConfigValues.put(ParameterNaming.ENCRYPTOR_REGISTERED_NAME, testEncryptorCfgName);
		testConfigValues.put(AvailableSettings.DRIVER, testEncryptedCfgDriver);
		testConfigValues.put(AvailableSettings.URL, testEncryptedCfgUrl);
		testConfigValues.put(AvailableSettings.USER, testEncryptedCfgUser);
		testConfigValues.put(AvailableSettings.PASS, testEncryptedCfgPass);
		// Track values to be asserted later in the test.
		int cfgsCt = testConfigValues.size();
		// Initialize the test instance and invoke target method to trigger mock expectation assertion.
		testCPImpl = new EncryptedC3P0ConnectionProvider();
		testCPImpl.injectServices(mockSRImpl);
		testCPImpl.configure(testConfigValues);
		// Mock expectations should have already verified decryption, now proceed 
		// with manual assertions.
		// First, verify that the decrypted values were set in the cfg values map
		// since it gets passed to super to complete actual configuration.
		assertEquals("Cfg values map not updated with decrypted driver!", 
				testDriver, testConfigValues.get(AvailableSettings.DRIVER));
		assertEquals("Cfg values map not updated with decrypted url!", 
				testUrl, testConfigValues.get(AvailableSettings.URL));
		assertEquals("Cfg values map not updated with decrypted user!", 
				testUser, testConfigValues.get(AvailableSettings.USER));
		assertEquals("Cfg values map not updated with decrypted password!", 
				testPassword, testConfigValues.get(AvailableSettings.PASS));
		// Now verify there were no other modifications to config values.
		assertEquals("Unexpected no. of config values!", cfgsCt, testConfigValues.size());
		assertEquals("Config value other than encryptable set modified!", 
				testEncryptorCfgName, testConfigValues.get(ParameterNaming.ENCRYPTOR_REGISTERED_NAME));
	}

	/**
	 * Verifies that unencrypted values for supported configuration parameters
	 * are passed through unchanged. 
	 */
	@Test
	public final void testConfigurePassThrough() {
		// Set mock expectations to be auto-validated.
		final Sequence passThroughSequence = context.sequence("passThroughSequence");
		context.checking(new Expectations(){{
			oneOf(mockSRImpl).getService(ClassLoaderService.class); inSequence(passThroughSequence); will(returnValue(mockCLService));
			oneOf(mockCLService).classForName(testDriver); inSequence(passThroughSequence);
		}});		
		// Setup test configuration.
		Map<String, String> testConfigValues = new HashMap<String, String>();
		testConfigValues.put(ParameterNaming.ENCRYPTOR_REGISTERED_NAME, testEncryptorCfgName);
		testConfigValues.put(AvailableSettings.DRIVER, testDriver);
		testConfigValues.put(AvailableSettings.URL, testUrl);
		testConfigValues.put(AvailableSettings.USER, testUser);
		testConfigValues.put(AvailableSettings.PASS, testPassword);
		// Track values to be asserted later in the test.
		int cfgsCt = testConfigValues.size();
		// Initialize the test instance and invoke target method. Mock 
		// expectations aren't set for encryptor since encryptor should 
		// never be invoked in this case.
		testCPImpl = new EncryptedC3P0ConnectionProvider();
		testCPImpl.injectServices(mockSRImpl);
		testCPImpl.configure(testConfigValues);
		// Verify that the specified values are unchanged.
		assertEquals("Cfg values map for driver modified!", 
				testDriver, testConfigValues.get(AvailableSettings.DRIVER));
		assertEquals("Cfg values map for url modified!", 
				testUrl, testConfigValues.get(AvailableSettings.URL));
		assertEquals("Cfg values map for user modified!", 
				testUser, testConfigValues.get(AvailableSettings.USER));
		assertEquals("Cfg values map for password modified!", 
				testPassword, testConfigValues.get(AvailableSettings.PASS));
		// Now verify there were no other modifications to config values.
		assertEquals("Unexpected no. of config values!", cfgsCt, testConfigValues.size());
		assertEquals("Config value other than encryptable set modified!", 
				testEncryptorCfgName, testConfigValues.get(ParameterNaming.ENCRYPTOR_REGISTERED_NAME));
	}

}
