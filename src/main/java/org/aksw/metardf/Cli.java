package org.aksw.metardf;

import java.util.List;
import java.util.Random;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.jena.base.Sys;
import org.apache.jena.ext.com.google.common.base.Utf8;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.StreamOps;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.json.*;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * 
 * Need to be a soloinstance for
 * 
 * @author marvin
 *
 */
public class Cli {

	public static void main(String[] args) {

		if (args.length != 2) {
			printUsage();
			System.exit(0);
		}
		
		String input_path = args[0];
		String output_path = args[1];
		
	
	
		
		JSONObject object = null;
		try {
			
			OutputStream output = new FileOutputStream(new File(output_path+".nt"));
			StreamRDF writer = StreamRDFWriter.getWriterStream(output , RDFFormat.NTRIPLES_UTF8) ;
			
			Writer jsonwriter = new OutputStreamWriter(new FileOutputStream(output_path), StandardCharsets.UTF_8);
			BufferedWriter jsonout = new BufferedWriter(jsonwriter);
			
			// Reader reader = new InputStreamReader(new
			// FileInputStream(args[0]), "UTF-8");
			// BufferedReader fin = new BufferedReader(reader);


			BufferedReader br = Files.newBufferedReader(Paths.get(input_path), StandardCharsets.UTF_8);
			String line;

			while ((line = br.readLine()) != null) {
				object = new JSONObject(line);
				Converter conv = new Converter(object);
				
				Model rdfdata = conv.metardf();
				JSONObject jsondata = conv.join();
				
				jsonout.write(jsondata.toString());
				jsonout.newLine();
				
				StreamOps.graphToStream(rdfdata.getGraph(), writer) ;
			}

			// System.out.print(jsondata);

			jsonout.close();
			
		} catch (Exception e) {
		}

		// writer.write(res.toString());
		// writer.flush();

	}

	public static void printUsage() {
		System.out.println("args[0] = path/to/input/file");
		System.out.println("args[1] = path/to/output/json/result/file");
		System.out.println("args[2] = path/to/output/mids/rdf/file");
	}
}
