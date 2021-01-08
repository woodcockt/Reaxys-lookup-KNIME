package com.elsevier.reaxys.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

import javax.xml.bind.DatatypeConverter;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;
import org.w3c.dom.Document;

import com.elsevier.reaxys.NodeModelUtilities;
import com.elsevier.reaxys.ReaxysDataTypes;
import com.elsevier.reaxys.ReaxysFieldTypes;
import com.elsevier.reaxys.memoryBackedList.MemoryBackedList;
import com.elsevier.reaxys.xml.ReaxysDocument.ReaxysAuthentication;
import com.elsevier.reaxys.xml.ReaxysDocument.ReaxysDocument;
import com.elsevier.reaxys.xml.ReaxysDocument.RetrieveResults;



/**
 * Class actually executes the Reaxys query and loops through result retrieval
 * process
 * 
 * @author CLARKM
 * @author TOMW
 * @date  5 Sept 2014
 * @revised Jan 2021
 * 
 */

public class ReaxysAPI {

	ReaxysDocument reaxysDocumentFactory = null;;
	ExecutionContext exec = null;
	int resultCount = 0;
	String resultSet = "";
	String resultStatus = "";
	
	/**
	 * return total number of results, even if only a subset are being returned.
	 * 
	 * @return number of results from query.
	 */
	public int size() {
		return resultCount;
	}
	
	/**
	 * return result set name.
	 * 
	 * @return name of result set from query.
	 */
	public String resName() {
		return resultSet;
	}
	
	/*
	 * KNIME specific logger
	 */
	private static NodeLogger logger = null;
	
	/*
	 * list of fields that have structures, these are treated slightly differently.
	 */
	private static final List<String> structureFields = Arrays.asList(new String[]{"YY", "RX", "RY"});
	
	/**
	 * API constructor
	 * 
	 * @param context   KNIME context
	 * @param reaxysAuthentication   authentication token object
	 * @param debug      flag for debug output
	 */
	public ReaxysAPI(final ExecutionContext context,
			final ReaxysAuthentication reaxysAuthentication, boolean debug) {
		
		reaxysDocumentFactory = new ReaxysDocument(reaxysAuthentication);
		reaxysDocumentFactory.setDebug(debug);
		exec = context;
		
		try {
		logger = NodeLogger.getLogger(ReaxysAPI.class);
		} catch (Throwable er) {
			
		}

	}
	

	/**
	 * copy constructor
	 * @param old
	 */
	public ReaxysAPI(final ReaxysAPI old) {
		this.reaxysDocumentFactory = old.reaxysDocumentFactory;
		this.exec = old.exec;
	}
	
	
	/**
	 * return the set of keys for this data set; the total of all columns
	 * 
	 * @param maps arraylist of hashmaps
	 * @return set of unique hashmap keys
	 */
	public static Set<String> keySet(List<HashMap<String, String>> maps) {

		final HashSet<String> result = new HashSet<String>();

		for (final HashMap<String, String> map : maps) {
			result.addAll(map.keySet());
		}

		return result;
	}

	/**
	 * 
	 * @param idField
	 *            - if query isn't used, is the name of the field that is used
	 *            to match incoming data.
	 * @param id
	 *            string - query value. e.g. idField = "IDE.CN", id = "benzene"
	 * 
	 * @param query
	 *            query string like "SOL.SOL > 0 AND IDE.RXN > 109291". This is
	 *            appended to the idfield/id query
	 * 
	 * @param dataType type of data requested
	 * 
	 * @return array list of hashmaps with data
	 * @throws CanceledExecutionException 
	 * 
	 * @throws Exception
	 */
	public List<HashMap<String, String>> getFact(final String idField,
			final String id, final String query, ReaxysDataTypes dataType, int startResult, int maxResults, 
			boolean useSampling, boolean addStructures, boolean sd_v3, String sortString) throws Exception  {
		
			int max = maxResults;
			if (maxResults < 1) {
				max = Integer.MAX_VALUE;
			}
			final int min = startResult < 1 ? 1 : startResult;
		
			List<HashMap<String, String>> result;
			
			if (useSampling) {
				result =  getFactSampling(idField, id, query, dataType, min, max, sd_v3, sortString);
			} else {
				result =  getFactComplete(idField, id, query, dataType, min, max, sd_v3, sortString);
			}
			
			if (addStructures) {
				result = addStructures(result, sd_v3);
			}
			
			return result;
	}


