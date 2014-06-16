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

package com.tadamski.glassfish.mongo.realm.test.war;

import com.mongodb.DBObject;
import com.tadamski.glassfish.mongo.realm.api.MongoRealmApi;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.bson.types.ObjectId;

/**
 *
 * @author t.adamski
 */
@Path("rest")
@ApplicationScoped
public class TestRest {
    
    @GET
    public String test(){
        MongoRealmApi mongoRealmApi = new MongoRealmApi("attensee-realm");
        DBObject userByLogin = mongoRealmApi.getUserByLogin("testUser2");
        mongoRealmApi.deleteUser((ObjectId) userByLogin.get("_id"));
        
        ObjectId userId = mongoRealmApi.createUser("testUser", "testPassword".toCharArray());
        mongoRealmApi.changeLogin((ObjectId) userByLogin.get("_id"), "testUser", "testUser2");
        mongoRealmApi.changeLogin(userId, "testUser", "testUser2");
        mongoRealmApi.validateCredentials("testUser2", "testPassword".toCharArray());
        mongoRealmApi.changePassword(userId, "testPassword".toCharArray(), "testPassword2".toCharArray());
        mongoRealmApi.validateCredentials("testUser2", "testPassword2".toCharArray());
        return mongoRealmApi.getUserByLogin("testUser2").toString();
        
        
    }
}
