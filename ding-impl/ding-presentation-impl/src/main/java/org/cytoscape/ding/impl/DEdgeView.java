/*
 Copyright (c) 2006, 2007, 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.ding.impl;


import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.List;

import org.cytoscape.ding.DArrowShape;
import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.EdgeView;
import org.cytoscape.ding.GraphView;
import org.cytoscape.ding.Label;
import org.cytoscape.graph.render.immed.EdgeAnchors;
import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.Handle;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.presentation.property.values.LineType;


/**
 * Ding implementation of Edge View.
 */
public class DEdgeView extends AbstractDViewModel<CyEdge> implements EdgeView, Label, EdgeAnchors {

	// Parent network view.  This view exists only in this network view.
	private final DGraphView graphView;
	private final HandleFactory handleFacgtory;

	// Since Fonts are created from size and font face, we need this local value.
	private Integer fontSize;
	private LineType lineType;
	private boolean selected;
	
	DEdgeView(final DGraphView graphView, final CyEdge model, final HandleFactory handleFactory, final VisualLexicon lexicon) {
		super(model, lexicon);

		if (graphView == null)
			throw new IllegalArgumentException("Constructor needs its parent DGraphView.");

		this.handleFacgtory = handleFactory;
		this.graphView = graphView;
		this.selected = false;
		this.fontSize = DVisualLexicon.EDGE_LABEL_FONT_SIZE.getDefault();
	}

	@Override
	public CyEdge getCyEdge() {
		return model;
	}


	@Override
	public GraphView getGraphView() {
		return graphView;
	}


	@Override
	public void setStrokeWidth(final float width) {
		synchronized (graphView.m_lock) {
			graphView.m_edgeDetails.overrideSegmentThickness(model, width);
			graphView.m_contentChanged = true;
		}
	}


	@Override
	public void setStroke(Stroke stroke) {
		synchronized (graphView.m_lock) {
			graphView.m_edgeDetails.overrideSegmentStroke(model, stroke);
			graphView.m_contentChanged = true;
		}
	}


	@Override
	public void setLineCurved(int lineType) {
		if ((lineType == EdgeView.CURVED_LINES) || (lineType == EdgeView.STRAIGHT_LINES)) {
			synchronized (graphView.m_lock) {
				graphView.m_edgeDetails.overrideLineCurved(model, lineType);
				graphView.m_contentChanged = true;
			}
		} else
			throw new IllegalArgumentException("unrecognized line type");
	}


	@Override
	public void setUnselectedPaint(final Paint paint) {
		synchronized (graphView.m_lock) {
			if (paint == null)
				throw new NullPointerException("paint is null");
			
			final Paint transpColor = getTransparentColor(paint, graphView.m_edgeDetails.getTransparency(model));
			
			if (!isSelected()) {
				graphView.m_edgeDetails.setUnselectedPaint(model, transpColor);
				graphView.m_contentChanged = true;
			}
		}
	}


	@Override
	public void setSelectedPaint(final Paint paint) {
		synchronized (graphView.m_lock) {
			if (paint == null)
				throw new NullPointerException("paint is null");

			final Paint transpColor = getTransparentColor(paint, graphView.m_edgeDetails.getTransparency(model));
			
			if (isSelected()) {
				graphView.m_edgeDetails.setSelectedPaint(model, transpColor);
				graphView.m_contentChanged = true;
			}
		}
	}

	@Override
	public void setSourceEdgeEndSelectedPaint(Paint paint) {
		synchronized (graphView.m_lock) {
			if (paint == null)
				throw new NullPointerException("paint is null");

			final Paint transpColor = getTransparentColor(paint, graphView.m_edgeDetails.getTransparency(model));
			if (isSelected()) {
				graphView.m_edgeDetails.overrideSourceArrowSelectedPaint(model, transpColor);
				graphView.m_contentChanged = true;
			}
		}
	}

