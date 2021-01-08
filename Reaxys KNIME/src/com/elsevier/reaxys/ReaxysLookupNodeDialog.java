package com.elsevier.reaxys;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.JTabbedPane;

import org.knime.core.data.IntValue;
import org.knime.core.data.StringValue;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButton;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentLabel;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.DialogComponentPasswordField;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection;

import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;

import com.elsevier.reaxys.xml.utils.ReaxysDefs;

/**
 * <code>NodeDialog</code> for the "ReaxysLookup" Node. Lookup data from Reaxys
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Matthew Clark
 */
public class ReaxysLookupNodeDialog extends DefaultNodeSettingsPane implements ActionListener, TreeSelectionListener {
	
	final GridLayout layout = new GridLayout(1,1,0,0);
	JFrame helperFrame = null;
	JTree tree = null;
	JTextField searchBox = null;
	JFrame frame = null;

	/**
	 * New pane for configuring ReaxysLookup node dialog. 
	 */
	protected ReaxysLookupNodeDialog() {

		helperFrame = makeFieldHelper();
		final DialogComponentLabel version = new DialogComponentLabel("version: " + ReaxysDefs.getDateVersionString());
		
		@SuppressWarnings("unchecked")
		final DialogComponentColumnNameSelection columnChooser = 

		new DialogComponentColumnNameSelection(
				new SettingsModelString(ReaxysLookupNodeModel.CFGKEY_COLUMN_LIST, ReaxysLookupNodeModel.CFGKEY_DEFAULT_LIST), 
				ReaxysLookupNodeModel.CFGLABEL_COLUMN_LIST,
				0, 
				false, 
				new Class[] {StringValue.class, IntValue.class }
				);

		final DialogComponentString url = new DialogComponentString(
				new SettingsModelString(ReaxysLookupNodeModel.CFGKEY_URL,
						ReaxysLookupNodeModel.DEFAULT_URL), String.format(
								"%15s", ReaxysLookupNodeModel.CFGLABEL_URL), false, 60);

		final DialogComponentPasswordField apikey = new DialogComponentPasswordField(
				new SettingsModelString(ReaxysLookupNodeModel.CFGKEY_APIKEY,
						ReaxysLookupNodeModel.DEFAULT_APIKEY), String.format(
								"%15s", ReaxysLookupNodeModel.CFGLABEL_APIKEY), 60);

		final DialogComponentString username = new DialogComponentString(
				new SettingsModelString(ReaxysLookupNodeModel.CFGKEY_USERNAME,
						ReaxysLookupNodeModel.DEFAULT_USERNAME), String.format(
								"%15s", ReaxysLookupNodeModel.CFGLABEL_USERNAME), false, 60);

		final DialogComponentPasswordField password = new DialogComponentPasswordField(
				new SettingsModelString(ReaxysLookupNodeModel.CFGKEY_PASSWORD,
						ReaxysLookupNodeModel.DEFAULT_PASSWORD), String.format(
								"%15s", ReaxysLookupNodeModel.CFGLABEL_PASSWORD), 60);


		createNewGroup("URL and API key");
		addDialogComponent(url);

		addDialogComponent(apikey);
		createNewGroup("User/Password authentication (optional)");
		addDialogComponent(username);
		addDialogComponent(password);

		createNewGroup("Query");

		addDialogComponent(columnChooser);

		final DialogComponentString dcs = new DialogComponentString(
				new SettingsModelString(ReaxysLookupNodeModel.CFGKEY_IDTYPE,
						ReaxysLookupNodeModel.DEFAULT_IDTYPE), String.format(
								"%15s", ReaxysLookupNodeModel.CFGLABEL_IDTYPE), false, 60);

		addDialogComponent(dcs);

		addDialogComponent(new DialogComponentString(new SettingsModelString(
				ReaxysLookupNodeModel.CFGKEY_QUERY,
				ReaxysLookupNodeModel.DEFAULT_QUERY), String.format("%15s",
						ReaxysLookupNodeModel.CFGLABEL_QUERY), false, 60));
		
		DialogComponentButton button = new DialogComponentButton("Guide to Reaxys Fields");
		button.addActionListener(this);
		addDialogComponent(button);

		DialogComponentStringListSelection s = new DialogComponentStringListSelection(
				new SettingsModelStringArray(ReaxysLookupNodeModel.CFGKEY_DATA, null),
				ReaxysLookupNodeModel.CFGLABEL_DATA,
				ReaxysDataTypes.getDescriptionList(),
				ListSelectionModel.MULTIPLE_INTERVAL_SELECTION,
				true,
				10);

		DialogComponentBoolean addStructure =  new DialogComponentBoolean(
				new SettingsModelBoolean(ReaxysLookupNodeModel.CFGKEY_ADDSTRUCT, 
						ReaxysLookupNodeModel.CFGKEY_ADDSTRUCT_DEFAULT),
				ReaxysLookupNodeModel.CFGLABEL_ADDSTRUCT);

		addDialogComponent(s);
		addDialogComponent(addStructure);

		DialogComponentNumber maxResults =  new DialogComponentNumber(
				new SettingsModelInteger(ReaxysLookupNodeModel.CFGKEY_MAXRESULTS, ReaxysLookupNodeModel.CFGKEY_DEFAULT_MAXRESULTS), 
				ReaxysLookupNodeModel.CFGLABEL_MAXRESULTS, 
				1000, 7);

		DialogComponentNumber startResult =  new DialogComponentNumber(
		        new SettingsModelInteger(ReaxysLookupNodeModel.CFGKEY_STARTRESULT, ReaxysLookupNodeModel.DEFAULT_STARTRESULT), 
	                ReaxysLookupNodeModel.CFGLABEL_STARTRESULT, 
	                1000, 7);

		DialogComponentBoolean sampling =  new DialogComponentBoolean(
				new SettingsModelBoolean(ReaxysLookupNodeModel.CFGKEY_SAMPLE, 
						ReaxysLookupNodeModel.CFGKEY_SAMPLE_DEFAULT),
				ReaxysLookupNodeModel.CFGLABEL_SAMPLE);
		
		DialogComponentString sort = new DialogComponentString(
				new SettingsModelString(ReaxysLookupNodeModel.CFGKEY_SORT, 
						ReaxysLookupNodeModel.CFGKEY_SORT_DEFAULT),
				ReaxysLookupNodeModel.CFGLABEL_SORT);
				
		addDialogComponent(sort);

		createNewGroup("Data Subsets");
		addDialogComponent(sampling);
		
		
		addDialogComponent(maxResults);
		addDialogComponent(startResult);
		closeCurrentGroup();

		addDialogComponent(new DialogComponentBoolean(new SettingsModelBoolean(
				ReaxysLookupNodeModel.CFGKEY_DEBUG,
				ReaxysLookupNodeModel.DEFAULT_DEBUG),
				ReaxysLookupNodeModel.CFGLABEL_DEBUG));
		closeCurrentGroup();

		addDialogComponent(version);

		createStructureSearchOptions();
	
		this.getPanel().validate();
	}


