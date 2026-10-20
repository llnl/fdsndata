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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.fdsn.xml.quakeml.CreationInfo;
import org.fdsn.xml.quakeml.Event;
import org.fdsn.xml.quakeml.EventParameters;
import org.fdsn.xml.quakeml.EventType;
import org.fdsn.xml.quakeml.Magnitude;
import org.fdsn.xml.quakeml.Origin;
import org.fdsn.xml.quakeml.Quakeml;
import org.fdsn.xml.quakeml.QuakemlParser;
import org.fdsn.xml.station.Channel;
import org.fdsn.xml.station.Latitude;
import org.fdsn.xml.station.Longitude;
import org.fdsn.xml.station.Network;
import org.fdsn.xml.station.Station;

import edu.iris.dmc.criteria.CriteriaException;
import edu.iris.dmc.criteria.OutputLevel;
import edu.iris.dmc.criteria.StationCriteria;
import edu.iris.dmc.criteria.WaveformCriteria;
import edu.iris.dmc.service.EventService;
import edu.iris.dmc.service.NoDataFoundException;
import edu.iris.dmc.service.ServiceNotSupportedException;
import edu.iris.dmc.service.StationService;
import edu.iris.dmc.service.WaveformService;
import edu.iris.dmc.timeseries.model.Segment;
import edu.iris.dmc.timeseries.model.Timeseries;
import edu.iris.dmc.ws.util.RespUtil;
import llnl.gnem.core.dataObjects.DataAccessException;
import llnl.gnem.core.dataObjects.EventSelectionCriteria;
import llnl.gnem.core.dataObjects.ExternalEvent;
import llnl.gnem.core.dataObjects.ExternalWaveformService;
import llnl.gnem.core.dataObjects.StaChanInfo;
import llnl.gnem.core.util.ApplicationLogger;
import llnl.gnem.core.util.Epoch;
import llnl.gnem.core.util.QuakeMLUtil;
import llnl.gnem.core.util.StreamKey;
import llnl.gnem.core.util.TimeT;
import llnl.gnem.core.waveform.merge.MergeException;
import llnl.gnem.core.waveform.seismogram.CssSeismogram;

public class FDSNWaveformService implements ExternalWaveformService {

    private final WaveformServices waveformServices;
    private final StationServices stationServices;
    private final EventServices eventServices;

    public FDSNWaveformService() {
        waveformServices = WaveformServices.getInstance();
        stationServices = StationServices.getInstance();
        eventServices = EventServices.getInstance();
    }

    @Override
    public List<String> getNetworks(String agency) throws DataAccessException {
        Set<String> result = new HashSet<>();
        StationService stationService = stationServices.getService(agency);
        if (stationService != null) {
            try {
                StationCriteria stationCriteria = new StationCriteria();
                List<Network> networkList = stationService.fetch(stationCriteria, OutputLevel.NETWORK);
                if (networkList != null) {
                    for (Network net : networkList) {
                        result.add(net.getCode());
                    }
                }
            } catch (NoDataFoundException | IOException | CriteriaException | ServiceNotSupportedException ex) {
                ApplicationLogger.getInstance().log(Level.INFO, "No network information could be retrieved.");
            }
        }

        List<String> tmp = new ArrayList<>(result);
        Collections.sort(tmp);
        return tmp;
    }

    @Override
    public List<String> getStations(String agency, String network) throws DataAccessException {
        Set<String> result = new HashSet<>();
        StationService stationService = stationServices.getService(agency);
        if (stationService != null) {
            try {
                StationCriteria stationCriteria = new StationCriteria();
                stationCriteria.addNetwork(network);
                List<Network> networkList = stationService.fetch(stationCriteria, OutputLevel.STATION);
                if (networkList != null) {
                    for (Network net : networkList) {
                        List<Station> stations = net.getStation();
                        for (Station sta : stations) {
                            result.add(sta.getCode());
                        }
                    }
                }
            } catch (NoDataFoundException | IOException | CriteriaException | ServiceNotSupportedException ex) {
                ApplicationLogger.getInstance().log(Level.INFO, "No station information could be retrieved.");
            }
        }

        List<String> tmp = new ArrayList<>(result);
        Collections.sort(tmp);
        return tmp;
    }

