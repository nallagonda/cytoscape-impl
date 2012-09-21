
package org.cytoscape.internal.select;


import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.events.AboutToRemoveEdgeViewsEvent;
import org.cytoscape.view.model.events.AboutToRemoveEdgeViewsListener;
import org.cytoscape.view.model.events.AboutToRemoveNodeViewsEvent;
import org.cytoscape.view.model.events.AboutToRemoveNodeViewsListener;
import org.cytoscape.view.model.events.AddedEdgeViewsEvent;
import org.cytoscape.view.model.events.AddedEdgeViewsListener;
import org.cytoscape.view.model.events.AddedNodeViewsEvent;
import org.cytoscape.view.model.events.AddedNodeViewsListener;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;

public class RowViewTracker implements NetworkViewAddedListener, 
	AddedNodeViewsListener, AddedEdgeViewsListener, 
	AboutToRemoveNodeViewsListener, AboutToRemoveEdgeViewsListener,
	NetworkViewAboutToBeDestroyedListener {

	private Map<CyRow,View<?>> rowViewMap;

	public RowViewTracker() {
		rowViewMap = new HashMap<CyRow,View<?>>();
	}

	public void handleEvent(final NetworkViewAddedEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				final CyNetworkView view = e.getNetworkView();
				final CyNetwork net = view.getModel(); 

				for ( View<CyNode> nv : view.getNodeViews() )
					rowViewMap.put( net.getRow(nv.getModel()), nv);
		
				for ( View<CyEdge> ev : view.getEdgeViews() ) 
					rowViewMap.put( net.getRow(ev.getModel()), ev);
			}
		});
	}
	
	public void handleEvent(final AddedNodeViewsEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				final CyNetworkView view = e.getSource();
				final CyNetwork net = view.getModel(); 
				
				for ( View<CyNode> v : e.getNodeViews()) 
					if (net.containsNode(v.getModel())) 
						rowViewMap.put( net.getRow(v.getModel()), v );
			}
		});
	}
	
	public void handleEvent(final AddedEdgeViewsEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				final CyNetworkView view = e.getSource();
				final CyNetwork net = view.getModel(); 

				for ( View<CyEdge> v : e.getEdgeViews()) 
					if (net.containsEdge(v.getModel())) 
						rowViewMap.put( net.getRow(v.getModel()), v );
			}
		});
	}
	
	public void handleEvent(final AboutToRemoveNodeViewsEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Collection<View<?>> values = rowViewMap.values();
				for ( View<CyNode> v : e.getPayloadCollection()) {
					values.remove(v);
				}
			}
		});
	}
	
	public void handleEvent(final AboutToRemoveEdgeViewsEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Collection<View<?>> values = rowViewMap.values();
				for ( View<CyEdge> v : e.getPayloadCollection()) 
					values.remove(v);
			}
		});
	}
	
	@Override
	public void handleEvent(final NetworkViewAboutToBeDestroyedEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				CyNetworkView networkView = e.getNetworkView();
				Collection<View<?>> values = rowViewMap.values();
				for ( View<?> v : networkView.getAllViews()) 
					values.remove(v);
			}
		});
	}
	
	public Map<CyRow,View<?>> getRowViewMap() {
		return Collections.unmodifiableMap(rowViewMap);  
	}
}
