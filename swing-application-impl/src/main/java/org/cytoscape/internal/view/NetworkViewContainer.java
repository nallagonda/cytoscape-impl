package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static org.cytoscape.internal.util.ViewUtil.styleToolBarButton;
import static org.cytoscape.util.swing.IconManager.ICON_EXTERNAL_LINK_SQUARE;
import static org.cytoscape.util.swing.IconManager.ICON_EYE_SLASH;
import static org.cytoscape.util.swing.IconManager.ICON_LOCATION_ARROW;
import static org.cytoscape.util.swing.IconManager.ICON_TH;
import static org.cytoscape.util.swing.IconManager.ICON_THUMB_TACK;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.RootPaneContainer;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicInternalFrameUI;

import org.cytoscape.internal.util.ViewUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;

@SuppressWarnings("serial")
public class NetworkViewContainer extends JComponent implements RootPaneContainer {
	
	private final CyNetworkView networkView;
	private final RenderingEngineFactory<CyNetwork> engineFactory;
	private final RenderingEngine<CyNetwork> renderingEngine;

	/**
     * The <code>JRootPane</code> instance that manages the <code>contentPane</code>
     * and optional <code>menuBar</code> for this frame, as well as the <code>glassPane</code>.
     */
	protected JRootPane rootPane;
	
	private JDesktopPane viewDesktopPane;
	private JInternalFrame viewInternalFrame;
	
	private JPanel toolBar;
	private JButton gridModeButton;
	private JButton detachViewButton;
	private JButton reattachViewButton;
	private JLabel viewTitleLabel;
	private JTextField viewTitleTextField;
	private JLabel nodeEdgeSelectionLabel;
	private JLabel hiddenInfoLabel;
	private JButton birdsEyeViewButton;
	private BirdsEyeViewPanel birdsEyeViewPanel;
	
	final JSeparator sep1 = new JSeparator(JSeparator.VERTICAL);
	final JSeparator sep2 = new JSeparator(JSeparator.VERTICAL);
	final JSeparator sep3 = new JSeparator(JSeparator.VERTICAL);
	final JSeparator sep4 = new JSeparator(JSeparator.VERTICAL);
	
	/**
     * If true then calls to <code>add</code> and <code>setLayout</code>
     * will be forwarded to the <code>contentPane</code>. This is initially
     * false, but is set to true when the <code>NetworkViewContainer</code> is constructed.
     */
    private boolean rootPaneCheckingEnabled;
    
    private boolean detached;
    private boolean comparing;
	
	private final CyServiceRegistrar serviceRegistrar;

	public NetworkViewContainer(
			final CyNetworkView networkView,
			final RenderingEngineFactory<CyNetwork> engineFactory,
			final CyServiceRegistrar serviceRegistrar
	) {
		this.networkView = networkView;
		this.engineFactory = engineFactory;
		this.serviceRegistrar = serviceRegistrar;
		
		setName(ViewUtil.createUniqueKey(networkView));
		init();
		
		renderingEngine = engineFactory.createRenderingEngine(viewInternalFrame, networkView);
	}
	
	public boolean isDetached() {
		return detached;
	}
	
	public void setDetached(boolean detached) {
		this.detached = detached;
		
		if (detached)
			this.comparing = false;
	}
	
	public boolean isComparing() {
		return comparing;
	}
	
	public void setComparing(boolean comparing) {
		this.comparing = comparing;
		
		if (comparing)
			this.detached = false;
	}
	
	public void update() {
		updateTollBar();
		
		if (getBirdsEyeViewPanel().isVisible())
			getBirdsEyeViewPanel().update();
	}
	