    @Override
    public List<String> getStations(String agency, String network, double centerLat, double centerLon, double radiusDeg) throws DataAccessException {
        Set<String> result = new HashSet<>();
        StationService stationService = stationServices.getService(agency);
        if (stationService != null) {
            try {
                StationCriteria stationCriteria = new StationCriteria();
                stationCriteria.addNetwork(network);
                stationCriteria.setLatitude(centerLat);
                stationCriteria.setLongitude(centerLon);
                stationCriteria.setMaximumRadius(radiusDeg);
                List<Network> networkList = stationService.fetch(stationCriteria, OutputLevel.STATION);
                if (networkList != null) {
                    for (Network net : networkList) {
                        List<Station> stations = net.getStation();
                        for (Station sta : stations) {
                            result.add(sta.getCode());
                        }
                    }
                }
            } catch (NoDataFoundException | IOException | CriteriaException | ServiceNotSupportedException ex) {
                ApplicationLogger.getInstance().log(Level.INFO, "No station information could be retrieved.");
            }
        }

        List<String> tmp = new ArrayList<>(result);
        Collections.sort(tmp);
        return tmp;
    }

    @Override
    public List<String> getStations(String agency, String network, double minLat, double maxLat, double minLon, double maxLon) throws DataAccessException {
        Set<String> result = new HashSet<>();
        StationService stationService = stationServices.getService(agency);
        if (stationService != null) {
            try {
                StationCriteria stationCriteria = new StationCriteria();
                stationCriteria.addNetwork(network);
                stationCriteria.setMinimumLatitude(minLat);
                stationCriteria.setMaximumLatitude(maxLat);
                stationCriteria.setMinimumLongitude(minLon);
                stationCriteria.setMaximumLongitude(maxLon);
                List<Network> networkList = stationService.fetch(stationCriteria, OutputLevel.STATION);
                if (networkList != null) {
                    for (Network net : networkList) {
                        List<Station> stations = net.getStation();
                        for (Station sta : stations) {
                            result.add(sta.getCode());
                        }
                    }
                }
            } catch (NoDataFoundException | IOException | CriteriaException | ServiceNotSupportedException ex) {
                ApplicationLogger.getInstance().log(Level.INFO, "No station information could be retrieved.");
            }
        }

        List<String> tmp = new ArrayList<>(result);
        Collections.sort(tmp);
        return tmp;
    }

    @Override
    public String getName() {
        return "FDSN";
    }

    @Override
    public String toString() {
        return "(FDSN) This (default) service retrieves data from FDSN web services.";
    }

    @Override
    public List<String> getWaveformAgencies() throws DataAccessException {
        return new ArrayList<>(waveformServices.getAgencyList());
    }

    @Override
    public List<String> getStationAgencies() throws DataAccessException {
        return new ArrayList<>(stationServices.getAgencyList());
    }

    @Override
    public List<String> getEventAgencies() throws DataAccessException {
        return new ArrayList<>(eventServices.getAgencyList());
    }

    @Override
    public Collection<CssSeismogram> getSeismogram(StreamKey key, Epoch epoch) throws DataAccessException, MergeException {
        Collection<CssSeismogram> seismograms = new ArrayList<>();
        WaveformService service = waveformServices.getService(key.getAgency());
        if (service != null) {
            try {
                Date startDate = Util.convertToDate(epoch.getStart());
                Date endDate = Util.convertToDate(epoch.getEnd());
                WaveformCriteria criteria = new WaveformCriteria();
                criteria.add(key.getNet(), key.getSta(), key.getLocationCode(), key.getChan(), startDate, endDate);

                List<Timeseries> timeseriesList = service.fetch(criteria);
                if (timeseriesList != null) {
                    for (Timeseries ts : timeseriesList) {
                        String sta = ts.getStationCode();
                        String chan = ts.getChannelCode();
                        String locid = ts.getLocation().trim();
                        if (locid.isEmpty()) {
                            locid = "--";
                        }
                        StreamKey newKey = new StreamKey(key.getAgency(), key.getNet(), sta, chan, locid);
                        Collection<Segment> segments = ts.getSegments();
                        if (segments != null) {
                            for (Segment segment : segments) {
                                CssSeismogram waveform = new CssSeismogram(newKey, segment);
                                seismograms.add(waveform);
                            }
                        }
                    }
                }
            } catch (ParseException | NoDataFoundException | IOException | CriteriaException | ServiceNotSupportedException ex) {
                ApplicationLogger.getInstance().log(Level.INFO, "No data could be retrieved.");
            }
        }
        return seismograms;
    }

