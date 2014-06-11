package com.tadamski.glassfish.mongo.realm;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.sun.appserv.security.AppservPasswordLoginModule;
import com.sun.enterprise.security.auth.realm.InvalidOperationException;
import com.sun.enterprise.security.auth.realm.NoSuchUserException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.security.auth.login.LoginException;

public class MongoLoginModule extends AppservPasswordLoginModule {

    @Override
    protected void authenticateUser() throws LoginException {
        if (!(_currentRealm instanceof MongoRealm)) {
            throw new LoginException("Not supported realm, check login.conf");
        }
        MongoRealm mongoRealm = (MongoRealm) _currentRealm;

        if (!checkCredentials(mongoRealm, _username, _passwd)) {
            throw new LoginException(String.format("Authenthication failed for user %s", _username));
        }
        // Get group names for the authenticated user from the Realm class
        Enumeration enumeration = null;
        try {
            enumeration = mongoRealm.getGroupNames(_username);
        } catch (InvalidOperationException e) {
            throw new LoginException("InvalidOperationException was thrown for getGroupNames() on MongoRealm");
        } catch (NoSuchUserException e) {
            throw new LoginException("NoSuchUserException was thrown for getGroupNames() on MongoRealm");
        }

        List<String> g = new ArrayList<>();
        while (enumeration != null && enumeration.hasMoreElements()) {
            g.add((String) enumeration.nextElement());
        }

        String[] authenticatedGroups = g.toArray(new String[g.size()]);

        // Call commitUserAuthentication with the group names the user belongs to.
        // Note that this method is called after the authentication has succeeded.
        // If authentication failed do not call this method. Global instance field
        // succeeded is set to true by this method.
        commitUserAuthentication(authenticatedGroups);
    }

    private boolean checkCredentials(MongoRealm mongoRealm, String login, char[] password) {
        try {
            final String loginProperty = mongoRealm.getProperty(MongoRealm.LOGIN_PROPERTY);
            final String passwordProperty = mongoRealm.getProperty(MongoRealm.PASSWORD_PROPERTY);
            final String hashFunction = mongoRealm.getProperty(MongoRealm.HASH_FUNCTION);

            final String saltProperty = mongoRealm.getProperty(MongoRealm.SALT_PROPERTY);
            final DBObject findSaltQuery = QueryBuilder.start(loginProperty).is(login).get();
            String salt = (String) mongoRealm.getMongoCollection().findOne(findSaltQuery).get(saltProperty);
            
            //for backward compatibility
            if (salt == null) {
                SecureRandom random = new SecureRandom();
                salt = new BigInteger(130, random).toString(32);
                BasicDBObject updateSaltQuery = new BasicDBObject();
                updateSaltQuery.append("$set", new BasicDBObject().append(saltProperty, salt));
                mongoRealm.getMongoCollection().update(findSaltQuery, updateSaltQuery);
            }

            char[] passwordWithSalt = new char[password.length + salt.length()];
            System.arraycopy(password, 0, passwordWithSalt, 0, password.length);
            System.arraycopy(salt.toCharArray(), 0, passwordWithSalt, password.length, salt.length());
            String hashedPassword = PasswordHasher.hash(password, hashFunction);
            final DBObject query = QueryBuilder.start(loginProperty).is(login).and(passwordProperty).is(hashedPassword).get();
            DBObject userWithGivenLoginAndPassword = mongoRealm.getMongoCollection().findOne(query);
            final boolean userFound = userWithGivenLoginAndPassword != null;
            return userFound;
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }
}