	@Override
	public void setTargetEdgeEndSelectedPaint(Paint paint) {
		synchronized (graphView.m_lock) {
			if (paint == null)
				throw new NullPointerException("paint is null");

			final Paint transpColor = getTransparentColor(paint, graphView.m_edgeDetails.getTransparency(model));
			if (isSelected()) {
				graphView.m_edgeDetails.overrideTargetArrowSelectedPaint(model, transpColor);
				graphView.m_contentChanged = true;
			}
		}
	}

	@Override
	public void setSourceEdgeEndPaint(final Paint paint) {
		synchronized (graphView.m_lock) {
			if (paint == null)
				throw new NullPointerException("paint is null");

			final Paint transpColor = getTransparentColor(paint, graphView.m_edgeDetails.getTransparency(model));
			
			if (!isSelected()) {
				graphView.m_edgeDetails.overrideSourceArrowPaint(model, transpColor);
				graphView.m_contentChanged = true;
			}
		}
	}

	@Override
	public void setTargetEdgeEndPaint(Paint paint) {
		synchronized (graphView.m_lock) {
			if (paint == null)
				throw new NullPointerException("paint is null");

			final Paint transpColor = getTransparentColor(paint, graphView.m_edgeDetails.getTransparency(model));
			graphView.m_edgeDetails.overrideTargetArrowPaint(model, transpColor);
			
			if (!isSelected()) {
				graphView.m_contentChanged = true;
			}
		}
	}

	private final void select() {
		final boolean somethingChanged;

		synchronized (graphView.m_lock) {
			somethingChanged = selectInternal(false);

			if (somethingChanged)
				graphView.m_contentChanged = true;
		}
	}

	// Should synchronize around m_view.m_lock.
	boolean selectInternal(boolean selectAnchors) {
		if (selected)
			return false;

		selected = true;
		graphView.m_edgeDetails.select(model);		
		graphView.m_selectedEdges.insert(model.getSUID());

		List<Handle> handles = graphView.m_edgeDetails.getBend(model).getAllHandles();
		for (int j = 0; j < handles.size(); j++) {
			final Handle handle = handles.get(j);
			final Point2D newPoint = handle.calculateHandleLocation(graphView.getViewModel(),this);
			final double x = newPoint.getX();
			final double y = newPoint.getY();
			final double halfSize = graphView.getAnchorSize() / 2.0;
			
			graphView.m_spacialA.insert((model.getSUID() << 6) | j,
					(float) (x - halfSize), (float) (y - halfSize),
					(float) (x + halfSize), (float) (y + halfSize));

			if (selectAnchors)
				graphView.m_selectedAnchors.insert((model.getSUID() << 6) | j);
		}
		return true;
	}

	public void unselect() {
		final boolean somethingChanged;

		synchronized (graphView.m_lock) {
			somethingChanged = unselectInternal();

			if (somethingChanged)
				graphView.m_contentChanged = true;
		}
	}

	// Should synchronize around m_view.m_lock.
	boolean unselectInternal() {
		if (!selected)
			return false;

		selected = false;
		graphView.m_edgeDetails.unselect(model);
		graphView.m_selectedEdges.delete(model.getSUID());

		final int numHandles = graphView.m_edgeDetails.getBend(model).getAllHandles().size();
		for (int j = 0; j < numHandles; j++) {
			graphView.m_selectedAnchors.delete((model.getSUID() << 6) | j);
			graphView.m_spacialA.delete((model.getSUID() << 6) | j);
		}
		return true;
	}

	@Override
	public boolean setSelected(boolean state) {
		if (state)
			select();
		else
			unselect();

		return true;
	}

	@Override
	public boolean isSelected() {
		return selected;
	}

	
	@Override
	public void setSourceEdgeEnd(final int rendererTypeID) {
		synchronized (graphView.m_lock) {
			graphView.m_edgeDetails.overrideSourceArrow(model, (byte) rendererTypeID);
		}

		graphView.m_contentChanged = true;
	}

	@Override
	public void setTargetEdgeEnd(final int rendererTypeID) {
		synchronized (graphView.m_lock) {
			graphView.m_edgeDetails.overrideTargetArrow(model, (byte) rendererTypeID);
		}

		graphView.m_contentChanged = true;

	}


