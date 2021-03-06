/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package de.dennisguse.opentracks.stats;

import androidx.annotation.NonNull;

/**
 * Statistical data about a trip.
 * The data in this class should be filled out by TripStatisticsBuilder.
 *
 * @author Rodrigo Damazio
 */
public class TripStatistics {

    // The min and max latitude seen in this trip.
    private final ExtremityMonitor latitudeExtremities = new ExtremityMonitor();
    // The min and max longitude seen in this trip.
    private final ExtremityMonitor longitudeExtremities = new ExtremityMonitor();
    // The min and max elevation (meters) seen on this trip.
    private final ExtremityMonitor elevationExtremities = new ExtremityMonitor();
    // The min and max grade seen on this trip.
    private final ExtremityMonitor gradeExtremities = new ExtremityMonitor();

    // The trip start time. This is the system time, might not match the GPs time.
    private long startTime = -1L;
    // The trip stop time. This is the system time, might not match the GPS time.
    private long stopTime = -1L;
    // The total trip distance (meters).
    private double totalDistance;
    // The total time (ms). Updated when new points are received, may be stale.
    private long totalTime;
    // The total moving time (ms). Based on when we believe the user is traveling.
    private long movingTime;
    // The maximum speed (meters/second) that we believe is valid.
    private double maxSpeed;
    // The total elevation gained (meters).
    private double totalElevationGain;

    public TripStatistics() {
    }

    /**
     * Copy constructor.
     *
     * @param other another statistics data object to copy from
     */
    public TripStatistics(TripStatistics other) {
        startTime = other.startTime;
        stopTime = other.stopTime;
        totalDistance = other.totalDistance;
        totalTime = other.totalTime;
        movingTime = other.movingTime;
        latitudeExtremities.set(other.latitudeExtremities.getMin(), other.latitudeExtremities.getMax());
        longitudeExtremities.set(other.longitudeExtremities.getMin(), other.longitudeExtremities.getMax());
        maxSpeed = other.maxSpeed;
        elevationExtremities.set(other.elevationExtremities.getMin(), other.elevationExtremities.getMax());
        totalElevationGain = other.totalElevationGain;
        gradeExtremities.set(other.gradeExtremities.getMin(), other.gradeExtremities.getMax());
    }

    /**
     * Combines these statistics with those from another object.
     * This assumes that the time periods covered by each do not intersect.
     *
     * @param other another statistics data object
     */
    public void merge(TripStatistics other) {
        startTime = Math.min(startTime, other.startTime);
        stopTime = Math.max(stopTime, other.stopTime);
        totalDistance += other.totalDistance;
        totalTime += other.totalTime;
        movingTime += other.movingTime;
        if (other.latitudeExtremities.hasData()) {
            latitudeExtremities.update(other.latitudeExtremities.getMin());
            latitudeExtremities.update(other.latitudeExtremities.getMax());
        }
        if (other.longitudeExtremities.hasData()) {
            longitudeExtremities.update(other.longitudeExtremities.getMin());
            longitudeExtremities.update(other.longitudeExtremities.getMax());
        }
        maxSpeed = Math.max(maxSpeed, other.maxSpeed);
        if (other.elevationExtremities.hasData()) {
            elevationExtremities.update(other.elevationExtremities.getMin());
            elevationExtremities.update(other.elevationExtremities.getMax());
        }
        totalElevationGain += other.totalElevationGain;
        if (other.gradeExtremities.hasData()) {
            gradeExtremities.update(other.gradeExtremities.getMin());
            gradeExtremities.update(other.gradeExtremities.getMax());
        }
    }

    /**
     * Gets the trip start time. The number of milliseconds since epoch.
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Sets the trip start time.
     *
     * @param startTime the trip start time in milliseconds since the epoch
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets the trip stop time. The number of milliseconds since epoch.
     */
    public long getStopTime() {
        return stopTime;
    }

    /**
     * Sets the trip stop time.
     *
     * @param stopTime the stop time in milliseconds since the epoch
     */
    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    /**
     * Gets the total distance the user traveled in meters.
     */
    public double getTotalDistance() {
        return totalDistance;
    }

