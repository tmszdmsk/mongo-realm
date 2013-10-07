package com.tadamski.mongo.realm;

import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.sun.appserv.security.AppservRealm;
import com.sun.enterprise.security.auth.realm.BadRealmException;
import com.sun.enterprise.security.auth.realm.InvalidOperationException;
import com.sun.enterprise.security.auth.realm.NoSuchRealmException;
import com.sun.enterprise.security.auth.realm.NoSuchUserException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

/**
 *
 * @author tmszdmsk
 */
public class MongoAppservRealm extends AppservRealm {

    public static final String AUTH_TYPE = "MongoAuth";
    private static final String JAAS_CONTEXT = "jaas-context";
    private DBCollection collection;

    @Override
    protected void init(Properties properties) throws BadRealmException, NoSuchRealmException {
        try {
            collection = new MongoClient().getDB("dbName").getCollection("dbCollection");

            System.out.println("Init MyRealm");

            // Pass the properties declared in the console to the system
            String propJaasContext = properties.getProperty(JAAS_CONTEXT);
            if (propJaasContext != null) {
                setProperty(JAAS_CONTEXT, propJaasContext);
            }
        } catch (UnknownHostException ex) {
            throw new RuntimeException(ex);
        }

    }

    @Override
    public Enumeration getGroupNames(String user) throws InvalidOperationException, NoSuchUserException {
        return new Vector<String>(Arrays.asList("USER_GROUP")).elements();
    }

    @Override
    public String getAuthType() {
        return AUTH_TYPE;
    }

}
