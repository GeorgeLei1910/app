import numpy as np
import matplotlib
import argparse
import matplotlib.patches as mpatches
import matplotlib.pyplot as plt
from scipy.interpolate import interp1d
# from scipy import interpolate
# from scipy.interpolate import make_interp_spline, BSpline
# from mpl_toolkits.mplot3d import Axes3D
# from scipy.signal import savgol_filter
import pandas as pd
# import pickle
import os
# import csv
import sys
import math
from pyproj import Proj
from math import pi, sin, cos, tan, sqrt
from shapely.geometry import Point
from shapely.geometry.polygon import Polygon, LineString, LinearRing
import simplejson
import urllib
import time

# LatLong- UTM conversion..h
# definitions for lat/long to UTM and UTM to lat/lng conversions
# include <string.h>

# This file include the functions for the FlightPlanning and Quality

LARGE_NUMBER = 1000000000000000000
print("These are the arguments")


# Class for flight planning
class FlightPlanning(object):
    def __init__(self, filepath):
        self.Clockwise = 1
        self.filepath = filepath
        self.surveyName = ""
        self.blockName = ""
        self.flightName = ""
        ss = self.filepath[self.filepath.index("Data"):]
        ss = ss.split('/')
        print(ss)
        for x in ss:
            if ("Survey" in x):
                self.surveyName = x[7:]
                print(self.surveyName)
            elif ("Block" in x):
                self.blockName = x[5:]
                print(self.blockName)
            elif ("Flight" in x):
                self.flightName = x[6:]
                print(self.flightName)
                break
        self.elevation_type = 0
        self.two_way = 0
        self.dfPolygan = pd.DataFrame(columns=['LAT', 'LON'])
        self.dfPolyganUTM = pd.DataFrame(columns=['UTMX', 'UTMY'])
        self.dirAngle = 0
        self.initPoint = np.array([])
        self.initPointUTM = np.array([])
        self.spacing = 0
        self.linespacing = 0
        self.overshoot = 0
        self.overshootBlocks = 0
        self.elevation_buffer = 50
        self.tie_spacing = 0
        self.tie_line = 0
        self.tie_line1 = 0
        self.smallest_dist = 0
        self.tie_init_point = np.array([])
        self.converter = Proj(proj='utm', zone=1, ellps='WGS84')
        self.wayPoints = pd.DataFrame(columns=['utmX', 'utmY', 'elevation', 'line', 'index', 'angle'])
        # All the UTM Zones
        self.utmZones = [(-180, -174), (-174, -168), (-168, -162), (-162, -156), (-156, -150), (-150, -144),
                         (-144, -138)
            , (-138, -132), (-132, -126), (-126, -120), (-120, -114), (-114, -108), (-108, -102), (-102, -96),
                         (-96, -90), (-90, -84)
            , (-84, -78), (-78, -72), (-72, -66), (-66, -60), (-60, -54), (-54, -48), (-48, -42), (-42, -36),
                         (-36, -30), (-30, -24), (-24, -18),
                         (-18, -12), (-12, -6), (-6, -0), (0, 6), (6, 12), (12, 18), (18, 24), (24, 30), (30, 36),
                         (36, 42), (42, 48), (48, 54), (54, 60), (60, 66),
                         (66, 72), (72, 78), (78, 84), (84, 90), (90, 96), (96, 102), (102, 108), (108, 114),
                         (114, 120), (120, 126), (126, 132), (132, 138), (138, 144), (144, 150),
                         (150, 156), (156, 162), (162, 168), (168, 174), (174, 180)]
        print(filepath)
        print("Survey Name: " + self.surveyName)
        print("Block Name: " + self.blockName)
        print("Flight Name: " + self.flightName)
        print("FlightPlanning object made")

    # Makes a Two vector list of coordinates
    def coords(self, str):
        list = []
        x, _, y = str.partition(',')
        x = x.lstrip('(')
        y = y.rstrip(')')
        list.append(x)
        list.append(y)
        return list

    # Iterates through list to see if lon is within the utmZone list above
    def findUtmZone(self, lon):
        for item in self.utmZones:
            if (item[0] <= lon and item[1] > lon):
                return self.utmZones.index(item) + 1

        return -1

    #
    def removeline(self, line):
        with open(self.filepath, "r") as f:
            lines = f.readlines()
        with open(self.filepath, "w") as f:
            for lineNew in lines:
                if lineNew != line:
                    f.write(lineNew)

    # Updates parameters in the plan_settings.txt in the Survey/FlightPlan folder
    def updateParams(self):
        file = open(self.filepath, "r+")
        line = file.readline()
        segs = []
        if (line.startswith('Direction:')):
            segs = line.split(":")
            self.dirAngle = float((segs[1])[0:len(segs[1]) - 1])
        print("Angle: ", self.dirAngle)
        segs.clear()
        line = file.readline()
        if (line.startswith('Points:')):
            segs = line.split(":")
            print(segs)
            for i in range(1, len(segs) - 1):
                coordinates = self.coords(segs[i])
                self.dfPolygan.loc[len(self.dfPolygan)] = coordinates

        line = file.readline()
        segs.clear()
        if (line.startswith('Start:')):
            segs = line.split(":")
            self.initPoint = np.append(self.initPoint, self.coords((segs[1])[0:len(segs[1]) - 1]))
            self.initPoint = [float(val) for val in self.initPoint]

        line = file.readline()
        segs.clear()
        if (line.startswith('Spacing:')):
            segs = line.split(":")
            self.spacing = float((segs[1])[0:len(segs[1]) - 1])

        line = file.readline()
        segs.clear()
        if (line.startswith('LineSpacing:')):
            segs = line.split(":")
            self.linespacing = float((segs[1])[0:len(segs[1]) - 1])

        print("LineSpacing:", self.linespacing)

        line = file.readline()
        segs.clear()
        if (line.startswith('OvershootSurvey:')):
            segs = line.split(":")
            self.overshoot = float((segs[1])[0:len(segs[1]) - 1])

        line = file.readline()
        segs.clear()
        if (line.startswith('OvershootBlock:')):
            segs = line.split(":")
            self.overshootBlocks = float((segs[1])[0:len(segs[1]) - 1])

        line = file.readline()
        segs.clear()
        if (line.startswith('ElevationBuffer:')):
            segs = line.split(":")
            if (len(segs[1]) > 1):
                self.elevation_buffer = float((segs[1])[0:len(segs[1]) - 1])

        line = file.readline()
        segs.clear()
        if (line.startswith('Clockwise:')):
            segs = line.split(":")
            self.Clockwise = float((segs[1])[0:len(segs[1]) - 1])

        line = file.readline()
        segs.clear()
        if (line.startswith('Elevation:')):
            segs = line.split(":")
            self.elevation_type = float((segs[1])[0:len(segs[1]) - 1])
        print("Elevation: ", self.elevation_type)

        line = file.readline()
        segs.clear()
        if (line.startswith('TwoWay:')):
            segs = line.split(":")
            self.two_way = float((segs[1])[0:len(segs[1]) - 1])

        line = file.readline()
        segs.clear()
        if (line.startswith('TieLineSpacing:')):
            segs = line.split(":")
            print("kkkkkk", segs[1])
            if (len(segs[1]) > 1):
                self.tie_spacing = float((segs[1])[0:len(segs[1]) - 1])
                self.tie_line1 = 1
                print("=======> srfkfs")

        print("TieLineSpacing: ", self.tie_spacing)

        line = file.readline()
        segs.clear()
        zoneOrig = self.findUtmZone(float(self.dfPolygan['LON'][0]))
        if (line.startswith('TieStart:')):
            segs = line.split(":")
            print("kkkkkk", segs[1])
            if (len(segs[1]) > 2):
                self.tie_init_point = np.append(self.tie_init_point, self.coords((segs[1])[0:len(segs[1]) - 1]))
                self.tie_init_point = [float(val) for val in self.tie_init_point]
                self.converter = Proj(proj='utm', zone=zoneOrig, ellps='WGS84')
                print(self.tie_init_point[0])
                tup = self.converter(self.tie_init_point[0], self.tie_init_point[1])
                self.tie_init_point = np.array([tup[0], tup[1]])
                self.tie_line = 1 and self.tie_line1
                print("=======> srfkfs")

                print("TieStart: ", self.tie_init_point[0], self.tie_init_point[1])

        line = file.readline()
        segs.clear()
        if (line.startswith('UTM:')):
            self.removeline(line)

        # Created after clicking on "Create and Show Plan"
        with open(self.filepath, "a") as f:
            f.write('UTM:')
            print("Starting Zone: ", zoneOrig)
            for index, row in self.dfPolygan.iterrows():
                self.converter = Proj(proj='utm', zone=zoneOrig, ellps='WGS84')
                print(coordinates)
                self.dfPolyganUTM.loc[len(self.dfPolyganUTM)] = self.converter(row['LON'], row['LAT'])
                string = str(self.dfPolyganUTM.iloc[index]['UTMX']) + "," + str(
                    self.dfPolyganUTM.iloc[index]['UTMY']) + ":"
                f.write(string)
            f.write('\n')

        line = file.readline()
        segs.clear()
        if (line.startswith('initUTM:')):
            self.removeline(line)
        # Written/Created after clicking on "Create and Show Plan"
        with open(self.filepath, "a") as f:
            f.write('initUTM:')
            self.converter = Proj(proj='utm', zone=zoneOrig, ellps='WGS84')
            tup = self.converter(self.initPoint[0], self.initPoint[1])
            self.initPointUTM = np.array([tup[0], tup[1]])
            strInitpointUtm = str(tup[0]) + "," + str(tup[1])
            f.write(strInitpointUtm)
            f.write('\n')
        file.close()
        self.smallest_dist = self.spacing
        if self.smallest_dist > self.overshoot:
            self.smallest_dist = self.overshoot
        if self.smallest_dist > self.overshootBlocks:
            self.smallest_dist = self.overshootBlocks
        print("Polygan Points: ", self.dfPolygan)
        # print(self.dfPolyganUTM)
        print("Initial Point: ", self.initPoint)
        print("Smallest Distance", self.smallest_dist)

    def get_elevation(self, loc):
        if (self.elevation_type == 0):
            # print("Getting elevation on preset")
            return self.get_elevation_srtm(loc)
        else:
            print("Getting elevation on Google")
            return self.get_elevation_google_api(loc)

    def get_elevation_srtm(self, loc):
        return (10, 10)

    # Gets elevation from Google API
    def get_elevation_google_api(self, loc):
        # TODO: Ask Curtis to use their own elevation thing.
        url = 'https://maps.googleapis.com/maps/api/elevation/json?locations=' + loc + '&key=' + 'AIzaSyC6zMxKN6hULA2vpvTRaAgnVxmScK-VH3w'
        response = simplejson.load(urllib.request.urlopen(url))
        results = response['results'][0]
        print(results)
        elevation = results['elevation']
        resolution = results['resolution']
        print("Elevation: " + elevation + "  Resolution:" + resolution)
        return (elevation, resolution)

    def creatFlight(self):

        # Creates flight txts for waypoints
        folderBlock = os.path.dirname(self.filepath)
        folderBlock = os.path.dirname(folderBlock)
        folderBlock = os.path.dirname(folderBlock)
        # Get the initial of the file
        prefixblock = "/S" + self.surveyName + "-B" + self.blockName
        prefixflight = prefixblock + "-F" + self.flightName
        # Files in flight_plan
        fileFlightWaypointsLL = folderBlock + "/flight_plan" + prefixblock + "-WayPointsblocksLL.txt"
        fileFlightWaypoints = folderBlock + "/flight_plan" + prefixblock + "-waypointsDataBlock.txt"
        fileFlightWaypointsTieLinesLL = folderBlock + "/flight_plan" + prefixblock + "-WayPointsblocksTiesLL.txt"
        fileFlightWaypointsTieLines = folderBlock + "/flight_plan" + prefixblock + "-waypointsDataBlockTieLines.txt"

        # Files in flight folder
        filePointsFlights = os.path.dirname(self.filepath) + prefixflight + "-waypointsDataFlight.txt"
        filePointsFlightsLL = os.path.dirname(self.filepath) + prefixflight + "-waypointsDataFlightLL.txt"
        dfWayPointsLL = pd.read_csv(fileFlightWaypointsLL, sep=" ", header=None)
        dfWayPointsLL.columns = ["LON", "LAT", "elevation", "resolution", "index", "line"]
        dfWayPoints = pd.read_csv(fileFlightWaypoints, sep=" ", header=None)
        dfWayPoints.columns = ["utmX", "utmY", "elevation", "line", "index", "angle", "Block"]

        # Reads through and records the values in flightplan.txt in the Flight Folder
        file = open(self.filepath, "r+")
        line = file.readline()
        segs = []
        from_line = 0
        to_line = 0
        from_tie_line = 0
        use_seperate_lines = 0
        to_tie_line = 0

        if (line.startswith('From:')):
            segs = line.split(":")
            if (len(segs[1]) > 1):
                from_line = float((segs[1])[0:len(segs[1]) - 1])

        line = file.readline()
        segs.clear()
        if (line.startswith('To:')):
            segs = line.split(":")
            if (len(segs[1]) > 1):
                to_line = float((segs[1])[0:len(segs[1]) - 1])

        line = file.readline()
        segs.clear()
        if (line.startswith('fromTie:')):
            segs = line.split(":")
            if (len(segs[1]) > 1):
                from_tie_line = float((segs[1])[0:len(segs[1]) - 1])

        line = file.readline()
        segs.clear()
        if (line.startswith('toTie:')):
            segs = line.split(":")
            if (len(segs[1]) > 1):
                to_tie_line = float((segs[1])[0:len(segs[1]) - 1])

        line = file.readline()
        segs.clear()
        if (line.startswith('useSeperateLines:')):
            segs = line.split(":")
            if (len(segs[1]) > 1):
                use_seperate_lines = float((segs[1])[0:len(segs[1]) - 1])
                line = file.readline()
                segs.clear()
                if (line.startswith('seperateLines:')):
                    segs = line.split(":")
                    if (len(segs[1]) > 1):
                        segs = segs[1].split(",")

        print(from_line, to_line)
        file.close()

        dfTemp = pd.DataFrame(columns=["utmX", "utmY", "elevation", "line", "index", "angle", "Block"])
        dfTempLL = pd.DataFrame(columns=["LON", "LAT", "elevation", "resolution", "index", "line"])
        step = 1
        if (from_line > to_line):
            step = -1
        if (use_seperate_lines == 0):
            for line in range(int(from_line), int(to_line + step), step):
                print(line)
                print(dfWayPointsLL.loc[dfWayPointsLL['line'] == line])
                dfTempLL = pd.concat([dfTempLL, dfWayPointsLL.loc[dfWayPointsLL['line'] == line]])
                dfTemp = pd.concat([dfTemp, dfWayPoints.loc[dfWayPoints['line'] == line]])
        else:
            prev = float(segs[0].strip())
            reverse_mode = 0
            for i in range(len(segs)):
                cur_line = float(segs[i].strip())
                dataLL = dfWayPointsLL.loc[dfWayPointsLL['line'] == cur_line]
                data = dfWayPoints.loc[dfWayPoints['line'] == cur_line]
                if (((bool(prev % 2) ^ bool(cur_line % 2)) == reverse_mode) and i != 0):
                    reverse_mode = 1
                    dataLL = dataLL.iloc[::-1]
                    data = data.iloc[::-1]
                dfTempLL = pd.concat([dfTempLL, dataLL])
                dfTemp = pd.concat([dfTemp, data])
                prev = cur_line
        try:
            dfWayPointsLL.drop(dfWayPointsLL.index, inplace=True)
            dfWayPoints.drop(dfWayPoints.index, inplace=True)
            dfWayPointsLL = pd.read_csv(fileFlightWaypointsTieLinesLL, sep=" ", header=None)
            dfWayPointsLL.columns = ["LON", "LAT", "elevation", "resolution", "index", "line"]
            dfWayPoints = pd.read_csv(fileFlightWaypointsTieLines, sep=" ", header=None)
            dfWayPoints.columns = ["utmX", "utmY", "elevation", "line", "index", "angle", "Block"]
        except FileNotFoundError:
            pass
        print("from_tie_line", from_tie_line)
        print("to_tie_line", to_tie_line)
        step = 1
        if (from_tie_line > to_tie_line):
            step = -1
        for line in range(int(from_tie_line), int(to_tie_line + step), step):
            print(line)
            print(dfWayPointsLL.loc[dfWayPointsLL['line'] == line])
            dfTempLL = pd.concat([dfTempLL, dfWayPointsLL.loc[dfWayPointsLL['line'] == line]])
            dfTemp = pd.concat([dfTemp, dfWayPoints.loc[dfWayPoints['line'] == line]])

        dfTempLL.loc[:, 'index'] = np.arange(len(dfTempLL))
        dfTemp.loc[:, 'index'] = np.arange(len(dfTemp))
        np.savetxt(filePointsFlightsLL, dfTempLL.values, fmt='%1.10f')
        np.savetxt(filePointsFlights, dfTemp.values, fmt='%1.10f')
        export_data(filePointsFlightsLL, prefixflight)

    # Makes grid when "Create and Show Block" is pressed
    # always put i and j before making point
    def makeGrid(self, type):
        # Type 1 is flight lines, Type 2 is tie lines
        self.wayPoints.drop(self.wayPoints.index, inplace=True)
        # print(self.dfPolyganUTM.values)
        # Put the
        exterior = [[float(val[0]), float(val[1])] for val in self.dfPolyganUTM.values]
        print("Exterior: ", exterior)
        x = self.initPointUTM[0]
        y = self.initPointUTM[1]
        polygon = Polygon(exterior)
        spacing = self.spacing
        if (type == 1):
            angle = self.dirAngle
            line_spacing = self.linespacing
            x = self.initPointUTM[0]
            y = self.initPointUTM[1]
        else:
            angle = self.dirAngle + 90
            line_spacing = self.tie_spacing
            x = self.tie_init_point[0]
            y = self.tie_init_point[1]
        point = Point(x, y)
        point2 = Point(x + math.cos(math.radians(angle)) * LARGE_NUMBER,
                       y + math.sin(math.radians(angle)) * LARGE_NUMBER)
        path = LineString([point, point2])
        straight = False
        # If First line
        angle = (angle + 360) % 360
        lines = 1
        i = 0
        j = 1
        if (path.intersects(polygon) == True):
            straight = True
            # Comment below to find the start point of this
        # if self.two_way == 0:
        i += 1
        self.wayPoints.loc[i] = [x, y, 0, lines, i, angle]
        # Draws the flight lines of the Flight Plan
        # Too many lines can Freeze up the app.
        # Making Waypoints Forwards
        while (True):
            distance = polygon.exterior.distance(point)
            print("distance", distance)
            print("angle", angle)
            print("lines", lines)
            # If it is the line proper
            if (straight):
                # Next Point program creates
                x = round(math.cos(math.radians(angle)) * spacing + x, 10)
                y = round(math.sin(math.radians(angle)) * spacing + y, 10)
                # Span and check if this path touches the survey. If not, that is the end of the survey
                point = Point(x, y)
                point2 = Point(x + math.cos(math.radians(angle)) *
                               LARGE_NUMBER, y + math.sin(math.radians(angle)) * LARGE_NUMBER)
                path = LineString([point2, point])
                # if the distance from survey's edge moving forward is less than previous distance
                # or the path from point to infinity in direction intersects the polygon
                if (path.intersects(polygon)):
                    straight = True
                    i += 1
                    self.wayPoints.loc[i] = [x, y, 0, lines, i, angle]
                else:
                    straight = False
                    i += 1
                    self.wayPoints.loc[i] = [x, y, 0, lines, i, angle]
                distance = polygon.exterior.distance(point)

            # Turning function
            else:
                lines += 1
                next_angle = (angle + 180) % 360
                if ((lines % 2) == 1):
                    angle = angle + 90 * self.Clockwise
                else:
                    angle = angle - 90 * self.Clockwise
                x = round(math.cos(math.radians(angle)) * line_spacing + x, 10)
                y = round(math.sin(math.radians(angle)) * line_spacing + y, 10)
                # point = Point(x, y)
                i += 1
                self.wayPoints.loc[i] = [x, y, 0, lines, i, next_angle]
                if ((lines % 2) == 1):
                    angle = angle + 90 * self.Clockwise
                else:
                    angle = angle - 90 * self.Clockwise
                angle = (angle + 360) % 360
                x = round(math.cos(math.radians(angle)) * spacing + x, 10)
                y = round(math.sin(math.radians(angle)) * spacing + y, 10)
                point = Point(x, y)
                i += 1
                self.wayPoints.loc[i] = [x, y, 0, lines, i, next_angle]
                point2 = Point(x + math.cos(math.radians(angle)) *
                               LARGE_NUMBER, y + math.sin(math.radians(angle)) * LARGE_NUMBER)
                path = LineString([point2, point])
                if (path.intersects(polygon) == False):
                    break
                straight = True
        if self.two_way == 1:
            if (type == 1):
                angle = self.dirAngle + 180
                spacing = self.spacing
                line_spacing = self.linespacing
                x = self.initPointUTM[0]
                y = self.initPointUTM[1]
            else:
                angle = self.dirAngle - 90
                spacing = self.spacing
                line_spacing = self.tie_spacing
                x = self.tie_init_point[0]
                y = self.tie_init_point[1]
            self.Clockwise = self.Clockwise * -1.0
            point = Point(x, y)
            point2 = Point(x + math.cos(math.radians(angle)) * LARGE_NUMBER,
                           y + math.sin(math.radians(angle)) * LARGE_NUMBER)
            path = LineString([point, point2])
            straight = False
            if (path.intersects(polygon) == True):
                straight = True
            lines = 1
            # Going Backwards
            while (True):
                distance = polygon.exterior.distance(point)
                print("distance", distance)
                print("angle", angle)
                print("lines", lines)
                if (straight):
                    x = round(math.cos(math.radians(angle)) * spacing + x, 10)
                    y = round(math.sin(math.radians(angle)) * spacing + y, 10)
                    point = Point(x, y)
                    point2 = Point(x + math.cos(math.radians(angle)) *
                                   LARGE_NUMBER, y + math.sin(math.radians(angle)) * LARGE_NUMBER)
                    path = LineString([point2, point])
                    if (path.intersects(polygon)):
                        straight = True
                        i += 1
                        j -= 1
                        self.wayPoints.loc[i] = [x, y, 0, lines, j, (angle + 180) % 360]
                    else:
                        straight = False
                        i += 1
                        j -= 1
                        self.wayPoints.loc[i] = [x, y, 0, lines, j, (angle + 180) % 360]
                        # self.wayPoints.loc[i] = [x, y, 0, lines, j, (angle + 180) % 360]
                else:
                    next_angle = (angle) % 360
                    lines -= 1
                    if ((lines % 2) == 0):
                        angle = angle + 90 * self.Clockwise
                    else:
                        angle = angle - 90 * self.Clockwise
                    x = round(math.cos(math.radians(angle)) * line_spacing + x, 10)
                    y = round(math.sin(math.radians(angle)) * line_spacing + y, 10)
                    i += 1
                    j -= 1
                    self.wayPoints.loc[i] = [x, y, 0, lines, j, next_angle]
                    if ((lines % 2) == 0):
                        angle = angle + 90 * self.Clockwise
                    else:
                        angle = angle - 90 * self.Clockwise
                    x = round(math.cos(math.radians(angle)) * spacing + x, 10)
                    y = round(math.sin(math.radians(angle)) * spacing + y, 10)
                    point = Point(x, y)
                    i += 1
                    j -= 1
                    self.wayPoints.loc[i] = [x, y, 0, lines, j, next_angle]
                    point2 = Point(x + math.cos(math.radians(angle)) *
                                   LARGE_NUMBER, y + math.sin(math.radians(angle)) * LARGE_NUMBER)
                    path = LineString([point2, point])
                    if (path.intersects(polygon)):
                        straight = True
                    else:
                        break
        self.wayPoints.sort_values(by=['index'])
        # print(self.wayPoints.values)
        self.wayPoints.loc[:, 'line'] -= (lines - 1)
        self.wayPoints.loc[:, 'index'] -= (j - 1)
        # Add min point and max point here

        self.wayPoints = self.wayPoints.sort_values('index')
        ss = self.filepath.split('/')
        print(ss)
        for x in ss:
            if ("Survey" in x):
                self.surveyName = x[7:]
                print(self.surveyName)

        if (type == 1):
            file = os.path.dirname(self.filepath) + "/S" + self.surveyName + "-waypointsData.txt"
        else:
            file = os.path.dirname(self.filepath) + "/S" + self.surveyName + "-waypointsDataTieLine.txt"
        np.savetxt(file, self.wayPoints.values, fmt='%1.10f')

    def containsFolder(self, parentFolder, fname):
        for name in os.listdir(parentFolder):
            if name == fname:
                return True
        return False

    # Converts UTM coordinates of waypoint file to LatLon
    def UTMtoLL(self, blockfilepath, type, prefix):
        print(blockfilepath)
        dfWayPointsblocks = pd.read_csv(blockfilepath, sep=" ", header=None)
        dfWayPointsblocks.columns = ["utmX", "utmY", "elevation", "line", "index", "angle", "block"]
        dfWayPointsblocksLL = pd.DataFrame(columns=["LON", "LAT", "elevation", "resolution", "index", "line"])
        for index, row in dfWayPointsblocks.iterrows():
            (lon, lat) = self.converter(row["utmX"], row["utmY"], inverse=True)
            loc = str(lat) + ',' + str(lon)
            (elev, resol) = self.get_elevation(loc)
            dfWayPointsblocksLL.loc[len(dfWayPointsblocksLL) - 1] = [lon, lat, elev + self.elevation_buffer, resol,
                                                                     row["index"], row["line"]]
        if (type == 1):
            file = os.path.dirname(blockfilepath) + prefix + "-WayPointsblocksLL.txt"
        else:
            file = os.path.dirname(blockfilepath) + prefix + "-WayPointsblocksTiesLL.txt"
        np.savetxt(file, dfWayPointsblocksLL.values, fmt='%1.7f')

    # Why doesn't this use isExterior?
    def find_intersection(self, exterior, x, y, dir):
        # Number of exterior side that crosses the point-point2 line
        negative = False
        # First Point of line
        dir = dir % 360
        print(x, " ", y, " angle:", dir)
        polygon = Polygon(exterior)
        point = Point(x, y)
        # Find a point outside the polygon
        x1 = x + LARGE_NUMBER * math.cos(np.radians(dir))
        y1 = y + LARGE_NUMBER * math.sin(np.radians(dir))
        point2 = Point(x1, y1)
        path = LineString([point, point2])
        if not path.intersects(polygon):
            negative = True
            x1 = x - LARGE_NUMBER * math.cos(np.radians(dir))
            y1 = y - LARGE_NUMBER * math.sin(np.radians(dir))
            point2 = Point(x1, y1)
            path = LineString([point, point2])
        # Point that is the spacing length in direction from point
        # while point2 is still in the polygon, move it until it is out of the polygon.
        # What about when it just slices the block shape? Doesn't because it's always a straight line.

        # Iterate through all the boundaries of the block and see which edge intersects the line.
        # Also check if it is right on the boundary. Substitute.
        # print("Iterating through sides")
        size = len(exterior)
        ret_dist = LARGE_NUMBER
        for i in range(1, size + 1):
            point3 = Point(exterior[i % size][0], exterior[i % size][1])
            point4 = Point(exterior[(i + 1) % size][0], exterior[(i + 1) % size][1])
            # print(point3, point4)
            path2 = LineString([point3, point4])
            if (path2.intersects(path)):
                ins_point = path2.intersection(path)
                curr_dist = point.distance(ins_point)
                if curr_dist < ret_dist:
                    ret_point = ins_point
                    ret_dist = curr_dist
        if negative:
            ret_dist *= -1.0
        return (ret_point.x, ret_point.y, ret_dist)

    def make_extra_waypoints(self, blockExterior, x, y, angle):

        (x_ret, y_ret, dist) = self.find_intersection(blockExterior, x, y, angle)
        surveyExterior = [[float(val[0]), float(val[1])] for val in self.dfPolyganUTM.values]
        surveyPolygon = Polygon(surveyExterior)
        oslength = self.overshoot
        # print(self.dfPolyganUTM)
        x_s = x_ret + self.overshoot * math.cos(np.radians(angle))
        y_s = y_ret + self.overshoot * math.sin(np.radians(angle))
        outpoint_s = Point(x_s, y_s)
        if outpoint_s.within(surveyPolygon):
            oslength = self.overshootBlocks
        # if (mode == 1): oslength = self.overshoot

        dist += oslength
        print(dist)
        if dist < 0:
            wpNeeded = math.floor(dist / self.spacing)
        else:
            wpNeeded = math.ceil(dist / self.spacing)
        ret_dist = dist
        wpDist = dist / wpNeeded
        waypoints = list()
        for j in range(0, wpNeeded):
            waypoints.append(wpDist)
        # nums_of_points = math.ceil(length / pitch)
        # extra_point = length % pitch
        # print("nums_of_points", pitch)
        # pitch = length / nums_of_points
        # waypoints.append(oslength)

        # print("length of waypoints", len(waypoints))
        # waypoints.append(extra_point)
        return waypoints, ret_dist

    def find_point_inside(self, point, point2, polygonBlock):
        line = LineString([point, point2])
        point3 = line.interpolate(0.5, normalized=True)
        while (point3.within(polygonBlock) == False):
            print(point3)
            line = LineString([point, point3])
            if line.intersects(polygonBlock):
                point3 = line.interpolate(0.5, normalized=True)
            else:
                line = LineString([point2, point3])
                point3 = line.interpolate(0.5, normalized=True)
        return point3

    def get_next_point(self, x, y, spacing, angle, forward):
        ret_x = x + forward * spacing * math.cos(math.radians(angle))
        ret_y = y + forward * spacing * math.sin(math.radians(angle))
        return (ret_x, ret_y)

    def createBlocks(self, type, block):
        # Function that Runs when Edit Block is running and Create BLock is pressed
        # Type 1 = Flight Lines, Type 2 = Tie lines
        # WP Spacing, Overshoot Block, Overshoot Survey smallest of all

        print("Creating Block")
        print("Angle Clockwise? ", self.Clockwise)
        dataFilename = ""
        dfListOfBlock = pd.DataFrame(columns=['Name', 'Exterior'])
        surveyPlanFolder = os.path.dirname(self.filepath)
        surveyFolder = os.path.dirname(surveyPlanFolder)
        exteriorSurvey = [[float(val[0]), float(val[1])] for val in self.dfPolyganUTM.values]
        polygonSurvey = Polygon(exteriorSurvey)
        print("surveyPlanFolder: ", surveyFolder)
        self.blockName = block
        prefix = "/S" + self.surveyName + "-B" + self.blockName
        # print(prefix)
        for name in os.listdir(surveyFolder):
            if name.startswith("Block") and self.containsFolder(surveyFolder, name):
                exterior = []
                try:
                    # print(surveyFolder + "/" + name + "/flight_plan" + prefix +"-flightPalnBlock.txt", "r+")
                    filePoints = open(surveyFolder + "/" + name + "/flight_plan" + prefix + "-flightPalnBlock.txt",
                                      "r+")
                    line = filePoints.readline()
                    # Lines from -flightPalnBlock
                    # print(line)
                    if (line.startswith('Points:')):
                        segs = line.split(":")
                        print("segs", segs)
                        for i in range(1, len(segs) - 1):
                            # print(self.coords(segs[i]))
                            coordinates = [(float(val)) for val in self.coords(segs[i])]
                            exterior.append(coordinates)
                    dfListOfBlock = dfListOfBlock.append({'Name': name, 'Exterior': exterior}, ignore_index=True)
                except:
                    continue
        print(dfListOfBlock)
        if (type == 1):
            dfWayPoints = pd.read_csv(surveyPlanFolder + "/S" + self.surveyName + "-waypointsData.txt", sep=" ",
                                      header=None)
            dataFilename = prefix + "-waypointsDataBlock.txt"
            using_angle = self.dirAngle
        else:
            dfWayPoints = pd.read_csv(surveyPlanFolder + "/S" + self.surveyName + "-waypointsDataTieLine.txt", sep=" ",
                                      header=None)
            dataFilename = prefix + "-waypointsDataBlockTieLines.txt"
            using_angle = self.dirAngle + 90

        dfWayPoints.columns = ["utmX", "utmY", "elevation", "line", "index", "angle"]
        # add 'Block' Column and make last column = -1
        dfWayPoints['Block'] = np.ones(len(dfWayPoints["utmX"])) * -1
        # print(dfListOfBlock[dfListOfBlock['Name'] == "Flight1"]['Exterior'])
        # If Point is within the block
        # create another polygon with the
        # If the waypoint is not in the polygon block?
        # Fill in the gaps (Since the function above only fills the points in the polygon, I will fill the points that are in between them and
        # outside of the polygon
        for indexBlocks, rowBlocks in dfListOfBlock.iterrows():
            polygonBlock = Polygon(rowBlocks['Exterior'])
            for i in range(1, int(dfWayPoints['line'].max()) + 1):
                dfTemp = dfWayPoints.loc[dfWayPoints['line'] == i]
                lineend = len(dfTemp.index)
                angle = dfTemp.iat[0, 5]
                print(angle)
                idxblk = float(rowBlocks['Name'][5:])
                x_o = dfTemp.iat[0, 0]
                y_o = dfTemp.iat[0, 1]
                x = dfTemp.iat[lineend - 1, 0]
                y = dfTemp.iat[lineend - 1, 1]
                # Check if line intersects
                point = Point(x_o, y_o)
                point2 = Point(x, y)
                line = LineString([point2, point])
                if line.intersects(polygonBlock):
                    first = 0
                    last = 0
                    # Testing each line segment
                    for j in range(0, lineend - 1):
                        point3 = Point(dfTemp.iat[j, 0], dfTemp.iat[j, 1])
                        point4 = Point(dfTemp.iat[j + 1, 0], dfTemp.iat[j + 1, 1])
                        line = LineString([point4, point3])
                        if line.intersects(polygonBlock):
                            first = j
                            break
                    for j in reversed(range(1, lineend)):
                        point3 = Point(dfTemp.iat[j, 0], dfTemp.iat[j, 1])
                        point4 = Point(dfTemp.iat[j - 1, 0], dfTemp.iat[j - 1, 1])
                        line = LineString([point3, point4])
                        if line.intersects(polygonBlock):
                            last = j
                            break
                    print(i, first, last)
                    end = last + 1
                    start = first
                    if first > last:
                        end = first + 1
                        start = last
                    for j in range(int(start), int(end)):
                        idx = dfTemp.iat[j, 4]
                        dfWayPoints.loc[dfWayPoints['index'] == idx, 'Block'] = idxblk
                    # If the line intersects but not points are inside the polygon, Find a point inside the polygon and make it a waypoint
                    # Adding extra in block waypoints before beginning of line if needed.

                    (x, y, elevation, lineno, idx, angle, idxblk) = dfTemp.iloc[0]
                    (x_b, y_b) = self.get_next_point(x, y, self.spacing, angle, -1)
                    (x_l, y_l) = self.get_next_point(x, y, LARGE_NUMBER, angle, -1)

                    idx_b = idx
                    point3 = Point(x_b, y_b)
                    point4 = Point(x_l, y_l)
                    line34 = LineString([point3, point4])
                    extra_wp = list()
                    while (line34.intersects(polygonBlock)):
                        extra_wp.append(point3)
                        (x_b, y_b) = self.get_next_point(x_b, y_b, self.spacing, angle, -1)
                        point3 = Point(x_b, y_b)
                        line34 = LineString([point3, point4])
                    deci = 0.2 / ((len(extra_wp) + 1))
                    for point4 in extra_wp:
                        idx_b -= deci
                        dfWayPoints.loc[len(dfWayPoints)] = [point4.x, point4.y, 0, i, idx_b, angle, idxblk]
                        print("New Waypoint made on line ", i)
                    # Adding extrea in block waypoints after end of line if needed
                    (x, y, elevation, lineno, idx, angle, idxblk) = dfTemp.iloc[len(dfTemp) - 1]
                    (x_b, y_b) = self.get_next_point(x, y, self.spacing, angle, 1)
                    (x_l, y_l) = self.get_next_point(x, y, LARGE_NUMBER, angle, 1)
                    idx_b = idx
                    point3 = Point(x_b, y_b)
                    point4 = Point(x_l, y_l)
                    line34 = LineString([point3, point4])
                    extra_wp.clear()
                    while line34.intersects(polygonBlock):
                        extra_wp.append(point3)
                        (x_b, y_b) = self.get_next_point(x_b, y_b, self.spacing, angle, 1)
                        point3 = Point(x_b, y_b)
                        line34 = LineString([point3, point4])
                    deci = 0.2 / (len(extra_wp) + 1)
                    for point4 in extra_wp:
                        idx_b += deci
                        dfWayPoints.loc[len(dfWayPoints)] = [point4.x, point4.y, 0, i, idx_b, angle, idxblk]
                        print("New Waypoint made on line ", i)

        for indexBlocks, rowBlocks in dfListOfBlock.iterrows():
            print("==========Block=========>>", indexBlocks + 1)
            filePointsBlock = surveyFolder + '/' + rowBlocks['Name'] + "/flight_plan" + dataFilename
            # Get the waypoints that has the Current Block number
            idxblk = float(rowBlocks['Name'][5:])
            df = dfWayPoints[dfWayPoints['Block'] == idxblk].sort_values(by="index")
            if (len(df) == 0):  continue
            # Add one to make the first one one
            (x, y, elevation, lineno, idx, angle, idxblk) = df.iloc[0]
            print("Min line: ", df['line'].min)
            df_holder = pd.DataFrame(columns=["utmX", "utmY", "elevation", "line", "index", "angle", "Block"])

            j = 0
            angleOrig = float(df.iat[j, 5])
            while (((angleOrig - using_angle) / 180).is_integer() == False):
                j += 1
                angleOrig = float(df.iat[j, 5])
            # (list_extra, dist) = self.make_extra_waypoints(rowBlocks['Exterior'], x, y, angleOrig + 180)
            # deci = 0.4 / (len(list_extra) + 1)
            # # Add the ends of the lines
            # for nums1 in list_extra:
            #     idx -= deci
            #     (x, y) = self.get_next_point(x, y, nums1, angle, -1)
            #     print(x, y, 0, lineno, idx, angle, idxblk)
            #     curr_wp = [x, y, 0, lineno, idx, angle, idxblk]
            #     df_holder.drop(df_holder.index, inplace=True)
            #     df_holder = pd.DataFrame([curr_wp], columns=["utmX", "utmY", "elevation", "line", "index", "angle", "Block"])
            #     df = pd.concat([df_holder, df])
            # max_x = df.iloc[0, "utmX"]
            # max_y = df.iloc[0, "utmY"]
            # min_x = df.iloc[0, "utmX"]
            # min_y = df.iloc[0, "utmY"]
            for line in range(int(df['line'].min()), int(df['line'].max())):
                # Get the last number of the current line
                dfTemp = df.loc[df['line'] <= line]
                # Create Rest of the list by cancelling out dfTemp with dfTempRest
                dfTempRest = pd.concat([dfTemp, df]).drop_duplicates(keep=False)
                (x, y, elevation, lineno, idx, angle, idxblk) = dfTemp.iloc[len(dfTemp) - 1]
                (x_o, y_o, elevation_o, lineno_o, idx_o, angle_o, idxblk_o) = dfTempRest.iloc[0]

                print("=======LINE======>>", lineno)
                print("=======angle======>>", angle)
                # Dot Product to check if point is ahead
                # Current direction vector dot vector to next point from curr point.
                anglex = math.cos(math.radians(angle)) * (x_o - x)
                angley = math.sin(math.radians(angle)) * (y_o - y)
                magnitude = round(math.sqrt((x_o - x) ** 2 + (y_o - y) ** 2), 6)
                # dot product of direction vector and vector(x, y to x_o, y_o)
                vdot = round((anglex + angley) / magnitude, 6)

                print("In Front: ", vdot)
                # # Length to catch up
                print(magnitude)
                print(vdot)
                wpNeeded = round(math.sqrt((magnitude ** 2) - (self.linespacing ** 2))) / self.spacing
                if vdot > 0.0:
                    print("Waypoints Needed: ", wpNeeded)
                    # If there are points in the waypoint file that are inbetween the end points
                    curr_idx = idx + 1
                    curr_wp = dfWayPoints.loc[dfWayPoints['index'] == curr_idx]

                    # Use existing waypoints first, then add waypoints
                    if idx.is_integer():
                        while (curr_wp.iat[0, 3] == lineno) and wpNeeded > 0:
                            print(curr_wp)
                            print("Adding existing waypoint")
                            curr_wp.iat[0, 6] = idxblk
                            dfTemp = dfTemp.append(curr_wp)
                            curr_idx += 1
                            wpNeeded -= 1
                            curr_wp = dfWayPoints.loc[dfWayPoints['index'] == curr_idx]
                    (x_t, y_t, elevation_t, lineno_t, idx_t, angle_t, idxblk_t) = dfTemp.iloc[len(dfTemp) - 1]
                    deci_t = 0.2 / (wpNeeded + 1)
                    dfTemp2 = pd.DataFrame(columns=["utmX", "utmY", "elevation", "line", "index", "angle", "Block"])
                    while wpNeeded > 0:
                        idx_t += deci_t
                        print("Adding additional waypoint ", wpNeeded)
                        (x_t, y_t) = self.get_next_point(float(x_t), float(y_t), self.spacing, angle, 1)
                        curr_wp = [x_t, y_t, 0, lineno, idx_t, angle, idxblk]
                        print(curr_wp)
                        dfTemp2 = pd.DataFrame([curr_wp],
                                               columns=["utmX", "utmY", "elevation", "line", "index", "angle", "Block"])
                        dfTemp = dfTemp.append(dfTemp2, ignore_index=True)
                        wpNeeded -= 1
                # Next Point playing catch up
                elif vdot < 0:
                    print("Waypoints Needed: ", wpNeeded)
                    # If there are points in the waypoint file that are inbetween the end points
                    curr_idx = idx_o - 1
                    curr_wp = dfWayPoints.loc[dfWayPoints['index'] == curr_idx]

                    # Use existing waypoints first, then add waypoints
                    if idx_o.is_integer():
                        while (curr_wp.iat[0, 3] == lineno_o) and wpNeeded > 0:
                            print("Adding existing waypoint ", wpNeeded)
                            curr_wp.iat[0, 6] = idxblk
                            df_holder.drop(df_holder.index, inplace=True)
                            df_holder = df_holder.append(curr_wp)
                            dfTempRest = pd.concat([df_holder, dfTempRest])
                            curr_idx -= 1
                            wpNeeded -= 1
                            curr_wp = dfWayPoints.loc[dfWayPoints['index'] == curr_idx]
                    (x_t, y_t, elevation_t, lineno_t, idx_t, angle_t, idxblk_t) = dfTempRest.iloc[0]
                    deci_t = 0.2 / (wpNeeded + 1)
                    while wpNeeded > 0:
                        idx_t -= deci_t
                        print("Adding additional waypoint ", wpNeeded)
                        # Should be in the same direction.
                        (x_t, y_t) = self.get_next_point(float(x_t), float(y_t), self.spacing, angle, 1)
                        curr_wp = [x_t, y_t, 0, lineno, idx_t, angle, idxblk]
                        dfTemp2 = pd.DataFrame([curr_wp],
                                               columns=["utmX", "utmY", "elevation", "line", "index", "angle", "Block"])
                        df_holder.drop(df_holder.index, inplace=True)
                        df_holder = df_holder.append(dfTemp2)
                        dfTempRest = pd.concat([df_holder, dfTempRest])
                        wpNeeded -= 1
                # df = pd.concat([dfTemp, dfTempRest])
                dfTemp = dfTemp.reset_index(drop=True)
                dfTempRest = dfTempRest.reset_index(drop=True)
                # # Fill in the overshoot extensions.
                # # If one is out and one is in , find the point outside to polygon boundary and use that point as reference
                # # If both are inside or out, then find the point that is closest the the polygon boundary and use that point as reference.
                (x, y, elevation, lineno, idx, angle, idxblk) = dfTemp.iloc[len(dfTemp) - 1]
                (x_o, y_o, elevation_o, lineno_o, idx_o, angle_o, idxblk_o) = dfTempRest.iloc[0]
                point = Point(x, y)
                point_o = Point(x_o, y_o)
                (ret_x, ret_y, dist) = self.find_intersection(rowBlocks['Exterior'], x, y, angle)
                (ret_x_o, ret_y_o, dist_o) = self.find_intersection(rowBlocks['Exterior'], x_o, y_o, angle)

                print(lineno, x, y, angle, "Distance From Block: ", dist)
                print(lineno_o, x_o, y_o, angle, "Distance From Block: ", dist_o)

                rem_dist = dist
                if abs(dist_o) < abs(rem_dist):
                    rem_dist = dist_o
                for i in range(0, math.ceil(abs(rem_dist) / self.spacing)):
                    print(dfTemp)
                    dfTemp = dfTemp.drop([len(dfTemp) - 1])
                    # Over here
                    dfTempRest = dfTempRest.drop([0])
                    dfTempRest = dfTempRest.reset_index(drop=True)

                (x, y, elevation, lineno, idx, angle, idxblk) = dfTemp.iloc[len(dfTemp) - 1]
                (x_o, y_o, elevation_o, lineno_o, idx_o, angle_o, idxblk_o) = dfTempRest.iloc[0]
                point = Point(x, y)
                point_o = Point(x_o, y_o)
                (ret_list, dist) = self.make_extra_waypoints(rowBlocks['Exterior'], x, y, angle)
                (ret_list_o, dist_o) = self.make_extra_waypoints(rowBlocks['Exterior'], x_o, y_o, angle)

                point_l = Point(self.get_next_point(x, y, LARGE_NUMBER, angle, 1))
                point_o_l = Point(self.get_next_point(x_o, y_o, LARGE_NUMBER, angle, 1))
                # if both points are insidex or outside
                if point.within(polygonBlock) is point_o.within(polygonBlock):
                    # Remove a few points
                    if abs(dist) > abs(dist_o):
                        list_use = ret_list
                        print("End list used")
                    else:
                        list_use = ret_list_o
                        print("Start list used")
                else:
                    if point.within(polygonBlock):
                        list_use = ret_list
                        print("Start list used")
                    else:
                        list_use = ret_list_o
                        print("End list used")
                deci = 0.1 / (len(list_use) + 1)
                for length in list_use:
                    print("Segment Length:", length)
                    (x, y) = self.get_next_point(x, y, length, angle, 1)
                    point = Point(x, y)
                    path = LineString([point, point_l])
                    if path.intersects(polygonSurvey) and length == self.spacing and idx.is_integer():
                        idx += 1
                        dfTemp2 = dfWayPoints.loc[dfWayPoints["index"] == idx]
                    else:
                        idx += deci
                        curr_wp = [x, y, 0, lineno, idx, angle, idxblk]
                        dfTemp2 = pd.DataFrame([curr_wp],
                                               columns=["utmX", "utmY", "elevation", "line", "index", "angle", "Block"])
                    print(dfTemp2)
                    dfTemp = dfTemp.append(dfTemp2)
                    (x_o, y_o) = self.get_next_point(x_o, y_o, length, angle, 1)
                    point_o = Point(x_o, y_o)
                    path = LineString([point_o, point_o_l])
                    if path.intersects(polygonSurvey) and length == self.spacing and idx_o.is_integer():
                        idx_o -= 1
                        dfTemp2 = dfWayPoints.loc[dfWayPoints["index"] == idx_o]
                    else:
                        idx_o -= deci
                        curr_wp = [x_o, y_o, 0, lineno_o, idx_o, angle_o, idxblk]
                        dfTemp2 = pd.DataFrame([curr_wp],
                                               columns=["utmX", "utmY", "elevation", "line", "index", "angle", "Block"])
                    print(dfTemp2)
                    df_holder.drop(df_holder.index, inplace=True)
                    df_holder = df_holder.append(dfTemp2)
                    dfTempRest = pd.concat([df_holder, dfTempRest])
                    # (x, y, elevation, lineno, idx, angle, idxblk) = dfTemp.iloc[len(dfTemp) - 1]
                    # (x_o, y_o, elevation_o, lineno_o, idx_o, angle_o, idxblk_o) = dfTempRest.iloc[0]
                    # x_array = [min_x, x, x_o, max_x]
                    # y_array = [min_y, y, y_o, max_y]
                    # x_array.sort()
                    # y_array.sort()
                    # min_x = x_array[0]
                    # max_x = x_array[-1]
                    # min_y = y_array[0]
                    # max_y = y_array[-1]
                df = pd.concat([dfTemp, dfTempRest])
                df = df.reset_index(drop=True)
            #Insert ending here.
            # (x, y, elevation, lineno, idx, angle, idxblk) = df.iloc[len(df) - 1]
            # point = Point(x, y)
            # point_l = Point(self.get_next_point(x, y, LARGE_NUMBER, angle, 1))
            # (list_extra, dist) = self.make_extra_waypoints(rowBlocks['Exterior'], x, y, angle)
            # for i in range(0, len(list_extra)):
            #     path = LineString([point, point_l])
            #     if path.intersects(polygonSurvey) and length == self.spacing and idx.is_integer():
            #         idx_o -= 1
            #         dfTemp2 = dfWayPoints.loc[dfWayPoints["index"] == idx_o]
            #     else:
            #         idx_o -= deci
            #         curr_wp = [x_o, y_o, 0, lineno_o, idx_o, angle_o, idxblk]
            #         dfTemp2 = pd.DataFrame([curr_wp],
            #                                columns=["utmX", "utmY", "elevation", "line", "index", "angle", "Block"])
            #     df = pd.concat([df, dfTemp2])
            df = df.reset_index(drop=True)
            # df = pd.con
        np.savetxt(filePointsBlock, df.values, fmt='%1.10f')
        self.UTMtoLL(filePointsBlock, type, prefix)


