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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import llnl.gnem.core.util.Epoch;
import llnl.gnem.core.util.StreamKey;
import llnl.gnem.core.waveform.seismogram.CssSeismogram;

public class FDSNOpsImplTest {

    public FDSNOpsImplTest() {
    }

    /**
     * Test of getWaveformAgencies method, of class FDSNWaveformService.
     */
    @org.junit.jupiter.api.Test
    public void testGetWaveformAgencies() throws Exception {
        System.out.println("getWaveformAgencies");
        List<String> result = new FDSNWaveformService().getWaveformAgencies();
        assertTrue(result.size() >= 7);
        assertTrue(result.contains("IRISDMC"));
    }

    /**
     * Test of getStationAgencies method, of class FDSNWaveformService.
     */
    @org.junit.jupiter.api.Test
    public void testGetStationAgencies() throws Exception {
        System.out.println("getStationAgencies");
        List<String> result = new FDSNWaveformService().getStationAgencies();
        assertTrue(result.size() >= 20);
        assertTrue(result.contains("IRISDMC"));
    }

    /**
     * Test of getEventAgencies method, of class FDSNWaveformService.
     */
    @org.junit.jupiter.api.Test
    public void testGetEventAgencies() throws Exception {
        System.out.println("getEventAgencies");
        List<String> result = new FDSNWaveformService().getEventAgencies();
        assertTrue(result.size() >= 2);
        assertTrue(result.contains("NEIC"));
        assertTrue(result.contains("ISC"));
    }

    /**
     * Test of getSeismogram method, of class FDSNWaveformService.
     */
    @org.junit.jupiter.api.Test
    public void testGetSeismogram() throws Exception {
        System.out.println("getSeismogram");
        String agency = "IRISDMC";
        StreamKey key = new StreamKey(agency, "US", "TPNV", "BHZ", "00");
        Epoch epoch = new Epoch(1777475172.0, 1777475472.0);

        Collection<CssSeismogram> result = new FDSNWaveformService().getSeismogram(key, epoch);
        assertTrue(result != null && result.size() == 1);
        CssSeismogram seis = result.iterator().next();
        assertEquals(key, seis.getStreamKey());
        assertEquals(seis.getSamprate(), 40.0, 0.001);
        assertEquals(seis.getLength(), 12001);
    }

    /**
     * Test of getSeismograms method, of class FDSNWaveformService.
     */
    @org.junit.jupiter.api.Test
    public void testGetSeismograms() throws Exception {
        System.out.println("getSeismograms");
        String agency = "IRISDMC";
        String network = "US";
        Collection<String> stations = new ArrayList<>();
        stations.add("TPNV");
        Collection<String> channels = new ArrayList<>();
        channels.add("BH1");
        channels.add("BHZ");
        Collection<String> locids = new ArrayList<>();
        locids.add("00");
        Epoch epoch = new Epoch(1777475172.0, 1777475472.0);
        Map<StreamKey, Collection<CssSeismogram>> result = new FDSNWaveformService().getSeismograms(agency, network, stations, channels, locids, epoch);
        assertTrue(result.size() == 2);
        channels.clear();
        channels.add("B*");
        result = new FDSNWaveformService().getSeismograms(agency, network, stations, channels, locids, epoch);
        assertTrue(result.size() >= 3);
        stations.add("B*");
        result = new FDSNWaveformService().getSeismograms(agency, network, stations, channels, locids, epoch);
        assertTrue(result.size() >= 9);
    }

}
