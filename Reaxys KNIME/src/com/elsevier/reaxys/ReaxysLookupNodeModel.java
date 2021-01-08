package com.elsevier.reaxys;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.chem.types.RxnCell;
import org.knime.chem.types.SdfCell;

import com.elsevier.reaxys.xml.ReaxysAPI;
import com.elsevier.reaxys.xml.ReaxysDocument.ReaxysDocument;


/**
 * This is the model implementation of ReaxysLookup. Lookup data from Reaxys
 * 
 * @author Matthew Clark
 * @version 1.0
 * @date July 20 2014
 */
public class ReaxysLookupNodeModel extends NodeModelUtilities{

	/**
	 * Constructor for the node model.
	 */
	protected ReaxysLookupNodeModel() {
		super(1, 1);
		

		try {
			new PropertyReader();
		} catch (Exception e) {
			logger.info("optional config file not present");
		}

		ReaxysLookupNodeModel.DEFAULT_USERNAME = System.getProperty(REAXYS_USERNAME);
		ReaxysLookupNodeModel.DEFAULT_PASSWORD = System.getProperty(REAXYS_PASSWORD);
		ReaxysLookupNodeModel.DEFAULT_APIKEY =   System.getProperty(REAXYS_APIKEY);
		
	}

	/**
	 * create the initial output table based on the column set returned in the hashmap.
	 * 
	 * @param data
	 *            arraylist of hashmaps with the tagged data
	 * @param exec
	 *            KNIME Execution context
	 * @return Buffered Data Container
	 * 
	 */
	final BufferedDataContainer createTable(final List<HashMap<String, String>> data,
			final ExecutionContext exec) {

		final Set<String> cols = ReaxysAPI.keySet(data);
		
		/* 
		 * translate to nice names
		 */
		final String[] colnames = cols.toArray(new String[cols.size()]);

		// the data table spec of the single output table,
		// the table will have three columns:
		final int offset = 2;
		final DataColumnSpec[] allColSpecs = new DataColumnSpec[colnames.length + offset];
		
		allColSpecs[0] = new DataColumnSpecCreator(QUERYCOLUMN,
				StringCell.TYPE).createSpec();
		
		allColSpecs[1] = new DataColumnSpecCreator(RESULTCOUNTCOLUMN,
				IntCell.TYPE).createSpec();

		for (int i = offset; i < colnames.length + offset; i++) {
			
			// make a structure column into a molecule column.
			if (isStructureTag(colnames[i-offset])) {
				allColSpecs[i] = new DataColumnSpecCreator(colnames[i - offset],
						SdfCell.TYPE).createSpec();
			} else if (isReactionTag(colnames[i-offset])) { 
				allColSpecs[i] = new DataColumnSpecCreator(colnames[i - offset],
						RxnCell.TYPE).createSpec();
			} else {
			
				allColSpecs[i] = new DataColumnSpecCreator(colnames[i - offset],
					StringCell.TYPE).createSpec();
			}
		}

		final DataTableSpec outputSpec = new DataTableSpec(allColSpecs);

		return exec.createDataContainer(outputSpec);

	}

	
	/**
	 * {@inheritDoc}
	 * 
	 * Execute interface.
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {
		
		logger.info("Reaxys lookup node started");
		// need to set a progress value so that it can be retrieved later
		exec.setProgress(0,"starting query");
		
		// identifier data
		final BufferedDataTable inputData = inData[0];
		
		/* get configuration values */
		final boolean debug = m_debug.getBooleanValue();

		final String rdt_desc = m_rdt.getStringArrayValue()[0];  // can return array in future
		final ReaxysDataTypes rdt = ReaxysDataTypes.getReaxysDataType(rdt_desc);

		// if this is true, instead of getting the top N values, get a uniform sampling
		final boolean useSampling = m_sample.getBooleanValue();
		
		final String sortString = m_sort.getStringValue();

		BufferedDataContainer outputTable = null;

