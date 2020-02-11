# LatLong- UTM conversion..h
# definitions for lat/long to UTM and UTM to lat/lng conversions
# include <string.h>

# This file include the functions for the FlightPlanning and Quality
# import scipy.interpolate
print("IS this still fucked")
from scipy.interpolate import interp1d
print("Hope Not")
import numpy as np
import scipy
import argparse
import os
import pandas as pd

LARGE_NUMBER = 1000000000000000000


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
        if (array[i + 1] - array[i]) > 0.1:
            array = array[i:]
            return array


# Processes data
def process_data(filename):
    print("Start Processing Data")
    # List out all the files for the process Data portion.
    file_paths = ["Mag.csv", "PiksiGPS.csv", "PiksiGPSTime.csv", "Mav.csv", "MavLaser.csv", "MavAtt.csv"]
    filepatOrig = os.path.dirname(filename)
    print(filepatOrig)
    foldername = os.path.basename(filepatOrig)
    listFlightFolders = list()
    print("Check")
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
    print(data_mag)
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
    print("Before Interpolation")
    Func_Polated_PiksiTime = scipy.interpolate.interp1d(BBB_time_piksi, Piksi_time, kind='linear', fill_value="extrapolate")
    Func_Polated_PiksiTimeGPSLAT = scipy.interpolate.interp1d(BBB_timePiksi_GPS, Lat_data, kind='linear', fill_value="extrapolate")
    Func_Polated_PiksiTimeGPSLON = scipy.interpolate.interp1d(BBB_timePiksi_GPS, Lon_data, kind='linear', fill_value="extrapolate")
    Func_Polated_MavDataGPSLON = scipy.interpolate.interp1d(BBB_mavTime, Lon_mav_data, kind='linear', fill_value="extrapolate")
    Func_Polated_MavDataGPSLAT = scipy.interpolate.interp1d(BBB_mavTime, Lat_mav_data, kind='linear', fill_value="extrapolate")
    Func_Polated_Laser = scipy.interpolate.interp1d(BBB_mavLaserTime, Laser, kind='linear', fill_value="extrapolate")
    print("after Intpl")

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


def export_data(filename):
    Folder = os.path.dirname(filename)
    file_save = open(Folder + "/waypoints.txt", "w+")
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

    io_args = parser.parse_args()
    arg1 = io_args.model
    filename = io_args.flight
    print(arg1);

    proc = int(io_args.proc)
    if int(io_args.range1) and int(io_args.range2):
        range1 = int(io_args.range1)
        range2 = int(io_args.range2)
    else:
        range2 = 0
        range1 = 0

    # if arg1 == "FourthDiff":
    #     qualitycheck = QualityCheck(filename)
    #     qualitycheck.load_data(proc)
    #     qualitycheck.fourtDifference(range1, range2)
    # if arg1 == "MagProfile":
    #     qualitycheck = QualityCheck(filename)
    #     qualitycheck.load_data(proc)
    #     qualitycheck.magProfile()
    # if arg1 == "LaserProfile":
    #     qualitycheck = QualityCheck(filename)
    #     qualitycheck.load_data(proc)
    #     qualitycheck.laserProfile()
    # if arg1 == "FourthDiffBMag":
    #     qualitycheck = QualityCheck(filename)
    #     qualitycheck.load_data(proc)
    #     qualitycheck.fourtDifference(range1, range2)
    # if arg1 == "BaseMagProfile":
    #     qualitycheck = QualityCheck(filename)
    #     qualitycheck.load_data(proc)
    #     qualitycheck.magProfile()
    # if arg1 == "FlightMap":
    #     qualitycheck = QualityCheck(filename)
    #     qualitycheck.load_data(proc)
    #     qualitycheck.flightMap()
    # if arg1 == "FlightMapPiksivsMav":
    #     qualitycheck = QualityCheck(filename)
    #     qualitycheck.load_data(proc)
    #     qualitycheck.FlightMapPiksivsMav()
    # if arg1 == "MavAltvsPiksiAlt":
    #     qualitycheck = QualityCheck(filename)
    #     qualitycheck.load_data(proc)
    #     qualitycheck.mavalt_piksialt()
    # if arg1 == "CounterMap":
    #     qualitycheck = QualityCheck(filename)
    #     qualitycheck.load_data(proc)
    #     qualitycheck.counter_map()
    if arg1 == "process":
        print("Processing Data")
        process_data(filename)
    if arg1 == "ExportFile":
        export_data(filename)
    # if arg1 == "FlightPlan":
    #     for name in os.listdir(os.path.dirname(filename)):
    #         if (name == "waypointsDataTieLine.txt" or name == "waypointsData.txt"):
    #             os.remove(os.path.dirname(filename) + "/" + name)
    #     flightPlanning = FlightPlanning(filename)
    #     flightPlanning.updateParams()
    #     flightPlanning.makeGrid(1)
    #     if (flightPlanning.tie_line == 1):
    #         flightPlanning.makeGrid(2)
    # if arg1 == "FlightPlanBlocks":
    #     flightPlanning = FlightPlanning(filename)
    #     flightPlanning.updateParams()
    #     flightPlanning.createBlocks(1)
    #     if (flightPlanning.tie_line == 1):
    #         flightPlanning.createBlocks(2)
    # if arg1 == "CreateFlight":
    #     flightPlanning = FlightPlanning(filename)
    #     flightPlanning.creatFlight()
    # if arg1 == "QualityNoTurn":
    #     QualityCheckNoTurns = QualityCheckNoTurns(filename)
    #     # QualityCheckNoTurns.flightMap()
    print("ENDER")
