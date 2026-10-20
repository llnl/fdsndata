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

import org.junit.jupiter.api.Test;

import llnl.gnem.core.dataObjects.EventSelectionCriteria;
import llnl.gnem.core.dataObjects.MinMax;
import llnl.gnem.core.dataObjects.RestrictionCircle;
import llnl.gnem.core.dataObjects.RestrictionRectangle;

public class EventSelectionCriteriaTest {

    RestrictionRectangle rect = new RestrictionRectangle(1.0, 2.0, 3.0, 4.0);
    MinMax time = new MinMax(0.0, 10.0);
    MinMax mag = new MinMax(0.0, 5.0);
    MinMax dep = new MinMax(50, 100);
    RestrictionCircle srcCircle = new RestrictionCircle(10.0, 10.0, 10.0);

    public EventSelectionCriteriaTest() {
    }

    @Test
    public void testConstructor1() {
        System.out.println("testConstructor1");
        EventSelectionCriteria instance = new EventSelectionCriteria("NEIC", "1234");
        String expResult = "&eventid=1234";
        String result = instance.getRequestString();
        assertEquals(expResult, result);
    }

    @Test
    public void testConstructor2() {
        System.out.println("testConstructor2");
        EventSelectionCriteria instance = new EventSelectionCriteria("NEIC", rect, time);
        String expResult = "&minlatitude=1.0&maxlatitude=2.0&minlongitude=3.0&maxlongitude=4.0&starttime=1970-01-01T00:00:00.000&endtime=1970-01-01T00:00:10.000";
        String result = instance.getRequestString();
        assertEquals(expResult, result);
    }

    @Test
    public void testConstructor3() {
        System.out.println("testConstructor3");
        EventSelectionCriteria instance = new EventSelectionCriteria("NEIC", rect, time, mag);
        String expResult = "&minlatitude=1.0&maxlatitude=2.0&minlongitude=3.0&maxlongitude=4.0&starttime=1970-01-01T00:00:00.000&endtime=1970-01-01T00:00:10.000&minmagnitude=0.0&maxmagnitude=5.0";
        String result = instance.getRequestString();
        assertEquals(expResult, result);
    }

    @Test
    public void testConstructor4() {
        System.out.println("testConstructor4");
        EventSelectionCriteria instance = new EventSelectionCriteria("NEIC", rect, time, mag, dep);
        String expResult = "&minlatitude=1.0&maxlatitude=2.0&minlongitude=3.0&maxlongitude=4.0&starttime=1970-01-01T00:00:00.000&endtime=1970-01-01T00:00:10.000&minmagnitude=0.0&maxmagnitude=5.0&mindepth=50.0&maxdepth=100.0";
        String result = instance.getRequestString();
        assertEquals(expResult, result);
    }

    @Test
    public void testConstructor5() {
        System.out.println("testConstructor5");
        EventSelectionCriteria instance = new EventSelectionCriteria("NEIC", srcCircle, time);
        String expResult = "&latitude=10.0&longitude=10.0&maxradius=10.0&starttime=1970-01-01T00:00:00.000&endtime=1970-01-01T00:00:10.000";
        String result = instance.getRequestString();
        assertEquals(expResult, result);
    }

    @Test
    public void testConstructor6() {
        System.out.println("testConstructor6");
        EventSelectionCriteria instance = new EventSelectionCriteria("NEIC", srcCircle, time,mag);
        String expResult = "&latitude=10.0&longitude=10.0&maxradius=10.0&starttime=1970-01-01T00:00:00.000&endtime=1970-01-01T00:00:10.000&minmagnitude=0.0&maxmagnitude=5.0";
        String result = instance.getRequestString();
        assertEquals(expResult, result);
    }

    @Test
    public void testConstructor7() {
        System.out.println("testConstructor7");
        EventSelectionCriteria instance = new EventSelectionCriteria("NEIC", srcCircle, time,mag,dep);
        String expResult = "&latitude=10.0&longitude=10.0&maxradius=10.0&starttime=1970-01-01T00:00:00.000&endtime=1970-01-01T00:00:10.000&minmagnitude=0.0&maxmagnitude=5.0&mindepth=50.0&maxdepth=100.0";
        String result = instance.getRequestString();
        assertEquals(expResult, result);
    }

}
