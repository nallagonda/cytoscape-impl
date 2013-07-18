package org.cytoscape.filter.internal.transformers;

import org.cytoscape.filter.model.Transformer;
import org.cytoscape.filter.model.TransformerFactory;
import org.cytoscape.filter.transformers.Transformers;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public class StringAttributeFilterFactory implements TransformerFactory<CyNetwork, CyIdentifiable> {

	@Override
	public Transformer<CyNetwork, CyIdentifiable> createTransformer() {
		return new StringAttributeFilter();
	}

	@Override
	public String getId() {
		return Transformers.STRING_ATTRIBUTE_FILTER;
	}

}
