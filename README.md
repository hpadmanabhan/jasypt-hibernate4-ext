# Overview

Updates and extensions to the jasypt-hibernate4 artifact for the API changes in Hibernate 4.2.x and 4.3.x.

# About

[Jasypt](http://jasypt.org/) is an excellent Java library that allows Java projects and applications to easily add encryption with minimum effort in a meaningful and secure manner without requiring a deep understanding of how cryptography works.

[jasypt-hibernate4](http://jasypt.org/hibernate.html) is an artifact of the Jasypt project that provides transparent integration of Jasypt with Hibernate for both data and configuration encryption.
The artifact was built and tested with the 4.0.x release branch of Hibernate which unfortunately no longer works with the 4.2.x and 4.3.x release branches with respect to the encrypted connection provider implementations for encrypting configuration data, a very sensitive requirement with Hibernate. 
For reasons unknown the folks at JBoss have broken the cardinal rules of semantic versioning by repeatedly changing and refactoring the relevant Hibernate APIs with each incremental minor version.

While the Jasypt project has not seen a release in about a year as of this writing, the package itself remains absolutely relevant and useful, particularly its Hibernate integration for encrypting configuration data for connection information. This project:
* Provides updated encrypted connection providers for both the 4.2.x and 4.3.x Hibernate release branches.
* Adds minimal sensible logging providing quick information on validating setups.
* Abstracts Hibernate API changes from 4.2.x to 4.3.x allowing users a transparent upgrade path of simply switching the jars used without needing any configuration or code changes.
* Detailed unit and integration tests that also illustrate simple usage scenarios.

# Usage

The folks at Jasypt have provided very good documentation on using the [jasypt-hibernate4](http://jasypt.org/hibernate.html) artifact and there are no syntactic changes in using this extension library from what is outlined there, only the following changes in specifying the encrypted connection providers in your Hibernate configuration, instead of:
```xml
<property name="connection.provider_class">
      org.jasypt.hibernate4.connectionprovider.EncryptedPasswordDriverManagerConnectionProvider
</property>
```
Use:
```xml
<property name="connection.provider_class">
      net.lizalab.util.jasypt.h4.ext.connectionprovider.EncryptedDriverManagerConnectionProviderImpl
</property>
```
And, instead of:
```xml
<property name="connection.provider_class">
      org.jasypt.hibernate4.connectionprovider.EncryptedPasswordC3P0ConnectionProvider
</property>
```
Use:
```xml
<property name="connection.provider_class">
      net.lizalab.util.jasypt.h4.ext.connectionprovider.EncryptedC3P0ConnectionProvider
</property>
```
As noted above, this works for both the 4.2.x and 4.3.x extensions.

# License

Artifacts of this extension project are provided under the Apache 2.0 License.

# Distribution

You can obtain the release binaries from the releases page above or alternately if you use Maven you can get it directly from Maven Central using the following dependency configurations:
```xml
    <dependency>
      <groupId>net.lizalab.util</groupId>
      <artifactId>jasypt-hibernate4-ext-42x</artifactId>
      <version>1.0.0</version>
      <scope>compile</scope>
    </dependency>
```
For Hibernate 4.2.x and the following for Hiber4.3.x:
```xml
    <dependency>
      <groupId>net.lizalab.util</groupId>
      <artifactId>jasypt-hibernate4-ext-43x</artifactId>
      <version>1.0.0</version>
      <scope>compile</scope>
    </dependency>
```