# Class for Quality Check
class QualityCheck(object):
    # Initialized QualityCheck Object
    def __init__(self, filepath):
        self.filepath = filepath
        print(self.filepath)

    # Loads the 6 csv files to
    def load_data(self, type):
        # IF using data_from_UAV.csv, go to load_data_processed()
        if (type == 1):
            self.load_data_processed()
        else:
            try:
                self.data_mag = pd.read_csv(self.filepath + "/Mag.csv")
                self.data_mag.columns = ["BBB Time", "Mag Vals", "Lamor"]
            except:
                pass
            try:
                self.data_laser = pd.read_csv(self.filepath + "/MavLaser.csv")
                self.data_laser.columns = ["BBB Time", "Laser"]
            except:
                pass
            try:
                self.data_mav = pd.read_csv(self.filepath + "/Mav.csv")
                self.data_mav.columns = ["BBB Time", "Mav Time", "Altitude", "Mav Lat", "Mav Lon"]
            except:
                pass
            try:
                self.data_piksi = pd.read_csv(self.filepath + "/PiksiGPS.csv")
                self.data_piksi.columns = ["BBB Time", "Piksi Lat", "Piksi Lon", "Piksi Alt"]
            except:
                pass

    def load_data_processed(self):
        self.data = pd.read_csv(os.path.dirname(self.filepath) + "/data_from_UAV.csv")
        self.data.columns = ["index", "BBB Time", "Piksi Lat", "Piksi Lon", "Mav Lat", "Mav Lon", "Mag Vals", "Lamor",
                             "Laser"]
        self.data_mag = self.data[["BBB Time", "Mag Vals", "Lamor"]]
        self.data_laser = self.data[["BBB Time", "Laser"]]
        self.data_mav = self.data[["BBB Time", "BBB Time", "Altitude", "Mav Lat", "Mav Lon"]]
        self.data_piksi = self.data[["BBB Time", "Piksi Lat", "Piksi Lon", "Piksi Alt"]]

    # fouth difference formula for mag (x - 4x + 6x^2 - 4x^3 + 1)
    def fourthDiffFormula(self, mag1, mag2, mag3, mag4, mag5):
        val = (mag1 - 4 * mag2 + 6 * mag3 - 4 * mag4 + mag5) / 16
        return val

    #
    def saveShowFig(self, profileName, yAxis, xAxisName, yAxisName, colour, format, *positional_parameters,
                    **keyword_parameters):

        # plot figure
        plt.figure(figsize=(8, 6))
        fig = plt.gcf()
        fig.canvas.set_window_title(profileName)
        if ('xAxis' in keyword_parameters):
            plt.plot(keyword_parameters['xAxis'], yAxis, format, color=colour, markersize=1)
        else:
            plt.plot(yAxis, format, color=colour, markersize=1)
        plt.grid()
        fig.suptitle(profileName, fontsize=20)
        plt.xlabel(xAxisName, fontsize=18)
        plt.ylabel(yAxisName, fontsize=18)
        filename = os.path.dirname(self.filepath)
        fig.savefig(filename + '/figures/' + profileName + '.png', dpi=fig.dpi)
        return plt

    def fourtDifference(self, low, high):

        fourthDiffVals = []
        mag_vals = self.data_mag["Mag Vals"].values
        for i in range(0, len(mag_vals)):
            mag1 = mag_vals[i - 2]
            mag2 = mag_vals[i - 1]
            mag3 = mag_vals[i]
            if (i >= len(mag_vals) - 2):
                mag4 = 0
                mag5 = 0
            else:
                mag4 = mag_vals[i + 1]
                mag5 = mag_vals[i + 2]
            val = self.fourthDiffFormula(mag1, mag2, mag3, mag4, mag5)
            fourthDiffVals.append(val)
        if (low == 0 and high == 0):
            low = 0
            high = len(fourthDiffVals)
        std = np.std(fourthDiffVals[low:high], dtype=np.float32)
        mean = np.mean(fourthDiffVals[low:high])
        plt = self.saveShowFig("4th Difference", yAxis=fourthDiffVals[low:high], xAxis=range(low, high),
                               xAxisName="Data Points", yAxisName="4th Difference", colour="red", format='.')
        string = 'Standard Deviation: ' + str(std) + '\n\n' + 'Mean: ' + str(mean)
        plt.title(string, y=0.78)
        plt.show()

    def magProfile(self):

        mag_vals = self.data_mag["Mag Vals"].values
        plt = self.saveShowFig("Mag Profile", yAxis=mag_vals,
                               xAxisName="Data Points", yAxisName="Mag", colour="orchid", format='.')
        plt.show()

    def laserProfile(self):

        laser_vals = self.data_laser["Laser"].values
        plt = self.saveShowFig("Laser Profile", yAxis=laser_vals,
                               xAxisName="Data Points", yAxisName="Laser", colour="blue", format='.')
        plt.show()

    def flightMap(self):

        MavLat = self.data_mav["Mav Lat"] / 10000000
        MavLon = self.data_mav["Mav Lon"] / 10000000
        plt = self.saveShowFig("Flight Map", xAxis=MavLon, yAxis=MavLat,
                               xAxisName="LON", yAxisName="LAT", colour="black", format='.')
        plt.show()

    # TODO: Nothing here is showing. Fix me!
    def FlightMapPiksivsMav(self):
        PiksiLat = self.data_piksi["Piksi Lat"]
        PiksiLon = self.data_piksi["Piksi Lon"]
        plt.figure(figsize=(8, 6))
        plt.plot(PiksiLon, PiksiLat, '.', color="blue", markersize=1)
        MavLat = self.data_mav["Mav Lat"] / 10000000
        MavLon = self.data_mav["Mav Lon"] / 10000000
        plt.plot(MavLon, MavLat, '.', color="red", markersize=1)
        plt.suptitle("Mav GPS vs Piksi GPS")
        red_patch = mpatches.Patch(color='red', label='Mavlink Data')
        blue_patch = mpatches.Patch(color='blue', label='Piksi Data')
        plt.legend(handles=[red_patch, blue_patch])
        plt.xlabel("LAT")
        plt.ylabel("LON")
        plt.show()

    def mavalt_piksialt(self):
        MavAlt = self.data_mav["Altitude"] / 1000
        BBB_mavLaserTimeMav = [StringToDecimal(ss) for ss in self.data_mav["BBB Time"]]
        BBB_mavLaserTimePiksi = [StringToDecimal(ss) for ss in self.data_piksi["BBB Time"]]
        print(len(MavAlt))
        PiksiAlt = self.data_piksi["Piksi Alt"]
        xPiksi = np.linspace(0, len(PiksiAlt), len(PiksiAlt))
        xMav = np.linspace(0, len(PiksiAlt), len(MavAlt))
        print(len(PiksiAlt))
        plt.figure(figsize=(8, 6))
        plt.plot(BBB_mavLaserTimeMav, MavAlt, '.', color="red", markersize=1)
        plt.plot(BBB_mavLaserTimePiksi, PiksiAlt, '.', color="blue", markersize=1)
        plt.ylabel("Altitude(Meter)")
        plt.xlabel("Time(seconds)")
        red_patch = mpatches.Patch(color='red', label='Mavlink Data')
        blue_patch = mpatches.Patch(color='blue', label='Piksi Data')
        plt.legend(handles=[red_patch, blue_patch])
        plt.suptitle("Mav Alt vs Piksi Alt")
        plt.show()

    def counter_map(self):
        filepath = os.path.dirname(self.filepath)
        data = pd.read_csv(filepath + "/data_from_UAV.csv")
        X = list()
        Y = list()
        Z = list()
        data_mag = data["Mag"].values
        data_lat = data["Lat Piksi"].values
        data_lon = data["Lon Piksi"].values
        X = data_lat
        Y = data_lon
        xs, ys = np.meshgrid(X, Y)

        dataMesh = np.empty_like(xs)
        for index, row in data.iterrows():
            dataMesh[index, index] = row["Mag"]

        print(dataMesh)
        # X, Y = np.meshgrid(x, y)

        fig, ax = plt.subplots(figsize=(6, 6))
        ax.contourf(xs, ys, dataMesh)
        plt.show()


