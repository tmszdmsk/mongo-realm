MongoDB-backed realm for Glassfish
===========

With Mongo-Realm you can store your users credentials directly in MongoDB with other data of your applicaton. Forget about setting up separate MySQL or LDAP server only for storing users data.

Setup
----------

 1. download mongo-realm jar and put it in your glassfish domains lib folder (i.e. `$GLASSFISH_HOME/glassfish/domains/$DOMAINNAME/lib/`)
 2. at the end of `$GLASSFISH_HOME/glassfish/domains/$DOMAINNAME/config/login.conf` file paste:

 ```
 mongoRealm { 
   com.tadamski.glassfish.mongo.realm.MongoLoginModule required; 
 };
 ```
 3. create realm in glassfish using `asadmin` tool
 
 ```
 asadmin create-auth-realm --classname com.tadamski.glassfish.mongo.realm.MongoRealm --property jaas-context=mongoRealm $REALM_NAME
 ```
 4. configure your applicaton to use newly created realm (in most cases few lines in `web.xml` will be enough)