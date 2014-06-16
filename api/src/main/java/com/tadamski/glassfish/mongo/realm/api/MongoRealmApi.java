/*
 * The MIT License
 *
 * Copyright 2014 t.adamski.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.tadamski.glassfish.mongo.realm.api;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.sun.enterprise.security.auth.realm.NoSuchRealmException;
import com.sun.enterprise.security.auth.realm.Realm;
import com.tadamski.glassfish.mongo.realm.internal.api.MongoRealmInternalApi;
import static com.tadamski.glassfish.mongo.realm.api.MongoRealmProperties.*;
import org.bson.types.ObjectId;

/**
 *
 * @author t.adamski
 */
public class MongoRealmApi {

    private MongoRealmInternalApi mongoRealm;

    public MongoRealmApi(String realmName) {
        try {
            mongoRealm = (MongoRealmInternalApi) Realm.getInstance(realmName);
        } catch (NoSuchRealmException ex) {
            throw new RuntimeException("Cannot find realm with given name: " + realmName, ex);
        }
    }

    public ObjectId createUser(String login, char[] password) {
        return mongoRealm.createUser(login, password);
    }

    public void changeLogin(ObjectId userId, String currentLogin, String newLogin) {
        mongoRealm.changeLogin(userId, currentLogin, newLogin);
    }

    public void changePassword(ObjectId userId, char[] currentPassword, char[] newPassword) {
        mongoRealm.changePassword(userId, currentPassword, newPassword);
    }

    public DBCursor getUsers() {
        return getUsers(BasicDBObjectBuilder.start().get());

    }

    public DBCursor getUsers(DBObject query) {
        return mongoRealm.getMongoCollection().find();

    }

    public DBObject getUserByLogin(String login) {
        DBObject queryByLogin = BasicDBObjectBuilder.start(getProperty(LOGIN_PROPERTY), login).get();
        return mongoRealm.getMongoCollection()
                .findOne(
                        queryByLogin,
                        excludePasswordAndSaltProperties()
                );
    }
    
    public void deleteUser(ObjectId userId){
        mongoRealm.getMongoCollection().remove(
                BasicDBObjectBuilder.start("_id", userId).get()
        );
    }

    public boolean validateCredentials(String login, char[] password) {
        return mongoRealm.validateCredentials(login, password);
    }

    public String getProperty(String propertyName) {
        String propertyValue = mongoRealm.getProperty(propertyName);
        if (propertyValue == null) {
            throw new IllegalArgumentException("requested property[" + propertyName + "] cannot be found");
        }
        return propertyValue;
    }

    private DBObject excludePasswordAndSaltProperties() {
        return BasicDBObjectBuilder.start()
                .append(getProperty(MongoRealmProperties.PASSWORD_PROPERTY), 0)
                .append(getProperty(MongoRealmProperties.SALT_PROPERTY), 0)
                .get();
    }

}
