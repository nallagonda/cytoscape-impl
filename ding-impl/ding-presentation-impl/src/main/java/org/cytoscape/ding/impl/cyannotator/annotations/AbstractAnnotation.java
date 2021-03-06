package org.cytoscape.ding.impl.cyannotator.annotations;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.swing.JComponent;
import javax.swing.JDialog;

import org.cytoscape.ding.impl.ArbitraryGraphicsCanvas;
import org.cytoscape.ding.impl.ContentChangeListener;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.utils.ViewUtils;
import org.cytoscape.ding.internal.util.ViewUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.ArrowAnnotation;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

@SuppressWarnings("serial")
public abstract class AbstractAnnotation extends JComponent implements DingAnnotation {
	
	protected boolean selected;

	private double globalZoom = 1.0;
	private double myZoom = 1.0;

	private DGraphView.Canvas canvasName;
	private UUID uuid = UUID.randomUUID();

	private Set<ArrowAnnotation> arrowList = new HashSet<>();

	protected final boolean usedForPreviews;
	protected DGraphView view;
	protected ArbitraryGraphicsCanvas canvas;
	protected GroupAnnotationImpl parent;
	protected CyAnnotator cyAnnotator;
	protected String name;
	protected Point2D offset; // Offset in node coordinates
	protected Rectangle2D initialBounds;

	protected static final String ID = "id";
	protected static final String TYPE = "type";
	protected static final String ANNOTATION_ID = "uuid";
	protected static final String PARENT_ID = "parent";

	protected Map<String, String> savedArgMap;
	protected double zOrder;

	protected final Window owner;
	
	/**
	 * This constructor is used to create an empty annotation
	 * before adding to a specific view.  In order for this annotation
	 * to be functional, it must be added to the AnnotationManager
	 * and setView must be called.
	 */
	protected AbstractAnnotation(DGraphView view, Window owner, boolean usedForPreviews) {
		this.owner = owner;
		this.view = view;
		this.cyAnnotator = view == null ? null : view.getCyAnnotator();
		this.usedForPreviews = usedForPreviews;
		this.canvas = (ArbitraryGraphicsCanvas)(view.getCanvas(DGraphView.Canvas.FOREGROUND_CANVAS));
		this.canvasName = DGraphView.Canvas.FOREGROUND_CANVAS;
		this.globalZoom = view.getZoom();
		name = getDefaultName();
	}

	protected AbstractAnnotation(AbstractAnnotation c, Window owner, boolean usedForPreviews) {
		this(c.view, owner, usedForPreviews);
		arrowList = new HashSet<>(c.arrowList);
		this.canvas = c.canvas;
		this.canvasName = c.canvasName;
	}

	protected AbstractAnnotation(DGraphView view, double x, double y, double zoom, Window owner) {
		this(view, owner, false);
		setLocation((int)x, (int)y);
	}

	protected AbstractAnnotation(DGraphView view, Map<String, String> argMap, Window owner) {
		this(view, owner, false);

		Point2D coords = ViewUtils.getComponentCoordinates(view, argMap);
		this.globalZoom = ViewUtils.getDouble(argMap, ZOOM, 1.0);
		this.zOrder = ViewUtils.getDouble(argMap, Z, 0.0);
		
		if (argMap.get(NAME) != null)
			name = argMap.get(NAME);
		
		String canvasString = ViewUtils.getString(argMap, CANVAS, FOREGROUND);
		
		if (canvasString != null && canvasString.equals(BACKGROUND)) {
			this.canvas = (ArbitraryGraphicsCanvas)(view.getCanvas(DGraphView.Canvas.BACKGROUND_CANVAS));
			this.canvasName = DGraphView.Canvas.BACKGROUND_CANVAS;
		}

		setLocation((int)coords.getX(), (int)coords.getY());

		if (argMap.containsKey(ANNOTATION_ID))
			this.uuid = UUID.fromString(argMap.get(ANNOTATION_ID));
	}

	//------------------------------------------------------------------------

	protected String getDefaultName() {
		return cyAnnotator.getDefaultAnnotationName(getType().getSimpleName());
	}
	
	@Override
	public String toString() {
		return getArgMap().get("type")+" annotation "+uuid.toString()+" at "+getX()+", "+getY()+" zoom="+globalZoom+" on canvas "+canvasName;
	}

	@Override
	public String getCanvasName() {
		if (canvasName.equals(DGraphView.Canvas.BACKGROUND_CANVAS))
			return BACKGROUND;
		return FOREGROUND;
	}