	/**
	 * do initial search in Reaxy to get the result set
	 * 
	 * @param idField   Reaxys field to create the query, e.g. IDE.XRN
	 * @param id		identifier to use with the field.
	 * @param query 	extra query
	 * @param dataType	type of data requested
	 * @param maxResults maximum results to return
	 * @return RetrieveResults object
	 * @throws InterruptedException 
	 */
	final RetrieveResults initialSearch(final String idField,
			final String id, final String query, final ReaxysDataTypes dataType, int maxResults, boolean sd_v3, String sortString) throws InterruptedException {


		final Document searchDoc = reaxysDocumentFactory.createDocument("search");
		String queryString = "";

		if (idField != null && !idField.equals("") && !id.equals("")) {
			// assume the id is the value, and idField is the field to use the value on
			// e.g. IDE.XRN is the field, and id is '39192909'.
			queryString = idField + "='" + id + "'";
		} else if (query != null 
				&& (query.toLowerCase().startsWith("struc") || query.toLowerCase().startsWith("reac"))) {
		   // The string of the column is an SD file, so do nothing
		} else {
			// if the idfield is not identified, use the entire string of column as the query, except for structure and reaction queries
			queryString = id;
		}

		if (query != null && !query.equals("")) {
			if (queryString.length() > 0) {
				queryString += " AND ";
			}
			queryString += query;
			//retrieveOption = ",HITONLY"; // this causes weird highlighting
			// tags to appear in the data which are difficult to parse.
		}

		reaxysDocumentFactory.setTextNode(searchDoc, "where_clause", queryString);
		reaxysDocumentFactory.setAttribute(searchDoc, "from_clause", "dbname", dataType.getDatabase());
		reaxysDocumentFactory.setAttribute(searchDoc, "from_clause", "context", dataType.getContext());
		
		// special case: if looking for bioactivities, sort by PAUREUS descending
		//the "order_by_clause must be added in ReaxysDocument
		//

		/*
		 * allow users to specify a string, check that it ends with a correct sort order before using.
		 * if the sort field is incorrect or is not in the data returned then Reaxys will throw an
		 * error and not return anything.
		 */
		if (sortString != null && (sortString.trim().endsWith(" DESC") || sortString.trim().endsWith(" ASC"))) {
			
			reaxysDocumentFactory.setTextNode(searchDoc, "order_by_clause", sortString);
			
		} else if (dataType.getContext().equals("DPI")) {
			reaxysDocumentFactory.setTextNode(searchDoc, "order_by_clause", "DAT.PAUREUS DESC");
		} else if (dataType.getCode().equals("CIT")) {
			reaxysDocumentFactory.setTextNode(searchDoc, "order_by_clause", "CNR.CUPD DESC");
		} else if (dataType.getCode().equals("IDE")) {
			reaxysDocumentFactory.setTextNode(searchDoc, "order_by_clause", "");
		} /* else {
			final String sort = dataType.getCode() + ".UPD DESC";
			reaxysDocumentFactory.setTextNode(searchDoc, "order_by_clause", sort);
		}  */
		
		
		reaxysDocumentFactory.setTextNode(searchDoc, "options", "WORKER,NO_CORESULT");

		Document tempResult = reaxysDocumentFactory.request(searchDoc);
		
		RetrieveResults tempRetrieveResults = new RetrieveResults(reaxysDocumentFactory, tempResult, sd_v3);
		resultStatus = tempRetrieveResults.resStatus();
		resultCount = tempRetrieveResults.size();
		resultSet = tempRetrieveResults.resName();
		// If resultStatus is 'running' then wait 30 seconds and try again. repeat up to 60x (30 mins)
		if(resultStatus.equals("running")) {
			reaxysDocumentFactory.setAttribute(searchDoc, "from_clause", "resultname", resultSet);
			reaxysDocumentFactory.setAttribute(searchDoc, "from_clause", "first_item", "1");
			reaxysDocumentFactory.setAttribute(searchDoc, "from_clause", "last_item", "1");
			reaxysDocumentFactory.removeNode(searchDoc, "where_clause");
			reaxysDocumentFactory.createElement(searchDoc, "request", "select_list");
			reaxysDocumentFactory.createElement(searchDoc, "select_list", "select_item");
			reaxysDocumentFactory.setTextNode(searchDoc, "select_item", "RX.ID");
			int i = 1;
			while(i <= 60) {
				if (logger != null) logger.info("Still running, waiting 30 sec (repeat: " + i + ")");
				Thread.sleep(30000);
				tempResult = reaxysDocumentFactory.request(searchDoc);
				tempRetrieveResults = new RetrieveResults(reaxysDocumentFactory, tempResult, sd_v3);
				resultStatus = tempRetrieveResults.resStatus();
				resultCount = tempRetrieveResults.size();
				resultSet = tempRetrieveResults.resName();
				// if not running, then we can move on
				if(!resultStatus.equals("running")) {
					if (logger != null) logger.info("Results are ready");
					break;
				}
				i++;
			}
		}
		
		final RetrieveResults retrieveResults = tempRetrieveResults;
		
		return retrieveResults;
	}
	
	
	/**
	 * 
	 * @param idField
	 *            - if query isn't used, is the name of the field that is used
	 *            to match incoming data.
	 * @param id
	 *            string - query value. e.g. idField = "IDE.CN", id = "benzene"
	 * 
	 * @param query
	 *            query string like "SOL.SOL > 0 AND IDE.RXN > 109291". This is
	 *            appended to the idfield/id query
	 * 
	 * @param dataType type of data requested
	 * 
	 * @return array list of hashmaps with data
	 * @throws CanceledExecutionException 
	 * 
	 * @throws Exception
	 */
	final public List<HashMap<String, String>> getFactComplete(final String idField,
			final String id, final String query, final ReaxysDataTypes dataType, int minResult, int maxResults, 
			boolean sd_v3, String sortString) throws Exception  {
		
		/*
		 * storage for results
		 */
		final MemoryBackedList<HashMap<String, String>> results = new MemoryBackedList<HashMap<String, String>>();
		/*
		 * initial search
		 */
		final RetrieveResults retrieveResults = initialSearch(idField, id, query, dataType, maxResults, sd_v3, sortString);
	
		
		final int numResults = Math.min(maxResults, resultCount);
		final String fact = dataType.getCode();

		if (logger != null) logger.info("search resulted in " + numResults + " hits");
		if (logger != null) logger.info("search set:  " + resultSet);
		if (logger != null) logger.info("search status:  " + resultStatus);
		final String msg = String.format("search resulted in %d hits", numResults);
		if (exec != null) exec.getProgressMonitor().setMessage(msg);
		/*
		 * if 0 results then return immediately with empty list
		 */
		if (numResults == 0)  {
			return results;
		}

		final int MAX_START = 10000; // fail safe, maximum chunks
		final int RESULT_MAX = 50; // changed from 1000; smaller numbers are faster
		final int RESULT_INCREMENT = Math.min(RESULT_MAX, numResults); // chunk size for molecules
		final int RESULT_CHUNK = 100; // chunk size for facts
		int resultsReturned = 0;
		
		// loop over results in chunks of about 1000
		for (int i = minResult; i <= numResults + minResult -1; i += RESULT_INCREMENT) {

			boolean done = false;
			int start = 1;

			/* for each chunk of molecules/reactions, get chunks of facts in
			 groups of RESULT_CHUNK. we don't know how many we will get so we loop until no more are
			 returned. 
			 */
			while (!done && start < MAX_START) {

				final int end = start + RESULT_CHUNK - 1; // starts at 1, not zero
				String queryRange =fact + "(" + start + "," + end + ")";
				
				// special case; there is only one  structure per compound , so we are done here
				// after one
				if (structureFields.contains(fact)) {
					queryRange = fact;
					done = true;
				}
								
				final String msg1 = String.format("fetching facts %d to %d for rows %d to %d of %d",
						start, end, i, Math.min(numResults, (i+RESULT_INCREMENT - 1)), numResults);
				
				if (exec != null) exec.getProgressMonitor().setProgress(msg1);

				final Document request = retrieveResults.retrieveValues(queryRange, i, 
						i + RESULT_INCREMENT - 1);
				
				// increment start value by RESULT_CHUNK
				start += RESULT_CHUNK;
				
				final Document resultDocument = reaxysDocumentFactory.request(request);

				if (resultDocument == null) {  // fail-safe; this is an error
					continue;
				}

				// result from this loop.  For streaming output one could do something with this instead
				// of accumulating data in memory.
				final List<HashMap<String, String>> resultMap = retrieveResults
						.getResults(resultDocument);
				
				fixSDF(resultMap);
				resultsReturned = resultMap.size();

				if (resultsReturned == 0 ) {
					done = true;
					break;
				} else {
					results.addAll(resultMap);
					if (resultsReturned < RESULT_CHUNK) {
						done = true;
					}
				}
				
				if (exec != null) {
					try { exec.checkCanceled(); } catch (CanceledExecutionException cee) {
						results.close();
						throw new CanceledExecutionException();
					}
				}
			} // end of inner loop for facts
			
			if (exec != null) {
				try { exec.checkCanceled(); } catch (CanceledExecutionException cee){
					results.close();
					throw new CanceledExecutionException();
				}
			}
			
			if (results.size() == 0) {
				done = true;
				break;
			}
		}

		if(logger != null) logger.debug("total results returned: " + results.size());
		
		return results;
	}
	
	
	

	
	
