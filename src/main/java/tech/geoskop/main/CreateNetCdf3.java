package tech.geoskop.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ucar.nc2.Attribute;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.write.NetcdfFormatWriter;

// XXX plan mínimo
// Deseable: ... 
public class CreateNetCdf3 {

	public static void main(String[] args)
			throws InvalidRangeException, StreamReadException, DatabindException, IOException {

		String pathToJson = args[0];
		// ./src/main/resources/vars.json"
		
		ObjectMapper mapper = new ObjectMapper();
		NetcdfCreationTemplate netcdfTemplate = mapper.readValue(new File(pathToJson),
				NetcdfCreationTemplate.class);

		NetcdfFormatWriter.Builder builder = NetcdfFormatWriter.createNewNetcdf3(
				netcdfTemplate.getFilePathName().getDir() + netcdfTemplate.getFilePathName().getName());

		// The file metadata is copied
		for (tech.geoskop.main.Attribute attribute : netcdfTemplate.getFileMetadata()) {
			builder.addAttribute(new Attribute(attribute.getName(), attribute.getValue()));
		}

		List<tech.geoskop.main.Variable> variables = netcdfTemplate.getVariables();
		Map<String, Dimension> mapDims = new HashMap<>();

		// The variables are copied.
		for (tech.geoskop.main.Variable variable : variables) {
			List<Dimension> dimensions = new ArrayList<Dimension>();

			for (Dim dim : variable.getDimensions()) {
				Dimension dimension = null;
				if (mapDims.containsKey(dim.getName())) {
					dimension = mapDims.get(dim.getName());
				} else {
					dimension = builder.addDimension(dim.getName(), dim.getSize());
					mapDims.put(dim.getName(), dimension);
				}

				dimensions.add(dimension);
			}

			Variable.Builder varBuilder = builder.addVariable(variable.getName(), variable.getType(), dimensions);

			for (tech.geoskop.main.Attribute attribute : variable.getAttributes()) {
				varBuilder.addAttribute(new Attribute(attribute.getName(), attribute.getValue()));
			}
		}

		// 5. MainVar2 XXX ?

		// 10) Now that the metadata (Dimensions, Variables, and Attributes) is added to
		// the builder, build the writer
		// At this point, the (empty) file will be written to disk, and the metadata is
		// fixed and cannot be changed or
		// added.
		try (NetcdfFormatWriter writer = builder.build()) {
			// write data
		} catch (IOException e) {
//		  logger.log(yourCreateNetcdfFileErrorMsgTxt);
		}
	}

}
