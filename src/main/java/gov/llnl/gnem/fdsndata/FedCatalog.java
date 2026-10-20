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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

/**
 *
 * @author dodge1
 */
public class FedCatalog {
    
    private static List<String> BLACK_LISTED_STATION_SERVICE_SOURCES = new ArrayList<>();
    
    static {
        BLACK_LISTED_STATION_SERVICE_SOURCES.add("IRISPH5");
        BLACK_LISTED_STATION_SERVICE_SOURCES.add("RASPISHAKE");
    }

    private List<DataCenter> dcs;

    public FedCatalog() throws IOException {
        dcs = new ArrayList<>();
 
        URL url = new URL("https://service.earthscope.org/irisws/fedcatalog/1/datacenters");
        try (InputStream is = url.openStream()) {
            InputStreamReader isr = new InputStreamReader(is);
            JsonReader reader = new JsonReader(isr);
            while (reader.hasNext()) {

                JsonToken token = reader.peek();
                switch (token) {
                    case BEGIN_ARRAY:
                        reader.beginArray();
                        while (reader.hasNext()) {
                            JsonToken token2 = reader.peek();
                            switch (token2) {
                                case BEGIN_OBJECT:

                                    dcs.addAll(processObject(reader));
                                    break;
                                case END_OBJECT:
                                    System.out.println("end object");
                                    break;
                                default:
                                    System.out.println(token2);
                            }
                        }

                        break;
                    case END_ARRAY:
                        reader.endArray();
                        break;
                }
            }
            //only add the static set one time
            dcs.addAll(getStaticDataCenterDefinitions());
        }
        for (DataCenter dc : dcs) {
            if(dc.getName().equals("IRISPH5")){
                dcs.remove(dc);
                return;
            }
        }
    }
    
    private List<DataCenter> processObject(JsonReader reader) throws IOException {
        ArrayList<DataCenter> result = new ArrayList<>();
        String location = null;
        String website = null;
        String lastUpdate = null;
        String description = null;
        String name = null;

        String tokenName = "";
        reader.beginObject();
        while (reader.hasNext()) {
            JsonToken token3 = reader.peek();
            switch (token3) {
                case NAME:
                    tokenName = reader.nextName();

                    break;
                case STRING: {
                    String val3 = reader.nextString();
                    switch (tokenName) {
                        case "name":
                            name = val3;
                            break;
                        case "location":
                            location = val3;
                            break;
                        case "website":
                            website = val3;
                            break;
                        case "lastUpdate":
                            lastUpdate = val3;
                            break;
                        case "description":
                            description = val3;
                            break;
                    }
                    break;
                }
                case BEGIN_OBJECT:
                    List<DataService> services = processServiceURLs(reader);
                    DataCenter dc = new DataCenter(location, website, lastUpdate, description, name, services);
                    result.add(dc);
                    break;
                default:
                    System.out.println(token3);
                    break;
            }
        }
        reader.endObject();
        return result;
    }

    public static FedCatalog makeFromList() {
        List<DataCenter> dcs = new ArrayList<>();
        String location = "North America";
        String website = "http://service.iris.edu";
        String lastUpdate = "";
        String description = "IRIS Data Management Center";
        String name = "IRISDMC";
        List<DataService> services = new ArrayList<>();
        services.add(new DataService("waveformService", "http://service.iris.edu/fdsnws/dataselect/1/"));
        services.add(new DataService("stationService", "http://service.iris.edu/fdsnws/station/1/"));
        services.add(new DataService("eventService", "http://service.iris.edu/fdsnws/event/1/"));
        DataCenter dc = new DataCenter(location, website, lastUpdate, description, name, services);
        dcs.add(dc);

        location = "North America";
        website = "http://service.ncedc.org";
        lastUpdate = "";
        description = "Northern California Earthquake Data Center";
        name = "NCEDC";
        services = new ArrayList<>();
        services.add(new DataService("waveformService", "http://service.ncedc.org/fdsnws/dataselect/1/"));
        services.add(new DataService("stationService", "http://service.ncedc.org/fdsnws/station/1/"));
        services.add(new DataService("eventService", "http://service.ncedc.org/fdsnws/event/1/"));
        dc = new DataCenter(location, website, lastUpdate, description, name, services);
        dcs.add(dc);

        location = "Europe";
        website = "http://webservices.rm.ingv.it";
        lastUpdate = "";
        description = "INGC";
        name = "INGV";
        services = new ArrayList<>();
        services.add(new DataService("waveformService", "http://webservices.rm.ingv.it/fdsnws/dataselect/1/"));
        services.add(new DataService("stationService", "http://webservices.rm.ingv.it/fdsnws/station/1/"));
        services.add(new DataService("eventService", "http://webservices.rm.ingv.it/fdsnws/event/1/"));
        dc = new DataCenter(location, website, lastUpdate, description, name, services);
        dcs.add(dc);

        location = "Europe";
        website = "http://geofon.gfz-potsdam.de/";
        lastUpdate = "";
        description = "GEOFON Program, GFZ";
        name = "GEOFON";
        services = new ArrayList<>();
        services.add(new DataService("waveformService", "http://geofon.gfz-potsdam.de/fdsnws/dataselect/1/"));
        services.add(new DataService("stationService", "http://geofon.gfz-potsdam.de/fdsnws/station/1/"));
        dc = new DataCenter(location, website, lastUpdate, description, name, services);
        dcs.add(dc);

        location = "Global";
        website = "https://www.isc.ac.uk/";
        lastUpdate = "";
        description = "International Seismological Centre";
        name = "ISC";
        services = new ArrayList<>();
        services.add(new DataService("eventService", "http://isc-mirror.iris.washington.edu/fdsnws/event/1/"));
        dc = new DataCenter(location, website, lastUpdate, description, name, services);
        dcs.add(dc);

        return new FedCatalog(dcs);
    }

