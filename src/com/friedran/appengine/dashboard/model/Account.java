/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.friedran.appengine.dashboard.model;

import java.util.ArrayList;
import java.util.List;

public class Account {
    protected int id;
    protected String name;
    protected List<App> apps;

    Account() {
        // required by ormlite
    }

    public Account(String name) {
        this.id = 0;    // TODO: Currently just a mockup
        this.name = name;
        this.apps = new ArrayList<App>();
    }

    public Account(String name, List apps) {
        this.id = 0;    // TODO: Currently just a mockup
        this.name = name;
        this.apps = apps;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<App> apps() {
        return apps;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id=").append(id);
        sb.append(", ").append("name=").append(name);
        return sb.toString();
    }
}