	@Override
	public void setCanvas(String cnvs) {
		canvasName = (cnvs.equals(BACKGROUND)) ? 
				DGraphView.Canvas.BACKGROUND_CANVAS : DGraphView.Canvas.FOREGROUND_CANVAS;
		canvas = (ArbitraryGraphicsCanvas)(view.getCanvas(canvasName));
		for (ArrowAnnotation arrow: arrowList) 
			if (arrow instanceof DingAnnotation)
				((DingAnnotation)arrow).setCanvas(cnvs);

		update();		// Update network attributes
	}

	@Override
	public void changeCanvas(final String cnvs) {
		// Are we really changing anything?
		if ((cnvs.equals(BACKGROUND) && canvasName.equals(DGraphView.Canvas.BACKGROUND_CANVAS)) ||
		    (cnvs.equals(FOREGROUND) && canvasName.equals(DGraphView.Canvas.FOREGROUND_CANVAS)))
			return;

		ViewUtil.invokeOnEDTAndWait(() -> {
			if (!(this instanceof ArrowAnnotationImpl)) {
				for (ArrowAnnotation arrow: arrowList) {
					if (arrow instanceof DingAnnotation)
						((DingAnnotation)arrow).changeCanvas(cnvs);
				}
			}
			
			canvas.remove(this);	// Remove ourselves from the current canvas
			canvas.repaint();  	// update the canvas
			setCanvas(cnvs);		// Set the new canvas
			canvas.add(this);	// Add ourselves		
			canvas.repaint();  	// update the canvas
		});
	}

	@Override
	public CyNetworkView getNetworkView() {
		return (CyNetworkView)view;
	}

	@Override
	public ArbitraryGraphicsCanvas getCanvas() {
		return canvas;
	}

	public JComponent getComponent() {
		return (JComponent)this;
	}

	public UUID getUUID() {
		return uuid;
	}

	public double getZOrder() {
		return zOrder;
	}

	@Override
	public void addComponent(final JComponent cnvs) {
		ViewUtil.invokeOnEDTAndWait(() -> {
			if (inCanvas(canvas) && (canvas == cnvs)) {
				canvas.setComponentZOrder(this, (int)zOrder);
				return;
			}

			if (cnvs == null && canvas != null) {
	
			} else if (cnvs == null) {
				setCanvas(FOREGROUND);
			} else {
				if (cnvs.equals(view.getCanvas(DGraphView.Canvas.BACKGROUND_CANVAS)))
					setCanvas(BACKGROUND);
				else
					setCanvas(FOREGROUND);
			}
			canvas.add(this.getComponent());
			canvas.setComponentZOrder(this, (int)zOrder);
		});
	}
    
	@Override
	public CyAnnotator getCyAnnotator() {return cyAnnotator;}

	@Override
	public void setGroupParent(GroupAnnotation parent) {
		if (parent instanceof GroupAnnotationImpl) {
			this.parent = (GroupAnnotationImpl)parent;
		} else if (parent == null) {
			this.parent = null;
		}
		cyAnnotator.addAnnotation(this);
	}

	@Override
	public GroupAnnotation getGroupParent() {
		return (GroupAnnotation)parent;
	}
    
	// Assumes location is node coordinates
	@Override
	public void moveAnnotationRelative(Point2D location) {
		if (offset == null) {
			moveAnnotation(location);
			return;
		}

		// Get the relative move
		moveAnnotation(new Point2D.Double(location.getX()-offset.getX(), location.getY()-offset.getY()));
	}

	// Assumes location is node coordinates.
	@Override
	public void moveAnnotation(Point2D location) {
		// Location is in "node coordinates"
		Point2D coords = ViewUtils.getComponentCoordinates(view, location.getX(), location.getY());
		if (!(this instanceof ArrowAnnotationImpl)) {
			setLocation((int)coords.getX(), (int)coords.getY());
		}
	}

	@Override
	public void setLocation(final int x, final int y) {
		ViewUtil.invokeOnEDTAndWait(() -> {
			super.setLocation(x, y);
			canvas.modifyComponentLocation(x, y, this);
		});
	}

	@Override
	public void setSize(final int width, final int height) {
		ViewUtil.invokeOnEDTAndWait(() -> {
			super.setSize(width, height);
		});
	}

	@Override
	public Point getLocation() {
		return super.getLocation();
	}

	@Override
	public boolean contains(int x, int y) {
		if (x > getX() && y > getY() && x-getX() < getWidth() && y-getY() < getHeight())
			return true;
		return false;
	}

	@Override
	public void removeAnnotation() {
		ViewUtil.invokeOnEDTAndWait(() -> {
			canvas.remove(this);
			cyAnnotator.removeAnnotation(this);
			for (ArrowAnnotation arrow: arrowList) {
				if (arrow instanceof DingAnnotation)
					((DingAnnotation)arrow).removeAnnotation();
			}
			if (parent != null)
				parent.removeMember(this);
	
			canvas.repaint();
		});
	}