    /**
     * Sets the total trip distance.
     *
     * @param totalDistance the trip distance in meters
     */
    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    /**
     * Adds to the current total distance.
     *
     * @param distance the distance to add in meters
     */
    public void addTotalDistance(double distance) {
        totalDistance += distance;
    }

    /**
     * Gets the total time in milliseconds that this track has been active.
     * This statistic is only updated when a new point is added to the statistics, so it may be off.
     * If you need to calculate the proper total time, use {@link #getStartTime} with the current time.
     */
    public long getTotalTime() {
        return totalTime;
    }

    /**
     * Sets the trip total time.
     *
     * @param totalTime the trip total time in milliseconds
     */
    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    /**
     * Gets the moving time in milliseconds.
     */
    public long getMovingTime() {
        return movingTime;
    }

    /**
     * Sets the trip total moving time.
     *
     * @param movingTime the trip total moving time in milliseconds
     */
    public void setMovingTime(long movingTime) {
        this.movingTime = movingTime;
    }

    /**
     * Adds to the trip total moving time.
     *
     * @param time the time in milliseconds
     */
    public void addMovingTime(long time) {
        movingTime += time;
    }

    /**
     * Gets the topmost position (highest latitude) of the track, in signed degrees.
     */
    public double getTopDegrees() {
        return latitudeExtremities.getMax();
    }

    /**
     * Gets the topmost position (highest latitude) of the track, in signed millions of degrees.
     */
    public int getTop() {
        return (int) (latitudeExtremities.getMax() * 1E6);
    }

    /**
     * Gets the bottommost position (lowest latitude) of the track, in signed
     * degrees.
     */
    public double getBottomDegrees() {
        return latitudeExtremities.getMin();
    }

    /**
     * Gets the bottommost position (lowest latitude) of the track, in signed millions of degrees.
     */
    public int getBottom() {
        return (int) (latitudeExtremities.getMin() * 1E6);
    }

    /**
     * Gets the leftmost position (lowest longitude) of the track, in signed degrees.
     */
    public double getLeftDegrees() {
        return longitudeExtremities.getMin();
    }

    /**
     * Gets the leftmost position (lowest longitude) of the track, in signed millions of degrees.
     */
    public int getLeft() {
        return (int) (longitudeExtremities.getMin() * 1E6);
    }

    /**
     * Gets the rightmost position (highest longitude) of the track, in signed degrees.
     */
    public double getRightDegrees() {
        return longitudeExtremities.getMax();
    }

    /**
     * Gets the rightmost position (highest longitude) of the track, in signed millions of degrees.
     */
    public int getRight() {
        return (int) (longitudeExtremities.getMax() * 1E6);
    }

    /**
     * Gets the mean latitude position of the track, in signed degrees.
     */
    public double getMeanLatitude() {
        return (getBottomDegrees() + getTopDegrees()) / 2.0;
    }

    /**
     * Gets the mean longitude position of the track, in signed degrees.
     */
    public double getMeanLongitude() {
        return (getLeftDegrees() + getRightDegrees()) / 2.0;
    }

    /**
     * Sets the bounding box for this trip. The unit for all parameters is signed
     * millions of degree (degrees * 1E6).
     *
     * @param leftE6   the leftmost longitude reached
     * @param topE6    the topmost latitude reached
     * @param rightE6  the rightmost longitude reached
     * @param bottomE6 the bottommost latitude reached
     */
    public void setBounds(int leftE6, int topE6, int rightE6, int bottomE6) {
        latitudeExtremities.set(bottomE6 / 1E6, topE6 / 1E6);
        longitudeExtremities.set(leftE6 / 1E6, rightE6 / 1E6);
    }

    /**
     * Updates a new latitude value.
     *
     * @param latitude the latitude value in signed decimal degrees
     */
    public void updateLatitudeExtremities(double latitude) {
        latitudeExtremities.update(latitude);
    }

    /**
     * Updates a new longitude value.
     *
     * @param longitude the longitude value in signed decimal degrees
     */
    public void updateLongitudeExtremities(double longitude) {
        longitudeExtremities.update(longitude);
    }