	@Override
	public void setToolTip(String tip) {
		graphView.m_edgeDetails.m_edgeTooltips.put(model, tip);
	}


	@Override
	public Paint getTextPaint() {
		synchronized (graphView.m_lock) {
			return graphView.m_edgeDetails.getLabelPaint(model, 0);
		}
	}

	@Override
	public void setTextPaint(Paint textPaint) {
		synchronized (graphView.m_lock) {
			graphView.m_edgeDetails.overrideLabelPaint(model, 0, textPaint);
			graphView.m_contentChanged = true;
		}
	}

	@Override
	public String getText() {
		synchronized (graphView.m_lock) {
			return graphView.m_edgeDetails.getLabelText(model, 0);
		}
	}

	@Override
	public void setText(final String text) {
		synchronized (graphView.m_lock) {
			graphView.m_edgeDetails.overrideLabelText(model, 0, text);

			if ("".equals(graphView.m_edgeDetails.getLabelText(model, 0)))
				graphView.m_edgeDetails.overrideLabelCount(model, 0); // TODO is this correct?
			else
				graphView.m_edgeDetails.overrideLabelCount(model, 1);

			graphView.m_contentChanged = true;
		}
	}

	@Override
	public Font getFont() {
		synchronized (graphView.m_lock) {
			return graphView.m_edgeDetails.getLabelFont(model, 0);
		}
	}
	
	@Override
	public void setFont(final Font font) {
		synchronized (graphView.m_lock) {
			graphView.m_edgeDetails.overrideLabelFont(model, 0, font);
			graphView.m_contentChanged = true;
		}
	}

	protected final void moveHandleInternal(final int inx, double x, double y) {
		final Bend bend = graphView.m_edgeDetails.getBend(model);
		final HandleImpl handle = (HandleImpl) bend.getAllHandles().get(inx);
		handle.defineHandle(graphView.getViewModel(), this, x, y);

		if (graphView.m_spacialA.delete((model.getSUID() << 6) | inx))
			graphView.m_spacialA.insert((model.getSUID() << 6) | inx,
					(float) (x - (graphView.getAnchorSize() / 2.0d)),
					(float) (y - (graphView.getAnchorSize() / 2.0d)),
					(float) (x + (graphView.getAnchorSize() / 2.0d)),
					(float) (y + (graphView.getAnchorSize() / 2.0d)));
	}

	/**
	 * Add a new handle and returns its index.
	 * 
	 * @param pt location of handle
	 * @return new handle index.
	 */
	protected int addHandlePoint(final Point2D pt) {
		synchronized (graphView.m_lock) {
			
			// Obtain existing Bend object
			final Bend bend = graphView.m_edgeDetails.getBend(model, true);
			
			if (bend.getAllHandles().size() == 0) {
				// anchors object is empty. Add first handle.
				addHandleInternal(0, pt);
				// Index of this handle, which is first (0)
				return 0;
			}

			final Point2D sourcePt = graphView.getDNodeView(getCyEdge().getSource()).getOffset();
			final Point2D targetPt = graphView.getDNodeView(getCyEdge().getTarget()).getOffset();
			final Handle firstHandle = bend.getAllHandles().get(0); 
			final Point2D point = firstHandle.calculateHandleLocation(graphView.getViewModel(),this);
			double bestDist = (pt.distance(sourcePt) + pt.distance(point)) - sourcePt.distance(point);
			int bestInx = 0;

			for (int i = 1; i < bend.getAllHandles().size(); i++) {
				final Handle handle1 = bend.getAllHandles().get(i);
				final Handle handle2 = bend.getAllHandles().get(i-1);
				final Point2D point1 = handle1.calculateHandleLocation(graphView.getViewModel(),this);
				final Point2D point2 = handle2.calculateHandleLocation(graphView.getViewModel(),this);

				final double distCand = (pt.distance(point2) + pt.distance(point1)) - point1.distance(point2);

				if (distCand < bestDist) {
					bestDist = distCand;
					bestInx = i;
				}
			}

			final int lastIndex = bend.getAllHandles().size() - 1;
			final Handle lastHandle = bend.getAllHandles().get(lastIndex);
			final Point2D lastPoint = lastHandle.calculateHandleLocation(graphView.getViewModel(),this);
			
			final double lastCand = (pt.distance(targetPt) + pt.distance(lastPoint)) - targetPt.distance(lastPoint);

			if (lastCand < bestDist) {
				bestDist = lastCand;
				bestInx = bend.getAllHandles().size();
			}

			addHandleInternal(bestInx, pt);

			return bestInx;
		}
	}
	