	protected void updateTollBar() {
		getGridModeButton().setVisible(!isDetached() && !isComparing());
		getDetachViewButton().setVisible(!isDetached() && !isComparing());
		getReattachViewButton().setVisible(isDetached());
		getNodeEdgeSelectionLabel().setVisible(!isComparing());
		sep1.setVisible(!isComparing());
		sep2.setVisible(!isComparing());
		
		final CyNetworkView view = getNetworkView();
		getViewTitleLabel().setText(view != null ? ViewUtil.getTitle(view) : "");
		
		if (getNodeEdgeSelectionLabel().isVisible()) {
			if (view != null) {
				final int nodes = view.getModel().getNodeCount();
				final int edges = view.getModel().getEdgeCount();
				final int selNodes = view.getModel().getDefaultNodeTable().countMatchingRows(CyNetwork.SELECTED,
						Boolean.TRUE);
				final int selEdges = view.getModel().getDefaultEdgeTable().countMatchingRows(CyNetwork.SELECTED,
						Boolean.TRUE);

				String text = "Selected: " +
						selNodes + "/" + nodes + " node" + (nodes == 1 ? "" : "s") + ", " +
						selEdges + "/" + edges + " edge" + (edges == 1 ? "" : "s");
				
				getNodeEdgeSelectionLabel().setText(text);
			} else {
				getNodeEdgeSelectionLabel().setText("");
			}
		}
		
		if (getHiddenInfoLabel().isVisible()) {
			final int nodes = ViewUtil.getHiddenNodeCount(view);
			final int edges = ViewUtil.getHiddenEdgeCount(view);
			
			String text = "<html>";
			
			if (nodes > 0 || edges > 0) {
				if (nodes > 0)
					text += ( "<b>" + nodes + "</b> hidden node" + (nodes > 1 ? "s" : "") );
				if (edges > 0)
					text += (
							(nodes > 0 ? "<br>" : "") + 
							"<b>" + edges + "</b> hidden edge" + (edges > 1 ? "s" : "")
					);
			} else {
				text += "No hidden nodes or edges";
			}
			
			text += "</html>";
			
			getHiddenInfoLabel().setForeground(nodes > 0 || edges > 0 ?
					LookAndFeelUtil.getWarnColor() : UIManager.getColor("Separator.foreground"));
			getHiddenInfoLabel().setToolTipText(text);
		}
		
		updateBirdsEyeButton();
		
		getToolBar().updateUI();
	}
	
	private void updateBirdsEyeButton() {
		final boolean bevVisible = getBirdsEyeViewPanel().isVisible();
		getBirdsEyeViewButton().setToolTipText((bevVisible ? "Hide" : "Show") + " Navigator");
		getBirdsEyeViewButton().setForeground(UIManager.getColor(bevVisible ? "Focus.color" : "Button.foreground"));
	}
	
	public void dispose() {
		getRootPane().getLayeredPane().removeAll();
		getRootPane().getContentPane().removeAll();
		
		getBirdsEyeViewPanel().dispose();
	}
	
	@Override
	public JRootPane getRootPane() {
        return rootPane;
    }
	
	protected void setRootPane(JRootPane root) {
		if (rootPane != null)
			remove(rootPane);
		
		JRootPane oldValue = getRootPane();
		rootPane = root;
		
		if (rootPane != null) {
			boolean checkingEnabled = isRootPaneCheckingEnabled();
			
			try {
				setRootPaneCheckingEnabled(false);
				add(rootPane, BorderLayout.CENTER);
			} finally {
				setRootPaneCheckingEnabled(checkingEnabled);
			}
		}
		
		firePropertyChange("rootPane", oldValue, root);
    }
	
	@Override
	public Container getContentPane() {
        return getRootPane().getContentPane();
    }
	
	@Override
	public void setContentPane(final Container c) {
		Container oldValue = getContentPane();
        getRootPane().setContentPane(c);
        firePropertyChange("contentPane", oldValue, c);
	}
	
	@Override
	public JLayeredPane getLayeredPane() {
		return getRootPane().getLayeredPane();
	}
	
	@Override
    public void setLayeredPane(JLayeredPane layered) {
        final JLayeredPane oldValue = getLayeredPane();
        getRootPane().setLayeredPane(layered);
        firePropertyChange("layeredPane", oldValue, layered);
    }
	
	@Override
	public Component getGlassPane() {
		return getRootPane().getGlassPane();
	}
	
	@Override
    public void setGlassPane(Component glass) {
        Component oldValue = getGlassPane();
        getRootPane().setGlassPane(glass);
        firePropertyChange("glassPane", oldValue, glass);
    }
	
    /**
     * Removes the specified component from the container. If
     * <code>comp</code> is not the <code>rootPane</code>, this will forward
     * the call to the <code>contentPane</code>. This will do nothing if
     * <code>comp</code> is not a child of the <code>JFrame</code> or <code>contentPane</code>.
     */
	@Override
	public void remove(Component comp) {
		final int oldCount = getComponentCount();
		super.remove(comp);
		
		if (oldCount == getComponentCount())
			getContentPane().remove(comp);
	}

