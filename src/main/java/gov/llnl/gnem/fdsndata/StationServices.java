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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.iris.dmc.service.StationService;
import llnl.gnem.core.dataObjects.DataAccessException;

public class StationServices {

    Map<String, StationService> agencyServiceMap;
    private boolean initialized;

    private StationServices() {
        agencyServiceMap = new HashMap<>();
        initialized = false;
    }

    public void initialize() throws DataAccessException {
        if (initialized) {
            return;
        }
        try{
        FedCatalog cat = new FedCatalog();
        for (DataCenter dc : cat.getDataCenters()) {
            if (dc.hasStationService()) {
                String url = dc.getStationServiceUrl();
                StationService service = new StationService(url, "2.0.19", "1.1", "STATION_SERVICES");
                agencyServiceMap.put(dc.getName(), service);
            }
        }
        initialized = true;
        }
        catch(IOException ex){
            throw new DataAccessException(ex.getMessage());
        }
    }

    public StationService getService(String agency) throws DataAccessException {
        if (!initialized) {
            initialize();
        }

        return agencyServiceMap.get(agency);
    }

    public Collection<String> getAgencyList() throws DataAccessException {
        if (!initialized) {
            initialize();
        }
        if (agencyServiceMap.isEmpty()) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>(agencyServiceMap.keySet());
        }
    }

    public static StationServices getInstance() {
        return WaveformServicesHolder.INSTANCE;
    }

    private static class WaveformServicesHolder {

        private static final StationServices INSTANCE = new StationServices();
    }
}