	/**
	 * Insert a new handle to bend object.
	 * 
	 * @param insertInx
	 * @param handleLocation
	 */
	private void addHandleInternal(final int insertInx, final Point2D handleLocation) {
		synchronized (graphView.m_lock) {
			final Bend bend = graphView.m_edgeDetails.getBend(model);			
			final Handle handle = handleFacgtory.createHandle(graphView, this, handleLocation.getX(), handleLocation.getY());
			bend.insertHandleAt(insertInx, handle);

			if (selected) {
				for (int j = bend.getAllHandles().size() - 1; j > insertInx; j--) {
					graphView.m_spacialA.exists((model.getSUID() << 6) | (j - 1),
							graphView.m_extentsBuff, 0);
					graphView.m_spacialA.delete((model.getSUID() << 6) | (j - 1));
					graphView.m_spacialA.insert((model.getSUID() << 6) | j,
							graphView.m_extentsBuff[0], graphView.m_extentsBuff[1],
							graphView.m_extentsBuff[2], graphView.m_extentsBuff[3]);

					if (graphView.m_selectedAnchors.delete((model.getSUID() << 6) | (j - 1)))
						graphView.m_selectedAnchors.insert((model.getSUID() << 6) | j);
				}
				
				graphView.m_spacialA.insert((model.getSUID() << 6) | insertInx,
						(float) (handleLocation.getX() - (graphView.getAnchorSize() / 2.0d)),
						(float) (handleLocation.getY() - (graphView.getAnchorSize() / 2.0d)),
						(float) (handleLocation.getX() + (graphView.getAnchorSize() / 2.0d)),
						(float) (handleLocation.getY() + (graphView.getAnchorSize() / 2.0d)));
			}

			graphView.m_contentChanged = true;
		}
	}

	void removeHandle(int inx) {
		synchronized (graphView.m_lock) {
			final Bend bend = graphView.m_edgeDetails.getBend(model);
			bend.removeHandleAt(inx);
			//m_anchors.remove(inx);

			if (selected) {
				graphView.m_spacialA.delete((model.getSUID() << 6) | inx);
				graphView.m_selectedAnchors.delete((model.getSUID() << 6) | inx);

				for (int j = inx; j < bend.getAllHandles().size(); j++) {
					graphView.m_spacialA.exists((model.getSUID() << 6) | (j + 1),
							graphView.m_extentsBuff, 0);
					graphView.m_spacialA.delete((model.getSUID() << 6) | (j + 1));
					graphView.m_spacialA.insert((model.getSUID() << 6) | j,
							graphView.m_extentsBuff[0], graphView.m_extentsBuff[1],
							graphView.m_extentsBuff[2], graphView.m_extentsBuff[3]);

					if (graphView.m_selectedAnchors.delete((model.getSUID() << 6) | (j + 1)))
						graphView.m_selectedAnchors.insert((model.getSUID() << 6) | j);
				}
			}
			graphView.m_contentChanged = true;
		}
	}

	// Interface org.cytoscape.graph.render.immed.EdgeAnchors:
	@Override
	public int numAnchors() {
		final Bend bend; 
		if(isValueLocked(DVisualLexicon.EDGE_BEND))
			bend = this.getVisualProperty(DVisualLexicon.EDGE_BEND);
		else
			bend = graphView.m_edgeDetails.getBend(model);
		
		final int numHandles = bend.getAllHandles().size();
		
		if (numHandles == 0)
			return 0;
		
		if (graphView.m_edgeDetails.getLineCurved(model) == EdgeView.CURVED_LINES)
			return numHandles;
		else
			return 2 * numHandles;
	}

