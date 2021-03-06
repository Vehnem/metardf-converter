package org.aksw.metardf;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.json.JSONArray;
import org.json.JSONObject;

public class Converter {

	private static JSONArray statementgroups = new JSONArray();
	private static JSONArray metadata = new JSONArray();
	private static Model model = ModelFactory.createDefaultModel();

	private static final String rdfType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	private static final String sameAs = "owl:sameAs";
	private static final String metatype = "kv-rdf-meta";

	public static List<String> mids = new ArrayList<String>();
	
	public Converter(JSONObject json) {
		transform(json);
	}

	/**
	 * 
	 * @param input
	 * @return
	 */
	public static void transform(JSONObject input) {

		String uri = input.getString("uri");

		for (Object o : input.keySet().toArray()) {

			Object json = input.get(o.toString());

			if (json instanceof JSONArray) {
				// It's an array
				JSONArray ja = (JSONArray) json;
				for (int i = 0; i < ja.length(); i++) {
					buildStatementGroup(uri, ja.getJSONObject(i), o.toString(), String.format("%02d", i));
				}
			} else if (json instanceof JSONObject) {
				// It's an object
				buildStatementGroup(uri, (JSONObject) json, o.toString(), "00");
				;
			} else {
				// System.out.println(json);
				// It's something else, like a string or number
			}

		}
	}

	/**
	 * 
	 * @param uri
	 * @param group
	 * @param propertyname
	 * @param propertynumber
	 */
	public static void buildStatementGroup(String uri, JSONObject group, String propertyname, String propertynumber) {

		// TODO Uri sdw.aksw/resources?
		String groupid = "<" + uri + "-" + propertyname + "-" + propertynumber + ">";

		if (propertyname.equals("a")) {
			groupid = "<" + uri + "-" + "rdfType" + "-" + propertynumber + ">";
		}

		JSONObject newgroup = new JSONObject();

		newgroup.put("groupid", groupid);

		String o = group.get(propertyname).toString();
		if (propertyname.equals("authorOf")) {
			o = group.getJSONObject("authorOf").get("label").toString();
		}

		newgroup.put("statements", new JSONArray().put(buildStatement(uri, propertyname, o)));

		JSONArray mids = new JSONArray();
		
		// wasGeneratedBy Array?
		Object generatedBy = group.get("wasGeneratedBy");
		if (generatedBy instanceof JSONArray) {
			// It's an array
			JSONArray ja = (JSONArray) generatedBy;
			for (int i = 0; i < ja.length(); i++) {
				mids.put(buildmeta(ja.getJSONObject(i)));
			}
		} else if (generatedBy instanceof JSONObject) {
			// It's an object
			mids.put(buildmeta((JSONObject) generatedBy));
		}

		newgroup.put("mids", mids);

		statementgroups.put(newgroup);
	}

	/**
	 * MetaObject
	 * 
	 * @return
	 */
	public static String buildmeta(JSONObject obj) {
		
		
		String metaHash = "<http://sdw.aksw.org/datasets/artists-dataset/mids/" + hashForMid(obj) + ">";
		
		if (!mids.contains(metaHash)) {

			mids.add(metaHash);

			JSONObject newmeta = new JSONObject();
			newmeta.put("groupid", metaHash);
			
			newmeta.put("grouptype", "strong");

			JSONArray mFacts = new JSONArray();

			// rdf:type
			JSONObject type = new JSONObject();
			type.put("type", metatype);
			type.put("key", "<" + rdfType + ">");
			type.put("value", "<http://sdw.aksw.org/datasets/artists-dataset/ontology/" + obj.get("a").toString() + ">");
			mFacts.put(type);

			// startedAtTime
			JSONObject startedAt = new JSONObject();
			startedAt.put("type", metatype);
			startedAt.put("key", "<http://sdw.aksw.org/datasets/artists-dataset/ontology/startedAtTime>");
			startedAt.put("value", "\"" + obj.get("startedAtTime").toString() + "\"");
			mFacts.put(startedAt);
			
			// confidence
			JSONObject confidence = new JSONObject();
			confidence.put("type", metatype);
			confidence.put("key", "<http://sdw.aksw.org/datasets/artists-dataset/ontology/confidence>");
			confidence.put("value",
					"\"" + (new Random().nextFloat()) + "\"" + "^^" + "<http://www.w3.org/2001/XMLSchema#decimal>");
			mFacts.put(confidence);
			
			// L2_used_as_source
			JSONArray l2 = obj.getJSONArray("L2_used_as_source");

			for (int i = 0; i < l2.length(); i++) {
				JSONObject used_as_source = new JSONObject();
				used_as_source.put("type", metatype);
				used_as_source.put("key", "<http://sdw.aksw.org/datasets/artists-dataset/ontology/L2_used_as_source>");
				used_as_source.put("value", "\"" + l2.getJSONObject(i).get("uri") + "\"");
				mFacts.put(used_as_source);
			}

			// L23_used_software_or_firmware
			JSONArray l23 = obj.getJSONArray("L23_used_software_or_firmware");

			JSONObject used_software_or_firmware = new JSONObject();
			used_software_or_firmware.put("type", metatype);
			used_software_or_firmware.put("key",
					"<http://sdw.aksw.org/datasets/artists-dataset/ontology/L23_used_software_or_firmware>");
			
			String value = buildSeperateRdf(l23);
			used_software_or_firmware.put("value",
						"<" + "http://sdw.aksw.org/datasets/artists-dataset/ids/" + value + ">");
			mFacts.put(used_software_or_firmware);

			newmeta.put("metadataFacts", mFacts);

			metadata.put(newmeta);

		}

		return metaHash;
	}

