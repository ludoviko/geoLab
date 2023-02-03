package tech.geoskop.main;

import static java.lang.System.out;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;
import ucar.nc2.write.NetcdfFormatWriter;

/**
 * Class for inspecting and copying a netcdf file .
 * 
 * See
 * https://docs.unidata.ucar.edu/netcdf-java/current/userguide/reading_cdm.html
 * 
 * Next steps:
 * 
 * 
 * [netCDF ] >> Ingest >> [Zarr]
 * 
 * [Zarr] >> Client request >> [csv]
 * 
 * [Zarr] >> leaflet request >> [web map]
 * 
 * tas_modeloXXX_algoYYY_sspZZZ_2021-2075.nc
 * 
 * LAT0LON0/LAT0LON1/LAT0LON2 (three regions) -> netCDF3 (single netCDF) ZARR de
 * temperatura ZARR de precipitacion ZARR de viento ZARR de noches tropicales ZA
 * 
 * TO DO: Use log and not console for printing data.
 *
 */
public class NetCDFTool {

	public static final String NOT_EXISTING = "XXX";
	public static final String UNITS = "units";
	public static final String LATITUDE = "lat";
	public static final String LONGITUDE = "lon";
	public static final String SPATIAL_REF = "spatial_ref";
	public static final String TIME = "time";
	public static final String TEMPERATURE = "tas";
	public static final String PATH_TO_NET_CDF_FILE = "src/main/resources/testmean.nc";
	public static final String PATH_TO_NET_CDF_COPY_FILE = "src/main/resources/copyByRange.nc";

	public static int[] convertTimeMetaDateToIntArray(String metadata) {
		// "minutes since 1850-01-01";
		String[] data = metadata.split(" ");

		String dateStartSince = data[data.length - 1];

		String[] dateArrayYYYMMDD = dateStartSince.split("-");

		return Stream.of(dateArrayYYYMMDD).mapToInt(Integer::parseInt).toArray();

	}

	public static String minutesFrom_1850_01_01_toDateString(int minutes) {
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

		Calendar calendar = Calendar.getInstance();

		// set(int year, int month, int date, int hourOfDay, int minute, int second)
		calendar.set(1850, 0, 1);

		calendar.add(Calendar.MINUTE, minutes);

		return formatter.format(calendar.getTime());
	}

	public static String dateMetaDatatoDateString(int minutes, String dateMetaData) {
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

		Calendar calendar = Calendar.getInstance();

		int[] dateInArray = convertTimeMetaDateToIntArray(dateMetaData);

		// calendar.set(int year, int month, int date, int hourOfDay, int minute, int
		// second)
		// Month is zero-based.
		calendar.set(dateInArray[0], dateInArray[1] - 1, dateInArray[2]);

		calendar.add(Calendar.MINUTE, minutes);

		return formatter.format(calendar.getTime());
	}

