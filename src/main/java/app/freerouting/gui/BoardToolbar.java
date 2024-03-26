package app.freerouting.gui;

import app.freerouting.board.RoutingBoard;
import app.freerouting.board.Unit;
import app.freerouting.interactive.DragMenuState;
import app.freerouting.interactive.InteractiveActionThread;
import app.freerouting.interactive.InteractiveState;
import app.freerouting.interactive.RouteMenuState;
import app.freerouting.interactive.SelectMenuState;

import app.freerouting.management.FRAnalytics;
import app.freerouting.management.TextManager;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.border.BevelBorder;

/** Implements the toolbar panel of the board frame. */
class BoardToolbar extends JPanel {
  private final float ICON_FONT_SIZE = 22;
  final JComboBox<Unit> toolbar_unit_combo_box;
  private final BoardFrame board_frame;
  private final JToggleButton toolbar_select_button;
  private final JToggleButton toolbar_route_button;
  private final JToggleButton toolbar_drag_button;
  /** Creates a new instance of BoardToolbarPanel */
  BoardToolbar(BoardFrame p_board_frame, boolean p_disable_select_mode) {
    this.board_frame = p_board_frame;

    TextManager tm = new TextManager(this.getClass(), p_board_frame.get_locale());

    this.setLayout(new BorderLayout());

    // create the left toolbar

    final JToolBar left_toolbar = new JToolBar();

    final ButtonGroup toolbar_button_group = new ButtonGroup();
    this.toolbar_select_button = new JToggleButton();
    this.toolbar_route_button = new JToggleButton();
    this.toolbar_drag_button = new JToggleButton();
    final JLabel jLabel1 = new JLabel();

    left_toolbar.setMaximumSize(new Dimension(1200, 30));

    if (!p_disable_select_mode) {
      toolbar_button_group.add(toolbar_select_button);
      toolbar_select_button.setSelected(true);
      tm.setText(toolbar_select_button, "select_button");
      toolbar_select_button.addActionListener(evt -> board_frame.board_panel.board_handling.set_select_menu_state());
      toolbar_select_button.addActionListener(evt -> FRAnalytics.buttonClicked("toolbar_select_button", toolbar_select_button.getText()));

      left_toolbar.add(toolbar_select_button);
    }

    toolbar_button_group.add(toolbar_route_button);
    if (p_disable_select_mode) {
      toolbar_route_button.setSelected(true);
    }
    tm.setText(toolbar_route_button, "route_button");
    toolbar_route_button.addActionListener(evt -> board_frame.board_panel.board_handling.set_route_menu_state());
    toolbar_route_button.addActionListener(evt -> FRAnalytics.buttonClicked("toolbar_route_button", toolbar_route_button.getText()));
    left_toolbar.add(toolbar_route_button);

    toolbar_button_group.add(toolbar_drag_button);
    tm.setText(toolbar_drag_button, "drag_button");
    toolbar_drag_button.addActionListener(evt -> board_frame.board_panel.board_handling.set_drag_menu_state());
    toolbar_drag_button.addActionListener(evt -> FRAnalytics.buttonClicked("toolbar_drag_button", toolbar_drag_button.getText()));
    left_toolbar.add(toolbar_drag_button);

    SegmentedButtons segmentedPanel = new SegmentedButtons(tm, "Mode", "select_button", "route_button", "drag_button");
    left_toolbar.add(segmentedPanel, BorderLayout.CENTER);


    jLabel1.setMaximumSize(new Dimension(30, 10));
    jLabel1.setMinimumSize(new Dimension(3, 10));
    jLabel1.setPreferredSize(new Dimension(30, 10));
    left_toolbar.add(jLabel1);

    this.add(left_toolbar, BorderLayout.WEST);

    // create the middle toolbar

    final JToolBar middle_toolbar = new JToolBar();

    // Add "Settings" button to the toolbar
    final JButton settings_button = new JButton();
    tm.setText(settings_button, "settings_button");
    settings_button.addActionListener(
        evt -> {
          board_frame.autoroute_parameter_window.setVisible(true);
        });
    settings_button.addActionListener(evt -> FRAnalytics.buttonClicked("settings_button", settings_button.getText()));
    middle_toolbar.add(settings_button);

    // Add "Autoroute" button to the toolbar
    final JButton toolbar_autoroute_button = new JButton();
    tm.setText(toolbar_autoroute_button, "autoroute_button");
    toolbar_autoroute_button.setDefaultCapable(true);
    Font currentFont = toolbar_autoroute_button.getFont();
    Font boldFont = new Font(currentFont.getFontName(), Font.BOLD, currentFont.getSize());
    toolbar_autoroute_button.setFont(boldFont);
    toolbar_autoroute_button.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
    // Set padding (top, left, bottom, right)
    toolbar_autoroute_button.setBorder(BorderFactory.createCompoundBorder(
        toolbar_autoroute_button.getBorder(),
        BorderFactory.createEmptyBorder(2, 5, 2, 5)
    ));
    toolbar_autoroute_button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    toolbar_autoroute_button.addActionListener(
        evt -> {
          InteractiveActionThread thread = board_frame.board_panel.board_handling.start_autorouter_and_route_optimizer();

          if (board_frame.board_panel.board_handling.autorouter_listener != null) {
            // Add the auto-router listener to save the design file when the auto-router is running
            thread.addListener(board_frame.board_panel.board_handling.autorouter_listener);
          }
        });
    toolbar_autoroute_button.addActionListener(evt -> FRAnalytics.buttonClicked("toolbar_autoroute_button", toolbar_autoroute_button.getText()));
    middle_toolbar.add(toolbar_autoroute_button);

    // Add "Delete All Tracks and Vias" button to the toolbar
    final JButton delete_all_tracks_button = new JButton();
    tm.setText(delete_all_tracks_button, "delete_all_tracks_button");
    delete_all_tracks_button.addActionListener(
        evt -> {
          RoutingBoard board = board_frame.board_panel.board_handling.get_routing_board();
          // delete all tracks and vias
          board.delete_all_tracks_and_vias();
          // update the board
          board_frame.board_panel.board_handling.update_routing_board(board);
          // create a deep copy of the routing board
          board = board_frame.board_panel.board_handling.deep_copy_routing_board();
          // update the board again
          board_frame.board_panel.board_handling.update_routing_board(board);
          // create ratsnest
          board_frame.board_panel.board_handling.create_ratsnest();
          // redraw the board
          board_frame.board_panel.board_handling.repaint();
        });
    delete_all_tracks_button.addActionListener(evt -> FRAnalytics.buttonClicked("delete_all_tracks_button", delete_all_tracks_button.getText()));
    middle_toolbar.add(delete_all_tracks_button);


    final JLabel separator_2 = new JLabel();
    separator_2.setMaximumSize(new Dimension(10, 10));
    separator_2.setPreferredSize(new Dimension(10, 10));
    separator_2.setRequestFocusEnabled(false);
    middle_toolbar.add(separator_2);

    final JButton toolbar_undo_button = new JButton();
    tm.setText(toolbar_undo_button, "undo_button");
    toolbar_undo_button.addActionListener(
        evt -> {
          board_frame.board_panel.board_handling.cancel_state();
          board_frame.board_panel.board_handling.undo();
          board_frame.refresh_windows();
        });
    toolbar_undo_button.addActionListener(evt -> FRAnalytics.buttonClicked("toolbar_undo_button", toolbar_undo_button.getText()));

    middle_toolbar.add(toolbar_undo_button);

    final JButton toolbar_redo_button = new JButton();
    tm.setText(toolbar_redo_button, "redo_button");
    toolbar_redo_button.addActionListener(evt -> board_frame.board_panel.board_handling.redo());
    toolbar_redo_button.addActionListener(evt -> FRAnalytics.buttonClicked("toolbar_redo_button", toolbar_redo_button.getText()));

    middle_toolbar.add(toolbar_redo_button);

    final JLabel separator_1 = new JLabel();
    separator_1.setMaximumSize(new Dimension(10, 10));
    separator_1.setPreferredSize(new Dimension(10, 10));
    middle_toolbar.add(separator_1);

    final JButton toolbar_incompletes_button = new JButton();
    tm.setText(toolbar_incompletes_button, "incompletes_button");
    toolbar_incompletes_button.addActionListener(evt -> board_frame.board_panel.board_handling.toggle_ratsnest());
    toolbar_incompletes_button.addActionListener(evt -> FRAnalytics.buttonClicked("toolbar_incompletes_button", toolbar_incompletes_button.getText()));

    middle_toolbar.add(toolbar_incompletes_button);

    final JButton toolbar_violation_button = new JButton();
    tm.setText(toolbar_violation_button, "violations_button");
    toolbar_violation_button.addActionListener(evt -> board_frame.board_panel.board_handling.toggle_clearance_violations());
    toolbar_violation_button.addActionListener(evt -> FRAnalytics.buttonClicked("toolbar_violation_button", toolbar_violation_button.getText()));

    middle_toolbar.add(toolbar_violation_button);

    final JLabel separator_3 = new JLabel();
    separator_3.setMaximumSize(new Dimension(10, 10));
    separator_3.setPreferredSize(new Dimension(10, 10));
    separator_3.setRequestFocusEnabled(false);
    middle_toolbar.add(separator_3);

    final JButton toolbar_display_region_button = new JButton();
    tm.setText(toolbar_display_region_button, "display_region_button");
    toolbar_display_region_button.addActionListener(evt -> board_frame.board_panel.board_handling.zoom_region());
    toolbar_display_region_button.addActionListener(evt -> FRAnalytics.buttonClicked("toolbar_display_region_button", toolbar_display_region_button.getText()));
    middle_toolbar.add(toolbar_display_region_button);

    final JButton toolbar_display_all_button = new JButton();
    tm.setText(toolbar_display_all_button, "display_all_button");
    toolbar_display_all_button.addActionListener(evt -> board_frame.zoom_all());
    toolbar_display_all_button.addActionListener(evt -> FRAnalytics.buttonClicked("toolbar_display_all_button", toolbar_display_all_button.getText()));
    middle_toolbar.add(toolbar_display_all_button);

    this.add(middle_toolbar, BorderLayout.CENTER);

    // create the right toolbar

    final JToolBar right_toolbar = new JToolBar();

    right_toolbar.setAutoscrolls(true);

    final JLabel unit_label = new JLabel();
    tm.setText(unit_label, "unit_button");
    right_toolbar.add(unit_label);

    toolbar_unit_combo_box = new JComboBox<>();
    toolbar_unit_combo_box.setModel(new DefaultComboBoxModel<>(Unit.values()));
    toolbar_unit_combo_box.setFocusTraversalPolicyProvider(true);
    toolbar_unit_combo_box.setInheritsPopupMenu(true);
    toolbar_unit_combo_box.setOpaque(false);
    toolbar_unit_combo_box.addActionListener(
        evt -> {
          Unit new_unit = (Unit) toolbar_unit_combo_box.getSelectedItem();
          board_frame.board_panel.board_handling.change_user_unit(new_unit);
          board_frame.refresh_windows();
        });
    toolbar_unit_combo_box.addActionListener(evt -> FRAnalytics.buttonClicked("toolbar_unit_combo_box", ((Unit) toolbar_unit_combo_box.getSelectedItem()).name()));

    right_toolbar.add(toolbar_unit_combo_box);

    final JLabel margin_on_right_label = new JLabel();
    margin_on_right_label.setMaximumSize(new Dimension(30, 14));
    margin_on_right_label.setPreferredSize(new Dimension(30, 14));
    right_toolbar.add(margin_on_right_label);

    this.add(right_toolbar, BorderLayout.EAST);

    changeToolbarFontSize(middle_toolbar, ICON_FONT_SIZE);
  }

  /** Sets the selected button in the menu button group */
  void hilight_selected_button() {
    InteractiveState interactive_state =
        this.board_frame.board_panel.board_handling.get_interactive_state();
    if (interactive_state instanceof RouteMenuState) {
      this.toolbar_route_button.setSelected(true);
    } else if (interactive_state instanceof DragMenuState) {
      this.toolbar_drag_button.setSelected(true);
    } else if (interactive_state instanceof SelectMenuState) {
      this.toolbar_select_button.setSelected(true);
    }
  }

  private static void changeToolbarFontSize(JToolBar toolBar, float newSize) {
    for (Component comp : toolBar.getComponents()) {
      Font font = comp.getFont();
      // Create a new font based on the current font but with the new size
      Font newFont = font.deriveFont(newSize);
      comp.setFont(newFont);

      // If the component is a container, update its child components recursively
      if (comp instanceof Container) {
        updateContainerFont((Container) comp, newFont);
      }
    }
  }

  private static void updateContainerFont(Container container, Font font) {
    for (Component child : container.getComponents()) {
      child.setFont(font);
      if (child instanceof Container) {
        updateContainerFont((Container) child, font);
      }
    }
  }
}