	/**
	 * L23 Hash
	 * 
	 * @param l23
	 * @return
	 */
	public static String hashForL23(JSONArray l23) {
		return String.valueOf(l23.toString().hashCode());
	}

	/**
	 * wasGeneratedBy Hash
	 * 
	 * @param obj
	 * @return
	 */
	public static String hashForMid(Object obj) {
		return toHashCode(obj.toString());
	}

	/**
	 * String -> MD5
	 * 
	 * @param input
	 * @return
	 */
	public static String toHashCode(String input) {
		String hashValue = "";
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(input.getBytes());
			byte[] db = md.digest();
			hashValue = new BigInteger(1, db).toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return hashValue;
	}

	public static String buildSeperateRdf(JSONArray l23) {
		String myhash = hashForL23(l23);

		
		
		String uri = "http://sdw.aksw.org/datasets/artists-dataset/ids/";
		String rootid = uri + myhash;
		
		//each JSONObject
		for (int k = 0; k < l23.length(); k++) { 
			JSONObject l23obj = l23.getJSONObject(k);
		
			
			String entryid = uri + myhash + "-entry-" + k;
			// Root->Entry
			RDFNode o_root = ResourceFactory.createResource(entryid);
			Resource s_root = ResourceFactory.createResource(rootid);
			Property p_root = ResourceFactory.createProperty(
					"http://sdw.aksw.org/datasets/artists-dataset/ontology/L13_used_parameters_Entry");
			model.add(ResourceFactory.createStatement(s_root, p_root, o_root));

			// EntryPos
			Resource s_entrypos = ResourceFactory.createResource(entryid);
			Property p_entrypos = ResourceFactory
					.createProperty("http://sdw.aksw.org/datasets/artists-dataset/ontology/position");
			RDFNode o_entrypos = ResourceFactory.createTypedLiteral(k);

			model.add(ResourceFactory.createStatement(s_entrypos, p_entrypos, o_entrypos));
			
			//Entry
			String[] str;
			str = JSONObject.getNames(l23obj);

			for (String st : str) {
				
				if(st.equalsIgnoreCase("a")) {
					Resource s_entry = ResourceFactory.createResource(entryid);
					Property p_entry = ResourceFactory.createProperty(
							rdfType);
					RDFNode o_entry = ResourceFactory.createStringLiteral(l23obj.getString("a"));
					model.add(ResourceFactory.createStatement(s_entry, p_entry, o_entry));
				}
				
				if(st.equalsIgnoreCase("comment")) {
					Resource s_entry = ResourceFactory.createResource(entryid);
					Property p_entry = ResourceFactory.createProperty(
							"http://www.w3.org/2000/01/rdf-schema#comment");
					RDFNode o_entry = ResourceFactory.createStringLiteral(l23obj.getString("comment"));
					model.add(ResourceFactory.createStatement(s_entry, p_entry, o_entry));
				}
				
				if (st.equals("L13_used_parameters")) {
					String[] l13params = l23obj.getString("L13_used_parameters").split(",");

					for (int i = 0; i < l13params.length; i++) {
						
						// Entry
						Resource s_entry = ResourceFactory.createResource(entryid);
						Property p_entry = ResourceFactory.createProperty(
								"http://sdw.aksw.org/datasets/artists-dataset/ontology/L13_used_parameters");
						RDFNode o_entry = ResourceFactory.createStringLiteral(l13params[i]);

						model.add(ResourceFactory.createStatement(s_entry, p_entry, o_entry));
						
					}
				}
			}
		}
		// model.write(System.out, "N_TRIPLES");

		return myhash;
	}

	/**
	 * statementgroups.statment
	 * 
	 * @param uri
	 * @param propertyname
	 * @param o
	 * @return
	 */
	public static JSONObject buildStatement(String uri, String propertyname, String o) {
		JSONObject statement = new JSONObject();
		statement.put("type", "triple");
		statement.put("sid", "");

		String s = uri;
		String p = "http://sdw.aksw.org/datasets/artists-dataset/ontology/" + propertyname;
		if (propertyname.equals("a")) {
			p = rdfType;
		}
		if (propertyname.equals("sameAs")) {
			p = sameAs;
		}
		String tuple = "<" + s + ">" + " " + "<" + p + ">" + " " + "\"" + o + "\"" + " .";

		statement.put("tuple", tuple);
		return statement;
	}

	/**
	 * Getter
	 * 
	 * @return
	 */
	public Model metardf() {

		return model;
	}

	/**
	 * Join the lists
	 * 
	 * @return
	 */
	public JSONObject join() {

		JSONObject result = new JSONObject();

		result.put("metadata", metadata);
		result.put("statementgroups", statementgroups);

		return result;
	}

}