    /**
     * Provide ability to test.
     * @param dcs
     */
    protected FedCatalog(List<DataCenter> dcs) {
        this.dcs = new ArrayList<>(dcs);
    }

    public boolean supports(String source) {
        for (DataCenter dc : dcs) {
            if (dc.getName().equals(source)) {
                return true;
            }
        }
        return false;
    }

    public DataCenter getDataCenter(String source) {
        for (DataCenter dc : dcs) {
            if (dc.getName().equals(source)) {
                return dc;
            }
        }
        return null;
    }

    /**
     * getStationServiceSources returns a List of Data Center names that has a Station Service defined. This includes
     * the list published by IRIS and a static collection defined in this class.
     * 
     * @return
     */
    public Collection<String> getStationServiceSources() {
        Collection<String> result = new ArrayList<>();
        for (DataCenter dc : dcs) {
            if (dc.hasStationService()) {
                result.add(dc.getName());
            }
        }
        return result;
    }
    
    /**
     * getStationServiceSources returns a List of Data Center names that has a Station Service defined. This includes
     * the list published by IRIS and a static collection defined in this class.
     * 
     * If onlyMetadataCapture is set to true, then known Data Centers that have stations services that we do not want to
     * capture additional information about will be pruned.
     * 
     * @param onlyMetadataCapture
     * @return
     */
    public Collection<String> getStationServiceSources(boolean onlyMetadataCapture) {
        Collection<String> result = getStationServiceSources();
        if (onlyMetadataCapture) {
            for (String blackListed : BLACK_LISTED_STATION_SERVICE_SOURCES) {
                result.remove(blackListed);
            }
        }

        return result;
    }

    private List<DataService> processServiceURLs(JsonReader reader) throws IOException {
        List<DataService> services = new ArrayList<>();
        String serviceName = null;
        String url = null;
        reader.beginObject();
        while (reader.hasNext()) {
            JsonToken token3 = reader.peek();
            switch (token3) {
                case NAME:
                    serviceName = reader.nextName();
                    break;
                case STRING:
                    url = reader.nextString();
                    if (serviceName != null) {
                        services.add(new DataService(serviceName, url));
                        serviceName = null;
                    }
                    break;
            }
        }
        reader.endObject();
        return services;
    }

    private Collection<? extends DataCenter> getStaticDataCenterDefinitions() {
        ArrayList<DataCenter> result = new ArrayList<>();
        String location = "New Zealand";
        String website = "http://info.geonet.org.nz/display/equip/New+Zealand+National+Seismograph+Network";
        String lastUpdate = "";
        String description = "New Zealand National Seismograph Network";
        String name = "NZNSN";
        List<DataService> services = new ArrayList<>();
        services.add(new DataService("stationService", "http://service.geonet.org.nz/fdsnws/station/1/"));
        DataCenter dc = new DataCenter(location, website, lastUpdate, description, name, services);
        result.add(dc);

        location = "Texas, USA";
        website = "http://www.beg.utexas.edu/texnet-cisr/texnet";
        lastUpdate = "";
        description = "Texas Earthquake Data Center";
        name = "TEXNET";
        services = new ArrayList<>();
        services.add(new DataService("stationService", "http://rtserve.beg.utexas.edu/fdsnws/station/1/"));
        dc = new DataCenter(location, website, lastUpdate, description, name, services);
        result.add(dc);
        

        location = "Global";
        website = "https://www.isc.ac.uk";
        lastUpdate = "";
        description = "International Seismological Centre";
        name = "ISC";
        services = new ArrayList<>();
        services.add(new DataService("eventService", "http://www.isc.ac.uk/fdsnws/event/1/"));
        dc = new DataCenter(location, website, lastUpdate, description, name, services);
        result.add(dc);

        location = "Global";
        website = "https://www.usgs.gov/programs/earthquake-hazards/national-earthquake-information-center-neic";
        lastUpdate = "";
        description = "National Earthquake Information Center";
        name = "NEIC";
        services = new ArrayList<>();
        services.add(new DataService("eventService", "https://earthquake.usgs.gov/fdsnws/event/1/"));
        dc = new DataCenter(location, website, lastUpdate, description, name, services);
        result.add(dc);

        location = "New Zealand";
        website = "https://www.geonet.org.nz/";
        lastUpdate = "";
        description = "New Zealand National Seismograph Network ";
        name = "NZNSN";
        services = new ArrayList<>();
        services.add(new DataService("eventService", "https://service.geonet.org.nz/fdsnws/event/1/"));
        dc = new DataCenter(location, website, lastUpdate, description, name, services);
        result.add(dc);
        
        
        return result;
    }

    @Override
    public String toString() {
        return "FedCatalog [dcs=" + dcs + "]";
    }

    public List<DataCenter> getDataCenters() {
        List<DataCenter> results = new ArrayList<>();
        results.addAll(dcs);
        return results;
    }
    
    
}
