//package org.cytoscape.internal.actions;
//
//import java.awt.Toolkit;
//import java.awt.Window;
//import java.awt.event.ActionEvent;
//import java.awt.event.InputEvent;
//import java.awt.event.KeyEvent;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Properties;
//
//import javax.swing.KeyStroke;
//
//import org.cytoscape.application.swing.AbstractCyAction;
//import org.cytoscape.application.swing.CySwingApplication;
//import org.cytoscape.internal.dialogs.Cy3PreferencesDialog;
//import org.cytoscape.internal.dialogs.Cy3PreferencesDialogFactory;
//import org.cytoscape.internal.dialogs.PreferencesDialogFactory;
//import org.cytoscape.internal.util.ViewUtil;
//import org.cytoscape.property.CyProperty;
//import org.cytoscape.property.bookmark.Bookmarks;
//
///*
// * #%L
// * Cytoscape Swing Application Impl (swing-application-impl)
// * $Id:$
// * $HeadURL:$
// * %%
// * Copyright (C) 2006 - 2016 The Cytoscape Consortium
// * %%
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Lesser General Public License as 
// * published by the Free Software Foundation, either version 2.1 of the 
// * License, or (at your option) any later version.
// * 
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Lesser Public License for more details.
// * 
// * You should have received a copy of the GNU General Lesser Public 
// * License along with this program.  If not, see
// * <http://www.gnu.org/licenses/lgpl-2.1.html>.
// * #L%
// */
//
//@SuppressWarnings("serial")
//public class Cy3PreferenceAction extends AbstractCyAction {
//	
//	private final CySwingApplication desktop;
//	private final Cy3PreferencesDialogFactory dialogFactory;
//	private Cy3PreferencesDialog preferencesDialog;
//	private Map<String, Properties> propMap = new HashMap<>();
//	private Map<String, Bookmarks> bookmarkMap = new HashMap<>();
//	private  Map<String, CyProperty<?>> cyPropMap = new HashMap<>();
//	
//	public Cy3PreferenceAction(final CySwingApplication desktop, final Cy3PreferencesDialogFactory dialogFactory) {
//		super("Preferences...");
//		this.desktop = desktop;
//		this.dialogFactory = dialogFactory;
//		
//		setAcceleratorKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
//				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + InputEvent.META_DOWN_MASK));
//		
//		setPreferredMenu("Edit");	//
//		setMenuGravity(11.0f);
//	}
//
//	@Override
//	public void actionPerformed(ActionEvent e) {
//		final Window owner = ViewUtil.getWindowAncestor(e, desktop);
//		preferencesDialog = dialogFactory.getPreferencesDialog(owner, propMap, cyPropMap); 
//		preferencesDialog.setVisible(true);
//	}
//
//	@Override
//	public boolean isEnabled() {
//		return !dialogFactory.isDialogVisible();
//	}
//	
//	public void addCyProperty(CyProperty<?> p, Map<?,?> d){
//		String propertyName = p.getName();
//		Object obj = p.getProperties();
//		
//		if (obj instanceof Properties){		
//			propMap.put(propertyName, (Properties)obj);
//			cyPropMap.put(propertyName, p);
//		} else if (obj instanceof Bookmarks){
//			bookmarkMap.put(propertyName, (Bookmarks)obj);
//		} else {
//			System.out.println("PreferenceAction: Unknown type of properties");
//		}
//	}
//	
//	public void removeCyProperty(CyProperty<?> p, Map<?,?> d){
//		String propertyName = p.getName();
//		Object obj = p.getProperties();
//		
//		if (obj instanceof Properties){
//			propMap.remove(propertyName);
//		} else if (obj instanceof Bookmarks){
//			bookmarkMap.remove(propertyName);
//		}
//	}
//}
