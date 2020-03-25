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
        self.tie_init_point = np.array([])
        self.converter = Proj(proj='utm', zone=1, ellps='WGS84')
        self.wayPoints = pd.DataFrame(columns=['utmX', 'utmY', 'elevation', 'line', 'index', 'angle'])
        #All the UTM Zones
        # TODO: Solve the cross utmZone problem.
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
        if (line.startswith('TieStart:')):
            segs = line.split(":")
            print("kkkkkk", segs[1])
            if (len(segs[1]) > 2):
                self.tie_init_point = np.append(self.tie_init_point, self.coords((segs[1])[0:len(segs[1]) - 1]))
                self.tie_init_point = [float(val) for val in self.tie_init_point]
                self.converter = Proj(proj='utm', zone=self.findUtmZone(float(self.tie_init_point[0])), ellps='WGS84')
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
            for index, row in self.dfPolygan.iterrows():
                self.converter = Proj(proj='utm', zone=self.findUtmZone(float(row['LON'])), ellps='WGS84')
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
            self.converter = Proj(proj='utm', zone=self.findUtmZone(float(self.initPoint[0])), ellps='WGS84')
            tup = self.converter(self.initPoint[0], self.initPoint[1])
            self.initPointUTM = np.array([tup[0], tup[1]])
            strInitpointUtm = str(tup[0]) + "," + str(tup[1])
            f.write(strInitpointUtm)
            f.write('\n')
        file.close()

        print("Polygan Points: ", self.dfPolygan)
        print(self.dfPolyganUTM)
        print("Initial Point: ", self.initPoint)
        print(self.spacing)

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
        if (path.intersects(polygon) == True):
            straight = True
            # Comment below to find the start point of this
            # i += 1
            # self.wayPoints.loc[i] = [x, y, 0, lines, i, angle]
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
                if (polygon.exterior.distance(point) <= distance or path.intersects(polygon)):
                    straight = True
                    i += 1
                    self.wayPoints.loc[i] = [x, y, 0, lines, i, angle]
                else:
                    straight = False
                distance = polygon.exterior.distance(point)

            # Turning function
            else:
                lines += 1
                if ((lines % 2) == 1):
                    angle = angle + 90 * self.Clockwise
                else:
                    angle = angle - 90 * self.Clockwise
                x = round(math.cos(math.radians(angle)) * line_spacing + x, 10)
                y = round(math.sin(math.radians(angle)) * line_spacing + y, 10)
                # point = Point(x, y)
                # i += 1
                # self.wayPoints.loc[i] = [x, y, 0, lines, i, angle]
                if ((lines % 2) == 1):
                    angle = angle + 90 * self.Clockwise
                else:
                    angle = angle - 90 * self.Clockwise
                angle = (angle + 360) % 360
                x = round(math.cos(math.radians(angle)) * spacing + x, 10)
                y = round(math.sin(math.radians(angle)) * spacing + y, 10)
                point = Point(x, y)
                i += 1
                self.wayPoints.loc[i] = [x, y, 0, lines, i, angle]
                point2 = Point(x + math.cos(math.radians(angle)) *
                               LARGE_NUMBER, y + math.sin(math.radians(angle)) * LARGE_NUMBER)
                path = LineString([point2, point])
                if (path.intersects(polygon) == False):
                    break
                straight = True
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
        j = 1
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
                if (polygon.exterior.distance(point) <= distance or path.intersects(polygon)):
                    straight = True
                    i += 1
                    j -= 1
                    self.wayPoints.loc[i] = [x, y, 0, lines, j, (angle + 180) % 360]
                else:
                    straight = False
                distance = polygon.exterior.distance(point)
            else:
                lines -= 1
                if ((lines % 2) == 0):
                    angle = angle + 90 * self.Clockwise
                else:
                    angle = angle - 90 * self.Clockwise
                x = round(math.cos(math.radians(angle)) * line_spacing + x, 10)
                y = round(math.sin(math.radians(angle)) * line_spacing + y, 10)
                # point = Point(x, y)
                # i += 1
                # j -= 1
                # self.wayPoints.loc[i] = [x, y, 0, lines, j, angle - 180]
                if ((lines % 2) == 0):
                    angle = angle + 90 * self.Clockwise
                else:
                    angle = angle - 90 * self.Clockwise
                x = round(math.cos(math.radians(angle)) * spacing + x, 10)
                y = round(math.sin(math.radians(angle)) * spacing + y, 10)
                point = Point(x, y)
                i += 1
                j -= 1
                self.wayPoints.loc[i] = [x, y, 0, lines, j, (angle + 180) % 360]
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
        ac = 0
        # print(exterior[0][0])
        # dist = math.sqrt((exterior[0][0]-x)**2 + (exterior[0][1]-y)**2)
        # dist += math.sqrt((exterior[1][0]-x)**2 + (exterior[1][1]-y)**2)
        # First Point of line
        polygon = Polygon(exterior)
        point = Point(x, y)
        point2 = Point(x, y)
        # Find a point outside the polygon
        while (point2.within(polygon)):
            x = x + self.spacing * math.cos(np.radians(dir))
            y = y + self.spacing * math.sin(np.radians(dir))
            point2 = Point(x, y)
        path = LineString([point, point2])
        # Point that is the spacing length in direction from point
        # while point2 is still in the polygon, move it until it is out of the polygon.
        # What about when it just slices the block shape? Doesn't because it's always a straight line.

        # Iterate through all the boundaries of the block and see which edge intersects the line.
        # Also check if it is right on the boundary. Substitute.
        # print("Iterating through sides")
        size = len(exterior)
        for i in range(1, size + 1):
            point3 = Point(exterior[i % size][0], exterior[i % size][1])
            point4 = Point(exterior[(i + 1) % size][0], exterior[(i + 1) % size][1])
            # print(point3, point4)
            path2 = LineString([point3, point4])
            if (path2.intersects(path)):
                print(point3, point4)
                ac = i
                break

        # print("ac", ac)
        # print("---->", x, y)
        # yLine1 = exterior[ac % size][1]
        # yLine2 = exterior[(ac + 1) % size][1]
        #
        # xLine1 = exterior[ac % size][0]
        # xLine2 = exterior[(ac + 1) % size][0]

        # p1 + ta(p2 - p1) =
        # p3 + tb(p4 - p3)

        y1 = point.y
        y2 = point2.y
        y3 = exterior[ac % size][1]
        y4 = exterior[(ac + 1) % size][1]

        x1 = point.x
        x2 = point2.x
        x3 = exterior[ac % size][0]
        x4 = exterior[(ac + 1) % size][0]

        ta = (y3 - y4) * (x1 - x3) + (x4 - x3) * (y1 - y3)
        ta = ta / ((x4 - x3) * (y1 - y2) - (x1 - x2) * (y4 - y3))
        tb = (y1 - y2) * (x1 - x3) + (x2 - x1) * (y1 - y3)
        tb = tb / ((x4 - x3) * (y1 - y2) - (x1 - x2) * (y4 - y3))
        x_ret = x3 + tb * (x4 - x3)
        y_ret = y3 + tb * (y4 - y3)

        new_dist = np.sqrt((y_ret - y1) ** 2 + (x_ret - x1) ** 2)

        return (x_ret, y_ret, new_dist)

    # Makes remaining waypoints
    def make_rem_waypoints(self, blockExterior, x, y, angle, rem):
        return 0

    def make_extra_waypoints(self, blockExterior, x, y, angle):

        (x_ret, y_ret, dist) = self.find_intersection(blockExterior, x, y, angle)
        surveyExterior = [[float(val[0]), float(val[1])] for val in self.dfPolyganUTM.values]
        surveyPolygon = Polygon(surveyExterior)
        oslength = self.overshoot
        # print(self.dfPolyganUTM)
        x_ret = x_ret + oslength * math.cos(np.radians(angle))
        y_ret = y_ret + oslength * math.sin(np.radians(angle))
        outpoint = Point(x_ret, y_ret)
        if outpoint.within(surveyPolygon):
            oslength = self.overshootBlocks
        # if (mode == 1): oslength = self.overshoot
        # print(length)
        waypoints = list()
        i = math.floor(dist / self.spacing)
        for j in range(0, i):
            waypoints.append(self.spacing)
        dist = dist - (self.spacing * i)
        # nums_of_points = math.ceil(length / pitch)
        # extra_point = length % pitch
        # print("nums_of_points", pitch)
        # pitch = length / nums_of_points
        waypoints.append(dist)
        waypoints.append(oslength)

        # print("length of waypoints", len(waypoints))
        # waypoints.append(extra_point)
        return waypoints
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
    def createBlocks(self, type, block):
        # Function that Runs when Edit Block is running and Create BLock is pressed
        # Type 1 = Flight Lines, Type 2 = Tie lines
        print("Creating Block")
        print("Angle Clockwise? ", self.Clockwise)
        dataFilename = ""
        dfListOfBlock = pd.DataFrame(columns=['Name', 'Exterior'])
        surveyPlanFolder = os.path.dirname(self.filepath)
        surveyFolder = os.path.dirname(surveyPlanFolder)
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
        # Grab the waypoint data from
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

        # TODO: Heading instead of angle
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
            borders = polygonBlock.bounds
            polygonBlockBounds = Polygon.from_bounds(borders[0], borders[1], borders[2], borders[3])
            for i in range(1, int(dfWayPoints['line'].max())):
                dfTemp = dfWayPoints.loc[dfWayPoints['line'] == i]
                lineend = len(dfTemp.index)
                # Check if line intersects
                point = Point(dfTemp.iat[0, 0], dfTemp.iat[0, 1])
                point2 = Point(dfTemp.iat[lineend - 1, 0], dfTemp.iat[lineend - 1, 1])
                line = LineString([point2, point])
                angle = dfTemp.iat[0, 5]
                if (line.intersects(polygonBlock)):
                    beg = dfTemp.iat[0, 4] - 1
                    first = 0
                    last = 0
                    for j in reversed(range(0, lineend)):
                        point3 = Point(dfTemp.iat[j, 0], dfTemp.iat[j, 1])
                        line = LineString([point2, point3])
                        last = j
                        if line.intersects(polygonBlock):
                            # if not point3.within(polygonBlock):
                            #     point4 = Point(dfTemp.iat[j + 1, 0], dfTemp.iat[j + 1, 1])
                            #     point3 = self.find_point_inside(point4, point3, polygonBlock)
                            #     dfWayPoints.loc[j + 1 + beg] = [point3.x, point3.y, 0, i, j + beg + 1, angle, float(rowBlocks['Name'][5:])]
                            point2 = point3
                            break
                    for j in range(0, lineend):
                        point3 = Point(dfTemp.iat[j, 0], dfTemp.iat[j, 1])
                        line = LineString([point, point3])
                        first = j
                        if line.intersects(polygonBlock):
                            # if not point3.within(polygonBlock):
                            #     point4 = Point(dfTemp.iat[j - 1, 0], dfTemp.iat[j - 1, 1])
                            #     point3 = self.find_point_inside(point4, point3, polygonBlock)
                            #     dfWayPoints.loc[j - 1 + beg] = [point3.x, point3.y, 0, i, j + beg - 1, angle, float(rowBlocks['Name'][5:])]
                            point = point3
                            break
                    print(i ,beg, first, last)
                    if first < last:
                        for j in range(int(first + beg), int(last + beg + 1)):
                            # print("Before", dfWayPoints.iat[j, 6])
                            # point = Point(dfWayPoints.iat[j,0],dfWayPoints.iat[j,1])
                            if dfWayPoints.iat[j, 6] == -1.0:  # and point.within(polygonBlockBounds):
                                dfWayPoints.iat[j, 6] = float(rowBlocks['Name'][5:])
                                # print("After", dfWayPoints.iat[j, 6])
                    # If the line intersects but not points are inside the polygon, Find a point inside the polygon and make it a waypoint
                    else:
                        print("No points in thing")
                        point3 = self.find_point_inside(point, point2, polygonBlock)
                        angle = dfWayPoints.iat[int(last + beg), 5]
                        dfWayPoints.loc[int(last + beg)] = [point3.x, point3.y, 0, i, last + beg - 1, angle,
                                                            float(rowBlocks['Name'][5:])]

                #     print(i)

        # Compare the end of line n - 1 and start of n
        # If the n is ahead, make n - 1 get to n
        # Else make n - 1 get to n
        # Check the length from the nearer point to the further point,
        # if length is smaller than the length of remaining waypoints,
        # Then add the remaining waypoints that are not further than the furthest point and add the remainder length + applicable overshoot
        # else add all the remaining points and add the remainder distance +
        # Make a loop that loops
        for indexBlocks, rowBlocks in dfListOfBlock.iterrows():
            dfTemp = dfWayPoints.loc[dfWayPoints['Block'] != 0 - 1.0]
            dfPrev = dfWayPoints.loc[dfWayPoints['line'] == dfTemp['line'].min()]
            borders = polygonBlock.bounds
            polygonBlockBounds = Polygon.from_bounds(borders[0], borders[1], borders[2], borders[3])
            print(dfPrev)
            idxblk = float(rowBlocks['Name'][5:])
            angle = float(dfPrev.iat[0,5])
            for i in range(int(dfTemp['line'].min()) + 1, int(dfTemp['line'].max()) + 1):
                dfCurr = dfWayPoints.loc[dfWayPoints['line'] == i]
                for j in range(0, len(dfCurr)):
                    start = j
                    if dfCurr.iat[j, 6] == idxblk:
                        break
                for j in reversed(range(0, len(dfPrev))):
                    end = j
                    if dfPrev.iat[j, 6] == idxblk:
                        break
                # Get the Coordinates for the start and end of block and compare
                # print(dfCurr)
                # print(i, end,  start)
                x = dfPrev.iat[end, 0]
                y = dfPrev.iat[end, 1]
                x_o = dfCurr.iat[start, 0]
                y_o = dfCurr.iat[start, 1]
                anglex = math.cos(math.radians(angle)) * (x_o - x)
                angley = math.sin(math.radians(angle)) * (y_o - y)
                magnitude = math.sqrt((x_o - x) ** 2 + (y_o - y) ** 2)
                #dot product of direction vector and vector(x, y to x_o, y_o)
                vdot = (anglex + angley)/magnitude
                #
                fdegs = (math.degrees(math.acos((y_o - y) / magnitude)))
                if(x_o < x):
                    fdegs = ((-1 * fdegs) + 360) % 360
                print("dot prod: ", vdot, "fdegs: ", fdegs)
                # if vdot is > 0, that means that x_o y_o is ahead of x y
                if fdegs % 90 > 10 and fdegs % 90 < 80:
                    if(vdot > 0):
                        # Point Previous line is behind point next line
                        x_orig = x
                        y_orig = y
                        x_dest = x_o
                        y_dest = y_o
                    else:
                        # Point next line is behind Point Previous Line
                        x_orig = x_o
                        y_orig = y_o
                        x_dest = x
                        y_dest = y
                    x_dest_barrier = LineString([Point(x_dest, 0 - LARGE_NUMBER), Point(x_dest, LARGE_NUMBER)])
                    y_dest_barrier = LineString([Point(0 - LARGE_NUMBER, y_dest), Point(LARGE_NUMBER, y_dest)])
                    point_o = Point([x_orig, y_orig])
                    point_d = Point([x_dest, y_dest])
                    print(x_dest, y_dest)
                    x1 = x_orig + LARGE_NUMBER * math.cos(np.radians(angle))
                    y1 = y_orig + LARGE_NUMBER * math.sin(np.radians(angle))
                    point = Point(x_orig, y_orig)
                    point2 = Point(x1, y1)
                    path = LineString([point, point2])
                    # Get the parallel distance from end of point to the other end of point
                    dist = 0
                    if path.intersects(x_dest_barrier) and path.intersects(y_dest_barrier):
                        itsx = path.intersection(x_dest_barrier)
                        itsy = path.intersection(y_dest_barrier)
                        if(itsx.distance(point) < itsy.distance(point)):
                            dist = itsx.distance(point)
                        else:
                            dist = itsy.distance(point)
                    elif path.intersects(x_dest_barrier):
                        itsx = path.intersection(x_dest_barrier)
                        dist = itsx.distance(point)
                    else :
                        itsy = path.intersection(y_dest_barrier)
                        dist = itsy.distance(point)
                    # Get distance of remaining waypoints and compare lengths to determine the number of waypoints to be added.
                    # path = last point of line
                    # If length is less than spacing, then directly make extra waypoints.
                    if vdot > 0:
                        # On Previous line, get the last coordinate
                        point = Point([dfPrev.iat[-1, 0],dfPrev.iat[-1, 1]])
                        # Get the length of'
                    else:
                        # On Next Line , get the first coordinate
                        point = Point([dfCurr.iat[0, 0],dfCurr.iat[0, 1]])
                    path = LineString([point, point_o])
                    plen = path.length
                    print("Distance to Next Point: ",dist,"Distance to last point on waypoint line", plen)
                    if plen > dist:
                        add = math.floor(dist / self.spacing)
                        rem = dist % self.spacing
                    else:
                        add = math.floor(plen / self.spacing)
                        rem = dist - plen
                    print("Add ", add, " Waypoints. Block number:", idxblk)
                    if vdot > 0:
                        for j in range(end + 1, end + add + 1):
                            idx = dfPrev.iat[j, 4]
                            x = dfPrev.iat[j, 0]
                            y = dfPrev.iat[j, 1]
                            dfWayPoints.iat[int(idx) - 1, 6] = idxblk
                            print(dfWayPoints.loc[dfWayPoints['index'] == idx])
                    else:
                        for j in reversed(range(start - 1, start - add - 1)):
                            idx = dfCurr.iat[j, 4]
                            x = dfPrev.iat[j, 0]
                            y = dfPrev.iat[j, 1]
                            dfWayPoints.iat[int(idx) - 1, 6] = idxblk
                            print(dfWayPoints.loc[dfWayPoints['index'] == idx])
                dfPrev = dfCurr

        # print(dfWayPoints)
        # file = os.path.dirname(self.filepath) + "/waypointsData2.txt"
        # np.savetxt(file, dfWayPoints.values, fmt='%1.10f')
        # Name, Exterior
        for indexBlocks, rowBlocks in dfListOfBlock.iterrows():
            print("==========Block=========>>", indexBlocks + 1)
            filePointsBlock = surveyFolder + '/' + rowBlocks['Name'] + "/flight_plan" + dataFilename
            # Get the waypoints that has the Current Block number
            idxblk = float(rowBlocks['Name'][5:])
            df = dfWayPoints[dfWayPoints['Block'] == idxblk]
            if (len(df) == 0):  continue
            # Add one to make the first one one
            print("Min line: ",df['line'].min)
            # df.loc[:, 'line'] -= (df['line'].min() - 1)
            # df.loc[:, 'index'] = np.arange(len(df))
            # df = df.reset_index(drop=True)
            df_holder = pd.DataFrame(columns=["utmX", "utmY", "elevation", "line", "index", "angle", "Block"])
            j = 0
            angleOrig = float(df.iat[j, 5])
            while (((angleOrig - using_angle) / 180).is_integer() == False):
                j += 1
                angleOrig = float(df.iat[j, 5])
            # np.savetxt(filePointsBlock, df.values, fmt='%1.10f')
            # #Get the first coordinate of the Lines (First nearest Start)
            x = df.loc[df['line'] == df['line'].min()].iat[0, 0]
            y = df.loc[df['line'] == df['line'].min()].iat[0, 1]
            print(rowBlocks['Exterior'])
            print(x, y, angleOrig)
            # Makes Extra Waypoints for Beginning
            # list_extra = self.make_extra_waypoints(rowBlocks['Exterior'], x, y, angleOrig + 180)
            # for nums1 in list_extra:
            #     x = x + nums1 * math.cos(math.radians(angleOrig + 180))
            #     y = y + nums1 * math.sin(math.radians(angleOrig + 180))
                # df_holder.drop(df_holder.index, inplace=True)
                # df_holder.loc[-1] = [x, y, 0, 1, 0, angleOrig, idxblk]
                # df = pd.concat([df_holder, df])
            # Makes Extra Waypoints for Middle
            # for line in range(1, int(df['line'].max())):
            #     dfTemp = df.loc[df['line'] <= line]
            #     dfTempRest = pd.concat([dfTemp, df]).drop_duplicates(keep=False)
            #     dfTempRest = dfTempRest.reset_index(drop=True)
            #     print("=======LINE======>>", line)
            #     dfTemp = dfTemp.reset_index(drop=True)
            #     angle = angleOrig + 180 * ((line % 2) - 1)
            #     angle = (angle + 360) % 360
            #     print("=======angle======>>", angle)
            #     x = dfTemp["utmX"][len(dfTemp) - 1]
            #     y = dfTemp["utmY"][len(dfTemp) - 1]
            #     print(x, y)
            #     x_o = dfTempRest["utmX"][0]
            #     y_o = dfTempRest["utmY"][0]
                # list_extra = self.make_extra_waypoints(rowBlocks['Exterior'], x, y, angle)
                # list_extra2 = self.make_extra_waypoints(rowBlocks['Exterior'], x_o, y_o, angle)
                # print(list_extra)
                # print(list_extra2)
                # for nums1 in list_extra:
                #     x = x + nums1 * math.cos(math.radians(angle))
                #     y = y + nums1 * math.sin(math.radians(angle))
                #     # dfTemp.loc[len(dfTemp)] = [x, y, 0, line, len(dfTemp), angle, idxblk]
                # for nums2 in list_extra2:
                #     x_o = x_o + nums2 * math.cos(math.radians(angle))
                #     y_o = y_o + nums2 * math.sin(math.radians(angle))
                    # df_holder.drop(df_holder.index, inplace=True)
                    # df_holder.loc[-1] = [x_o, y_o, 0, line + 1, 0, angle + 180, idxblk]
                    # dfTempRest = pd.concat([df_holder, dfTempRest])
                #Dot product to see if the next line is ahead
            #     anglex = math.cos(math.radians(angle)) * (x_o - x)
            #     angley = math.sin(math.radians(angle)) * (y_o - y)
            #     magnitude = math.sqrt((x_o - x) ** 2 + (y_o - y) ** 2)
            #     #dot product of direction vector and vector(x, y to x_o, y_o)
            #     vdot = (anglex + angley)/magnitude
            #     #
            #     fdegs = (math.degrees(math.acos((y_o - y) / magnitude)))
            #     if(x_o < x):
            #         fdegs = ((-1 * fdegs) + 360) % 360
            #     print("dot prod: ", vdot, "fdegs: ", fdegs)
            #     # if the point is infront of the vector
            #     angle2 = (angle / 90)
            #     tolerance = 5
            #     x_orig = 0
            #     y_orig = 0
            #     x_dest = 0
            #     y_dest = 0
            #     mode = 0
            #     if((fdegs % 90) > tolerance and (fdegs % 90) < (90 - tolerance)):
            #         #TODO: This Conditiion is a little wonky.
            #         if(vdot > 0 and int(angle2) % 4 == int(fdegs / 90) % 4) or (vdot < 0 and not (int(angle2) + 2) % 4 == int(fdegs / 90) % 4):
            #             x_orig = x
            #             y_orig = y
            #             x_dest = x_o
            #             y_dest = y_o
            #             mode = 0
            #         else:
            #             x_orig = x_o
            #             y_orig = y_o
            #             x_dest = x
            #             y_dest = y
            #             mode = 1
            #         x_dest_barrier = LineString([Point(x_dest, 0 - LARGE_NUMBER), Point(x_dest, LARGE_NUMBER)])
            #         y_dest_barrier = LineString([Point(0 - LARGE_NUMBER, y_dest), Point(LARGE_NUMBER, y_dest)])
            #         print(x_orig, y_orig, mode)
            #         print(x_dest, y_dest)
            #         x1 = x_orig + self.spacing * math.cos(np.radians(angle))
            #         y1 = y_orig + self.spacing * math.sin(np.radians(angle))
            #         point = Point(x_orig, y_orig)
            #         point2 = Point(x1, y1)
            #         path = LineString([point, point2])
            #         while(not (path.intersects(x_dest_barrier) or path.intersects(y_dest_barrier))):
            #             print("Current Point",x1, y1)
            #             if(mode == 0):
            #                 dfTemp.loc[len(dfTemp)] = [x1, y1, 0, line, len(dfTemp), angle, idxblk]
            #             else:
            #                 df_holder.drop(df_holder.index, inplace=True)
            #                 df_holder.loc[-1] = [x1, y1, 0, line + 1, 0, angle + 180, idxblk]
            #                 dfTempRest = pd.concat([df_holder, dfTempRest])
            #             x1 = x1 + self.spacing * math.cos(np.radians(angle))
            #             y1 = y1 + self.spacing * math.sin(np.radians(angle))
            #             point2 = Point(x1, y1)
            #             path = LineString([point, point2])
            #         # Add the coordinate that is perpendicular to the next
            #         if (path.intersects(x_dest_barrier) and path.intersects(y_dest_barrier)):
            #             itsx = path.intersection(x_dest_barrier)
            #             itsy = path.intersection(y_dest_barrier)
            #             if(itsx.distance(point) < itsy.distance(point)):
            #                 x1 = itsx.x
            #                 y1 = itsx.y
            #             else:
            #                 x1 = itsy.x
            #                 y1 = itsy.y
            #         elif path.intersects(x_dest_barrier):
            #             itsx = path.intersection(x_dest_barrier)
            #             x1 = itsx.x
            #             y1 = itsx.y
            #         else :
            #             itsy = path.intersection(y_dest_barrier)
            #             x1 = itsy.x
            #             y1 = itsy.y
            #         if(mode == 0):
            #             dfTemp.loc[len(dfTemp)] = [x1, y1, 0, line, len(dfTemp), angle, idxblk]
            #         else:
            #             df_holder.drop(df_holder.index, inplace=True)
            #             df_holder.loc[-1] = [x1, y1, 0, line + 1, 0, angle + 180, idxblk]
            #             dfTempRest = pd.concat([df_holder, dfTempRest])
            #
            #     df = pd.concat([dfTemp, dfTempRest])
            #     df = df.reset_index(drop=True)
            #     df.loc[:, 'index'] = np.arange(len(df))
            # x = df.loc[len(df) - 1]["utmX"]
            # y = df.loc[len(df) - 1]["utmY"]
            # list_extra = self.make_extra_waypoints(rowBlocks['Exterior'], x, y, angle + 180)
            # for nums1 in list_extra:
            #     x = x + nums1 * math.cos(math.radians(angle + 180))
            #     y = y + nums1 * math.sin(math.radians(angle + 180))
                # x_o = x_o  + nums2* math.cos(math.radians(angle))
                # y_o = y_o  + nums2* math.sin(math.radians(angle))
                # df.loc[len(df)] = [x, y, 0, df['line'].max(), len(df), angle + 180, idxblk]
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
    # if arg1 == "QualityNoTurn":
    #     QualityCheckNoTurns = QualityCheckNoTurns(filename)
    #     # QualityCheckNoTurns.flightMap()
    print("ENDER")