def StringToDecimal(ss):
    # print(ss)
    try:
        (h, m, s) = ss.split(':')
    except ValueError:
        (m, s) = ss.split(':')
        h = 0
    result = int(h) * 3600 + int(m) * 60 + float(s)
    return result


def treamdata(array):
    size = array.size
    for i in range(0, size - 1):
        if ((array[i + 1] - array[i]) > 0.1):
            array = array[i:]
            return array


# Processes data
def process_data(filename):
    file_paths = ["Mag.csv", "PiksiGPS.csv", "PiksiGPSTime.csv", "Mav.csv", "MavLaser.csv", "MavAtt.csv"]
    filepatOrig = os.path.dirname(filename)
    print(filepatOrig)
    foldername = os.path.basename(filepatOrig)
    listFlightFolders = list()
    for name in os.listdir(filepatOrig):
        if name.startswith("Flight"):
            listFlightFolders.append()
    if (foldername.startswith("Block")):
        for file_name in file_paths:
            for folder in listFlightFolders:
                print("Thing")

    print("Processing data")
    filepath = filename
    data_mag = pd.read_csv(filepath + "/Mag.csv")
    print("data_mag", len(data_mag))
    data_piksiGPS = pd.read_csv(filepath + "/PiksiGPS.csv")
    print("data_piksiGPS", len(data_piksiGPS))
    data_piksiGPSTime = pd.read_csv(filepath + "/PiksiGPSTime.csv")
    print("data_piksiGPSTime", len(data_piksiGPSTime))
    data_mav = pd.read_csv(filepath + "/Mav.csv")
    print("data_mav", len(data_mav))
    data_mav_laser = pd.read_csv(filepath + "/MavLaser.csv")
    print("data_mav_laser", len(data_mav_laser))
    data_mav_att = pd.read_csv(filepath + "/MavAtt.csv")
    print("data_mav_att", len(data_mav_att))
    data_mag.columns = ["BBB Time", "Mag Vals", "Lamor"]
    data_piksiGPS.columns = ["BBB Time", "Piksi Lat", "Piksi Lon", "Piksi Alt"]
    data_piksiGPSTime.columns = ["BBB Time", "Weeks", "Secs"]
    data_mav.columns = ["BBB Time", "Mav Time", "Altitude", "Mav Lat", "Mav Lon"]
    data_mav_laser.columns = ["BBB Time", "Laser"]
    # print(data_mag)
    TIME_MAG_USED = data_mag["BBB Time"].values
    TIME_MAG_USED = [StringToDecimal(ss) for ss in TIME_MAG_USED]
    TIME_MAG_USED = np.array(TIME_MAG_USED)

    BBB_time_piksi = data_piksiGPSTime["BBB Time"].values
    BBB_time_piksi = [StringToDecimal(ss) for ss in BBB_time_piksi]
    BBB_time_piksi = np.array(BBB_time_piksi)
    Piksi_time = data_piksiGPSTime["Secs"].values
    BBB_timePiksi_GPS = data_piksiGPS["BBB Time"].values
    BBB_timePiksi_GPS = [StringToDecimal(ss) for ss in BBB_timePiksi_GPS]
    BBB_timePiksi_GPS = np.array(BBB_timePiksi_GPS)
    Lat_data = data_piksiGPS["Piksi Lat"].values
    Lon_data = data_piksiGPS["Piksi Lon"].values
    Alt_data = data_piksiGPS["Piksi Alt"].values

    BBB_mavTime = data_mav["BBB Time"].values
    BBB_mavTime = [StringToDecimal(ss) for ss in BBB_mavTime]
    BBB_mavTime = np.array(BBB_mavTime)
    Lat_mav_data = data_mav["Mav Lat"].values
    Lon_mav_data = data_mav["Mav Lon"].values
    Alt_mav_data = data_mav["Altitude"].values
    BBB_mavLaserTime = data_mav_laser["BBB Time"].values
    BBB_mavLaserTime = [StringToDecimal(ss) for ss in BBB_mavLaserTime]
    BBB_mavLaserTime = np.array(BBB_mavLaserTime)
    Laser = data_mav_laser["Laser"].values
    MAG_VALS = data_mag["Mag Vals"].values
    LAMOR = data_mag["Lamor"].values
    # plt.plot( BBB_timePiksi_GPS, Lat_data , '.', color ="red", markersize=1)
    # plt.show()

    TIME_MAG_USED = treamdata(TIME_MAG_USED)

    BBB_time_piksi = treamdata(BBB_time_piksi)
    Piksi_time = Piksi_time[Piksi_time.size - BBB_time_piksi.size:Piksi_time.size]

    BBB_timePiksi_GPS = treamdata(BBB_timePiksi_GPS)
    Lat_data = Lat_data[Lat_data.size - BBB_timePiksi_GPS.size:Lat_data.size]
    Lon_data = Lon_data[Lon_data.size - BBB_timePiksi_GPS.size:Lon_data.size]

    BBB_mavTime = treamdata(BBB_mavTime)
    Lat_mav_data = Lat_mav_data[Lat_mav_data.size - BBB_mavTime.size:Lat_mav_data.size]
    Lon_mav_data = Lon_mav_data[Lon_mav_data.size - BBB_mavTime.size:Lon_mav_data.size]

    BBB_mavLaserTime = treamdata(BBB_mavLaserTime)
    Laser = Laser[Laser.size - BBB_mavLaserTime.size:Laser.size]

    MAG_VALS = MAG_VALS[MAG_VALS.size - TIME_MAG_USED.size:MAG_VALS.size]
    LAMOR = LAMOR[LAMOR.size - TIME_MAG_USED.size:LAMOR.size]

    Func_Polated_PiksiTime = interp1d(BBB_time_piksi, Piksi_time, kind='linear', fill_value="extrapolate")
    Func_Polated_PiksiTimeGPSLAT = interp1d(BBB_timePiksi_GPS, Lat_data, kind='linear', fill_value="extrapolate")
    Func_Polated_PiksiTimeGPSLON = interp1d(BBB_timePiksi_GPS, Lon_data, kind='linear', fill_value="extrapolate")
    Func_Polated_MavDataGPSLON = interp1d(BBB_mavTime, Lon_mav_data, kind='linear', fill_value="extrapolate")
    Func_Polated_MavDataGPSLAT = interp1d(BBB_mavTime, Lat_mav_data, kind='linear', fill_value="extrapolate")
    Func_Polated_Laser = interp1d(BBB_mavLaserTime, Laser, kind='linear', fill_value="extrapolate")

    NEW_PIKSI_TIME_VALS = np.array(Func_Polated_PiksiTime(TIME_MAG_USED))
    NEW_PIKSI_LAT_VALS = np.array(Func_Polated_PiksiTimeGPSLAT(TIME_MAG_USED))
    NEW_PIKSI_LON_VALS = np.array(Func_Polated_PiksiTimeGPSLON(TIME_MAG_USED))
    NEW_MAV_LON_VALS = np.array(Func_Polated_MavDataGPSLON(TIME_MAG_USED)) / 10000000
    NEW_MAV_LAT_VALS = np.array(Func_Polated_MavDataGPSLAT(TIME_MAG_USED)) / 10000000
    NEW_LASER_MAV = np.array(Func_Polated_Laser(TIME_MAG_USED))

    FINAL_RESULT = pd.DataFrame()
    FINAL_RESULT['Piksi Time'] = NEW_PIKSI_TIME_VALS
    FINAL_RESULT['Lat Piksi'] = NEW_PIKSI_LAT_VALS
    FINAL_RESULT['Lon Piksi'] = NEW_PIKSI_LON_VALS
    FINAL_RESULT['Lat Mav'] = NEW_MAV_LAT_VALS
    FINAL_RESULT['Lon Mav'] = NEW_MAV_LON_VALS
    FINAL_RESULT['Mag'] = MAG_VALS
    FINAL_RESULT['Lamor'] = LAMOR
    FINAL_RESULT['Laser'] = NEW_LASER_MAV

    # Makes data_from_UAV.csv
    pd.DataFrame(FINAL_RESULT).to_csv(filepatOrig + "/data_from_UAV.csv")