	/**
	 * Actual method to be used in the Graph Renderer.
	 */
	@Override
	public void getAnchor(int anchorIndex, float[] anchorArr, int offset) {
		final Bend bend; 
		if(isValueLocked(DVisualLexicon.EDGE_BEND))
			bend = this.getVisualProperty(DVisualLexicon.EDGE_BEND);
		else
			bend = graphView.m_edgeDetails.getBend(model);
		
		final Handle handle;
		if (graphView.m_edgeDetails.getLineCurved(model) == EdgeView.CURVED_LINES)
			handle = bend.getAllHandles().get(anchorIndex);
		else
			handle = bend.getAllHandles().get(anchorIndex/2);

		final Point2D newPoint = handle.calculateHandleLocation(graphView.getViewModel(),this);
		anchorArr[offset] = (float) newPoint.getX();
		anchorArr[offset + 1] = (float) newPoint.getY();
	}

	public void setLabelWidth(double width) {
		synchronized (graphView.m_lock) {
			graphView.m_edgeDetails.overrideLabelWidth(model, width);
			graphView.m_contentChanged = true;
		}
	}


	@Override
	public void setTransparency(final int trans) {
		synchronized (graphView.m_lock) {
			Integer transparency;
			if (trans < 0 || trans > 255) {
				// If out of range, use default value.
				transparency = BasicVisualLexicon.EDGE_TRANSPARENCY.getDefault();
			} else {
				transparency = trans;
			}
			graphView.m_edgeDetails.overrideTransparency(model, transparency);
			
			setUnselectedPaint(graphView.m_edgeDetails.getUnselectedPaint(model));
			setSelectedPaint(graphView.m_edgeDetails.getSelectedPaint(model));
			setTargetEdgeEndPaint(graphView.m_edgeDetails.getTargetArrowPaint(model));
			setSourceEdgeEndPaint(graphView.m_edgeDetails.getSourceArrowPaint(model));
			
			graphView.m_contentChanged = true;
		}
	}
	
	@Override
	public void setLabelTransparency(final int trans) {
		synchronized (graphView.m_lock) {
			Integer transparency;
			if (trans < 0 || trans > 255) {
				// If out of range, use default value.
				transparency = BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY.getDefault();
			} else {
				transparency = trans;
			}
			
			graphView.m_edgeDetails.overrideLabelTransparency(model, transparency);
			setTextPaint(graphView.m_edgeDetails.getLabelPaint(model, 0));
			
			graphView.m_contentChanged = true;
		}
	}
	
	@Override
	public void setBend(final Bend bend) {
		synchronized (graphView.m_lock) {
			graphView.m_edgeDetails.m_edgeBends.put(model, bend);
		}
		graphView.m_contentChanged = true;
	}
	
