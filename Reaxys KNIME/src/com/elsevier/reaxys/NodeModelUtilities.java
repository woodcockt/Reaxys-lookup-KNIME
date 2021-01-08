package com.elsevier.reaxys;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.knime.chem.types.RxnCell;
import org.knime.chem.types.RxnCellFactory;
import org.knime.chem.types.SdfCell;
import org.knime.chem.types.SdfCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.def.StringCell.StringCellFactory;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
/**
 * this class has utilities for the Reaxys KNIME node so that the file is broken
 * into two pieces, this class which extends NodeModel, and the ReaxysLookupNodeModel which extends
 * this class.  Otherwise the size of the class was getting too large.
 * 
 * @author CLARKM
 *
 */
public abstract class NodeModelUtilities extends NodeModel {
	
	final String newline = "\n";
	final String EOF = "$$$$";

	// the logger instance
	protected static final NodeLogger logger = NodeLogger
			.getLogger(ReaxysLookupNodeModel.class);


	/*
	 * property names read from config file to set apikey, username, password
	 */
	static final String REAXYS_APIKEY = "reaxys.apikey";
	static final String REAXYS_USERNAME = "reaxys.username";
	static final String REAXYS_PASSWORD = "reaxys.password";
	static final String REAXYS_URL = "reaxys.url";

	static final String PRODUCT_UUID = "78da9ea3-9613-4631-8aed-1865395ce720";
	
	/*
	 * set these in constructor. 
	 */
	static String DEFAULT_USERNAME = null;
	static String DEFAULT_PASSWORD = null;
	static String DEFAULT_APIKEY = null;

	/* default username for uname/password */
	static final String CFGKEY_USERNAME = "Username";
    static final String CFGLABEL_USERNAME = "Username";
	/* default password */
	static final String CFGKEY_PASSWORD = "Password";
    static final String CFGLABEL_PASSWORD = "Password";
	/* default api key */
	static final String CFGKEY_APIKEY = "API key";
    static final String CFGLABEL_APIKEY = "API key";

	/* default URL for connection */
	static final String CFGKEY_URL = "Reaxys URL";
    static final String CFGLABEL_URL = "Reaxys URL";
	static final String DEFAULT_URL = "https://www.reaxys.com/reaxys/api";

	/* default data to retrieve */
	static final String CFGKEY_DATA = "Reaxys Data";
    static final String CFGLABEL_DATA = "Reaxys data to return";
	static final String DEFAULT_DATA = "MP";

	/* default identifier for input data to match */
	static final String CFGKEY_IDTYPE = "Identifier";
    static final String CFGLABEL_IDTYPE = "Identifier";
	static final String DEFAULT_IDTYPE = "IDE.XRN";

	/* default query */
	static final String CFGKEY_QUERY = "Query";
    static final String CFGLABEL_QUERY = "Query";
	static final String DEFAULT_QUERY = "";

	/* debug status */
	static final String CFGKEY_DEBUG = "Debug";
	static final String CFGLABEL_DEBUG = "Write debug data to log";
	static final boolean DEFAULT_DEBUG = false;

	static final String QUERYCOLUMN = "queryValue";
	static final String RESULTCOUNTCOLUMN = "numberOfResults";
	static final String DEFAULT_CELL_VALUE = "";

	static final String CFGKEY_STARTRESULT = "Start Result to Return";
    static final String CFGLABEL_STARTRESULT = "Start result to return (advanced)";
    static final Integer DEFAULT_STARTRESULT = 1;

	static final String CFGKEY_MAXRESULTS = "Maximum Results to Return";
    static final String CFGLABEL_MAXRESULTS = "Maximum results to return";
	static final Integer CFGKEY_DEFAULT_MAXRESULTS = -1;
	
	static String CFGKEY_COLUMN_LIST = "Query Column";
    static String CFGLABEL_COLUMN_LIST = "Query Column";
	static String CFGKEY_DEFAULT_LIST = "";
	
	static final String CFGKEY_SAMPLE = "Random subset of results, instead of top N";
    static final String CFGLABEL_SAMPLE = "Random subset of results, instead of top N";
	static final boolean CFGKEY_SAMPLE_DEFAULT = false;
	
	static final String CFGKEY_SORT = "Sort order ";
    static final String CFGLABEL_SORT = "Sort order (advanced)";
	static final String CFGKEY_SORT_DEFAULT = null;
	
	static final String CFGKEY_ADDSTRUCT = "Add structures to results";
    static final String CFGLABEL_ADDSTRUCT = "Add structures to results";
	static final boolean CFGKEY_ADDSTRUCT_DEFAULT = false;
	
	// name of input column to use for auto-generated query
	protected final SettingsModelString m_column = new SettingsModelString(
			ReaxysLookupNodeModel.CFGKEY_COLUMN_LIST, CFGKEY_DEFAULT_LIST);
	