	/**
	 * Retrieve a random sampling of the results.  That is if N results are requested, randomly choose N from the
	 * total result set.
	 * 
	 * 
	 * @param idField  - if query isn't used, is the name of the field that is used
	 *                   to match incoming data.
	 * @param id string - query value. e.g. idField = "IDE.CN", id = "benzene"
	 * 
	 * @param query  query string like "SOL.SOL > 0 AND IDE.RXN > 109291". This is
	 *               appended to the idfield/id query
	 * 
	 * @param fact  name of the specific fact to retrieve, e.g. "MP"
	 * @param type  class of the fact, eg IDE or PHARM
	 * 
	 * @param dbname name of datbase RX -reaxys; PH1 - pharmapendium, RMC
	 * @context one of R, S, or other data contexts.
	 * 
	 * @return array list of hashmaps with data
	 * @throws CanceledExecutionException 
	 * 
	 * @throws Exception
	 */
	final public List<HashMap<String, String>> getFactSampling(final String idField,
			final String id, final String query, final ReaxysDataTypes dataType, 
			int minResult, int maxResults, boolean sd_v3, String sortString) throws Exception  {
		
		if (exec != null) exec.getProgressMonitor().setMessage("executing search...");

		/*
		 * storage for results
		 */
		final MemoryBackedList<HashMap<String, String>> results = new MemoryBackedList<HashMap<String, String>>();
		/*
		 * initial search
		 */
		final RetrieveResults retrieveResults = initialSearch(idField, id, query, dataType, maxResults, sd_v3, sortString);
		final int numResults = Math.min(maxResults, resultCount);
		final String fact = dataType.getCode();
		
		if(logger != null) logger.info("search resulted in " + numResults + " hits");
		final String msg = String.format("search resulted in %d hits", numResults);
		if (exec != null) exec.getProgressMonitor().setMessage(msg);

		/*
		 * if 0 results then return immediately with empty list
		 */
		if (numResults == 0)  {
			return results;
		}
		
		final int MAX_START = 10000; // fail safe, maximum chunks
		final int RESULT_CHUNK = 1; // chunk size for facts
		final int SAMPLE_SIZE = 1;
		// source of randomness.
		final Random rand = new Random();
		
		// create a non-duplicated array of items
		final HashSet<Integer> sampleList = new HashSet<Integer>();
		
		while (sampleList.size() < Math.ceil((float)numResults/SAMPLE_SIZE)) {
			sampleList.add((int) ((Math.floor(rand.nextInt(resultCount)/SAMPLE_SIZE) * SAMPLE_SIZE) + minResult ) + 1);
		}
		
		// create a sorted list
		final Integer[] listArray = sampleList.toArray(new Integer[sampleList.size()]);
		Arrays.sort(listArray);
		
		int counter = 0;
		int resultsReturned = 0;
		
		// loop over random samples
		for (Integer i : listArray) {
				
			boolean done = false;
			int start = 1;

			/* for each chunk of molecules/reactions, get chunks of facts in
			 groups of RESULT_CHUNK. we don't know how many we will get so we loop until no more are
			 returned. 
			 */
			while (!done && start < MAX_START) {

				final int end = start + SAMPLE_SIZE - 1; // starts at 1, not zero
				
				String queryRange = fact + "(" + start + "," + end + ")";
				
				// special case; there is only one  structure per compound , so we are done here
				// after one
				if (structureFields.contains(fact)) {
					queryRange = fact;
					done = true;
				}
								
				final String msg1 = String.format("fetching row %d of %d",
						counter, numResults);
				counter++;
				if (exec != null) exec.getProgressMonitor().setProgress(msg1);

				final Document request = retrieveResults.retrieveValues(queryRange, i, 
						i + SAMPLE_SIZE - 1);
				
				final Document resultDocument = reaxysDocumentFactory.request(request);
				
				if (resultDocument == null) {  // fail-safe; this is an error
					continue;
				}

				// result from this loop.  For streaming output one could do something with this instead
				// of accumulating data in memory.
				final List<HashMap<String, String>> resultMap = retrieveResults
						.getResults(resultDocument);
				
				fixSDF(resultMap);
				resultsReturned = resultMap.size();

				if (resultsReturned == 0 ) {
					break;
				} else {
					results.addAll(resultMap);
					if (resultsReturned <= RESULT_CHUNK || results.size() > maxResults) {
						done = true;
					}
				}
				
				if (exec != null) {
					try { exec.checkCanceled(); } catch (CanceledExecutionException cee){
						results.close();
						throw new CanceledExecutionException();
					}
				}
			}
			
			if (exec != null) {
				try { exec.checkCanceled(); } catch (CanceledExecutionException cee){
					results.close();
					throw new CanceledExecutionException();
				}
			}
			
			if (results.size() == 0 || results.size() > maxResults) break;
		}

		if (logger != null) logger.debug("total results returned: " + results.size());
		
		// trim excess results
		while(results.size() > maxResults) {
			results.get(results.size() - 1).clear();
			results.remove(results.size()-1);
		}
		return results;
	}