	@Override
	public Bend getBend() {
		synchronized (graphView.m_lock) {
			return graphView.m_edgeDetails.getBend(model);
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void clearValueLock(final VisualProperty<?> vp) {
		final boolean isDefault = !visualProperties.containsKey(vp);
		super.clearValueLock(vp);
		
		// Reset to the visual style default if visualProperties map doesn't contain this vp
		if (isDefault) {
			if (vp == BasicVisualLexicon.EDGE_VISIBLE)
				applyVisualProperty((VisualProperty) vp, vp.getDefault());
			else
				graphView.edgeViewDefaultSupport.setViewDefault((VisualProperty) vp,
						graphView.m_edgeDetails.getDefaultValue(vp));
		}
	}
	
	/**
	 * This method sets a mapped value.  NOT Defaults.
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected <T, V extends T> void applyVisualProperty(final VisualProperty<? extends T> vpOriginal, V value) {
		VisualProperty<?> vp = vpOriginal;
		
		// If value is null, simply use the VP's default value.
		if (value == null)
			value = (V) vp.getDefault();

		if (vp == DVisualLexicon.EDGE_STROKE_SELECTED_PAINT) {
			setSelectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT) {
			setUnselectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.EDGE_SELECTED_PAINT) {
			setSelectedPaint((Paint) value);
			setSourceEdgeEndSelectedPaint((Paint) value);
			setTargetEdgeEndSelectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.EDGE_UNSELECTED_PAINT) {
			setUnselectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.EDGE_WIDTH) {
			final float w = ((Number) value).floatValue();
			setStrokeWidth(w);
			setStroke(DLineType.getDLineType(lineType).getStroke(w));
		} else if (vp == DVisualLexicon.EDGE_LINE_TYPE) {
			lineType = (LineType) value;
			final Stroke newStroke = DLineType.getDLineType(lineType).getStroke(graphView.m_edgeDetails.getWidth(model));
			setStroke(newStroke);
		} else if (vp == DVisualLexicon.EDGE_TRANSPARENCY) {
			setTransparency(((Number) value).intValue());
		} else if (vp == DVisualLexicon.EDGE_LABEL_TRANSPARENCY) {
			final int labelTransparency = ((Number) value).intValue();
			setLabelTransparency(labelTransparency);
		} else if (vp == DVisualLexicon.EDGE_SOURCE_ARROW_SELECTED_PAINT) {
			setSourceEdgeEndSelectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.EDGE_TARGET_ARROW_SELECTED_PAINT) {
			setTargetEdgeEndSelectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.EDGE_SOURCE_ARROW_UNSELECTED_PAINT) {
			setSourceEdgeEndPaint((Paint) value);
		} else if (vp == DVisualLexicon.EDGE_TARGET_ARROW_UNSELECTED_PAINT) {
			setTargetEdgeEndPaint((Paint) value);
		} else if (vp == BasicVisualLexicon.EDGE_SELECTED) {
			setSelected((Boolean) value);
		} else if (vp == BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE) {
			final ArrowShape shape = (ArrowShape) value;
			final String shapeID = shape.getSerializableString();
			setTargetEdgeEnd(DArrowShape.parseArrowText(shapeID).getRendererTypeID());
		} else if (vp == BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE) {
			final ArrowShape shape = (ArrowShape) value;
			final String shapeID = shape.getSerializableString();
			setSourceEdgeEnd(DArrowShape.parseArrowText(shapeID).getRendererTypeID());
		} else if (vp == BasicVisualLexicon.EDGE_LABEL) {
			setText(value.toString());
		} else if (vp == DVisualLexicon.EDGE_TOOLTIP) {
			setToolTip(value.toString());
		} else if (vp == DVisualLexicon.EDGE_LABEL_FONT_FACE) {
			final Font newFont = ((Font) value).deriveFont(fontSize);
			setFont(newFont);
		} else if (vp == DVisualLexicon.EDGE_LABEL_FONT_SIZE) {
			fontSize = ((Number) value).intValue();
			final Font f = getFont();
			if (f != null)
				setFont(f.deriveFont(fontSize));
		} else if (vp == BasicVisualLexicon.EDGE_LABEL_COLOR) {
			setTextPaint((Paint) value);
		} else if (vp == BasicVisualLexicon.EDGE_VISIBLE) {
			if (((Boolean) value).booleanValue())
				graphView.showGraphObject(this);
			else
				graphView.hideGraphObject(this);
		} else if (vp == DVisualLexicon.EDGE_CURVED) {
			final Boolean curved = (Boolean) value;
			if (curved)
				setLineCurved(EdgeView.CURVED_LINES);
			else
				setLineCurved(EdgeView.STRAIGHT_LINES);
		} else if (vp == DVisualLexicon.EDGE_BEND) {
			setBend((Bend) value);
		}
	}

	@Override
	protected <T, V extends T> V getDefaultValue(VisualProperty<T> vp) {
		return graphView.m_edgeDetails.getDefaultValue(vp);
	}
}
