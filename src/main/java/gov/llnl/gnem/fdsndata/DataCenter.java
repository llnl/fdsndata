/*-
 * #%L
 * FDSN Data Services (FDSNData)
 *  LLNL-CODE-2025872
 *  This work was performed under the auspices of the U.S. Department of Energy
 *  by Lawrence Livermore National Laboratory under Contract DE-AC52-07NA27344.
 * %%
 * Copyright (C) 2026 Lawrence Livermore National Laboratory
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package gov.llnl.gnem.fdsndata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author dodge1
 */
public class DataCenter {

    private final String location;
    private final String website;
    private final String lastUpdate;
    private final String description;
    private final String name;
    private final List<DataService> services;

    public DataCenter(String location, String website, String lastUpdate, String description, String name, List<DataService> services) {
        this.location = location;
        this.website = website;
        this.lastUpdate = lastUpdate;
        this.description = description;
        this.name = name;
        this.services = new ArrayList<>(services);
    }

    public Collection<String> getServiceList() {
        Collection<String> result = new ArrayList<>();
        for (DataService ds : services) {
            result.add(ds.getServiceName());
        }
        return result;
    }

    public String getCompoundDescription() {
        return description + ", " + location + "; " + website;
    }

    /**
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @return the website
     */
    public String getWebsite() {
        return website;
    }

    /**
     * @return the lastUpdate
     */
    public String getLastUpdate() {
        return lastUpdate;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the services
     */
    public List<DataService> getServices() {
        return new ArrayList<>(services);
    }

    public boolean hasStationService() {
        for (DataService ds : services) {
            if (ds.getServiceName().equals("stationService")) {
                return true;
            }
        }
        return false;
    }

    public String getStationServiceUrl() {
        for (DataService ds : services) {
            if (ds.getServiceName().equals("stationService")) {
                return ds.getUrl();
            }
        }
        return null;
    }

    public boolean hasWaveformService() {
        for (DataService ds : services) {
            if (ds.getServiceName().equals("dataselectService")) {
                return true;
            }
        }
        return false;
    }

    public String getWaveformServiceUrl() {
        for (DataService ds : services) {
            if (ds.getServiceName().equals("dataselectService")) {
                return ds.getUrl();
            }
        }
        return null;
    }

    public boolean hasEventService() {
        for (DataService ds : services) {
            if (ds.getServiceName().equals("eventService")) {
                return true;
            }
        }
        return false;
    }

    public String getEventServiceUrl() {
        for (DataService ds : services) {
            if (ds.getServiceName().equals("eventService")) {
                return ds.getUrl();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "DataCenter [name=" + name + ", location=" + location + ", lastUpdate=" + lastUpdate + ", website=" + website + ", description="
                + description + ", services=" + services + "]";
    }

}
