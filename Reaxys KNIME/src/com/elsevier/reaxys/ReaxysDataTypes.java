package com.elsevier.reaxys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * class lists the various data types that are available in Reaxys, and the codes used
 * to retrieve them.  This includes the description the code, the context, and the database
 * 
 * @date 19 Aug 2014
 * @revised Feb 2015
 * @revised Jan 2021 added Commercial Substances
 * 
 * @author CLARKM
 * @author TOM W
 */
public enum ReaxysDataTypes {
	AB ("Abstract", "AB", "C", "RX", new String[] {"CIT", "AB", "CNR" }),
	ADSM ("Adsorption (MCS)", "ADSM", "S", "RX", new String[] {"IDE"}),
	ALLOY ("Alloy Composition", "ALLOY", "S", "RX", new String[] {"IDE"}),
	ASSM ("Association (MCS)", "ASSM", "S", "RX", new String[] {"IDE"}),
	AUTEX ("Auto Extract", "AUTEX", "S", "RX", new String[] {"IDE"}),
	AUTI ("Autoignition", "AUTI", "S", "RX", new String[] {"IDE"}),
	AZE ("Azeotropes (MCS)", "AZE", "S", "RX", new String[] {"IDE"}),
	BI ("Basic Indexes", "BI", "S", "RX", null),
	BIO ("Bioaccumulation, Biomagnification and Biomonitoring", "BIO", "S", "RX", null),
	BIOD ("Biodegradation", "BIOD", "S", "RX", new String[] {"IDE"}),
	BP ("Boiling Point", "BP", "S", "RX", new String[] {"IDE"} ),
	BSPM ("Boundary Surface Phenomena (MCS)", "BSPM", "S", "RX", new String[] {"IDE"}),
	BV ("Bulk Viscosity", "BV", "S", "RX", new String[] {"IDE"}),
	CA ("Citation Availability", "CA", "C", "RX", null),
	CALC ("Druglikeness", "CALC", "S", "RX", new String[] {"IDE"}),
	CAT ("Catalyst Investigation", "CAT", "S", "RX", new String[] {"IDE"}),
	CDEN ("Density of the Crystal", "CDEN", "S", "RX", new String[] {"IDE"}),
	CDER ("Derivative", "CDER", "S", "RX", new String[] {"IDE"}),
	CDIC ("Circular Dichroism", "CDIC", "S", "RX", new String[] {"IDE"}),
	CHROMAT ("Chromatographic Data", "CHROMAT", "S", "RX", new String[] {"IDE"}),
	CIP ("Electron Binding", "CIP", "S", "RX", new String[] {"IDE"}),
	CIT ("Citation", "CIT", "C", "RX", null), //  new String[] {"CIT", "AB", "CNR" }
	CLPHASE ("Highest Clinical Phase", "CLPHASE", "S", "RX", new String[] {"IDE"}),
	CMC ("Critical Micelle Concentration (MCS)", "CMC", "S", "RX", new String[] {"IDE"}),
	CNF ("Conformation", "CNF", "S", "RX", new String[] {"IDE"}),
	CNR ("Citation Number", "CNR", "C", "RX", new String[] {"CIT", "AB", "CNR" }),
	COMP ("Compressibility", "COMP", "S", "RX", new String[] {"IDE"}),
	CP ("Heat Capacity Cp", "CP", "S", "RX", new String[] {"IDE"}),
	CP0 ("Heat Capacity Cp0", "CP0", "S", "RX", new String[] {"IDE"}),
	CPD ("Crystal Property Description", "CPD", "S", "RX", new String[] {"IDE"}),
	CPEM ("Complex Phase Equilibria (MCS)", "CPEM", "S", "RX", new String[] {"IDE"}),
	CPTP ("Transition Point(s) of Crystalline Modification(s)", "CPTP", "S", "RX", new String[] {"IDE"}),
	CRD ("Critical Density", "CRD", "S", "RX", new String[] {"IDE"}),
	CRP ("Critical Pressure", "CRP", "S", "RX", new String[] {"IDE"}),
	CRT ("Critical Temperature", "CRT", "S", "RX", new String[] {"IDE"}),
	CRV ("Critical Volume", "CRV", "S", "RX", new String[] {"IDE"}),
	CRYPH ("Crystal Phase", "CRYPH", "S", "RX", new String[] {"IDE"}),
	CSG ("Space Group", "CSG", "S", "RX", new String[] {"IDE"}),
	CSYS ("Crystal System", "CSYS", "S", "RX", new String[] {"IDE"}),
	CV ("Heat Capacity Cv", "CV", "S", "RX", new String[] {"IDE"}),
	DAT ("Bioactivity", "DAT", "DPI", "RX", new String[] {"IDE"}),
	DE ("Dissociation Exponent", "DE", "S", "RX", new String[] {"IDE"}),
	DEN ("Density", "DEN", "S", "RX", new String[] {"IDE"}),
	DFM ("Molecular Deformation", "DFM", "S", "RX", new String[] {"IDE"}),
	DIC ("Dielectric Constant", "DIC", "S", "RX", new String[] {"IDE"}),
	DP ("Decomposition", "DP", "S", "RX", new String[] {"IDE"}),
	DV ("Dynamic Viscosity", "DV", "S", "RX", new String[] {"IDE"}),
	DX ("Bioactivity Data Point Availability", "DX", "DPI", "RX", new String[] {"DAT"}),
	EBC ("Energy Barriers", "EBC", "S", "RX", new String[] {"IDE"}),
	ECA ("Exposure Assessment", "ECA", "S", "RX", new String[] {"IDE"}),
	ECC ("Concentration in the Environment", "ECC", "S", "RX", new String[] {"IDE"}),
	ECDH ("Abiotic Degradation, Hydrolysis", "ECDH", "S", "RX", new String[] {"IDE"}),
	ECDP ("Abiotic Degradation, Photolysis", "ECDP", "S", "RX", new String[] {"IDE"}),
	ECS ("Stability in Soil", "ECS", "S", "RX", new String[] {"IDE"}),
	ECT ("Ecotoxicology", "ECT", "S", "RX", new String[] {"IDE"}),
	ECTD ("Transport and Distribution", "ECTD", "S", "RX", new String[] {"IDE"}),
	EDIS ("Dissociation Energy", "EDIS", "S", "RX", new String[] {"IDE"}),
	EDM ("Electrical Data (MCS)", "EDM", "S", "RX", new String[] {"IDE"}),
	ELCB ("Electrochemical Behaviour", "ELCB", "S", "RX", new String[] {"IDE"}),
	ELCH ("Electrochemistry Data", "ELCH", "S", "RX", new String[] {"IDE"}),
	ELE ("Electrical Data", "ELE", "S", "RX", new String[] {"IDE"}),
	ELP ("Electrical Polarizability", "ELP", "S", "RX", new String[] {"IDE"}),
	ELYC ("Electrolytic Conductivity", "ELYC", "S", "RX", new String[] {"IDE"}),
	EM ("E-molecules", "IDE", "S", "EM", new String[] {"IDE"}),
	EMO ("Electrical Moment", "EM", "S", "RX", new String[] {"IDE"}),
	ENEM ("Energy Data (MCS)", "ENEM", "S", "RX", new String[] {"IDE"}),
	EOD ("Oxygen Demand", "EOD", "S", "RX", new String[] {"IDE"}),
	ESR ("ESR Spectroscopy", "ESR", "S", "RX", new String[] {"IDE"}),
	EXPL ("Explosion Limits", "EXPL", "S", "RX", new String[] {"IDE"}),
	EXTID ("External Identifiers", "EXTID", "S", "RX", new String[] {"IDE"}),
	FA ("Field Availability", "FA", "S", "RX", new String[] {"IDE"}),
	FINFO ("Further Information", "FINFO", "S", "RX", new String[] {"IDE"}),
	FLAP ("Flash Point", "FLAP", "S", "RX", new String[] {"IDE"}),
	FLU ("Fluorescence Spectroscopy", "FLU", "S", "RX", new String[] {"IDE"}),
	GP ("Gas Phase", "GP", "S", "RX", new String[] {"IDE"}),
	HCOM ("Enthalpy of Combustion", "HCOM", "S", "RX", new String[] {"IDE"}),
	HEN ("Henry Constant (MCS)", "HEN", "S", "RX", new String[] {"IDE"}),
	HFOR ("Enthalpy of Formation", "HFOR", "S", "RX", new String[] {"IDE"}),
	HFUS ("Enthalpy of Fusion", "HFUS", "S", "RX", new String[] {"IDE"}),
	HHDG ("Enthalpy of Hydrogenation", "HHDG", "S", "RX", new String[] {"IDE"}),
	HPTP ("Enthalpies of Other Phase Transitions", "HPTP", "S", "RX", new String[] {"IDE"}),
	HSP ("Enthalpy of Sublimation", "HSP", "S", "RX", new String[] {"IDE"}),
	HVAP ("Enthalpy of Vaporization", "HVAP", "S", "RX", new String[] {"IDE"}),
	IDA ("Interatomic Distances and Angles", "IDA", "S", "RX", new String[] {"IDE"}),
	IDE ("Substance Identification", "IDE", "S", "RX", null),
	IEP ("Isoelectric Point  pH", "IEP", "S", "RX", new String[] {"IDE"}),
	INP ("Isolation from Natural Product", "INP", "S", "RX", new String[] {"IDE"}),
	IP ("Ionization Potential", "IP", "S", "RX", new String[] {"IDE"}),
	IR ("IR Spectroscopy", "IR", "S", "RX", new String[] {"IDE"}),
	KV ("Kinematic Viscosity", "KV", "S", "RX", new String[] {"IDE"}),
	KWD ("Keywords", "KWD", "C", "RX", null),
	LB ("Substance Label", "LB", "S", "RX", new String[] {"IDE"}),
	LIGM ("Multi-Center Ligands", "LIGM", "S", "RX", new String[] {"IDE"}),
	LIGO ("One-Center Ligands", "LIGO", "S", "RX", new String[] {"IDE"}),
	LIQPH ("Liquid Phase", "LIQPH", "S", "RX", new String[] {"IDE"}),
	LLSM ("Liquid/Liquid Systems (MCS)", "LLSM", "S", "RX", new String[] {"IDE"}),
	LPTP ("Transition Point(s) of Liquid Modification(s)", "LPTP", "S", "RX", new String[] {"IDE"}),
	LSSM ("Liquid/Solid Systems (MCS)", "LSSM", "S", "RX", new String[] {"IDE"}),
	LUM ("Luminescence Spectroscopy", "LUM", "S", "RX", new String[] {"IDE"}),
	LVSM ("Liquid/Vapour Systems (MCS)", "LVSM", "S", "RX", new String[] {"IDE"}),
	MAG ("Magnetic Data", "MAG", "S", "RX", new String[] {"IDE"}),
	MEC ("Mechanical Properties", "MEC", "S", "RX", new String[] {"IDE"}),
	MECM ("Mechanical & Physical Properties (MCS)", "MECM", "S", "RX", new String[] {"IDE"}),
	MP ("Melting Point", "MP", "S", "RX", new String[] {"IDE"}),
	MS ("Mass Spectrometry", "MS", "S", "RX", new String[] {"IDE"}),
	MSUS ("Magnetic Susceptibility", "MSUS", "S", "RX", new String[] {"IDE"}),
	MUT ("Mutarotation", "MUT", "S", "RX", new String[] {"IDE"}),
	NMR ("NMR Spectroscopy", "NMR", "S", "RX", new String[] {"IDE"}),
	NQR ("NQR Spectroscopy", "NQR", "S", "RX", new String[] {"IDE"}),
	ODM ("Optical Data (MCS)", "ODM", "S", "RX", new String[] {"IDE"}),
	OPT ("Optics", "OPT", "S", "RX", new String[] {"IDE"}),
	ORD ("Optical Rotatory Dispersion", "ORD", "S", "RX", new String[] {"IDE"}),
	ORP ("Optical Rotatory Power", "ORP", "S", "RX", new String[] {"IDE"}),
	OSM ("Other Spectroscopic Methods", "OSM", "S", "RX", new String[] {"IDE"}),
	OTHE ("Other Thermochemical Data", "OTHE", "S", "RX", new String[] {"IDE"}),
	PBIB ("Patent Bibliography", "PBIB", "C", "RX", new String[] {"CIT", "AB", "CNR" }),
	PC ("PubChem", "IDE", "S", "PU", new String[] {"IDE"}),
	PHARM ("Pharmacological Data", "PHARM", "S", "RX", new String[] {"IDE"}),
	PHO ("Phosphorescence Spectroscopy", "PHO", "S", "RX", new String[] {"IDE"}),
	POT ("Electrochemical Characteristics", "POT", "S", "RX", new String[] {"IDE"}),
	POW ("Partition octanol/water (MCS)", "POW", "S", "RX", new String[] {"IDE"}),
	PSD ("Patent-Specific Data", "PSD", "S", "RX",  new String[] {"CIT", "AB", "CNR" }),
	PUR ("Purification", "PUR", "S", "RX", new String[] {"IDE"}),
	QUAN ("Quantum Chemical Calculations", "QUAN", "S", "RX", new String[] {"IDE"}),
	RA ("Reaction Availability", "RA", "R", "RX", null),
	RAMAN ("Raman Spectroscopy", "RAMAN", "S", "RX", new String[] {"IDE"}),
	RI ("Refractive Index", "RI", "S", "RX", new String[] {"IDE"}),
	ROT ("Rotational Spectroscopy", "ROT", "S", "RX", new String[] {"IDE"}),
	RSTR ("Related Structure", "RSTR", "S", "RX", new String[] {"IDE"}),
	RX ("Reaction", "RX", "R", "RX", null),
	RXD ("Reaction Details", "RXD", "R", "RX", new String[] {"RX"}),
	RXNLINK ("Reaction Link", "RXNLINK", "R", "RX", null),
	RY ("Reaction SDFiles", "RY", "R", "RX", null),
	SDIC ("Static Dielectric Constant", "SDIC", "S", "RX", new String[] {"IDE"}),
	SDIF ("Self-diffusion", "SDIF", "S", "RX", new String[] {"IDE"}),
	SEQ ("Sequence", "SEQ", "S", "RX", new String[] {"IDE"}),
	SLB ("Solubility (MCS)", "SLB", "S", "RX", new String[] {"IDE"}),
	SLBP ("Solubility Product (MCS)", "SLBP", "S", "RX", new String[] {"IDE"}),
	SOLM ("Solution Behaviour (MCS)", "SOLM", "S", "RX", new String[] {"IDE"}),
	SOUND ("Sound Properties", "SOUND", "S", "RX", new String[] {"IDE"}),
	SOVERW ("Substance overview", "SOVERW", "S", "RX", new String[] {"IDE"}),
	SP ("Sublimation", "SP", "S", "RX", new String[] {"IDE"}),
	SPE ("Spectra", "SPE", "0", "RX", new String[] {"IDE"}),
	ST ("Surface Tension", "ST", "S", "RX", new String[] {"IDE"}),
	SUBLINK ("Database Link", "SUBLINK", "S", "RX", null),
	SUBUNIT ("Subunit", "SUBUNIT", "TGI", "RX", new String[] {"TARGET"}),
	SUPL ("Supplier", "SUPL", "S", "EM", null),
	TARGET ("Target", "TARGET", "TGI", "RX", null),
	TD ("Transport Data", "TD", "S", "RX", new String[] {"IDE"}),
	TEXP ("Thermal Expansion", "TEXP", "S", "RX", new String[] {"IDE"}),
	TOVERW ("Target overview", "TOVERW", "TGI", "RX", new String[] {"TARGET"}),
	TP ("Triple Point", "TP", "S", "RX", new String[] {"IDE"}),
	TRAM ("Transport Phenomena (MCS)", "TRAM", "S", "RX", new String[] {"IDE"}),
	TX ("Target Availability", "TX", "TGI", "RX", new String[] {"TARGET"}),
	USE ("Use", "USE", "S", "RX", new String[] {"IDE"}),
	UV ("UV/VIS Spectroscopy", "UV", "S", "RX", new String[] {"IDE"}),
	VP ("Vapour Pressure", "VP", "S", "RX", new String[] {"IDE"}),
	XS ("Cross-Sections", "XS", "S", "RX", new String[] {"IDE"}),
	YY ("Structure", "YY", "S", "RX", new String[] {"IDE"}),
	ZIT ("Supplier/vendor", "ZIT", "S", "RX", new String[] {"IDE"}),
    LN ("LabNetworks", "IDE", "S", "LN", new String[] {"IDE"}),
    LN_SUPL ("LabNetworks supplier", "SUPL", "S", "LN", null),
    SA ("SigmaAldrich", "IDE", "S", "SA", new String[] {"IDE"}),
    SA_SUPL ("SigmaAldrich supplier", "SUPL", "S", "SA", null),
    RC ("Commercial Substances", "IDE", "S", "RC", new String[] {"IDE"}),
	PROD ("Supplier Information", "PROD", "S", "RC", new String[] {"IDE"}),
	SNR ("Supplier Number", "SNR", "S", "RC", new String[] {"PROD", "IDE"});