	/**
     * add the corresponding structures to a data set. Creates a list of identifiers,
     * searches them, and then matches them to the data. Similar to a left-outer-join.
     * 
     * this performs a series of "batch" xrn searches. The batches are created to avoid
     * creating one very long query that can crash the server.
     * 
     * @param results   data results of normal serach to be augmented.
     * @param v3   true if v3000 structures should be retrieved
     * 
     * @throws Exception
     */
	List<HashMap<String, String>> addStructures(List<HashMap<String, String>> results, 
            final boolean v3) throws Exception {

	    // Check if we have a reaction, otherwise default to compound
	    ReaxysDataTypes dataType = ReaxysDataTypes.YY;
	    
	    // Reaction id shows we have a reaction
	    final String rxidLabel = ReaxysFieldTypes.getLabel("RX.ID");

	    boolean haveRxid = false;
	    for (HashMap<String, String> result : results) {
	        final String rxid = result.get(rxidLabel);
	        if (rxid != null) {
	            haveRxid = true;
	            break; // have at least one reaction id
	        }
	    }
	    
	    if (haveRxid) {
	        dataType = ReaxysDataTypes.RY;
	    }
	    
	    return addStructures(results, v3, dataType);
	    
	            
	}
	
	
	/**
	 * add the corresponding structures to a data set. Creates a list of identifiers,
	 * searches them, and then matches them to the data. Similar to a left-outer-join.
	 * 
	 * this performs a series of "batch" xrn searches. The batches are created to avoid
	 * creating one very long query that can crash the server.
	 * 
	 * @param results   data results of normal serach to be augmented.
	 * @param v3   true if v3000 structures should be retrieved
	 * @param dataType molecule or reaction
	 * 
	 * @throws Exception
	 */
	private List<HashMap<String, String>> addStructures(List<HashMap<String, String>> results, 
			final boolean v3, ReaxysDataTypes dataType) throws Exception {
		
		if (exec != null) exec.getProgressMonitor().setMessage("getting list of structures to add");
		final String slabel   = ReaxysFieldTypes.getLabel("YY.STR");
		final String rylabel = ReaxysFieldTypes.getLabel("RY.STR");
		
		// either of these could be used for structure mapping
		final String xrnLabel = ReaxysFieldTypes.getLabel("IDE.XRN");
		final String mrnLabel = ReaxysFieldTypes.getLabel("DAT.MRN");
		// And this one for the Reaction ID
		final String rxidLabel = ReaxysFieldTypes.getLabel("RX.ID");
		
		MemoryBackedList<HashMap<String, String>> structureData = new MemoryBackedList<HashMap<String, String>>();
		
		/*
		 * max XRN count for query this is the size limit, in units of
		 * XRN numbers.  each one is about 10 digits
		 */
		final int maxQueryLength = 256; 
		
		// a unique set
		final HashSet<String> uniqueIDSet = new HashSet<String>();
		
		if (dataType == ReaxysDataTypes.RY) { 
		    for (HashMap<String, String> result : results) {
		        final String rxid = result.get(rxidLabel);
		        if (rxid != null) {
		            uniqueIDSet.add(rxid);
		        }
		    }
		} else {
		    for (HashMap<String, String> result : results) {
		        final String mrn = result.get(mrnLabel);

		        if (mrn != null) {
		            uniqueIDSet.add(mrn);
		        } else {
		            final String xrn = result.get(xrnLabel);
		            if (xrn != null) {
		                uniqueIDSet.add(xrn);
		            }
		        }
		    }
		}

		// array so that we can access by index
		final String[] uniqueIDArray = uniqueIDSet.toArray(new String[uniqueIDSet.size()]);
		uniqueIDSet.clear(); // reclaim immediately in case memory is an issue;
		
		String idQueryStart = "IDE.XRN=";
		if (dataType == ReaxysDataTypes.RY) {
		    idQueryStart = "RX.ID=";
		}
		
		int counter = 0;
		while (counter < uniqueIDArray.length ) {
			
			String ideQuery= idQueryStart;
			// break the query into smaller sections in case a very large string causes memory
			// problems
			int count = 0;// internal loop counter
			for (; counter <uniqueIDArray.length; counter++ ) {
				count++;
				ideQuery += uniqueIDArray[counter] + ";";
				if (count > maxQueryLength) {
					counter++; // break will bypass increment
					break;
				}
			}

			ideQuery = ideQuery.substring(0, ideQuery.length() -1);
			
			// To make it transparent that we use the dataType from the method argument, create dummy variable
			ReaxysDataTypes queryDataType = dataType;
			
			final List<HashMap<String, String>> temp =  new ReaxysAPI(this)
				.getFactComplete("", "", ideQuery, queryDataType, 1, Integer.MAX_VALUE, v3, null);
			
			if (temp.size() > 0) {
				structureData.addAll(temp);
			}
			((MemoryBackedList<HashMap<String, String>>)temp).close();
		}
		
		if (exec != null) exec.getProgressMonitor().setMessage("joining structures to data");
		
		final List<HashMap<String, String>> updatedResults = new MemoryBackedList<HashMap<String, String>>();
		
		// a double loop over these files is slow, so index 
		final HashMap<String, Integer> structureIndex = new HashMap<String, Integer>();
		
		if (dataType == ReaxysDataTypes.RY) {
		    for (int i = 0; i < structureData.size(); i++) {
		        final HashMap<String, String> v = structureData.get(i);
		        final String rx = v.get(rxidLabel);
		        final String mol = v.get(rylabel);
		        if (rx != null) {
		            structureIndex.put(rx, i);
		        } else if (mol != null) {
		            structureIndex.put(mol, i);
		        }
		    }
		    // end of indexing

		    // add the structures to the results map.  This is an inner join.
		    for (final HashMap<String, String> result : results) {

		        final String rxid = result.get(rxidLabel);

		        if (rxid != null) {
		            final Integer index = structureIndex.get(rxid);

		            if (index != null) {
		                final HashMap<String, String> struct = structureData.get(index);

		                if (struct != null) {

		                    final String rx = struct.get(rxidLabel);
		                    final String mol = struct.get(rylabel);

		                    if (rx != null & mol != null && rx.equals(rxid)) {
		                        result.putAll(struct);
		                    }
		                }
		            }
		        }

		        updatedResults.add(result);

		    }
		} else { // We have compound structures
            for (int i = 0; i < structureData.size(); i++) {
                final HashMap<String, String> v = structureData.get(i);
                final String xrn = v.get(xrnLabel);
                final String mol = v.get(slabel);
                if (xrn != null) {
                    structureIndex.put(xrn, i);
                } else if (mol != null) {
                    structureIndex.put(mol, i);
                }
            }
            // end of indexing

            // add the structures to the results map.  This is an inner join.
            for (final HashMap<String, String> result : results) {

                String mrn = result.get(mrnLabel);
                final String rxrn = result.get(xrnLabel);

                if (mrn == null && rxrn != null) {
                    mrn = rxrn;
                }

                if (mrn != null) {
                    final Integer index = structureIndex.get(mrn);

                    if (index != null) {
                        final HashMap<String, String> struct = structureData.get(index);

                        if (struct != null) {

                            final String xrn = struct.get(xrnLabel);
                            final String mol = struct.get(slabel);

                            if (xrn != null & mol != null && xrn.equals(mrn)) {
                                result.putAll(struct);
                            }
                        }
                    }
                }

                updatedResults.add(result);

            }

		}

		results.clear();
		structureData.close(); // probably not necessary, but shouldn't hurt memory.
		if (exec != null) exec.getProgressMonitor().setMessage("completed joining structures to data");
		return updatedResults;
	}
	
