package tech.geoskop.main;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;

import static java.lang.System.*;

public class Main {

	public static void main(String[] args) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		NetcdfCreationTemplate netcdf = mapper.readValue(new File("./src/main/resources/vars.json"),
				NetcdfCreationTemplate.class);
		out.println(netcdf);
	}
}