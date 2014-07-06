package com.tadamski.glassfish.mongo.realm;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.sun.appserv.security.AppservRealm;
import com.sun.enterprise.security.auth.realm.BadRealmException;
import com.sun.enterprise.security.auth.realm.InvalidOperationException;
import com.sun.enterprise.security.auth.realm.NoSuchRealmException;
import com.sun.enterprise.security.auth.realm.NoSuchUserException;
import static com.tadamski.glassfish.mongo.realm.api.MongoRealmProperties.*;
import com.tadamski.glassfish.mongo.realm.internal.api.MongoRealmInternalApi;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;
import org.bson.types.ObjectId;

/**
 *
 * @author tmszdmsk
 */
public class MongoRealm extends AppservRealm implements MongoRealmInternalApi {
    
    private static Logger logger = Logger.getLogger(MongoRealm.class.getName());

    public static final String AUTH_TYPE = "MongoAuth";
    private DBCollection collection;
    private String hostname;
    private Integer port;
    private String dbName;
    private String collectionName;
    private String loginProperty;
    private String saltProperty;
    private String passwordProperty;
    private String groupsProperty;
    private String hashFunction;

    @Override
    protected void init(Properties properties) throws BadRealmException, NoSuchRealmException {
        hostname = propertyOrDefault(properties, MONGO_HOSTNAME, "localhost");
        port = Integer.valueOf(propertyOrDefault(properties, MONGO_PORT, "27017"));
        dbName = propertyOrDefault(properties, MONGO_DB_NAME, "users");
        collectionName = propertyOrDefault(properties, MONGO_COLLECTION_NAME, "users");
        loginProperty = propertyOrDefault(properties, LOGIN_PROPERTY, "login");
        saltProperty = propertyOrDefault(properties, SALT_PROPERTY, "salt");
        passwordProperty = propertyOrDefault(properties, PASSWORD_PROPERTY, "password");
        groupsProperty = propertyOrDefault(properties, GROUPS_PROPERTY, "groups");
        //SUPPORTED: MD2, MD5, SHA-1, SHA-256, SHA-384, and SHA-512
        hashFunction = propertyOrDefault(properties, HASH_FUNCTION, "SHA-512");
        try {
            collection = new MongoClient(hostname, port).getDB(dbName).getCollection(collectionName);
            collection.setWriteConcern(WriteConcern.ACKNOWLEDGED);
            String propJaasContext = properties.getProperty(JAAS_CONTEXT);
            if (propJaasContext != null) {
                setProperty(JAAS_CONTEXT, propJaasContext);
            }
        } catch (UnknownHostException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public DBCollection getMongoCollection() {
        return collection;
    }

    @Override
    public Enumeration getGroupNames(String login) throws InvalidOperationException, NoSuchUserException {
        DBObject query = QueryBuilder.start(loginProperty).is(login).get();
        DBObject userObject = collection.findOne(query);
        if (userObject == null) {
            throw new NoSuchUserException(String.format("User with login property(%s)==%s not found", loginProperty, login));
        }
        BasicDBList groupsList = (BasicDBList) userObject.get(groupsProperty);
        return new Vector(groupsList).elements();
    }

    @Override
    public String getAuthType() {
        return AUTH_TYPE;
    }

    private String propertyOrDefault(Properties properties, String name, String defaultValue) {
        String property = properties.getProperty(name, defaultValue);
        if (defaultValue.equals(property)) {
            properties.setProperty(name, property);
        }
        logger.info("MongoRealm property "+name+" =  "+property);
        return property;
    }

    @Override
    public ObjectId createUser(String login, char[] password) {
        String randomSalt = generateRandomSalt();
        DBObject newUser = BasicDBObjectBuilder.start()
                .append(loginProperty, login)
                .append(passwordProperty, PasswordHasher.hash(concatenatePasswordWithSalt(password, randomSalt.toCharArray()), hashFunction))
                .append(saltProperty, randomSalt)
                .append(groupsProperty, new Vector<String>())
                .get();
        collection.insert(newUser);
        return (ObjectId) newUser.get("_id");
    }

    @Override
    public void changeLogin(ObjectId userId, String newLogin) {
        DBObject query = BasicDBObjectBuilder.start()
                .append("_id", userId)
                .get();
        DBObject update = BasicDBObjectBuilder.start()
                .push("$set").append(loginProperty, newLogin).pop()
                .get();
        WriteResult result = getMongoCollection().update(query, update, false, false);
        if (result.getN() == 0) {
            throw new RuntimeException(
                    String.format("Cannot change user login to %s for user with id %s, didn't find in db", newLogin, userId)
            );
        }
    }

    @Override
    public void changePassword(ObjectId userId, char[] newPassword) {
        SecureRandom random = new SecureRandom();
        String salt = new BigInteger(130, random).toString(32);
        DBObject query = BasicDBObjectBuilder.start()
                .append("_id", userId)
                .get();
        DBObject update = BasicDBObjectBuilder.start()
                .push("$set")
                .append(passwordProperty, PasswordHasher.hash(concatenatePasswordWithSalt(newPassword, salt.toCharArray()), hashFunction))
                .append(saltProperty, salt)
                .pop()
                .get();
        WriteResult updateResult = collection.update(query, update, false, false);
        if (updateResult.getN() == 0) {
            throw new IllegalArgumentException("Wrong password");
        }
    }

    @Override
    public boolean validateCredentials(String login, char[] password) {
        final DBObject findUserQuery = QueryBuilder.start(loginProperty).is(login).get();
        DBObject user = getMongoCollection().findOne(findUserQuery);
        boolean userAuthenticated = false;
        String salt;
        if (user != null) {

            //for backward compatubility
            boolean salted = user.containsField(saltProperty);
            salt = salted ? (String) user.get(saltProperty) : "";

            char[] passwordWithSalt = concatenatePasswordWithSalt(password, salt.toCharArray());
            String hashedPassword = PasswordHasher.hash(passwordWithSalt, hashFunction);
            userAuthenticated = hashedPassword.equals(user.get(passwordProperty));

            //update passwords that haven't been salted
            if (!salted & userAuthenticated) {
                SecureRandom random = new SecureRandom();
                salt = new BigInteger(130, random).toString(32);
                passwordWithSalt = concatenatePasswordWithSalt(password, salt.toCharArray());
                hashedPassword = PasswordHasher.hash(passwordWithSalt, hashFunction);
                BasicDBObject updateSaltAndPasswordQuery = new BasicDBObject()
                        .append("$set", new BasicDBObject()
                                .append(saltProperty, salt)
                                .append(passwordProperty, hashedPassword)
                        );
                collection.update(findUserQuery, updateSaltAndPasswordQuery);
            }
        }
        return userAuthenticated;
    }

    private char[] concatenatePasswordWithSalt(char[] password, char[] salt) {
        char[] passwordWithSalt = new char[password.length + salt.length];
        System.arraycopy(password, 0, passwordWithSalt, 0, password.length);
        System.arraycopy(salt, 0, passwordWithSalt, password.length, salt.length);
        return passwordWithSalt;
    }

    private String generateRandomSalt() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }
}