def export_data(filename, prefixflight):
    Folder = os.path.dirname(filename)
    file_save = open(Folder + prefixflight + "-waypoints.txt", "w+")
    data = pd.read_csv(filename, sep=" ", header=None)
    data.columns = ["LON", "LAT", "elevation", "resolution", "index", "line"]
    data["index"] = data["index"] + 1
    file_save.write("QGC WPL 110\n")
    file_save.write(str(data["index"].iloc[0]) + " 1" + " 0" + " 22" + " 0" + " 0" + " 0" + " 0 "
                    + str(data["LAT"].iloc[0]) + " " + str(data["LON"].iloc[0]) + " " + str(
        data["elevation"].iloc[0]) + " 1" + '\n')
    data = data[1:]
    for index, row in data.iterrows():
        file_save.write(str(row["index"]) + " 0" + " 0" + " 16" + " 0" + " 0" + " 0" + " 0 "
                        + str(row["LAT"]) + " " + str(row["LON"]) + " " + str(row["elevation"]) + " 1" + '\n')

    file_save.write(str(data["index"].iloc[len(data) - 1]) + " 0" + " 0" + " 20" + " 0" + " 0" + " 0" + " 0 "
                    + str(data["LAT"].iloc[len(data) - 1]) + " " + str(data["LON"].iloc[len(data) - 1]) + " " + str(
        data["elevation"].iloc[len(data) - 1]) + " 1" + '\n')

    file_save.close()