    @Override
    public Map<StreamKey, Collection<CssSeismogram>> getSeismograms(String agency, String network, Collection<String> stations, Collection<String> channels, Collection<String> locids, Epoch epoch)
            throws DataAccessException, MergeException {
        Map<StreamKey, Collection<CssSeismogram>> keySeisMap = new HashMap<>();
        WaveformService waveformService = waveformServices.getService(agency);
        if (waveformService != null) {
            try {
                Date startDate = Util.convertToDate(epoch.getStart());
                Date endDate = Util.convertToDate(epoch.getEnd());
                WaveformCriteria waveformCriteria = new WaveformCriteria();
                if (locids == null) {
                    locids = new ArrayList<>();
                }
                if (locids.isEmpty()) {
                    locids.add("*");
                }
                for (String station : stations) {
                    for (String channel : channels) {
                        for (String locid : locids) {
                            waveformCriteria.add(network, station, locid, channel, startDate, endDate);
                        }
                    }
                }
                List<Timeseries> timeseriesList = waveformService.fetch(waveformCriteria);
                if (timeseriesList != null) {
                    for (Timeseries ts : timeseriesList) {
                        String sta = ts.getStationCode();
                        String chan = ts.getChannelCode();
                        String locationCode = ts.getLocation().trim();
                        if (locationCode.isEmpty()) {
                            locationCode = "--";
                        }
                        StreamKey newKey = new StreamKey(agency, network, sta, chan, locationCode);
                        Collection<CssSeismogram> seismograms = new ArrayList<>();
                        if (keySeisMap.containsKey(newKey)) {
                            seismograms = keySeisMap.get(newKey);
                        } else {
                            keySeisMap.put(newKey, seismograms);
                        }
                        Collection<Segment> segments = ts.getSegments();
                        if (segments != null) {
                            for (Segment segment : segments) {
                                if (segment.getSamplerate() <= 0) {
                                    String msg = String.format("%s has bad sample rate value of (%f). Skipping...", newKey, segment.getSamplerate());
                                    ApplicationLogger.getInstance().log(Level.WARNING, msg);
                                } else {
                                    CssSeismogram waveform = new CssSeismogram(newKey, segment);
                                    seismograms.add(waveform);
                                }
                            }
                        }
                    }
                }
            } catch (ParseException | NoDataFoundException | IOException | CriteriaException | ServiceNotSupportedException ex) {
                ApplicationLogger.getInstance().log(Level.INFO, ex.getMessage());
            }
        }
        return keySeisMap;
    }

    @Override
    public Path retrieveResponseFile(String agency, String network, String stationCode, String chan, String locid, Epoch epoch, Path path) throws DataAccessException {
        String responseContents = retrieveResponseData(agency, network, stationCode, chan, locid, epoch);
        if (responseContents != null && !responseContents.trim().isEmpty()) {
            try {
                if (Files.isDirectory(path)) {

                    String filename = buildFileName(agency, network, stationCode, chan, locid, epoch);
                    Path fullPath = path.resolve(filename);
                    Files.writeString(fullPath, responseContents);
                    return fullPath;
                } else {
                    Files.writeString(path, responseContents);
                    return path;
                }
            } catch (IOException ex) {
                throw new DataAccessException(ex.getMessage());
            }
        }
        return null;
    }

