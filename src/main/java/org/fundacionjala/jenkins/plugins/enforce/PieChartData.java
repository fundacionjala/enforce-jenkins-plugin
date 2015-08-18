/*
 * Copyright (c) Fundacion Jala. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package org.fundacionjala.jenkins.plugins.enforce;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Represents the pie chart data to build the coverage percentage and analysis pie chart
 */
public class PieChartData {

    private String title;
    private Object data[][] = {};
    private Object coverageData[][] = {};

    public PieChartData() {
        title = "";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Object[][] getData() {
        return data;
    }

    public void setData(Object[][] data) {
        this.data = data;
    }

    public Object[][] getCoverageData() {
        return coverageData;
    }

    public void setCoverageData(Object[][] coverageData) {
        this.coverageData = coverageData;
    }

    /**
     * Gets coverage percentage
     */
    public double getPercentage() {
        double percentage = 0;
        if (coverageDataExists()) {
            Double coveredLines = coverageData[1][1] instanceof Double ? (Double) coverageData[1][1] :
                    coverageData[1][1] instanceof Integer ? (Integer) coverageData[1][1] : 0;
            Double uncoveredLines = coverageData[2][1] instanceof Double ? (Double) coverageData[2][1] :
                    coverageData[2][1] instanceof Integer ? (Integer) coverageData[2][1] : 0;
            Double totalLines = coveredLines + uncoveredLines;
            percentage = totalLines != 0? (coveredLines * 100.0f) / totalLines:0;
        }

        return percentage;
    }

    public String getCoveragePercentStatus() {
        double coveragePercent = this.getPercentage();
        String status = null;
        if (coveragePercent < 75) {
            status = "Danger";
        } else if (coveragePercent < 80) {
            status = "Risk";
        } else if (coveragePercent < 75) {
            status = "Danger";
        } else if (coveragePercent < 95) {
            status = "Acceptable";
        } else {
            status = "Safe";
        }
        return status;
    }

    public String getFileCoverageStatus() {
        StringBuilder result = new StringBuilder();
        for (Integer i = 1; i < this.data.length; i++) {
            if (null != this.data[i]) {
                result.append(this.data[i][0]);
                result.append(" = ");
                result.append(Double.valueOf(String.valueOf(this.data[i][1])).intValue());
                result.append(" files. ");
            }
        }
        return result.toString();
    }

    /**
     * Verifies if there are coverage data
     *
     * @return True if there are coverage data
     */
    public boolean coverageDataExists() {
        return null != coverageData && coverageData.length == Constants.COVERAGE_DATA_ROWS && coverageData[0].length == Constants.DATA_COLUMNS;
    }

    /**
     * Verifies if there are data for pie chart
     *
     * @return True if there are coverage data
     */
    public boolean dataExists() {
        return null != data && data.length == Constants.DATA_ROWS && coverageData[0].length == Constants.DATA_COLUMNS;
    }

    /**
     * Gets rounded coverage percentage
     *
     * @param scale decimal numbers
     * @return an float with value of coverage percentage
     */
    public double getRoundedPercentage(int scale) {
        return getRoundedValue(getPercentage(), scale);
    }

    /**
     * Gets a rounded float number
     *
     * @param value the float number
     * @param scale decimal numbers
     * @return a rounded float number
     */
    public double getRoundedValue(double value, int scale) {
        BigDecimal roundedValue = new BigDecimal(value).setScale(scale, BigDecimal.ROUND_HALF_UP);
        return roundedValue.doubleValue();
    }

    /**
     * Gets an array list for javascript code
     *
     * @param data the array was getting from JSON file
     */
    public ArrayList<ArrayList> getDataForJavascript(Object[][] data) {
        ArrayList<ArrayList> newData = new ArrayList<ArrayList>();
        for (int i = 0; i < data.length; i++) {
            Object[] list = data[i];
            ArrayList<Object> newList = new ArrayList<Object>();
            for (int j = 0; j < list.length; j++) {
                Object element = list[j];
                StringBuilder elementStr = new StringBuilder();
                if (element instanceof String) {
                    elementStr.append("\"").append(element).append("\"");
                    newList.add(elementStr.toString());
                }
                if (element instanceof Integer || element instanceof Double) {
                    newList.add(element);
                }
            }
            newData.add(newList);
        }
        return newData;
    }

    /**
     * Gets a new instance from path JSON file
     *
     * @param path JSON file path
     */
    public static PieChartData newInstance(String path) throws FileNotFoundException {
        PieChartData pieChartData = new PieChartData();
        if (new File(path).exists()) {
            Gson gson = new Gson();
            pieChartData = gson.fromJson(new FileReader(path), PieChartData.class);
        }
        return pieChartData;
    }
}