	/**
	 * Example of reading a netcdf file and copy some of its data into a
	 * destination file.
	 * 
	 * @param args
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	public static void main(String[] args) throws IOException, InvalidRangeException {

		try (NetcdfFile ncfile = NetcdfFiles.open(PATH_TO_NET_CDF_FILE)) {

			NetcdfFormatWriter.Builder builder = NetcdfFormatWriter.createNewNetcdf3(PATH_TO_NET_CDF_COPY_FILE);

			printMetadata(ncfile.getVariables());

			Variable latitude = ncfile.findVariable(LATITUDE);
			Variable longitude = ncfile.findVariable(LONGITUDE);
			Variable spatialRef = ncfile.findVariable(SPATIAL_REF);
			Variable time = ncfile.findVariable(TIME);

			// main var
			Variable mainVar = ncfile.findVariable(TEMPERATURE);
			Variable notExisting = ncfile.findVariable(NOT_EXISTING);

			// Ranges
//			Range latRange = new Range(10, 20); // 0, 105
//			Range lonRange = new Range(20, 40); // 0, 212
//			Range timeRange = new Range(0, 0); // 0, 20087
			
            // inclusive upper bound.
			Range latRange = new Range(0, latitude.getShape(0) - 1); // 0, 105
			Range lonRange = new Range(0, longitude.getShape(1) - 1); // 0, 212
			Range timeRange = new Range(0, 1); // 0, 20087
			
			List<Range> latRanges = new ArrayList<>();
			List<Range> lonRanges = new ArrayList<>();

			latRanges.add(latRange);
			latRanges.add(lonRange);

			lonRanges.add(latRange);
			lonRanges.add(lonRange);

			List<Range> timeRanges = new ArrayList<>();
			timeRanges.add(timeRange);

			// spatialRef
			Dimension spatialRefDim = builder.addDimension(spatialRef.getFullName(), spatialRef.getShape(0));
			List<Dimension> spatialRefDims = new ArrayList<Dimension>();
			spatialRefDims.add(spatialRefDim);

			builder.addVariable(spatialRef.getFullName(), spatialRef.getDataType(), spatialRefDims)
					.addAttributes(spatialRef.attributes());

			// Latitude
			Dimension latDim = builder.addDimension(latitude.getFullName(), latRange.last() - (latRange.first() - 1));
			Dimension lonDim = builder.addDimension(longitude.getFullName(), lonRange.last() - (lonRange.first() - 1));
			List<Dimension> latLonDims = new ArrayList<Dimension>();
			latLonDims.add(latDim);
			latLonDims.add(lonDim);

			builder.addVariable(latitude.getFullName(), latitude.getDataType(), latLonDims)
					.addAttributes(latitude.attributes());

			// Longitude
			builder.addVariable(longitude.getFullName(), longitude.getDataType(), latLonDims)
					.addAttributes(longitude.attributes());

			// Time
			Dimension timeDim = builder.addDimension(time.getFullName(), timeRange.last() - (timeRange.first() - 1));
			List<Dimension> timeDims = new ArrayList<Dimension>();
			timeDims.add(timeDim);

			builder.addVariable(time.getFullName(), time.getDataType(), timeDims).addAttributes(time.attributes());

			// main var
			List<Dimension> mainVarDims = new ArrayList<Dimension>();
			// Keep order !
			mainVarDims.add(timeDim);
			mainVarDims.add(latDim);
			mainVarDims.add(lonDim);

			builder.addVariable(mainVar.getFullName(), mainVar.getDataType(), mainVarDims)
			.addAttributes(mainVar.attributes());

			// fake main var 2: precipitation 
			
			builder.addVariable("precipitation", mainVar.getDataType(), mainVarDims)
					.addAttributes(mainVar.attributes());

			// The variables are copied to the destination file.
			try (NetcdfFormatWriter writer = builder.build()) {
				// write data
				// } catch (IOException e) {
			}

			// The actual data is copied to the destination file.
			try (NetcdfFormatWriter writer = NetcdfFormatWriter.openExisting(PATH_TO_NET_CDF_COPY_FILE).build()) {
				// write data to the new file.
				Variable latNew = writer.findVariable(LATITUDE);
				writer.write(latNew, latitude.read(latRanges));

				Variable lonNew = writer.findVariable(LONGITUDE);
				writer.write(lonNew, longitude.read(lonRanges));

				// Fortran 0 based inclusive index.
				Variable timeNew = writer.findVariable(TIME);
				writer.write(timeNew, time.read(timeRanges));

				Variable mainNew = writer.findVariable(TEMPERATURE);
				writer.write(mainNew, loadMainVar(mainVar, timeRange, latRange, lonRange));

				Variable fakeNew = writer.findVariable("precipitation");
				writer.write(fakeNew, loadMainVar(mainVar, timeRange, latRange, lonRange));
				
			}

			// The new file is printed on the console to check the contents.
			try (NetcdfFile destinationFile = NetcdfFiles.open(PATH_TO_NET_CDF_COPY_FILE)) {

				Variable latitudeDes = destinationFile.findVariable(LATITUDE);
				Variable longitudeDes = destinationFile.findVariable(LONGITUDE);
				Variable timeDes = destinationFile.findVariable(TIME);
				Variable mainVarDes = destinationFile.findVariable(TEMPERATURE);

				printMetadata(destinationFile.getVariables());

				printLatitude(latitudeDes);
				printLongitude(longitudeDes);
				printTime(timeDes);
				printMainVar(mainVarDes);
			}

		}
	}

	public static void createNetCdf3() {
		
	}
	
	
	public static void main2(String[] args) throws IOException, InvalidRangeException {

		try (NetcdfFile ncfile = NetcdfFiles.open(PATH_TO_NET_CDF_FILE)) {

			printMetadata(ncfile.getVariables());

			Variable latitude = ncfile.findVariable(LATITUDE);
			Variable longitude = ncfile.findVariable(LONGITUDE);
			Variable spatialRef = ncfile.findVariable(SPATIAL_REF);
			Variable time = ncfile.findVariable(TIME);
			Variable temperature = ncfile.findVariable(TEMPERATURE);
			Variable notExisting = ncfile.findVariable(NOT_EXISTING);

			printLatitude(latitude);
			printLongitude(longitude);
			printCRS(spatialRef);
			printTime(time);

			printMainVar(temperature, 0, 10, 4);
			printMainVar(temperature, 0, 10, 5);
			printMainVar(temperature, 0, 1, 0, 105, 0, 212);

//			loadMainVar(temperature);

		}
	}

	/**
	 * Utility for printing the metadata of a file.
	 * 
	 * @param variables
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	public static void printMetadata(List<Variable> variables) throws IOException, InvalidRangeException {
		out.println(variables);

		Iterator<Variable> itera = variables.iterator();
		out.println();

		Variable variable = null;

		while (itera.hasNext()) {
			variable = itera.next();

			out.printf("variable.getFullName() %s %n", variable.getFullName());
			out.printf("variable.getDataType() %s %n", variable.getDataType());
			out.printf("variable.getDimensions() %s %n", variable.getDimensions());
			out.printf("variable.getDimensionsString() %s %n", variable.getDimensionsString());
			out.printf("Units: %s %n", variable.getUnitsString());
			out.printf("Shape %s %n", Arrays.toString(variable.getShape()));
			Iterator<Attribute> attributes = variable.attributes().iterator();
			
			while (attributes.hasNext()) {
				Attribute attribute = attributes.next(); 
				out.printf("Attribute: %s  %n", attribute);
			}
			
			
			out.printf("Variable %s %n", variable);

			out.println();
		}
	}

	/**
	 * Utility for printing a time variable using a range.
	 * 
	 * @param time
	 * @param ranges
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	public static void printTime(Variable time, List<Range> ranges) throws IOException, InvalidRangeException {
		int[] shape = time.getShape();

		Array data = time.read(ranges);

		Index index = data.getIndex();

		out.printf("Time size: %d%n", shape[0]);

		// reference time is: (1850-01-01T00:00) + dval (en minutos) =
		out.printf("%s %-8s %s %n", "Index", "Minutes", "Date");
		for (int i = 0; i < shape[0]; i++) {
			int minutes = data.getInt(index.set(i));
//			out.printf("%5d %d %s %n", i, minutes, minutesFrom_1850_01_01_toDateString(minutes));
			out.printf("%5d %d %s %n", i, minutes, dateMetaDatatoDateString(minutes, time.getUnitsString()));

		}
		out.println();
	}

	/**
	 * Utility for printing the time variable, all.
	 * 
	 * @param time
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	public static void printTime(Variable time) throws IOException, InvalidRangeException {
		int[] shape = time.getShape();

		Array data = time.read();

		Index index = data.getIndex();

		out.printf("Time size: %d%n", shape[0]);

		// reference time is: (1850-01-01T00:00) + dval (en minutos) =
		out.printf("%s %-8s %s %n", "Index", "Minutes", "Date");
		for (int i = 0; i < shape[0]; i++) {
			int minutes = data.getInt(index.set(i));
//			out.printf("%5d %d %s %n", i, minutes, minutesFrom_1850_01_01_toDateString(minutes));
			out.printf("%5d %d %s %n", i, minutes, dateMetaDatatoDateString(minutes, time.getUnitsString()));

		}
		out.println();
	}

	/**
	 * Utility for reading the time data using a range.
	 * 
	 * @param time
	 * @param ranges
	 * @return
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	public static Array loadTime(Variable time, List<Range> ranges) throws IOException, InvalidRangeException {
		return time.read(ranges);
	}

	/**
	 * Utility for printing the spatialRef variable.
	 * 
	 * @param spatialRef
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	public static void printCRS(Variable spatialRef) throws IOException, InvalidRangeException {
		Array crsArray = spatialRef.read();

		out.printf("Name: %s%n", spatialRef.getFullName());
		out.println(crsArray);

		out.println();
	}

	/**
	 * Utility for reading a latitude using a range.
	 * 
	 * @param latitude
	 * @param ranges
	 * @return
	 * @throws IOException
	 * @throws InvalidRangeException
	 */