    @Override
    public Path retrieveResponseFile(String agency, String network, String stationCode, String chan, String locid, Epoch epoch) throws DataAccessException {
        String dir = System.getProperty("java.io.tmpdir");
        String fname = buildFileName(agency, network, stationCode, chan, locid, epoch);
        Path path = Paths.get(dir, fname);
        return retrieveResponseFile(agency, network, stationCode, chan, locid, epoch, path);
    }

    @Override
    public String retrieveResponseData(String agency, String network, String stationCode, String chan, String locid, Epoch epoch) throws DataAccessException {
        StationService stationService = stationServices.getService(agency);
        if (stationService != null) {
            locid = maybeFixLocid(locid);
            try {
                Date startDate = Util.convertToDate(epoch.getStart());
                Date endDate = Util.convertToDate(epoch.getEnd());
                StationCriteria stationCriteria = new StationCriteria();
                stationCriteria.setStartBefore(startDate);
                stationCriteria.setEndAfter(endDate);
                stationCriteria.addNetwork(network);
                stationCriteria.addStation(stationCode);
                stationCriteria.addChannel(chan);
                stationCriteria.addLocation(locid);
                try {
                    List<Network> networkList = stationService.fetch(stationCriteria, OutputLevel.RESPONSE);
                    if (networkList != null) {
                        for (Network ts : networkList) {
                            List<Station> retrievedStations = ts.getStation();
                            if (retrievedStations != null) {
                                for (Station sta : retrievedStations) {
                                    if (stationCode.equals(sta.getCode())) {
                                        List<Channel> retrievedChannels = sta.getChannel();
                                        if (retrievedChannels != null) {
                                            for (Channel channel : retrievedChannels) {
                                                if (channel.getCode().equals(chan)) {
                                                    StringWriter sw = new StringWriter();
                                                    RespUtil.write(new PrintWriter(sw), network, stationCode, channel);
                                                    String responseString = sw.getBuffer().toString();
                                                    return responseString;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (NoDataFoundException ex) {
                    ApplicationLogger.getInstance().log(Level.WARNING, "No response metadata found.");
                } catch (IOException ex) {
                    ApplicationLogger.getInstance().log(Level.WARNING, "IOException thrown while retrieving response data.");
                } catch (CriteriaException ex) {
                    ApplicationLogger.getInstance().log(Level.WARNING, "CriteriaException thrown while retrieving response data.");
                } catch (ServiceNotSupportedException ex) {
                    ApplicationLogger.getInstance().log(Level.WARNING, "ServiceNotSupportedException thrown while retrieving response data.");
                }
            } catch (ParseException ex) {
                ApplicationLogger.getInstance().log(Level.WARNING, "Error parsing response criteria.");
            }
        }
        return null;
    }

    public String maybeFixLocid(String locid) {
        if (locid == null || locid.trim().isEmpty()) {
            locid = "*";
        } else if (locid.equals("--")) {
            locid = "*";
        } else if (locid.equals("??")) {
            locid = "*";
        }
        return locid;
    }

    @Override
    public Map<StreamKey, StaChanInfo> getStaChanInfo(String agency, String network, Collection<String> stations, Collection<String> channels, Collection<String> locids, Epoch epoch)
            throws DataAccessException {
        Map<StreamKey, StaChanInfo> keyChanMap = new HashMap<>();
        StationService stationService = stationServices.getService(agency);
        if (stationService != null) {

            if (locids == null) {
                locids = new ArrayList<>();
            }
            if (locids.isEmpty()) {
                locids.add("*");
            }
            try {
                Date startDate = Util.convertToDate(epoch.getStart());
                Date endDate = Util.convertToDate(epoch.getEnd());
                StationCriteria stationCriteria = new StationCriteria();
                stationCriteria.setStartBefore(startDate);
                stationCriteria.setEndAfter(endDate);
                stationCriteria.addNetwork(network);
                for (String station : stations) {
                    stationCriteria.addStation(station);
                }
                for (String channel : channels) {
                    stationCriteria.addChannel(channel);

                }
                for (String locid : locids) {
                    stationCriteria.addLocation(locid);
                }
                try {
                    List<Network> networkList = stationService.fetch(stationCriteria, OutputLevel.CHANNEL);
                    processNetworkList(networkList, agency, network, keyChanMap);
                } catch (NoDataFoundException ex) {
                    ApplicationLogger.getInstance().log(Level.WARNING, "No response metadata found.");
                } catch (IOException ex) {
                    ApplicationLogger.getInstance().log(Level.WARNING, "IOException thrown while retrieving response data.");
                } catch (CriteriaException ex) {
                    ApplicationLogger.getInstance().log(Level.WARNING, "CriteriaException thrown while retrieving response data.");
                } catch (ServiceNotSupportedException ex) {
                    ApplicationLogger.getInstance().log(Level.WARNING, "ServiceNotSupportedException thrown while retrieving response data.");
                }
            } catch (ParseException ex) {
                ApplicationLogger.getInstance().log(Level.WARNING, "Error parsing response criteria.");
            }

        }
        return keyChanMap;
    }

    public void processNetworkList(List<Network> networkList, String agency, String network, Map<StreamKey, StaChanInfo> keyChanMap) {
        if (networkList != null) {
            for (Network ts : networkList) {
                List<Station> retrievedStations = ts.getStation();
                if (retrievedStations != null) {
                    for (Station sta : retrievedStations) {
                        String stationCode = sta.getCode();

                        Double staLat = sta.getLatitude().getValue();
                        Double staLon = sta.getLongitude().getValue();

                        List<Channel> retrievedChannels = sta.getChannel();
                        if (retrievedChannels != null) {
                            for (Channel chan : retrievedChannels) {
                                Double azimuth = chan.getAzimuth().getValue();
                                String channelCode = chan.getCode();
                                Double depth = chan.getDepth().getValue();
                                Double dip = chan.getDip().getValue();
                                Double elev = chan.getElevation().getValue();
                                Double lat;
                                Latitude rawLat = chan.getLatitude();
                                if (rawLat == null) {
                                    lat = staLat;
                                } else {
                                    lat = rawLat.getValue();
                                }
                                String locid = chan.getLocationCode();
                                if (locid.isEmpty()) {
                                    locid = "--";
                                }
                                Double lon;
                                Longitude rawLon = chan.getLongitude();
                                if (rawLon == null) {
                                    lon = staLon;
                                } else {
                                    lon = rawLon.getValue();
                                }
                                Double rate = chan.getSampleRate().getValue();
                                StreamKey newKey = new StreamKey(agency, network, stationCode, channelCode, locid);
                                StaChanInfo info = new StaChanInfo(stationCode, channelCode, locid, lat, lon, depth, elev, azimuth, dip, rate);
                                keyChanMap.put(newKey, info);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<ExternalEvent> getEvents(EventSelectionCriteria criteria) throws Exception {
        List<ExternalEvent> result = new ArrayList<>();

        EventService service = eventServices.getService(criteria.getAgency());
        String baseUrl = service.getBaseUrl();
        String request = "query?format=text" + criteria.getRequestString();
        String urlString = baseUrl + request;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(urlString);

        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            int code = response1.getCode();
            if (code != 404) {
                HttpEntity entity = response1.getEntity();

                if (entity != null) {
                    try (InputStream is = entity.getContent()) {
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                            String line;
                            while ((line = br.readLine()) != null) {
                                line = line.trim();
                                if (line.isEmpty()) {
                                    continue;
                                }
                                if (line.startsWith("#")) {
                                    continue;
                                }
                                try {
                                    result.add(new ExternalEvent(criteria.getAgency(), line));
                                } catch (Exception ex) {
                                    ApplicationLogger.getInstance().log(Level.FINE, "Failed parsing line: " + line);
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public ExternalEvent getSingleEvent(String agency, String eventID) throws Exception {
        EventService service = eventServices.getService(agency);
        String baseUrl = service.getBaseUrl();
        String request = "query?format=xml&eventid=" + eventID;
        String urlString = baseUrl + request;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(urlString);

        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            int code = response1.getCode();
            if (code != 404) {
                HttpEntity entity = response1.getEntity();

                if (entity != null) {
                    try (InputStream is = entity.getContent()) {
                        Quakeml message = QuakemlParser.parse(is);
                        EventParameters params = message.getEventParameters();
                        List<Event> events = params.getEvents();
                        for (Event event : events) {
                            Origin origin = getPreferredOrigin(event);
                            if (origin == null) {
                                return null;
                            }
                            Double depth = QuakeMLUtil.getDepth(origin);
                            TimeT originTime = QuakeMLUtil.getOriginTime(origin);
                            double time = originTime.getEpochTime();
                            Double lat = QuakeMLUtil.getRealQuantityValue(origin.getLatitude());
                            Double lon = QuakeMLUtil.getRealQuantityValue(origin.getLongitude());
                            if (lat == null || lon == null || time == 0) {
                                return null;
                            }

                            EventType type = event.getType();
                            String etype = type != null ? type.toString() : null;
                            CreationInfo info = origin.getCreationInfo();
                            String auth = QuakeMLUtil.createAuthString(info);
                            Double magnitude = null;
                            String magtype = null;
                            String magAuth = null;
                            for (Magnitude mag : event.getMagnitudes()) {
                                if (origin.getPublicID().equals(mag.getOriginID())) {
                                    magnitude = QuakeMLUtil.getRealQuantityValue(mag.getMag());
                                    magtype = mag.getType();
                                    CreationInfo magInfo = mag.getCreationInfo();
                                    magAuth = QuakeMLUtil.createAuthString(magInfo);
                                }

                            }
                            if (magnitude == null) {
                                for (Magnitude mag : event.getMagnitudes()) {
                                    magnitude = QuakeMLUtil.getRealQuantityValue(mag.getMag());
                                    magtype = mag.getType();
                                    CreationInfo magInfo = mag.getCreationInfo();
                                    magAuth = QuakeMLUtil.createAuthString(magInfo);
                                }
                            }
                            return new ExternalEvent(agency, eventID, originTime, lat, lon, depth, auth, magtype, magnitude, magAuth, "", etype);
                        }

                    }

                }
            }
        }
        return null;
    }

    public Origin getPreferredOrigin(Event event) {
        List<Origin> origins = event.getOrigins();
        if (origins == null || origins.isEmpty()) {
            return null;
        }
        String originID = event.getPreferredOriginID();
        if (originID != null) {
            for (Origin origin : origins) {
                if (origin.getPublicID().equals(originID)) {
                    return origin;
                }
            }
            return origins.get(0); // No origin matches the preferred origin string.
        } else {
            return origins.get(0);
        }

    }

    @Override
    public boolean keyMeetsRequirements(StreamKey key) {
        String agency = key.getAgency();
        if (agency == null || agency.isEmpty()) {
            ApplicationLogger.getInstance().log(Level.WARNING, "Agency must be set to use FDSN option to TRANSFER!");
            return false;
        }
        String networkCode = key.getNet();
        if (networkCode == null || networkCode.isEmpty()) {
            ApplicationLogger.getInstance().log(Level.WARNING, "Network (KNETWK) must be set to use FDSN option to TRANSFER!");
            return false;
        }
        String stationCode = key.getSta();
        if (stationCode == null || stationCode.isEmpty()) {
            ApplicationLogger.getInstance().log(Level.WARNING, "Station Code (KSTNM) must be set to use FDSN option to TRANSFER!");
            return false;
        }
        String chan = key.getChan();
        if (chan == null || chan.isEmpty()) {
            ApplicationLogger.getInstance().log(Level.WARNING, "Channel Code (KCMPNM) must be set to use FDSN option to TRANSFER!");
            return false;
        }
        String locid = key.getLocationCode();
        if (locid == null || locid.isEmpty()) {
            ApplicationLogger.getInstance().log(Level.WARNING, "Location Code (KHOLE) must be set to use FDSN option to TRANSFER!");
            return false;
        }

        return true;
    }

    @Override
    public boolean isRequiresNetworkCode() {
        return false;
    }
}