    /**
     * Gets the average speed in meters/second.
     * This calculation only takes into account the displacement until the last point that was accounted for in statistics.
     */
    public double getAverageSpeed() {
        if (totalTime == 0L) {
            return 0.0;
        }
        return totalDistance / ((double) totalTime / 1000.0);
    }

    /**
     * Gets the average moving speed in meters/second.
     */
    public double getAverageMovingSpeed() {
        if (movingTime == 0L) {
            return 0.0;
        }
        return totalDistance / ((double) movingTime / 1000.0);
    }

    /**
     * Gets the maximum speed in meters/second.
     */
    public double getMaxSpeed() {
        return Math.max(maxSpeed, getAverageMovingSpeed());
    }

    /**
     * Sets the maximum speed.
     *
     * @param maxSpeed the maximum speed in meters/second
     */
    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    /**
     * Gets the minimum elevation.
     * This is calculated from the smoothed elevation, so this can actually be more than the current elevation.
     */
    public double getMinElevation() {
        return elevationExtremities.getMin();
    }

    /**
     * Sets the minimum elevation.
     *
     * @param elevation the minimum elevation in meters
     */
    public void setMinElevation(double elevation) {
        elevationExtremities.setMin(elevation);
    }

    /**
     * Gets the maximum elevation.
     * This is calculated from the smoothed elevation, so this can actually be less than the current elevation.
     */
    public double getMaxElevation() {
        return elevationExtremities.getMax();
    }

    /**
     * Sets the maximum elevation.
     *
     * @param elevation the maximum elevation in meters
     */
    public void setMaxElevation(double elevation) {
        elevationExtremities.setMax(elevation);
    }

    /**
     * Updates a new elevation.
     *
     * @param elevation the elevation value in meters
     */
    public void updateElevationExtremities(double elevation) {
        elevationExtremities.update(elevation);
    }

    /**
     * Gets the total elevation gain in meters. This is calculated as the sum of all positive differences in the smoothed elevation.
     */
    public double getTotalElevationGain() {
        return totalElevationGain;
    }

    /**
     * Sets the total elevation gain.
     *
     * @param totalElevationGain the elevation gain in meters
     */
    public void setTotalElevationGain(double totalElevationGain) {
        this.totalElevationGain = totalElevationGain;
    }

    /**
     * Adds to the total elevation gain.
     *
     * @param gain the elevation gain in meters
     */
    public void addTotalElevationGain(double gain) {
        totalElevationGain += gain;
    }

    /**
     * Gets the minimum grade for this trip.
     */
    public double getMinGrade() {
        return gradeExtremities.getMin();
    }

    /**
     * Sets the minimum grade.
     *
     * @param grade the grade as a fraction (-1.0 would mean vertical downwards)
     */
    public void setMinGrade(double grade) {
        gradeExtremities.setMin(grade);
    }

    /**
     * Gets the maximum grade for this trip.
     */
    public double getMaxGrade() {
        return gradeExtremities.getMax();
    }

    /**
     * Sets the maximum grade.
     *
     * @param grade the grade as a fraction (1.0 would mean vertical upwards)
     */
    public void setMaxGrade(double grade) {
        gradeExtremities.setMax(grade);
    }

    /**
     * Updates a new grade value.
     *
     * @param grade the grade value as a fraction
     */
    public void updateGradeExtremities(double grade) {
        gradeExtremities.update(grade);
    }

    @NonNull
    @Override
    public String toString() {
        return "TripStatistics { Start Time: " + getStartTime() + "; Stop Time: " + getStopTime()
                + "; Total Distance: " + getTotalDistance() + "; Total Time: " + getTotalTime()
                + "; Moving Time: " + getMovingTime() + "; Min Latitude: " + getBottomDegrees()
                + "; Max Latitude: " + getTopDegrees() + "; Min Longitude: " + getLeftDegrees()
                + "; Max Longitude: " + getRightDegrees() + "; Max Speed: " + getMaxSpeed()
                + "; Min Elevation: " + getMinElevation() + "; Max Elevation: " + getMaxElevation()
                + "; Elevation Gain: " + getTotalElevationGain() + "; Min Grade: " + getMinGrade()
                + "; Max Grade: " + getMaxGrade() + "}";
    }
}