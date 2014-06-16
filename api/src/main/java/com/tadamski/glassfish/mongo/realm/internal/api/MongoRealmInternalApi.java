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
package com.tadamski.glassfish.mongo.realm.internal.api;

import com.mongodb.DBCollection;
import org.bson.types.ObjectId;

public interface MongoRealmInternalApi {

    DBCollection getMongoCollection();

    String getProperty(String propertyName);

    boolean validateCredentials(String login, char[] password);

    ObjectId createUser(String login, char[] password);

    void changeLogin(ObjectId userId, String currentLogin, String newLogin);
    
    void changePassword(ObjectId userId, char[] currentPassword, char[] newPassword);
}