    /**
     * Overridden to conditionally forward the call to the <code>contentPane</code>.
     * Refer to {@link javax.swing.RootPaneContainer} for more information.
     */
	@Override
	public void setLayout(LayoutManager manager) {
		if (isRootPaneCheckingEnabled())
			getContentPane().setLayout(manager);
		else
			super.setLayout(manager);
	}
	
	/**
     * This method is overridden to conditionally forward calls to the <code>contentPane</code>.
     * By default, children are added to the <code>contentPane</code> instead
     * of the frame, refer to {@link javax.swing.RootPaneContainer} for details.
     */
	@Override
	protected void addImpl(Component comp, Object constraints, int index) {
		if (isRootPaneCheckingEnabled())
			getContentPane().add(comp, constraints, index);
		else
			super.addImpl(comp, constraints, index);
	}
	
	private void init() {
		final JRootPane rp = new JRootPane();
		final JPanel glassPane = new JPanel();
		
		{
			final GroupLayout layout = new GroupLayout(glassPane);
			glassPane.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGap(0, 0, Short.MAX_VALUE)
					.addComponent(getBirdsEyeViewPanel(), 10, 200, 200)
					.addGap(1)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGap(0, 0, Short.MAX_VALUE)
					.addComponent(getBirdsEyeViewPanel(), 10, 200, 200)
					.addGap(getToolBar().getPreferredSize().height + 1)
			);
		}
		
		rp.setGlassPane(glassPane);
		glassPane.setOpaque(false);
		glassPane.setVisible(true);
		
		setRootPane(rp);
		setLayout(new BorderLayout());
		add(getRootPane(), BorderLayout.CENTER);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(getViewDesktopPane(), BorderLayout.CENTER);
		getContentPane().add(getToolBar(), BorderLayout.SOUTH);
		
		getViewInternalFrame().setVisible(true);
		
		try {
			getViewInternalFrame().setMaximum(true);
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
	}
	
	protected RenderingEngine<CyNetwork> getRenderingEngine() {
		return renderingEngine;
	}
	
	protected CyNetworkView getNetworkView() {
		return networkView;
	}
	
	private boolean isRootPaneCheckingEnabled() {
		return rootPaneCheckingEnabled;
	}

	private void setRootPaneCheckingEnabled(boolean enabled) {
		rootPaneCheckingEnabled = enabled;
	}
	
	private JDesktopPane getViewDesktopPane() {
		if (viewDesktopPane == null) {
			viewDesktopPane = new JDesktopPane();
			viewDesktopPane.add(getViewInternalFrame());
		}
		
		return viewDesktopPane;
	}
	
	private JInternalFrame getViewInternalFrame() {
		if (viewInternalFrame == null) {
			viewInternalFrame = new JInternalFrame("");
			viewInternalFrame.setIconifiable(false);
			viewInternalFrame.setClosable(false);
			
			// Remove border and title bar
			viewInternalFrame.setBorder(null);
			((BasicInternalFrameUI) viewInternalFrame.getUI()).setNorthPane(null);
		}
		
		return viewInternalFrame;
	}
	
	JPanel getToolBar() {
		if (toolBar == null) {
			toolBar = new JPanel();
			toolBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));
			