	// Start result. Default 1, useful when retrieving reactions or strutures in chuncks in a loop
    protected SettingsModelInteger startResult = new SettingsModelInteger(
            ReaxysLookupNodeModel.CFGKEY_STARTRESULT,
            ReaxysLookupNodeModel.DEFAULT_STARTRESULT);

	
	// maximum results. If the 'sampling' is false it is the top N results
	protected SettingsModelInteger maxResults = new SettingsModelInteger(
			ReaxysLookupNodeModel.CFGKEY_MAXRESULTS,
			ReaxysLookupNodeModel.CFGKEY_DEFAULT_MAXRESULTS);

	// optional username for Reaxys
	protected final SettingsModelString m_username = new SettingsModelString(
			ReaxysLookupNodeModel.CFGKEY_USERNAME, ReaxysLookupNodeModel.DEFAULT_USERNAME);

	// optional password for Reaxys
	protected final SettingsModelString m_password = new SettingsModelString(
			ReaxysLookupNodeModel.CFGKEY_PASSWORD, ReaxysLookupNodeModel.DEFAULT_PASSWORD);
	
	// apikey for Reaxys
	protected final SettingsModelString m_apikey =  new SettingsModelString(
			ReaxysLookupNodeModel.CFGKEY_APIKEY, ReaxysLookupNodeModel.DEFAULT_APIKEY);

	// url for contacting Reaxys
	protected final SettingsModelString m_url = new SettingsModelString(
			ReaxysLookupNodeModel.CFGKEY_URL, ReaxysLookupNodeModel.DEFAULT_URL);

	// the query field to use for auto-generated queries
	protected final SettingsModelString m_idtype = new SettingsModelString(
			ReaxysLookupNodeModel.CFGKEY_IDTYPE,
			ReaxysLookupNodeModel.DEFAULT_IDTYPE);

	// the query string for Reaxys
	protected final SettingsModelString m_query = new SettingsModelString(
			ReaxysLookupNodeModel.CFGKEY_QUERY,
			ReaxysLookupNodeModel.DEFAULT_QUERY);
	
	// debug output
	protected final SettingsModelBoolean m_debug = new SettingsModelBoolean(
			ReaxysLookupNodeModel.CFGKEY_DEBUG,
			ReaxysLookupNodeModel.DEFAULT_DEBUG);
	
	// use random sampling to get a result subset
	protected final SettingsModelBoolean m_sample = new SettingsModelBoolean(
			ReaxysLookupNodeModel.CFGKEY_SAMPLE,
			ReaxysLookupNodeModel.CFGKEY_SAMPLE_DEFAULT);
	
	// add structure to any query, if possible
	protected final SettingsModelBoolean m_addstruct = new SettingsModelBoolean(
			ReaxysLookupNodeModel.CFGKEY_ADDSTRUCT,
			ReaxysLookupNodeModel.CFGKEY_ADDSTRUCT_DEFAULT);
	
	// thesort string for Reaxys
	protected final SettingsModelString m_sort = new SettingsModelString(
			ReaxysLookupNodeModel.CFGKEY_SORT,
			ReaxysLookupNodeModel.CFGKEY_SORT_DEFAULT);

	// reaxys output data type
	protected final SettingsModelStringArray m_rdt = new SettingsModelStringArray(
			CFGKEY_DATA, 
			new String[]{DEFAULT_DATA});
	final SettingsModelString roleModel = new SettingsModelString("role", "");
	final SettingsModelString optsModel = new SettingsModelString("opts", "exact");
	final SettingsModelString stereoModel = new SettingsModelString("stereo", "");
	final SettingsModelBoolean isotopesModel = new SettingsModelBoolean("isotopes", false);
	final SettingsModelBoolean tautomersModel = new SettingsModelBoolean("tautomers", false);
	final SettingsModelBoolean separate_fragmentsModel = new SettingsModelBoolean("separate_fragments", false);
	final SettingsModelInteger similarityModel = new SettingsModelInteger("similarity", 75);
	final SettingsModelBoolean ignore_mappingModel = new SettingsModelBoolean("ignore_mapping", false);
	final SettingsModelBoolean saltsModel = new SettingsModelBoolean("salts", false);
	final SettingsModelBoolean no_extra_ringsModel = new SettingsModelBoolean("no_extra_rings", false);
	final SettingsModelBoolean chargesModel = new SettingsModelBoolean("charges", false);
	final SettingsModelBoolean radicalsModel = new SettingsModelBoolean("radicals", false);
	final SettingsModelBoolean mixturesModel = new SettingsModelBoolean("mixtures", false);
	final SettingsModelBoolean alignModel = new SettingsModelBoolean("align", true);
	final SettingsModelInteger atomlow = new SettingsModelInteger("atomCountsLow", 0);
	final SettingsModelInteger atomhigh = new SettingsModelInteger("atomCountsHigh", 0);
	final SettingsModelInteger fragmentlow = new SettingsModelInteger("fragmentCountsLow", 0);
	final SettingsModelInteger fragmenthigh = new SettingsModelInteger("fragmentCountsHigh", 0);
	final SettingsModelInteger ringlow = new SettingsModelInteger("ringCountsLow", 0);
	final SettingsModelInteger ringhigh = new SettingsModelInteger("ringCountsHigh", 0);
	final SettingsModelBoolean v3sdfile = new SettingsModelBoolean("v3sdfile", true);
	