	public void resizeAnnotation(double width, double height) {
		// Nothing to do here...
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		if (!Objects.equals(name, this.name)) {
			this.name = name;
			update();
		}
	}

	@Override
	public double getZoom() {
		return globalZoom;
	}

	@Override
	public void setZoom(double zoom) {
		if (zoom != globalZoom) {
			globalZoom = zoom;
			update();
		}
	}

	@Override
	public double getSpecificZoom() {
		return myZoom;
	}

	@Override
	public void setSpecificZoom(double zoom) {
		if (zoom != myZoom) {
			myZoom = zoom;
			update();
		}
	}

	@Override
	public boolean isSelected() {
		return selected;
	}

	@Override
	public void setSelected(boolean selected) {
		setSelected(selected, true);
	}
	
	protected void setSelected(boolean selected, boolean firePropertyChangeEvent) {
		if (selected != this.selected) {
			this.selected = selected;
			cyAnnotator.setSelectedAnnotation(this, selected);
			
			if (firePropertyChangeEvent)
				firePropertyChange("selected", !selected, selected);
		}
	}

	@Override
	public void addArrow(ArrowAnnotation arrow) {
		arrowList.add(arrow);
		update();
	}

	@Override
	public void removeArrow(ArrowAnnotation arrow) {
		arrowList.remove(arrow);
		update();
	}

	@Override
	public Set<ArrowAnnotation> getArrows() {
		return arrowList;
	}

	@Override
	public Map<String,String> getArgMap() {
		Map<String, String> argMap = new HashMap<>();
		if (name != null)
			argMap.put(NAME, this.name);
		ViewUtils.addNodeCoordinates(view, argMap, getX(), getY());
		argMap.put(ZOOM,Double.toString(this.globalZoom));
		if (canvasName.equals(DGraphView.Canvas.BACKGROUND_CANVAS))
			argMap.put(CANVAS, BACKGROUND);
		else
			argMap.put(CANVAS, FOREGROUND);
		argMap.put(ANNOTATION_ID, this.uuid.toString());

		if (parent != null)
			argMap.put(PARENT_ID, parent.getUUID().toString());

		int zOrder = canvas.getComponentZOrder(getComponent());
		argMap.put(Z, Integer.toString(zOrder));

		return argMap;
	}
	
	@Override
	public boolean isUsedForPreviews() {
		return usedForPreviews;
	}

	@Override
	public void drawAnnotation(Graphics g, double x, double y, double scaleFactor) {
	}

	@Override
	public void update() {
		updateAnnotationAttributes();
		getCanvas().repaint();
	}

	// Component overrides
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;

		/* Set up all of our anti-aliasing, etc. here to avoid doing it redundantly */
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

		// High quality color rendering is ON.
		g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

		// Text antialiasing is ON.
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);


		if (!isUsedForPreviews()) {
			// We need to control composite ourselves for previews...
			g2.setComposite(AlphaComposite.Src);
		}
	}

	@Override
	public JDialog getModifyDialog() {
		return null;
	}

	// Protected methods
	protected void updateAnnotationAttributes() {
		if (!usedForPreviews) {
			cyAnnotator.addAnnotation(this);
			contentChanged();
		}
	}

	// Save the bounds (in node coordinates)
	@Override
	public void saveBounds() {
		initialBounds = ViewUtils.getNodeCoordinates(view, getBounds().getBounds2D());
	}

	@Override
	public Rectangle2D getInitialBounds() {
		return initialBounds;
	}

	// Save the offset in node coordinates
	@Override
	public void setOffset(Point2D offset) {
		if (offset == null) {
			this.offset = null;
			return;
		}

		Point2D mouse = ViewUtils.getNodeCoordinates(view, offset.getX(), offset.getY());
		Point2D current = ViewUtils.getNodeCoordinates(view, getLocation().getX(), getLocation().getY());

		this.offset = new Point2D.Double(mouse.getX()-current.getX(), mouse.getY()-current.getY());
	}

	@Override
	public Point2D getOffset() {
		return offset;
	}

	@Override
	public void contentChanged() {
		if (view == null)
			return;
		
		final ContentChangeListener lis = view.getContentChangeListener();
		
		if (lis != null)
			lis.contentChanged();
	}

	/**
	 * Adjust the the size to correspond to the aspect ratio of the
	 * current annotation.  This should be overloaded by annotations that
	 * have an aspect ratio (e.g. Shape, Image, etc.)
	 */
	public Dimension adjustAspectRatio(Dimension d) {
		return d;
	}

	public boolean inCanvas(ArbitraryGraphicsCanvas cnvs) {
		for (Component c: cnvs.getComponents()) {
			if (c == this) return true;
		}
		return false;
	}

}