	static int STEP = 10;
	static int WIDTH = 5;

	/**
	 * structure search options
	 */
	void createStructureSearchOptions() {

		// default similarity for similarity search
		final int defaultSimilarity = 75;
		
		SettingsModelString roleModel = new SettingsModelString("role", "");
		SettingsModelString optsModel = new SettingsModelString("opts", "exact");
		SettingsModelString stereoModel = new SettingsModelString("stereo", "");
		SettingsModelBoolean isotopesModel = new SettingsModelBoolean("isotopes", false);

		SettingsModelBoolean v3sdfileModel = new SettingsModelBoolean("v3sdfile", true);
		SettingsModelBoolean tautomersModel = new SettingsModelBoolean("tautomers", false);
		SettingsModelBoolean separate_fragmentsModel = new SettingsModelBoolean("separate_fragments", false);
		SettingsModelInteger similarityModel = new SettingsModelInteger("similarity", defaultSimilarity);
		SettingsModelBoolean ignore_mappingModel = new SettingsModelBoolean("ignore_mapping", false);
		SettingsModelBoolean saltsModel = new SettingsModelBoolean("salts", false);
		SettingsModelBoolean no_extra_ringsModel = new SettingsModelBoolean("no_extra_rings", false);
		SettingsModelBoolean chargesModel = new SettingsModelBoolean("charges", false);
		SettingsModelBoolean radicalsModel = new SettingsModelBoolean("radicals", false);
		SettingsModelBoolean mixturesModel = new SettingsModelBoolean("mixtures", false);
		SettingsModelBoolean alignModel = new SettingsModelBoolean("align", true);
		SettingsModelInteger atomlow = new SettingsModelInteger("atomCountsLow", 0);
		SettingsModelInteger atomhigh = new SettingsModelInteger("atomCountsHigh", 0);
		SettingsModelInteger fragmentlow = new SettingsModelInteger("fragmentCountsLow", 0);
		SettingsModelInteger fragmenthigh = new SettingsModelInteger("fragmentCountsHigh", 0);
		SettingsModelInteger ringlow = new SettingsModelInteger("ringCountsLow", 0);
		SettingsModelInteger ringhigh = new SettingsModelInteger("ringCountsHigh", 0);

		this.createNewTab("Structure Search Options");

		DialogComponentButtonGroup role = 
				new DialogComponentButtonGroup(
						roleModel, 
						"Role Options", 
						false, 
						new String[] {"Any", "Reactant", "Product", "Reactant or Product", "Reagent", "Catalyst", "Solvent", "Reagent or Catalyst" }, 
						new String[] {"", "starting_material", "product", "all_reactions", "reagent", "catalyst", "solvent", "reagent_or_catalyst" });

		addDialogComponent(role);
		role.getComponentPanel().setLayout(layout);

		this.setHorizontalPlacement(true);

		DialogComponentButtonGroup opts = 
				new DialogComponentButtonGroup(
						optsModel, 
						"Search type", 
						false, 
						new String[] {"Exact", "Substructure", "Substructure on heteroatoms", "Similarity"}, 
						new String[] {"exact", "substructure", "sub_hetereo", "similarity"});

		addDialogComponent(opts);
		opts.getComponentPanel().setLayout(layout);

		DialogComponentNumberEdit similarity = new DialogComponentNumberEdit(similarityModel,
				"Similarity value (1-99)");
		addDialogComponent(similarity);
		similarity.getComponentPanel().setLayout(layout);
		this.setHorizontalPlacement(false);


		DialogComponentButtonGroup stereo = 
				new DialogComponentButtonGroup(
						stereoModel, 
						"Stereo Options", 
						false, 
						new String[] {"any", "Stereo Absolute", "Stereo Relative"}, 
						new String[] {"", "stereo_absolute", "stereo_relative" });

		addDialogComponent(stereo);
		stereo.getComponentPanel().setLayout(layout);

		DialogComponentBoolean isotopes = new DialogComponentBoolean(isotopesModel,
				"Isotopes. If unset, the hit may contain isotopes only if the query does. Valid for both exact and substructure.");
		addDialogComponent(isotopes);	
		isotopes.getComponentPanel().setLayout(layout);


		DialogComponentBoolean tautomers = new DialogComponentBoolean(tautomersModel,
				"Tautomers. If set, tautomers of original hits are also found.");
		addDialogComponent(tautomers);
		tautomers.getComponentPanel().setLayout(layout);



		DialogComponentBoolean separate_fragments = new DialogComponentBoolean(separate_fragmentsModel,
				"Separate Fragments. Request that non-interconnected fragments of the query structure are mapped onto different fragments in the hit.");
		addDialogComponent(separate_fragments);
		separate_fragments.getComponentPanel().setLayout(layout);


		DialogComponentBoolean ignore_mapping = new DialogComponentBoolean(ignore_mappingModel,
				"Ignore Mapping. Ignore requests of the query to specifically find reactant atoms mapped to product atoms.");
		addDialogComponent(ignore_mapping);
		ignore_mapping.getComponentPanel().setLayout(layout);


		DialogComponentBoolean salts = new DialogComponentBoolean(saltsModel,
				"Salts. If set, allow more fragments, charges, and radical dots to be present in the hit that in the query.");
		addDialogComponent(salts);
		salts.getComponentPanel().setLayout(layout);

		DialogComponentBoolean no_extra_rings = new DialogComponentBoolean(no_extra_ringsModel,
				"No extra rings. If set, do not allow rings in the hit that are connecting two atoms in the query but are not yet present in the query.");
		addDialogComponent(no_extra_rings);
		no_extra_rings.getComponentPanel().setLayout(layout);


		DialogComponentBoolean charges = new DialogComponentBoolean(chargesModel,
				"Charges. Allow the hit to contain more charges than the query.");
		addDialogComponent(charges);
		charges.getComponentPanel().setLayout(layout);


		DialogComponentBoolean radicals = new DialogComponentBoolean(radicalsModel,
				"Radicals. Allow the hit to contain more radical dots than the query.");
		addDialogComponent(radicals);
		radicals.getComponentPanel().setLayout(layout);


		DialogComponentBoolean mixtures = new DialogComponentBoolean(mixturesModel,
				"Mixtures. After a search for substances, add those substances to the result that reference a substance in the initial result as a mixture component.");
		addDialogComponent(mixtures);
		mixtures.getComponentPanel().setLayout(layout);

		DialogComponentBoolean align = new DialogComponentBoolean(alignModel,
				"Align. On display, highlighted fragments found by the query will be rotated to a position where highlights are oriented similarly to the atoms in the query.");
		addDialogComponent(align);
		align.getComponentPanel().setLayout(layout);

		DialogComponentBoolean v3sdfile = new DialogComponentBoolean(v3sdfileModel,
				"v3 SD file output if checked");
		addDialogComponent(v3sdfile);
		v3sdfile.getComponentPanel().setLayout(layout);


		createNewGroup("Atom Counts");
		{
			this.setHorizontalPlacement(true);

			DialogComponentNumber atom_low = new DialogComponentNumber(atomlow, "low range", STEP, WIDTH);
			addDialogComponent(atom_low);
			atom_low.getComponentPanel().setLayout(layout);

			DialogComponentNumber atom_high = new DialogComponentNumber(atomhigh, "high range", STEP, WIDTH);
			addDialogComponent(atom_high);
			atom_high.getComponentPanel().setLayout(layout);
		}
		closeCurrentGroup();

		this.setHorizontalPlacement(false);
		createNewGroup("Fragment Counts");
		{
			this.setHorizontalPlacement(true);

			DialogComponentNumber fragment_low = new DialogComponentNumber(fragmentlow, "low range", STEP, WIDTH);
			addDialogComponent(fragment_low);
			fragment_low.getComponentPanel().setLayout(layout);

			DialogComponentNumber fragment_high = new DialogComponentNumber(fragmenthigh, "high range", STEP, WIDTH);
			addDialogComponent(fragment_high);
			fragment_high.getComponentPanel().setLayout(layout);
		}
		closeCurrentGroup();

		this.setHorizontalPlacement(false);
		createNewGroup("Ring Counts");{
			this.setHorizontalPlacement(true);

			DialogComponentNumber ring_low = new DialogComponentNumber(ringlow, "low range", STEP, WIDTH);
			addDialogComponent(ring_low);

			ring_low.getComponentPanel().setLayout(layout);

			DialogComponentNumber ring_high = new DialogComponentNumber(ringhigh, "high range", STEP, WIDTH);
			addDialogComponent(ring_high);
			ring_high.getComponentPanel().setLayout(layout);
		}
		closeCurrentGroup();
	}