	// list for saving and restoring settings to use instead of listing these out one by one
	
	final SettingsModel[] settingsList = new SettingsModel[] {m_username, m_password, m_url, m_apikey,
			m_idtype, m_query, m_debug, m_rdt, roleModel, optsModel, stereoModel, isotopesModel,
			tautomersModel, separate_fragmentsModel, similarityModel, ignore_mappingModel,
			saltsModel, no_extra_ringsModel, chargesModel, radicalsModel, mixturesModel,
			alignModel, atomlow, atomhigh, fragmentlow, fragmenthigh,ringlow, 
			ringhigh, v3sdfile, maxResults, m_column, m_sample, m_addstruct, m_sort, startResult };
	
	// cache for molecules to try an reduce memory by canonicalizing the large SD file strings
	final Cache<String, DataCell> moleculeCache = new Cache<String, DataCell>();

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
		moleculeCache.clear();
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected NodeModelUtilities(int nrInDataPorts, int nrOutDataPorts) {
		super(nrInDataPorts, nrOutDataPorts);
	}


	/**
	 * create an empty data table to return when the query returns no results
	 * re bug reported by Roland Bauer
	 * 
	 * @date 2Jan2015
	 * @author mclark
	 * @param exec
	 * @return an empty data table, with one column
	 */
	BufferedDataTable[] emptyTable(final ExecutionContext exec) {
		
		final DataColumnSpec[] allColSpecs = new DataColumnSpec[1];

		allColSpecs[0] = new DataColumnSpecCreator(QUERYCOLUMN,
				StringCell.TYPE).createSpec();

		final DataTableSpec outputSpec = new DataTableSpec(allColSpecs);
		final BufferedDataContainer container = exec.createDataContainer(outputSpec);
		container.close();
		
		final DataTable dataTable = container.getTable();

		try {
			BufferedDataTable result = exec.createBufferedDataTable(dataTable, exec);
			return new BufferedDataTable[] {result};
		} catch (CanceledExecutionException e) {
			logger.error("emptyTable: " + e);
		} 
		
		return null;
	}
	
	

	


