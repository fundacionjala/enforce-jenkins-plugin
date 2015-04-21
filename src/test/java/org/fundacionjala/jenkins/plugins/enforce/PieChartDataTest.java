/*
 * Copyright (c) Fundacion Jala. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package org.fundacionjala.jenkins.plugins.enforce;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;

public class PieChartDataTest {

    private static PieChartData pieChartData;

    @BeforeClass
    public static void setup() {
        pieChartData = new PieChartData();
    }

    @Test
    public void testGetRoundedPercentage() {
        String[] titleCoverageData = new String[]{"Lines", "Number"};
        Object[] covered = new Object[]{"Covered", 2336};
        Object[] notCovered = new Object[]{"Not Covered", 210};
        Object[][] coverageData = new Object[][]{titleCoverageData, covered, notCovered};
        pieChartData.setCoverageData(coverageData);
        double percentage = pieChartData.getRoundedPercentage(2);
        double expectedNumber = 91.75;
        Assert.assertEquals(expectedNumber, percentage, 0);
    }

    @Test
    public void testGetZeroWhenCoveredAndNotCoveredLinesAreZeros() {
        String[] titleCoverageData = new String[]{"Lines", "Number"};
        Object[] covered = new Object[]{"Covered", 0};
        Object[] notCovered = new Object[]{"Not Covered", 0};
        Object[][] coverageData = new Object[][]{titleCoverageData, covered, notCovered};
        pieChartData.setCoverageData(coverageData);
        double percentage = pieChartData.getRoundedPercentage(2);
        double expectedNumber = 0;
        Assert.assertEquals(expectedNumber, percentage, 0);
    }

    @Test
    public void testDataForJavascript_shouldGetCoverageData() {


        String[] titleCoverageData = new String[]{"Lines", "Number"};
        Object[] covered = new Object[]{"Covered", 2336};
        Object[] notCovered = new Object[]{"Not Covered", 210};
        Object[][] coverageData = new Object[][]{titleCoverageData, covered, notCovered};

        ArrayList<ArrayList> listCoverageData = pieChartData.getDataForJavascript(coverageData);
        Assert.assertEquals("\"Number\"", listCoverageData.get(0).get(1));
        Assert.assertEquals("\"Covered\"", listCoverageData.get(1).get(0));
        Assert.assertEquals(2336, listCoverageData.get(1).get(1));
        Assert.assertEquals("\"Not Covered\"", listCoverageData.get(2).get(0));
        Assert.assertEquals(210, listCoverageData.get(2).get(1));

    }

    @Test
    public void testDataForJavascript_shouldGetAnalysisData() {
        String[] titleData = new String[]{"Lines", "Number"};
        Object[] danger = new Object[]{"Danger (0% - 74%)", 11};
        Object[] risk = new Object[]{"Risk (75% - 79%)", 5};
        Object[] acceptable = new Object[]{"Acceptable (80% - 94%)", 19};
        Object[] safe = new Object[]{"Safe (95% - 100%)", 20};
        Object[][] data = new Object[][]{titleData, danger, risk, acceptable, safe};
        ArrayList<ArrayList> listData = pieChartData.getDataForJavascript(data);
        Assert.assertEquals("\"Lines\"", listData.get(0).get(0));
        Assert.assertEquals("\"Risk (75% - 79%)\"", listData.get(2).get(0));
        Assert.assertEquals(5, listData.get(2).get(1));
        Assert.assertEquals("\"Safe (95% - 100%)\"", listData.get(4).get(0));
        Assert.assertEquals(20, listData.get(4).get(1));
    }

}
