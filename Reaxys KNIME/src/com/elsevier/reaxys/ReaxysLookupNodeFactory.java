package com.elsevier.reaxys;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "ReaxysLookup" Node. Lookup data from Reaxys
 * 
 * @author Matthew Clark
 */
public class ReaxysLookupNodeFactory extends NodeFactory<ReaxysLookupNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReaxysLookupNodeModel createNodeModel() {
		return new ReaxysLookupNodeModel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNrNodeViews() {
		return 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeView<ReaxysLookupNodeModel> createNodeView(final int viewIndex,
			final ReaxysLookupNodeModel nodeModel) {
		return new ReaxysLookupNodeView(nodeModel);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasDialog() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeDialogPane createNodeDialogPane() {
		return new ReaxysLookupNodeDialog();
	}

}
