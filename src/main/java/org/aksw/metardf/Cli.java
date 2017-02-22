package org.aksw.metardf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.StreamOps;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.json.*;

/**
 * 
 * Need to be a soloinstance for
 * 
 * @author marvin
 *
 */
public class Cli {

	public static void main(String[] args) {

		//Args stuff
		if (args.length != 2) {
			printUsage();
			System.exit(0);
		}
		String input_path = args[0];
		String output_path = args[1];
		
		
		JSONObject object = null;
		try {
			
			//Output RDF
			OutputStream rdfoutput = new FileOutputStream(new File(output_path+".nt"));
			StreamRDF rdfwriter = StreamRDFWriter.getWriterStream(rdfoutput , RDFFormat.NTRIPLES_UTF8) ;
			
			//Output json
			Writer jsonoutput = new OutputStreamWriter(new FileOutputStream(output_path), StandardCharsets.UTF_8);
			//BufferedWriter jsonwriter = new BufferedWriter(jsonoutput);
		
			//Input json
			BufferedReader br = Files.newBufferedReader(Paths.get(input_path), StandardCharsets.UTF_8);
			String line;

			while ((line = br.readLine()) != null) {
				object = new JSONObject(line);
				Converter conv = new Converter(object);
				
				Model rdfdata = conv.metardf();
				JSONObject jsondata = conv.join();
				
				jsonoutput.write(jsondata.toString());
				
//				jsonwriter.write(jsondata.toString());
//				jsonwriter.newLine();
				
				StreamOps.graphToStream(rdfdata.getGraph(), rdfwriter) ;
			}

			jsonoutput.close();
			rdfoutput.close();
			br.close();
		} catch (IOException ioe) {
			System.out.println("Input/Ouput error");
			ioe.printStackTrace();
		}

		//Clean rdf
		
		cleanrdf(output_path+".nt", output_path, "N-TRIPLES");
		
		System.out.println("writed json to "+output_path);
		System.out.println("writed rdf to "+output_path+".nt");
		System.out.println("writed clean rdf to "+output_path+".clean.nt");
			
	}
	
	/**
	 * Cleans the RDF data file
	 * 
	 * @param input
	 * @param output
	 * @param out_format
	 */
	public static void cleanrdf(String input, String output, String out_format) {
		Model model = ModelFactory.createDefaultModel();
		model.read(input, "N-TRIPLES");
		
		try {
			OutputStream outstream;
			outstream = new FileOutputStream(new File(output+".clean"+".nt"));
			StreamRDF writer = StreamRDFWriter.getWriterStream(outstream , RDFFormat.NTRIPLES_UTF8) ;
			StreamOps.graphToStream(model.getGraph(), writer) ;
			
			outstream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception ee) {
			System.out.println("outsrteam close failed");
		}
		
	}
	
	/**
	 * Print Usage
	 */
	public static void printUsage() {
		System.out.println("Usage of metardf-converter");
		System.out.println("--------------------------");
		System.out.println("args[0] = path/to/input/file");
		System.out.println("args[1] = path/to/output/file");
	}
}