		// create login document
		final ReaxysDocument ra = new ReaxysDocument(m_url.getStringValue(),
				m_apikey.getStringValue(), m_username.getStringValue(),
				m_password.getStringValue());

		try {

			// login
			final ReaxysAPI reaxys = new ReaxysAPI(exec, ra, debug);

			if (!ra.login()) {
				logger.fatal("error logging in to Reaxys server");
				return null;
			}


			logger.debug("Reaxys: login to Reaxys server successful");
			int inputRowCount = 0;
			int outputRowCount = 0;
			
			final int totalRows = inputData.getRowCount();

			if (totalRows == 0) {
				logger.info("Reaxys: no input queries");
				return null;
			}


			// get the column for query from the input.  default is 0, the first
			// column.  The result is "colNumber"
			final String columnName = m_column.getStringValue();
			int colNumber = 0;
			final String[] candidateData = inputData.getDataTableSpec().getColumnNames();
			
			for (int col = 0 ; col < candidateData.length; col++) {
				if (columnName.equals(candidateData[col])) {
					colNumber = col;
					break;
				}
			}
			
						
			/*
			 * loop over input data rows, if it isn't null.
			 */
			for (final DataRow row : inputData) {

				inputRowCount++;
				//use first column, 0, for the query
				final String id = row.getCell(colNumber).toString();
				/*
				 * reset these values from the dialog because if this is a structure search
				 * they are changed below - bug found by Roland  8-Jan-2015
				 */
				String idType = m_idtype.getStringValue();
				String query = m_query.getStringValue();

				// do the search on this molecule, the one in this row if the
				// input table.
				final long start = System.currentTimeMillis();

				// fix query if reaction or strukture
				final String lowerCaseIdType = idType.toLowerCase();
				// structure search
				if (lowerCaseIdType.startsWith("yy") || lowerCaseIdType.startsWith("struc")) {
					
					final String tempquery = makeMoleculeQuery("compound", id);
					if (!query.equals("")) {
						query = tempquery + " AND " + query;
					} else {
						query = tempquery;
					}
					
					idType = "";
				// reaction search
				} else if (lowerCaseIdType.startsWith("ry") || lowerCaseIdType.startsWith("reac")) {
					
					final String tempquery = makeMoleculeQuery("reaction", id);
					if (!query.equals("")) {
						query = tempquery + " AND " + query;
					} else {
						query = tempquery;
					}
					idType = "";
				}
				
				// do the search
				final List<HashMap<String, String>> resultTable = reaxys
						.getFact(idType, 	// root of query, e.g. IDE.XRN
								id, 		// value for query
								query, 		// additional query string
								rdt,
								startResult.getIntValue(), // startrecord to return in resultset
								maxResults.getIntValue(),  // maximum results to return
								useSampling,  // random sampling if maxResults is not -1
								m_addstruct.getBooleanValue(),// try to add structures to file
								v3sdfile.getBooleanValue(),  //  for adding structures, tell which kind
								sortString); // optional sorting string

				final int numberOfResults = reaxys.size();
				
				final String message = String.format(
						"Reaxys search for %15s took %5d ms", 
						id.substring(0, Math.min(id.length(), 30)),
						System.currentTimeMillis() - start);

				logger.info(message);

				// this counts on all of the subsequent rows containing tables
				// like the first
				if (outputTable == null && resultTable.size() > 0) {
					outputTable = createTable(resultTable, exec);
				}
				
				// if we've hit a row that had a result, now start collecting the
				// data. Without this 'if' it would fail if the first row didn't have
				// any data
				if (outputTable != null) {
					
					// loop over multiple results from this query row
					for (int i = 0; i < resultTable.size(); i++) {
						// if the query is very long, like a molecule or reaction
						//use the hashcode. very long here is 15 characters. add the
						// input table row count to help insure uniqueness since the
						// row labels must be unique
						String rowLabel = String.valueOf(++outputRowCount);
						
						final RowKey key = new RowKey(rowLabel + "_" + inputRowCount);
						// prepend molecule id as column
						HashMap<String, String> rowData = resultTable.get(i);
						
						// fix up data columns if this row has new columns
						outputTable = fixDataTableColumns(exec, outputTable, rowData);

						if (rowData == null) {
							continue;
						}
				
						rowData.put(QUERYCOLUMN, id);
						
						final DataRow newRow = createDataRow(outputTable, 
								key,rowData,
								numberOfResults);

						outputTable.addRowToTable(newRow);
						
					} // end of loop over results from this row
				}
				resultTable.clear(); // delete results
				exec.checkCanceled();
				// update of status is in the call that is retrieving the data
				float progress = (float)inputRowCount/inputData.getRowCount();
				exec.setProgress(progress);
			} // end of loop over input table rows

			exec.setMessage("search complete");
			
		} catch (final Exception e) {
			logger.error(e);
			e.printStackTrace();
		} 
		
