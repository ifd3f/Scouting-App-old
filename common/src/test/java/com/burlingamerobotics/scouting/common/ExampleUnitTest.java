package com.burlingamerobotics.scouting.common;

import com.burlingamerobotics.scouting.common.data.Competition;
import com.burlingamerobotics.scouting.common.data.CompetitionBuilder;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void testTBA() {
        CompetitionBuilder dat = BlueAllianceAPI.INSTANCE.fetchCompetition("2018ohmv");
        System.out.println(dat);
        System.out.println(dat.getQualSchedule().getMatches());
    }

}