	/**
	 * show the guide to Reaxys fields
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		helperFrame.setVisible(true);
		
		if (arg0.getActionCommand().equals("Search") || arg0.getSource().getClass() == JTextField.class) {
			DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
			Node root = (Node) model.getRoot();
		    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		    tree.setExpandsSelectedPaths(true);
		    // collapse path for result of new search
		    tree.removeSelectionPaths(tree.getSelectionPaths());
		    Enumeration<TreePath> exp = tree.getExpandedDescendants(new TreePath(root));
		    while (exp != null && exp.hasMoreElements()) {
		    	TreePath p = exp.nextElement();
		    	tree.collapsePath(p);
		    }

			frame.pack();
			
			expandMatches(root, searchBox.getText());
			frame.pack();
			frame.repaint();
			
		} else if (arg0.getActionCommand().equals("comboBoxChanged")) {
			@SuppressWarnings("unchecked")
			String item = ((JComboBox<String>)arg0.getSource()).getSelectedItem().toString();
			Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
			clpbrd.setContents(new StringSelection(item), null);
		}
	} 
	
	/**
	 * expand all nodes with leaf nodes starting with the string given as argument
	 * @param root
	 * @param value
	 */
	private void expandMatches(TreeNode root, String value) {
		int childCount = root.getChildCount();
		for (int i = 0; i < childCount; i++) {
			Node childNode = (Node) root.getChildAt(i);
			if (childNode.getChildCount() > 0) {
				expandMatches(childNode, value);
			} else {
				final String nodeText = childNode.toString();
				if (nodeText.toLowerCase().contains(value.toLowerCase())) {
					tree.addSelectionPath(new TreePath(childNode.getPath()));
				}
			}
		}
	}
	
