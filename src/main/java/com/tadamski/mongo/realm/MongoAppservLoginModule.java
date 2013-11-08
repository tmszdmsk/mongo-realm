package com.tadamski.mongo.realm;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.QueryBuilder;
import com.sun.appserv.security.AppservPasswordLoginModule;
import com.sun.enterprise.security.auth.realm.InvalidOperationException;
import com.sun.enterprise.security.auth.realm.NoSuchUserException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.security.auth.login.LoginException;

public class MongoAppservLoginModule extends AppservPasswordLoginModule {

    private DBCollection collection;

    public MongoAppservLoginModule() throws UnknownHostException {
        collection = new MongoClient().getDB("users").getCollection("users");
    }

    @Override
    protected void authenticateUser() throws LoginException {
        String _password = new String(_passwd);
        System.out.println("MyRealm LoginModule authenticateUser(), _username:"
                + _username + ", _password:" + _password + ", _currentrealm:" + _currentRealm);

        if (!(_currentRealm instanceof MongoAppservRealm)) {
            throw new LoginException("Realm not MyRealm. Check 'login.conf'.");
        }
        MongoAppservRealm myRealm = (MongoAppservRealm) _currentRealm;

        // Authenticate User
        if (!doAuthentication(_username, _password)) {
            //Login failed
            throw new LoginException("MyRealm LoginModule: Login Failed for user " + _username);
        }

        // Login succeeded
        System.out.println("MyRealm LoginModule: Login Succeded for user " + _username);

        // Get group names for the authenticated user from the Realm class
        Enumeration enumeration = null;
        try {
            enumeration = myRealm.getGroupNames(_username);
        } catch (InvalidOperationException e) {
            throw new LoginException("InvalidOperationException was thrown for getGroupNames() on MyRealm");
        } catch (NoSuchUserException e) {
            throw new LoginException("NoSuchUserException was thrown for getGroupNames() on MyRealm");
        }

        // Convert the Enumeration to String[]
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

    /**
     * Performs the authentication.
     */
    private boolean doAuthentication(String user, String password) {
        DBObject findOne = collection.findOne(QueryBuilder.start("username").is(user).and("password").is(password).get());
        return findOne != null;
    }

}
