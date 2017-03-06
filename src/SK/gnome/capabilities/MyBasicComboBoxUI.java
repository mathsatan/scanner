
package SK.gnome.capabilities;

import javax.swing.*;
import javax.swing.plaf.basic.*;

public class MyBasicComboBoxUI extends BasicComboBoxUI
{
  BasicComboPopup popup;

  protected ComboPopup createPopup() 
  {
    popup = new BasicComboPopup(comboBox);
    popup.getAccessibleContext().setAccessibleParent(comboBox);
    return popup;
  }

  public BasicComboPopup getPopup() 
  {
    return popup;
  }
}