			final GroupLayout layout = new GroupLayout(toolBar);
			toolBar.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addContainerGap()
					.addComponent(getGridModeButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getDetachViewButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getReattachViewButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(sep1, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getViewTitleLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getViewTitleTextField(), 100, 260, 320)
					.addGap(0, 10, Short.MAX_VALUE)
					.addComponent(sep2, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getNodeEdgeSelectionLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(sep3, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getHiddenInfoLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(sep4, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getBirdsEyeViewButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addContainerGap()
			);
			layout.setVerticalGroup(layout.createParallelGroup(CENTER, true)
					.addComponent(getGridModeButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getDetachViewButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getReattachViewButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(sep1, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getViewTitleLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getViewTitleTextField(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(sep2, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getNodeEdgeSelectionLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(sep3, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getHiddenInfoLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(sep4, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getBirdsEyeViewButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return toolBar;
	}
	
	JButton getGridModeButton() {
		if (gridModeButton == null) {
			gridModeButton = new JButton(ICON_TH);
			gridModeButton.setToolTipText("Show Thumbnails");
			styleToolBarButton(gridModeButton, serviceRegistrar.getService(IconManager.class).getIconFont(22.0f));
		}
		
		return gridModeButton;
	}
	
	JButton getDetachViewButton() {
		if (detachViewButton == null) {
			detachViewButton = new JButton(ICON_EXTERNAL_LINK_SQUARE);
			detachViewButton.setToolTipText("Detach Network View");
			styleToolBarButton(detachViewButton, serviceRegistrar.getService(IconManager.class).getIconFont(22.0f));
		}
		
		return detachViewButton;
	}
	
	JButton getReattachViewButton() {
		if (reattachViewButton == null) {
			reattachViewButton = new JButton(ICON_THUMB_TACK);
			reattachViewButton.setToolTipText("Reattach Network View");
			styleToolBarButton(reattachViewButton, serviceRegistrar.getService(IconManager.class).getIconFont(14.0f));
		}
		
		return reattachViewButton;
	}
	
	JLabel getViewTitleLabel() {
		if (viewTitleLabel == null) {
			viewTitleLabel = new JLabel();
			viewTitleLabel.setToolTipText("Click to change the title...");
			viewTitleLabel.setFont(viewTitleLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			viewTitleLabel.setMinimumSize(new Dimension(viewTitleLabel.getPreferredSize().width,
					getViewTitleTextField().getPreferredSize().height));
			viewTitleLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					showViewTitleEditor();
				}
				@Override
				public void mouseEntered(MouseEvent e) {
					viewTitleLabel.setForeground(UIManager.getColor("Focus.color"));
				}
				@Override
				public void mouseExited(MouseEvent e) {
					viewTitleLabel.setForeground(UIManager.getColor("Label.foreground"));
				}
			});
		}
		
		return viewTitleLabel;
	}
	
	JTextField getViewTitleTextField() {
		if (viewTitleTextField == null) {
			viewTitleTextField = new JTextField();
			viewTitleTextField.putClientProperty("JComponent.sizeVariant", "mini"); // Aqua (Mac OS X) only
			viewTitleTextField.setVisible(false);
		}
		
		return viewTitleTextField;
	}
	
	private JLabel getNodeEdgeSelectionLabel() {
		if (nodeEdgeSelectionLabel == null) {
			nodeEdgeSelectionLabel = new JLabel();
			nodeEdgeSelectionLabel.setFont(
					nodeEdgeSelectionLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
		}
		
		return nodeEdgeSelectionLabel;
	}
	
	private JLabel getHiddenInfoLabel() {
		if (hiddenInfoLabel == null) {
			hiddenInfoLabel = new JLabel(ICON_EYE_SLASH);
			hiddenInfoLabel.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(22.0f));
		}
		
		return hiddenInfoLabel;
	}
	
	private JButton getBirdsEyeViewButton() {
		if (birdsEyeViewButton == null) {
			birdsEyeViewButton = new JButton(ICON_LOCATION_ARROW);
			styleToolBarButton(birdsEyeViewButton, serviceRegistrar.getService(IconManager.class).getIconFont(22.0f));
			
			birdsEyeViewButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getBirdsEyeViewPanel().setVisible(!getBirdsEyeViewPanel().isVisible());
					updateBirdsEyeButton();
					
					if (getBirdsEyeViewPanel().isVisible())
						getBirdsEyeViewPanel().update();
				}
			});
		}
		
		return birdsEyeViewButton;
	}
	
	private BirdsEyeViewPanel getBirdsEyeViewPanel() {
		if (birdsEyeViewPanel == null) {
			birdsEyeViewPanel = new BirdsEyeViewPanel(getNetworkView(), serviceRegistrar);
			birdsEyeViewPanel.setVisible(true);
		}
		
		return birdsEyeViewPanel;
	}
	
	private void showViewTitleEditor() {
		getViewTitleTextField().setText(getViewTitleLabel().getText());
		getViewTitleLabel().setVisible(false);
		getViewTitleTextField().setVisible(true);
		getViewTitleTextField().requestFocusInWindow();
	}
	
	@Override
	public String toString() {
		return networkView.toString();
	}
}