# The main function of the python program
if __name__ == "__main__":
    print("These are the arguments")
    parser = argparse.ArgumentParser()
    parser.add_argument('-m', '--model', type=str, default='FC',
                        help='graph type to graph')
    parser.add_argument('-f', '--flight', type=str, default='FC',
                        help='graph type to graph')
    parser.add_argument('-r1', '--range1', type=str, default='0',
                        help='graph type to graph')
    parser.add_argument('-r2', '--range2', type=str, default='0',
                        help='graph type to graph')
    parser.add_argument('-i', '--proc', type=str, default='0',
                        help='graph type to graph')
    parser.add_argument('-b', '--block', type=str, default='0',
                        help='graph type to graph')

    io_args = parser.parse_args()
    arg1 = io_args.model
    filename = io_args.flight
    block = io_args.block

    proc = int(io_args.proc)
    if (int(io_args.range1) and int(io_args.range2)):
        range1 = int(io_args.range1)
        range2 = int(io_args.range2)
    else:
        range2 = 0
        range1 = 0

    if arg1 == "FourthDiff":
        qualitycheck = QualityCheck(filename)
        qualitycheck.load_data(proc)
        qualitycheck.fourtDifference(range1, range2)
    if arg1 == "MagProfile":
        qualitycheck = QualityCheck(filename)
        qualitycheck.load_data(proc)
        qualitycheck.magProfile()
    if arg1 == "LaserProfile":
        qualitycheck = QualityCheck(filename)
        qualitycheck.load_data(proc)
        qualitycheck.laserProfile()
    if arg1 == "FourthDiffBMag":
        qualitycheck = QualityCheck(filename)
        qualitycheck.load_data(proc)
        qualitycheck.fourtDifference(range1, range2)
    if arg1 == "BaseMagProfile":
        qualitycheck = QualityCheck(filename)
        qualitycheck.load_data(proc)
        qualitycheck.magProfile()
    if arg1 == "FlightMap":
        qualitycheck = QualityCheck(filename)
        qualitycheck.load_data(proc)
        qualitycheck.flightMap()
    if arg1 == "FlightMapPiksivsMav":
        qualitycheck = QualityCheck(filename)
        qualitycheck.load_data(proc)
        qualitycheck.FlightMapPiksivsMav()
    if arg1 == "MavAltvsPiksiAlt":
        qualitycheck = QualityCheck(filename)
        qualitycheck.load_data(proc)
        qualitycheck.mavalt_piksialt()
    if arg1 == "CounterMap":
        qualitycheck = QualityCheck(filename)
        qualitycheck.load_data(proc)
        qualitycheck.counter_map()
    if arg1 == "process":
        print("Processing Data")
        process_data(filename)
    if arg1 == "ExportFile":
        export_data(filename)
    if arg1 == "FlightPlan":
        for name in os.listdir(os.path.dirname(filename)):
            if name.__contains__("waypointsDataTieLine.txt") or name.__contains__("waypointsData.txt"):
                os.remove(os.path.dirname(filename) + "/" + name)
                print("removed " + name)
        flightPlanning = FlightPlanning(filename)
        flightPlanning.updateParams()
        flightPlanning.makeGrid(1)
        if (flightPlanning.tie_line == 1):
            flightPlanning.makeGrid(2)
    if arg1 == "FlightPlanBlocks":
        flightPlanning = FlightPlanning(filename)
        flightPlanning.updateParams()
        flightPlanning.createBlocks(1, block)
        if (flightPlanning.tie_line == 1):
            flightPlanning.createBlocks(2, block)
    if arg1 == "CreateFlight":
        flightPlanning = FlightPlanning(filename)
        flightPlanning.creatFlight()
    if arg1 == "GetCoords":
        flightPlanning = FlightPlanning(filename)
        flightPlanning.updateParams()
    # if arg1 == "QualityNoTurn":
    #     QualityCheckNoTurns = QualityCheckNoTurns(filename)
    #     # QualityCheckNoTurns.flightMap()
    print("ENDER")
