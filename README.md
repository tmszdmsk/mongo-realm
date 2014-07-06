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

Configuration
-------------
__By default__:

Mongo-Realm connects to `localhost` on `27017` and looks for data in `users` database in `users` collection. Informations about users are stored in separate documents [one user = one document]. Each document contains `login`, `password` simple string properties and `groups` with array of group names user belongs to. All passwords are hashed using `SHA-512` function.

Salt property is appended to the password before hashing. For users without salt, it will be generated on the first login.

__Custom configuration__:

Of course defaults can be overriden. Simply add properties to realm created in 3rd step of __Setup__.

| Property name             | Default value |
|---------------------------|---------------|
| __mongo.hostname__        | localhost     |
| __mongo.port__            | 27017         |
| __mongo.db.name__         | users         |
| __mongo.collection.name__ | users         |
| __login.property__        | login         |
| __salt.property__         | salt          |
| __password.property__     | password      |
| __groups.property__       | groups        |
| __hash.function__         | SHA-512       |