	/**
	 * fix the output table if the row has new columns that aren't in the table.  Enlarge the table by adding
	 * the column and recopy the data to the new table. The data should be copied by reference, so the 
	 * storage required for the recopying should be minimal.
	 * 
	 * The original data table is created based on the first record, so if subsequent data records have
	 * data that the first record didn't have they would be left out without this analysis.
	 * 
	 * @param exec  KNIME execution environment
	 * @param outputTable  current data table
	 * @param rowData  the new row of data to compare to the talble
	 * @return output table, possibly updated with new columns based on the row given as an argument.
	 */
	BufferedDataContainer fixDataTableColumns(final ExecutionContext exec, final BufferedDataContainer outputTable, 
			final HashMap<String, String> rowData) {
		
		
		final List<String> colNames = Arrays.asList(outputTable.getTableSpec()
				.getColumnNames());
		
		// fast path to return with no changes.
		if (colNames.size() == rowData.size()) {
			return outputTable;
		}
		
		BufferedDataContainer newContainer = outputTable;
		
		// shallow copy; same objects in new map.
		final ArrayList<String> rowDataSet = new ArrayList<String>(rowData.keySet());
		
		// subtract all of the ones we knew about from the original table to see what is left over
		rowDataSet.removeAll(colNames);
	
		// process the columns that were not in the output table
		for (final String colName : rowDataSet) {

				logger.info("output col " + colName + " not in table, adding it");
				// use newContainer since we may add several columns in this sweep;
				newContainer.close();
				int numCols = newContainer.getTableSpec().getNumColumns();
				final DataColumnSpec[] newSpec = new DataColumnSpec[numCols + 1];

				// copy the column specs. there is no method to return the whole array.
				for (int s = 0; s < numCols; s++) {
					newSpec[s] = newContainer.getTableSpec().getColumnSpec(s);
				}
				DataCell newCell;
				
				// add the new column
				if (isStructureTag(colName)) {
					newSpec[numCols] = new DataColumnSpecCreator(colName,
							SdfCell.TYPE).createSpec();
					newCell = SdfCellFactory.create("");
					
				} else if (isReactionTag(colName)) {
					newSpec[numCols] = new DataColumnSpecCreator(colName,
							RxnCell.TYPE).createSpec();
					newCell = RxnCellFactory.create("");
				} else {
					newSpec[numCols] = new DataColumnSpecCreator(colName,
						StringCell.TYPE).createSpec();
					newCell = StringCellFactory.create("");
				}

				final DataTableSpec newTable = new DataTableSpec(newSpec);
				// make a temporary new container to receive the rows from the old one
				final BufferedDataContainer brandNewContainer = exec.createDataContainer(newTable);

				// copy rows from from the original outputTable; we may end up doing this multiple times
				//  if this row has many more columns.
				// always copy from the original output table, as we are only adding columns,
				// not data.
				for (DataRow oldRow : newContainer.getTable()) {
					
					// for each row, copy each cell in the row.
					// hopefully this is referencing each cell object, 
					// not making new ones.
					final DataCell[] cells = new DataCell[numCols + 1];
					final RowKey oldKey = oldRow.getKey();
					final int nCells = oldRow.getNumCells();
					
					for (int ss= 0; ss < nCells; ss++) {
						cells[ss] = oldRow.getCell(ss);
					}
					// add empty data
					cells[numCols] = newCell; // new StringCell(DEFAULT_CELL_VALUE);
					final DataRow newRow = new DefaultRow(oldKey, cells);
					brandNewContainer.addRowToTable(newRow);
				}
				// "copy" the temporary new container to the new container to be returned
				newContainer = brandNewContainer;
		}
		return newContainer;
	}
	

	
	/**
	 * create a new data row for the output table with the current row data,
	 * a key, and the count of total results
	 * 
	 * @param outputTable current output table
	 * @param key rowkey derived from external data
	 * @param rowData HashMap<String, String> data for this row
	 * @param totalResults total Reaxys results
	 * @return
	 */
	DataRow createDataRow(BufferedDataContainer outputTable, 
			RowKey key, HashMap<String, String> rowData,
			int totalResults)  {

		// the cells of the current row, the types of the cells must
		// match the column spec (see above)
		final String[] colNames = outputTable.getTableSpec().getColumnNames();
		final DataCell[] cells = new DataCell[colNames.length];

		// assign all the column values for this row
		
		for (int c = 0; c < cells.length; c++) {
			
			String colName = colNames[c];
			String value = rowData.get(colName);
			
			DataColumnSpec spec = outputTable.getTableSpec().getColumnSpec(colName);
			
			if (value == null) {
				value = DEFAULT_CELL_VALUE; // looks nicer than null
			}

			try {
				if (spec.getType() == SdfCell.TYPE) {
					if (value.equals(DEFAULT_CELL_VALUE)) value = EOF;
					if (moleculeCache.containsKey(value)) { 
						cells[c] = moleculeCache.get(value);
					} else {
						cells[c] = SdfCellFactory.create(value); 
						moleculeCache.put(value, cells[c]);
					}

				} else if (spec.getType() == RxnCell.TYPE) {

					if (moleculeCache.containsKey(value)) { 
						cells[c] = moleculeCache.get(value);
					} else {
						cells[c] = RxnCellFactory.create(value);
						moleculeCache.put(value, cells[c]);
					}
				} else {
					cells[c] = new StringCell(value);
				}
			} catch (Exception e) { // catch sdf/rdf conversion errors and go on
				System.err.println("Error: " + e + " for sdf\n" + value);
			}
		}
		
		// cells[0] is query value
		cells[1] = new IntCell(totalResults);
		final DataRow newRow = new DefaultRow(key, cells);
		return newRow;
	}
	
	
	/**
	 * return true if the tag has structure data.
	 * @param tag  tag to test
	 * @return true if the data field has a structure, false if not.
	 */
	public static boolean isStructureTag(String tag) {
		
		final String[] tags = {"YY.STR","RY.RCT","RY.PRO"};
		for (String t : tags) {
			if (tag.contains(t)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * return true if the tag has reaction data.
	 * @param tag  tag to test
	 * @return true if the data field has a reaction, false if not.
	 */
	public static boolean isReactionTag(String tag) {
		
		final String[] tags = {"RX.RXNFILE", "RY.STR"};
		for (String t : tags) {
			if (tag.contains(t)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {

		return new DataTableSpec[] { null };
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {

		for (SettingsModel m : settingsList) {
			m.saveSettingsTo(settings);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {

		for (SettingsModel m : settingsList) {
			m.loadSettingsFrom(settings);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {

		for (SettingsModel m : settingsList) {
			m.validateSettings(settings);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
	}

}