		/*
		 * once we are done, we close the container and return its table. return
		 * empty table on error or null output.
		 */
		if (outputTable != null) {
			outputTable.close();
			final BufferedDataTable out = outputTable.getTable();
			return new BufferedDataTable[] { out };
		}
		/*
		 * if no results return an empty table
		 */
		return emptyTable(exec); 
	}
	
	
	/**
	 * prepare string for molecular structure query
	 * 
	 * @param type compound or reaction
	 * @return options string for query
	 */
	String getStructureQueryOptions(String type) {
		
		String result = type;
		if (!roleModel.getStringValue().equals("")) result += "," + roleModel.getStringValue();
		if (!optsModel.getStringValue().equals("")) {
			if (optsModel.getStringValue().equals("similarity")) {
				result += ",similarity=" + similarityModel.getIntValue();
			} else {
				result += "," + optsModel.getStringValue();
			}
		}
		if (!stereoModel.getStringValue().equals("")) result += "," + stereoModel.getStringValue();
		if (isotopesModel.getBooleanValue()) result += ",isotopes";
		if (tautomersModel.getBooleanValue()) result += ",tautomers";
		if (separate_fragmentsModel.getBooleanValue()) result += ",separate_fragments";
		if (ignore_mappingModel.getBooleanValue()) result += ",ignore_mappings";
		if (saltsModel.getBooleanValue()) result += ",salts";
		if (no_extra_ringsModel.getBooleanValue()) result += ",no_extra_rings";
		if (chargesModel.getBooleanValue()) result += ",charges";
		if (radicalsModel.getBooleanValue()) result += ",radicals";
		if (mixturesModel.getBooleanValue()) result += ",mixtures";
		if (alignModel.getBooleanValue()) result += ",align";
		if (atomlow.getIntValue() > 0 || atomhigh.getIntValue() > 0 && 
				(atomlow.getIntValue() < atomhigh.getIntValue())) {
				result += ",atoms=" + atomlow.getIntValue() + "-" + atomhigh.getIntValue();
			}
		
		if (fragmentlow.getIntValue() > 0 || fragmenthigh.getIntValue() > 0 && 
				(fragmentlow.getIntValue() < fragmenthigh.getIntValue())) {
				result += ",fragments=" + fragmentlow.getIntValue() + "-" + fragmenthigh.getIntValue();
			}
		
		if (ringlow.getIntValue() > 0 || ringhigh.getIntValue() > 0 && 
				(ringlow.getIntValue() < ringhigh.getIntValue())) {
				result += ",rings=" + ringlow.getIntValue() + "-" + ringhigh.getIntValue();
			}
		
		return result;
	}
	
	
	/**
	 * create the structure query from the SD file
	 * @param inputFile
	 * @return string for query.
	 */
	String makeMoleculeQuery(final String type, final String input) {
		
		final String result = "structure('"
				+ input 
				+ "','" 
				+ getStructureQueryOptions(type) 
				+ "')";
		
		return result;
	}

}