	public static Array loadLatitude(Variable latitude, List<Range> ranges) throws IOException, InvalidRangeException {
		return latitude.read(ranges);
	}

	/**
	 * Utility for printing the latitude points.
	 * 
	 * @param latitude
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	public static void printLatitude(Variable latitude) throws IOException, InvalidRangeException {
		int[] shape = latitude.getShape();

		// Convert number of rows to Fortran 0 base and inclusive index.
		int rows = shape[0] - 1;

		// Print in one line the first element of each row.
		Array latArray = latitude.read("0:" + rows + ", 0");

		out.printf("Latitude size: %d%n", shape[0]);
		out.printf("For each latitude there must be %d longitude points.%n", shape[1]);
		out.println(latArray);

		out.println();
	}

	/**
	 * Utility for reading the longitude points usinga range.
	 * 
	 * @param longitude
	 * @param ranges
	 * @return
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	public static Array loadLongitude(Variable longitude, List<Range> ranges)
			throws IOException, InvalidRangeException {
		return longitude.read(ranges);
	}

	/**
	 * Utility for printing the longitude points.
	 * 
	 * 
	 * @param longitude
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	public static void printLongitude(Variable longitude) throws IOException, InvalidRangeException {
		int[] shape = longitude.getShape();

		// Convert number of rows to Fortran 0 base and inclusive index.
		int columns = shape[1] - 1;

		// Print in one line all elements of the first row.
		Array lonArray = longitude.read("0, " + "0:" + columns);

		out.printf("Longitude size: %d%n", shape[1]);
		out.printf("For each longitude there must be %d latitude points.%n", shape[0]);
		out.println(lonArray);

		out.println();
	}

	/**
	 * Utility for reading the main var using indices type ini/end for each
	 * variable.
	 * 
	 * @param mainVar
	 * @param idxTimeIni
	 * @param idxTimeEnd
	 * @param idxLatIni
	 * @param idxLatEnd
	 * @param idxLonIni
	 * @param idxLonEnd
	 * @return
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	public static Array loadMainVar(Variable mainVar, int idxTimeIni, int idxTimeEnd, int idxLatIni, int idxLatEnd,
			int idxLonIni, int idxLonEnd) throws IOException, InvalidRangeException {

		String range = String.format("%d:%d, %d:%d, %d:%d", idxTimeIni, idxTimeEnd, idxLatIni, idxLatEnd, idxLonIni,
				idxLonEnd);

		return mainVar.read(range);

	}

	/**
	 * Utility for writing the main var using ranges.
	 * 
	 * @param mainVar
	 * @param mainRange
	 * @param latRange
	 * @param lonRange
	 * @return
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	public static Array loadMainVar(Variable mainVar, Range mainRange, Range latRange, Range lonRange)
			throws IOException, InvalidRangeException {
		List<Range> ranges = new ArrayList<>();

		ranges.add(mainRange);
		ranges.add(latRange);
		ranges.add(lonRange);

		return mainVar.read(ranges);
	}

	/**
	 * Utility for printing the main var using ranges.
	 * 
	 * @param mainVar
	 * @param mainRange
	 * @param latRange
	 * @param lonRange
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	public static void printMainVar(Variable mainVar, Range mainRange, Range latRange, Range lonRange)
			throws IOException, InvalidRangeException {

		List<Range> ranges = new ArrayList<>();

		ranges.add(mainRange);
		ranges.add(latRange);
		ranges.add(lonRange);

		String range = String.format("%d:%d, %d:%d, %d:%d", mainRange.first(), mainRange.last(), latRange.first(),
				latRange.last(), lonRange.first(), lonRange.last());

		Array data = mainVar.read(ranges);

		int[] shape = data.getShape();
		Index index = data.getIndex();

		out.println("Ranges[timeIni:timeEnd, latIni:latEnd, lonIni:lonEnd] (Fortran 0 based inclusive upper bound): "
				+ range);
		out.println("Indices temperature: " + index);
		out.println("Shape " + Arrays.toString(shape));

		for (int i = 0; i < shape[0]; i++) {
			out.printf("idx time %d %n", i);
			for (int j = 0; j < shape[1]; j++) {
				out.printf("idx lat %4d", j);
				for (int k = 0; k < shape[2]; k++) {
					double dval = data.getDouble(index.set(i, j, k));
					out.printf("%15.8f ", dval);
				}
				out.println();
			}
		}
		out.println();
	}

	/**
	 * Utility for printing the main var using indices type ini/end.
	 * 
	 * @param mainVar
	 * @param idxTimeIni
	 * @param idxTimeEnd
	 * @param idxLatIni
	 * @param idxLatEnd
	 * @param idxLonIni
	 * @param idxLonEnd
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	public static void printMainVar(Variable mainVar, int idxTimeIni, int idxTimeEnd, int idxLatIni, int idxLatEnd,
			int idxLonIni, int idxLonEnd) throws IOException, InvalidRangeException {

//		int[] shapeInitial = mainVar.getShape();

		String range = String.format("%d:%d, %d:%d, %d:%d", idxTimeIni, idxTimeEnd, idxLatIni, idxLatEnd, idxLonIni,
				idxLonEnd);

		Array data = mainVar.read(range);

		int[] shape = data.getShape();
		Index index = data.getIndex();

		out.println("Ranges[timeIni:timeEnd, latIni:latEnd, lonIni:lonEnd] (Fortran 0 based inclusive upper bound): "
				+ range);
		out.println("Indices temperature: " + index);
		out.println("Shape " + Arrays.toString(shape));

		for (int i = 0; i < shape[0]; i++) {
			out.printf("idx time %d %n", i);
			for (int j = 0; j < shape[1]; j++) {
				out.printf("idx lat %4d", j);
				for (int k = 0; k < shape[2]; k++) {
					double dval = data.getDouble(index.set(i, j, k));
					out.printf("%15.8f ", dval);
				}
				out.println();
			}
		}
		out.println();
	}

	/**
	 * Utility for reading a concrete mainVar.
	 * 
	 * @param msinVar
	 * @param idxTime
	 * @param idxLat
	 * @param idxLon
	 * @return
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	public static Array loadMainVar(Variable mainVar, int idxTime, int idxLat, int idxLon)
			throws IOException, InvalidRangeException {

		String range = String.format("%d, %d, %d", idxTime, idxLat, idxLon);

		return mainVar.read(range);
	}

	/**
	 * 
	 * Utility for printing a concrete mainVar.
	 * 
	 * @param mainVar
	 * @param idxTime
	 * @param idxLat
	 * @param idxLon
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	public static void printMainVar(Variable mainVar, int idxTime, int idxLat, int idxLon)
			throws IOException, InvalidRangeException {

//		int[] shapeInitial = mainVar.getShape();

		String range = String.format("%d, %d, %d", idxTime, idxLat, idxLon);

		Array data = mainVar.read(range);

		int[] shape = data.getShape();
		Index index = data.getIndex();

		out.println("Ranges[time, lat, lon]: " + range);
		out.println("Indices temperature: " + index);
		out.println("Shape " + Arrays.toString(shape));

		double dval = data.getDouble(index.set(0, 0, 0));
		out.printf("Temperature: %4.8f %n", dval);
		out.println();
	}

	/**
	 * Utility for printing all the data in the main var.
	 * 
	 * @param mainVar
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	public static void printMainVarAll(Variable mainVar) throws IOException, InvalidRangeException {

		// Exception in thread "main" java.lang.OutOfMemoryError: Java heap space

		int[] shape = mainVar.getShape();
		Array data = mainVar.read();

		Index index = data.getIndex();

		out.println("Shape " + Arrays.toString(shape));

		for (int i = 0; i < shape[0]; i++) {
			out.printf("idx time %d %n", i);
			for (int j = 0; j < shape[1]; j++) {
				out.printf("idx lat %4d  ", j);
				for (int k = 0; k < shape[2]; k++) {
					double dval = data.getDouble(index.set(i, j, k));
					out.printf("%4.8f ", dval);
				}
				out.println();
			}
		}
		out.println();
	}

	/**
	 * Wrapper for printing all the data using chunks.
	 * 
	 * @param mainVar
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	public static void printMainVar(Variable mainVar) throws IOException, InvalidRangeException {

		int[] shape = mainVar.getShape();
		int mainSize = shape[0];
		int latSize = shape[1];
		int lonSize = shape[2];

		// chunkSize could be static, it is a fraction of main size.
		int chunkSize = 1000;
		int offSet = 0;

		if (mainSize > chunkSize) {
			for (int t = chunkSize; t < shape[0]; t += chunkSize) {
				// out.printf("offset %d, upTo %d%n", offSet, t - 1);
				printMainVar(mainVar, offSet, t - 1, 0, latSize - 1, 0, lonSize - 1);
				offSet += chunkSize;
			}

			if (offSet < mainSize) {
				// out.printf("offset %d, upTo %d%n", offSet, temperaturaSize - 1);
				printMainVar(mainVar, offSet, mainSize - 1, 0, latSize - 1, 0, lonSize - 1);
			}

		} else {
			printMainVarAll(mainVar);
		}

	}
}