	private String description;
	private String code;
	private String context;
	private String database;
	private String[] associatedTypes;
	private static HashMap<String, ReaxysDataTypes> lookupHash = null;
	
	ReaxysDataTypes(String description, String code, String context, String database, String[] associatedTypes) {
		this.description = description;
		this.code = code;
		this.context = context;
		this.database = database;
		this.associatedTypes = associatedTypes;
	}
	
	String getDescription() {
		return description;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getContext() {
		return context;
	}
	
	public String getDatabase() {
		return database;
	}
	
	public String[] getAssociatedTypes() {
		return associatedTypes;
	}
	
	ReaxysDataTypes[] getList() {
		return ReaxysDataTypes.values();
	}
	
	/**
	 * get the item by name
	 * 
	 * @param name name of the item to get
	 * @return ReaxysDataTypes object
	 */
	public static ReaxysDataTypes getByName(final String name) {
		
		for (final ReaxysDataTypes rdt : ReaxysDataTypes.values()) {
			if (rdt.name().equals(name)) {
				return rdt;
			}
		}

		return null;
	}
	
	/**
	 * get the item by name
	 * 
	 * @param name name of the item to get
	 * @return ReaxysDataTypes object
	 */
	public static ReaxysDataTypes getByCode(final String name) {
		
		for (final ReaxysDataTypes rdt : ReaxysDataTypes.values()) {
			if (rdt.getCode().equals(name)) {
				return rdt;
			}
		}

		return null;
	}
	
	/**
	 * lookup an enum element by its description. Return null if the description 
	 * does not match an element.
	 * 
	 * @param description
	 * @return enumeration element
	 */
	public static ReaxysDataTypes getReaxysDataType(final String description) {
		
		if (lookupHash == null) {
			lookupHash = new HashMap<String, ReaxysDataTypes>();
			
			for (final ReaxysDataTypes rdt : ReaxysDataTypes.values()) {
				lookupHash.put(rdt.getDescription().toLowerCase(), rdt);
			}
		}
		
		return lookupHash.get(description.toLowerCase());
	}
	
	/**
	 * get a list of the descriptions
	 * @return list of descriptions
	 */
	public static List<String> getDescriptionList() {
		
		final ArrayList<String> result = new ArrayList<String>(ReaxysDataTypes.values().length);
		for (final ReaxysDataTypes rdt : ReaxysDataTypes.values()) {
			result.add(rdt.getDescription());
		}
		return result;
	}
}