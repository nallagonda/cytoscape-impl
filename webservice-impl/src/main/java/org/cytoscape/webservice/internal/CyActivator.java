



package org.cytoscape.webservice.internal;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.work.swing.GUITaskManager;

import org.cytoscape.webservice.internal.ui.UnifiedNetworkImportDialog;
import org.cytoscape.webservice.internal.task.ShowNetworkImportDialogAction;

import org.cytoscape.application.swing.CyAction;


import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		CyApplicationManager cyApplicationManagerServiceRef = getService(bc,CyApplicationManager.class);
		CySwingApplication cySwingApplicationServiceRef = getService(bc,CySwingApplication.class);
		GUITaskManager taskManagerServiceRef = getService(bc,GUITaskManager.class);
		
		UnifiedNetworkImportDialog unifiedNetworkImportDialog = new UnifiedNetworkImportDialog(taskManagerServiceRef);
		ShowNetworkImportDialogAction showNetworkImportDialogAction = new ShowNetworkImportDialogAction(cyApplicationManagerServiceRef,cySwingApplicationServiceRef,unifiedNetworkImportDialog);
		
		registerService(bc,showNetworkImportDialogAction,CyAction.class, new Properties());
	}
}