	/**
	 * adjust SDFiles v2, v3 and RD files that are compressed
	 * 
	 * @param data
	 */
	void fixSDF(List<HashMap<String, String>> data ) {
		
		/*
		 * fix sdfile data, if present
		 */
		// one could use InChi or other identifiers as well to identify the molecule
		final String idTag = "Reaxys Registry Number (IDE.XRN)";
		final String rdTag = "Reaction ID (RX.ID)";
	
			// fix YY.STR to be sdfile by putting on header and end.
		for (HashMap<String, String> rowData : data) {
			
			for (final String key : rowData.keySet()) {
				
				if (NodeModelUtilities.isStructureTag(key)) {
					final String sdfileTag = key;
					String sdfile  = rowData.get(sdfileTag);
					final String sdId = rowData.get(idTag);
					
					if (sdfile.startsWith("\n") && sdId != null) {
						sdfile = sdfile.replaceFirst("\n", sdId + "\n");
					}
					rowData.put(sdfileTag, sdfile);
				}

				// fix RY.RXNFILE OR RY.STR by uncompressing
				if (NodeModelUtilities.isReactionTag(key)) {
					String rdfile  = rowData.get(key);
					rdfile = decompress(rdfile);
					final String sdId = rowData.get(rdTag);
					// (?s) allows matching with newlines in the string.
 					if (rdfile.matches("(?s)^[$]RXN[^\n]*\n\n.*") && sdId != null) {
						rdfile = rdfile.replaceFirst("\n", "\n" + sdId);
					}

					rowData.put(key, rdfile);
				}
			}
		}
	}
	
	/**
	 * decompress a compressed string.  Particularly Rxn files that are delivered as compressed strings
	 * 
	 * @param compressed string with compressed data
	 * @return inflated string, or original string if it was not compressed
	 */
	String decompress(final String compressed) {

		try {
			final byte[] un64 = DatatypeConverter.parseBase64Binary(compressed);
			final ByteArrayOutputStream inflatedStream = new ByteArrayOutputStream();
			final Inflater decompresser = new Inflater(false);
			final InflaterOutputStream inflaterOutputStream = new InflaterOutputStream(inflatedStream, decompresser);
			inflaterOutputStream.write(un64);
			inflaterOutputStream.close();
			decompresser.end();
			return new String(inflatedStream.toByteArray(), "UTF-8");
			
		} catch(IOException e) {
			if (logger != null) logger.info("decompression error: " + e);
		}
		// maybe this failed because it wan't really compressed, so return original string
		return compressed;
	}
	
}