	private JFrame makeFieldHelper() {
		
		frame = new JFrame("Reaxys Fields");
		JPanel mainPanel = new JPanel();
		JPanel treePanel  = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		JTabbedPane tabPane = new JTabbedPane();
		tabPane.addTab("Fields", null, mainPanel, null);
		
		JPanel syntaxPanel = new JPanel();
		JLabel image = new JLabel();
		image.setIcon(new ImageIcon(getClass().getResource("ReaxysApi.png")));
		syntaxPanel.add(image);
		tabPane.addTab("Syntax", null, syntaxPanel, null);
		
		try {
			URL iconURL = getClass().getResource("reaxys.png");
			frame.setIconImage(new ImageIcon(iconURL).getImage());
		} catch (Exception e) {}
		
		Node top = new Node("Reaxys");
		// make the tree
		tree = new JTree(top);
	    
		makeTree(top);
		tree.getSelectionModel().setSelectionMode
        (TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(this);
		tree.expandRow(0);
		// add the tree to a scrolling pane
		JScrollPane scrollPane = new JScrollPane(tree);
		treePanel.setLayout(new BorderLayout());
		treePanel.add(scrollPane, BorderLayout.CENTER);
		
		JLabel searchLabel = new JLabel("Search:");
		searchBox = new JTextField(32);
		searchBox.addActionListener(this);
		JButton searchButton = new JButton("Search");
		searchButton.addActionListener(this);
		
		JPanel searchPanel = new JPanel();
		searchPanel.add(searchLabel);
		searchPanel.add(searchBox);
		searchPanel.add(searchButton);
		
		JLabel opLable = new JLabel("Logical Operators:");
		JComboBox<String> ops = new JComboBox<String>(new String[]{"AND", "OR","NOT", "NEAR", "NEXT","PROXIMITY"});
		ops.addActionListener(this);
		
		JPanel opPanel = new JPanel();
		
		opPanel.add(opLable);
		opPanel.add(ops);
	
		JPanel comboPanel = new JPanel();
		comboPanel.add(opPanel);
		comboPanel.add(searchPanel);
		
		mainPanel.add(treePanel, BorderLayout.CENTER);
		mainPanel.add(comboPanel, BorderLayout.SOUTH);
		
		frame.add(tabPane);
		frame.setPreferredSize(new Dimension(800, 750));
		frame.pack();
		
		return frame;
	}
	
	/**
	 * creates the data tree to view the Reaxys variables that could be used for a searh
	 * 
	 * @param top top node of data tree.
	 */
	private void makeTree(final Node top) {
		Node l = null;
		Node m = null;
		

		l = new Node("Field Availability");
		l.add(new Node("Reaction Availability: RA.RA" ));
		l.add(new Node("Citation Availability: CA.CA" ));
		l.add(new Node("Bioactivity Data Point Availability: DX.DX" ));
		l.add(new Node("Target Availability: TX.TX" ));		
		top.add(l);

		top.add(makeSubnodes("BI"));


		m = new Node("Substances");
	

		m.add(makeSubnodes("IDE"));
		m.add(makeSubnodes("LIGO"));
		m.add(makeSubnodes("LIGM"));
		m.add(makeSubnodes("ALLOY"));
		m.add(makeSubnodes("YY"));
		m.add(makeSubnodes("LB"));
		m.add(makeSubnodes("SEQ"));
		m.add(makeSubnodes("CALC"));
		m.add(makeSubnodes("CLPHASE"));
		m.add(makeSubnodes("EXTID"));
		m.add(makeSubnodes("RSTR"));
		m.add(makeSubnodes("CAT"));
		m.add(makeSubnodes("CDER"));
		m.add(makeSubnodes("PUR"));
		m.add(makeSubnodes("SOVERW"));
		m.add(makeSubnodes("USE"));
		m.add(makeSubnodes("INP"));
		m.add(makeSubnodes("QUAN"));

		top.add(m);
		
		m = new Node("Patent Data");
		
		m.add(makeSubnodes("PSD"));
		m.add(makeSubnodes("PBIB"));
		top.add(m);

		
		m = new Node("Citation Data");
		m.add(makeSubnodes("CNR"));
		m.add(makeSubnodes("CIT"));
		m.add(makeSubnodes("AB"));
		top.add(m);
		
		m.add(makeSubnodes("KWD"));
		top.add(l);

		m = new Node("Reaction Data");
		
		m.add(makeSubnodes("RX"));
		m.add(makeSubnodes("RXNLINK"));
		m.add(makeSubnodes("RXD"));
		m.add(makeSubnodes("RY"));
		top.add(m);

		m = new Node("Physical Data");
		m.add(makeSubnodes("MP"));
		m.add(makeSubnodes("BP"));
		m.add(makeSubnodes("SP"));
		m.add(makeSubnodes("RI"));
		m.add(makeSubnodes("DEN"));
		m.add(makeSubnodes("CNF"));
		m.add(makeSubnodes("IDA"));
		m.add(makeSubnodes("EM"));
		m.add(makeSubnodes("ELP"));
		m.add(makeSubnodes("DFM"));
		m.add(makeSubnodes("EBC"));
		m.add(makeSubnodes("EDIS"));
		m.add(makeSubnodes("IP"));
		m.add(makeSubnodes("CIP")); 
		m.add(makeSubnodes("CPD"));
		m.add(makeSubnodes("CRYPH"));
		m.add(makeSubnodes("CSYS"));
		m.add(makeSubnodes("DP"));
		m.add(makeSubnodes("TP"));
		m.add(makeSubnodes("CPTP"));
		m.add(makeSubnodes("CSG"));
		m.add(makeSubnodes("CDEN"));
		m.add(makeSubnodes("LIQPH"));
		m.add(makeSubnodes("LPTP"));
		m.add(makeSubnodes("CRT"));
		m.add(makeSubnodes("CRP"));
		m.add(makeSubnodes("CRD"));
		m.add(makeSubnodes("CRV"));
		m.add(makeSubnodes("VP"));
		m.add(makeSubnodes("GP"));
		m.add(makeSubnodes("MEC"));
		m.add(makeSubnodes("COMP"));
		m.add(makeSubnodes("SOUND"));
		m.add(makeSubnodes("ST"));
		m.add(makeSubnodes("DV"));
		m.add(makeSubnodes("KV"));
		m.add(makeSubnodes("BV"));
		m.add(makeSubnodes("SDIF"));
		m.add(makeSubnodes("CHROMAT"));
		m.add(makeSubnodes("TD"));
		m.add(makeSubnodes("TEXP"));
		m.add(makeSubnodes("HCOM"));
		m.add(makeSubnodes("HFOR"));
		m.add(makeSubnodes("HHDG"));
		m.add(makeSubnodes("HFUS"));
		m.add(makeSubnodes("HVAP"));
		m.add(makeSubnodes("HSP"));
		m.add(makeSubnodes("HPTP"));
		m.add(makeSubnodes("CP"));
		m.add(makeSubnodes("CP0"));
		m.add(makeSubnodes("CV"));
		m.add(makeSubnodes("OTHE"));
		m.add(makeSubnodes("OPT"));
		m.add(makeSubnodes("ORP"));
		m.add(makeSubnodes("MUT"));
		m.add(makeSubnodes("CDIC"));
		m.add(makeSubnodes("ORD"));
		m.add(makeSubnodes("MSUS"));
		m.add(makeSubnodes("MAG"));
		m.add(makeSubnodes("SDIC"));
		m.add(makeSubnodes("DIC"));
		m.add(makeSubnodes("ELE"));
		m.add(makeSubnodes("ELCB"));
		m.add(makeSubnodes("DE"));
		m.add(makeSubnodes("IEP"));
		m.add(makeSubnodes("POT"));
		m.add(makeSubnodes("ELYC"));
		m.add(makeSubnodes("ELCH"));
		m.add(makeSubnodes("XS"));
		m.add(makeSubnodes("FLAP"));
		m.add(makeSubnodes("AUTI"));
		m.add(makeSubnodes("EXPL"));
		m.add(makeSubnodes("FINFO"));
		top.add(m);
		
		m = new Node("Solution Properties");
		m.add(makeSubnodes("SLB"));
		m.add(makeSubnodes("SLBP"));
		m.add(makeSubnodes("SOLM"));
		m.add(makeSubnodes("CMC"));
		m.add(makeSubnodes("HEN"));
		m.add(makeSubnodes("POW"));
		m.add(makeSubnodes("LVSM"));
		m.add(makeSubnodes("AZE"));
		m.add(makeSubnodes("CPEM"));
		m.add(makeSubnodes("LLSM"));
		m.add(makeSubnodes("LSSM"));
		m.add(makeSubnodes("MECM"));
		m.add(makeSubnodes("TRAM"));
		m.add(makeSubnodes("ENEM"));
		m.add(makeSubnodes("EDM"));
		m.add(makeSubnodes("ODM"));
		m.add(makeSubnodes("BSPM"));
		m.add(makeSubnodes("ADSM"));
		m.add(makeSubnodes("ASSM"));
		top.add(m);

		m = new Node("Spectra");
		
		m.add(makeSubnodes("NMR"));
		m.add(makeSubnodes("IR"));
		m.add(makeSubnodes("MS"));
		m.add(makeSubnodes("UV"));
		m.add(makeSubnodes("ESR"));
		m.add(makeSubnodes("NQR"));
		m.add(makeSubnodes("ROT"));
		m.add(makeSubnodes("RAMAN"));
		m.add(makeSubnodes("LUM"));
		m.add(makeSubnodes("FLU"));
		m.add(makeSubnodes("PHO"));
		m.add(makeSubnodes("OSM"));
		top.add(m);
		
		

		m = new Node("Ecology");
	
		m.add(makeSubnodes("PHARM"));
		m.add(makeSubnodes("ECT"));
		m.add(makeSubnodes("ECA"));
		m.add(makeSubnodes("ECC"));
		m.add(makeSubnodes("ECTD"));
		m.add(makeSubnodes("BIO"));
		m.add(makeSubnodes("BIOD"));
		m.add(makeSubnodes("ECDH"));
		m.add(makeSubnodes("ECDP"));
		m.add(makeSubnodes("ECS"));
		m.add(makeSubnodes("EOD"));
		top.add(m);

		m = new Node("Bioactivity");
		
		m.add(makeSubnodes("DAT"));
		m.add(makeSubnodes("TARGET"));
		m.add(makeSubnodes("SUBUNIT"));
		m.add(makeSubnodes("TOVERW"));
		top.add(m);

		
		m = new Node("Vendors");
		
		l = new Node("PubChem");
		l.add(new Node("PubChem Compound ID: IDE.PID" ));
		l.add(new Node("Synonyms: IDE.SYN" ));
		l.add(new Node("Chemical Name Segment: IDE.CNS" ));
		l.add(new Node("Thumb: MB.MBB" ));
		l.add(new Node("RingBin: MB.MBR" ));
		l.add(new Node("Heading: TB.HEADING" ));
		l.add(new Node("Substance Basic Index: SBI.BISUB" ));
		m.add(l);
		
		l = new Node("eMolecules");
		l.add(new Node("eMolecules Compound ID: IDE.EID"));
		l.add(new Node("Version ID: IDE.VERSIONID"));
		l.add(new Node("Parent ID: IDE.PARENTID"));
		l.add(new Node("Compound Type: IDE.CTYPE"));
		l.add(new Node("EMolecules Link: IDE.LINK"));
		m.add(l);
		
		l = new Node("LabNetwork");	
		l.add(new Node("LabNetwork Compound ID: IDE.LNID"));
		m.add(l);
		
		l = new Node("SigmaAldrich");	
		l.add(new Node("SigmaAldrich Compound ID: IDE.SAID"));
		m.add(l);
		
		l = new Node("Generic Suppliers");	
		m.add(makeSubnodes("SUPL"));
		
		
		top.add(m);

	}
	
	
	/**
	 * create a subtree for this class of fields, from the set of data types.
	 * @param code code for class, e.g. "IDE"
	 * @return tree witn fields from that node
	 */
	private Node makeSubnodes(String code) {
		
		String topName = ReaxysDataTypes.getByCode(code).getDescription();
		Node result = new Node(topName);
		
		for (String field : ReaxysFieldTypes.getLabelsForClass(code + ".")) {
			result.add(new Node(field));
		}
		
		return result;
		
	}
	

	/**
	 * handle value change and put item on the clipboard
	 */
	@Override
	public void valueChanged(TreeSelectionEvent arg0) {
		
		Node node = (Node) arg0.getPath().getLastPathComponent();
		String value = node.toString();
		
		String myString = value.substring(value.indexOf(":") + 1).trim();
		StringSelection stringSelection = new StringSelection(myString);
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		clpbrd.setContents(stringSelection, null);
		 
	}
	
	/**
	 * node that sorts its members.  good only for small trees since it sorts every time a node is added.
	 * 
	 * @author CLARKM
	 *
	 */
	class Node extends DefaultMutableTreeNode implements Comparable<Object> {

		private static final long serialVersionUID = -6437165670674307073L;
		
		/**
		 * constructor
		 * @param v string to label node
		 */
		Node(String v) { super(v); }
		
		@SuppressWarnings("unchecked")
		public void add(Node n) {
			super.add(n);
			Collections.sort(this.children);
		}
		
		@Override
		public int compareTo(Object arg0) {
			return this.toString().compareTo(arg0.toString());
		}
	}

}
