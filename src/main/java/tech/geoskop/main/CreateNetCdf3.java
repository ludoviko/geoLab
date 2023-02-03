package tech.geoskop.main;

import ucar.ma2.Range;
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

import ucar.nc2.Attribute;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;
import ucar.nc2.write.NetcdfFormatWriter;



public class CreateNetCdf3 {
	
    // XXX nombre del archivo ?
	public static final String PATH_TO_NET_CDF_CREATE_FILE = "src/main/resources/createByHand.nc";


	public static void main(String[] args) throws InvalidRangeException {
		// XXX Cuáles variables obligatorias y cuales opcionales. 
		
		NetcdfFormatWriter.Builder builder = NetcdfFormatWriter.createNewNetcdf3(PATH_TO_NET_CDF_CREATE_FILE);

		// 1. spatial_ref
		String nameSR = "spatial_ref"; 
		Dimension  spatialRefDim = builder.addDimension("nchar", 256);
		List<Dimension> spatialRefDims = new ArrayList<Dimension>();
		spatialRefDims.add(spatialRefDim);

		Variable.Builder varBuilderSR = builder.addVariable(nameSR, DataType.CHAR, spatialRefDims);
		varBuilderSR.addAttribute(new Attribute("crs_wkt", "GEOGCS[\\\"WGS 84\\\",DATUM[\\\"WGS_1984\\\",SPHEROID[\\\"WGS 84\\\",6378137,298.257223563,AUTHORITY[\\\"EPSG\\\",\\\"7030\\\"]],AUTHORITY[\\\"EPSG\\\",\\\"6326\\\"]],PRIMEM[\\\"Greenwich\\\",0,AUTHORITY[\\\"EPSG\\\",\\\"8901\\\"]],UNIT[\\\"degree\\\",0.0174532925199433,AUTHORITY[\\\"EPSG\\\",\\\"9122\\\"]],AUTHORITY[\\\"EPSG\\\",\\\"4326\\\"]]"));
		
		
		// 2. Latitude
		String nameLat = "lat"; 
		Dimension latDim = builder.addDimension("y", 106);
		Dimension lonDim = builder.addDimension("x", 213);

		List<Dimension> latLonDims = new ArrayList<Dimension>();
		latLonDims.add(latDim);
		latLonDims.add(lonDim);		
		
		Variable.Builder varBuilderlat = builder.addVariable(nameLat, DataType.FLOAT, latLonDims);
		varBuilderlat.addAttribute(new Attribute("standard_name", "latitude"));
		varBuilderlat.addAttribute(new Attribute("long_name", "latitude"));
		varBuilderlat.addAttribute(new Attribute("units", "degrees_north"));
		varBuilderlat.addAttribute(new Attribute("axis", "Y"));

		
		// 3. Longitude
		String nameLon = "lon"; 
		Variable.Builder varBuilderlon = builder.addVariable(nameLon, DataType.FLOAT, latLonDims);
		varBuilderlon.addAttribute(new Attribute("standard_name", "longitude"));
		varBuilderlon.addAttribute(new Attribute("long_name", "longitude"));
		varBuilderlon.addAttribute(new Attribute("units", "degrees_east"));
		varBuilderlon.addAttribute(new Attribute("axis", "X"));
		
		
		// 4. Time
		String nameTime = "time"; 
		Dimension timeDim = builder.addDimension("time", 20088);
		List<Dimension> timeDims = new ArrayList<Dimension>();
		timeDims.add(timeDim);

		Variable.Builder varBuilderTime = builder.addVariable(nameTime, DataType.INT, timeDims);
//		varBuilderTime.addAttribute(new Attribute("standard_name", ""));
//		varBuilderTime.addAttribute(new Attribute("long_name", ""));
		varBuilderTime.addAttribute(new Attribute("units", "minutes since 1850-01-01"));
		varBuilderTime.addAttribute(new Attribute("calendar", "standard"));
		

		// 5. MainVar1
		String nameMainVar1 = "tas"; 
		List<Dimension> mainVarDims = new ArrayList<Dimension>();
		// Keep order !
		mainVarDims.add(timeDim);
		mainVarDims.add(latDim);
		mainVarDims.add(lonDim);

		Variable.Builder varBuilderMainVar = builder.addVariable(nameMainVar1, DataType.FLOAT, mainVarDims);
//		varBuilderMainVar.addAttribute(new Attribute("standard_name", ""));
		varBuilderMainVar.addAttribute(new Attribute("long_name", "2 metre temperature"));
		varBuilderMainVar.addAttribute(new Attribute("units", "K"));
		varBuilderMainVar.addAttribute(new Attribute("code", "167"));
		varBuilderMainVar.addAttribute(new Attribute("table", "128"));
		varBuilderMainVar.addAttribute(new Attribute("CDI_grid_type", "gaussian"));
		varBuilderMainVar.addAttribute(new Attribute("CDI_grid_num_LPE", "320"));
		varBuilderMainVar.addAttribute(new Attribute("grid_mapping", "spatial_ref"));
		
		
		// 5. MainVar2 XXX ?
		
		// 10) Now that the metadata (Dimensions, Variables, and Attributes) is added to the builder, build the writer
		// At this point, the (empty) file will be written to disk, and the metadata is fixed and cannot be changed or
		// added.
		try (NetcdfFormatWriter writer = builder.build()) {
		  // write data
		} catch (IOException e) {
//		  logger.log(yourCreateNetcdfFileErrorMsgTxt);
		}
	}

}
