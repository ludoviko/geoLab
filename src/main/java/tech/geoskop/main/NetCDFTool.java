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

import org.slf4j.LoggerFactory;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Dimensions;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;
import ucar.nc2.write.NetcdfFormatWriter;

/**
 * Utilidad para ver los datos del netcdf.dat .
 * 
 * Ver
 * https://docs.unidata.ucar.edu/netcdf-java/current/userguide/reading_cdm.html
 * 
 * Next steps:
 * 
 * ingerir
 * 
 * [netCDF ] >> Ingest >> [Zarr]
 * 
 * [Zarr] >> Client request >> [csv]
 * 
 * [Zarr] >> leaflet request >> [web map]
 * 
 * tas_modeloXXX_algoYYY_sspZZZ_2021-2075.nc
 * 
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
	public static final String PATH_TO_NET_CDF_COPY_FILE = "src/main/resources/copy.nc";

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

		// set(int year, int month, int date, int hourOfDay, int minute, int second)
		// Month is zero-based.
		calendar.set(dateInArray[0], dateInArray[1] - 1, dateInArray[2]);

		calendar.add(Calendar.MINUTE, minutes);

		return formatter.format(calendar.getTime());
	}

	public static void main(String[] args) throws IOException, InvalidRangeException {

		try (NetcdfFile ncfile = NetcdfFiles.open(PATH_TO_NET_CDF_FILE)) {

			NetcdfFormatWriter.Builder builder = NetcdfFormatWriter.createNewNetcdf3(PATH_TO_NET_CDF_COPY_FILE);

			printMetadata(ncfile.getVariables());

			Variable latitude = ncfile.findVariable(LATITUDE);
			Variable longitude = ncfile.findVariable(LONGITUDE);
			Variable spatialRef = ncfile.findVariable(SPATIAL_REF);
			Variable time = ncfile.findVariable(TIME);
			Variable temperature = ncfile.findVariable(TEMPERATURE);
			Variable notExisting = ncfile.findVariable(NOT_EXISTING);

			printTemperature(temperature, 0, 0, 0, 105, 0, 212);

			// Latitude
			Dimension latDim = builder.addDimension(latitude.getFullName(), latitude.getShape(0));
			Dimension lonDim = builder.addDimension(longitude.getFullName(), longitude.getShape(1));
			List<Dimension> dims = new ArrayList<Dimension>();

			dims.add(latDim);
			dims.add(lonDim);
			builder.addVariable(latitude.getFullName(), latitude.getDataType(), dims)
					.addAttributes(latitude.attributes());

			// Longitude
			builder.addVariable(longitude.getFullName(), longitude.getDataType(), dims)
					.addAttributes(longitude.attributes());

			// time
			Dimension timeDim = builder.addDimension("time", 1);
			List<Dimension> timeDims = new ArrayList<Dimension>();
			timeDims.add(timeDim);

			builder.addVariable(time.getFullName(), time.getDataType(), timeDims).addAttributes(time.attributes());

			// tas
			List<Dimension> tasDims = new ArrayList<Dimension>();
			tasDims.add(timeDim);
			tasDims.add(latDim);
			tasDims.add(lonDim);

			builder.addVariable(temperature.getFullName(), temperature.getDataType(), tasDims)
					.addAttributes(temperature.attributes());

			try (NetcdfFormatWriter writer = builder.build()) {
				// write data
				// } catch (IOException e) {
			}

			try (NetcdfFormatWriter writer = NetcdfFormatWriter.openExisting(PATH_TO_NET_CDF_COPY_FILE).build()) {
				// write data
				Variable latNew = writer.findVariable(LATITUDE);
				writer.write(latNew, latitude.read());
				Variable lonNew = writer.findVariable(LONGITUDE);
				writer.write(lonNew, longitude.read());

				// Fortran 0 based inclusive index.
				List<Range> ranges = new ArrayList<>();
				ranges.add(new Range(0, 0));
				Variable timeNew = writer.findVariable(TIME);
				writer.write(timeNew, time.read(ranges));

				Variable tasNew = writer.findVariable(TEMPERATURE);
				writer.write(tasNew,
						loadTemperature(temperature, 0, 0, 0, latitude.getShape(0) - 1, 0, latitude.getShape(1) - 1));

				// } catch (IOException e) {
			}

		}
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

			printTemperature(temperature, 0, 10, 4);
			printTemperature(temperature, 0, 10, 5);
			printTemperature(temperature, 0, 1, 0, 105, 0, 212);

//			loadMainVar(temperature);

		}
	}

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
			out.printf("Variable %s %n", variable);

			out.println();
		}

	}

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

	public static Array loadTime(Variable time) throws IOException, InvalidRangeException {
		return time.read();
	}

	public static Array loadCRS(Variable spatialRef) throws IOException, InvalidRangeException {
		return spatialRef.read();
	}

	public static void printCRS(Variable spatialRef) throws IOException, InvalidRangeException {
		Array crsArray = spatialRef.read();

		out.printf("Name: %s%n", spatialRef.getFullName());
		out.println(crsArray);

		out.println();
	}

	public static Array loadLatitude(Variable latitude) throws IOException, InvalidRangeException {
		return latitude.read();
	}

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

	public static Array loadLongitude(Variable longitude) throws IOException, InvalidRangeException {
		return longitude.read();
	}

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

	private static Array loadTemperature(Variable temperature, int idxTimeIni, int idxTimeEnd, int idxLatIni,
			int idxLatEnd, int idxLonIni, int idxLonEnd) throws IOException, InvalidRangeException {

		String range = String.format("%d:%d, %d:%d, %d:%d", idxTimeIni, idxTimeEnd, idxLatIni, idxLatEnd, idxLonIni,
				idxLonEnd);

		return temperature.read(range);

	}

	private static void printTemperature(Variable temperature, int idxTimeIni, int idxTimeEnd, int idxLatIni,
			int idxLatEnd, int idxLonIni, int idxLonEnd) throws IOException, InvalidRangeException {

//		int[] shapeInitial = temperature.getShape();

		String range = String.format("%d:%d, %d:%d, %d:%d", idxTimeIni, idxTimeEnd, idxLatIni, idxLatEnd, idxLonIni,
				idxLonEnd);

		Array data = temperature.read(range);

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

	public static Array loadTemperature(Variable temperature, int idxTime, int idxLat, int idxLon)
			throws IOException, InvalidRangeException {

		String range = String.format("%d, %d, %d", idxTime, idxLat, idxLon);

		return temperature.read(range);
	}

	public static void printTemperature(Variable temperature, int idxTime, int idxLat, int idxLon)
			throws IOException, InvalidRangeException {

//		int[] shapeInitial = temperature.getShape();

		String range = String.format("%d, %d, %d", idxTime, idxLat, idxLon);

		Array data = temperature.read(range);

		int[] shape = data.getShape();
		Index index = data.getIndex();

		out.println("Ranges[time, lat, lon]: " + range);
		out.println("Indices temperature: " + index);
		out.println("Shape " + Arrays.toString(shape));

		double dval = data.getDouble(index.set(0, 0, 0));
		out.printf("Temperature: %4.8f %n", dval);
		out.println();
	}

	public static Array loadMainVarAll(Variable temperature) throws IOException, InvalidRangeException {
		return temperature.read();
	}

	public static void printMainVarAll(Variable temperature) throws IOException, InvalidRangeException {

		// Exception in thread "main" java.lang.OutOfMemoryError: Java heap space

		int[] shape = temperature.getShape();
		Array data = temperature.read();

		Index index = data.getIndex();

		out.println("Shape " + Arrays.toString(shape));

		for (int i = 0; i < shape[0]; i++) {
			out.printf("idx time %d %n", i);
			for (int j = 0; j < shape[1]; j++) {
				out.printf("idx lat %4d", j);
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
	 * Utilidad para imprimir.
	 * 
	 * @param temperature
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	public static void printMainVar(Variable temperature) throws IOException, InvalidRangeException {

		int[] shape = temperature.getShape();
		int temperaturaSize = shape[0];
		int latSize = shape[1];
		int lonSize = shape[2];

		int chunkSize = 1000;
		int offSet = 0;

		if (temperaturaSize > chunkSize) {
			for (int t = chunkSize; t < shape[0]; t += chunkSize) {
				// out.printf("offset %d, upTo %d%n", offSet, t - 1);
				printTemperature(temperature, offSet, t - 1, 0, latSize - 1, 0, lonSize - 1);
				offSet += chunkSize;
			}

			if (offSet < temperaturaSize) {
				// out.printf("offset %d, upTo %d%n", offSet, temperaturaSize - 1);
				printTemperature(temperature, offSet, temperaturaSize - 1, 0, latSize - 1, 0, lonSize - 1);
			}

		} else {
			printMainVarAll(temperature);
		}

	}

}
