import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.PropertyResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;

import SK.gnome.morena.MorenaConstants;
import SK.gnome.morena.MorenaImage;
import SK.gnome.capabilities.MorenaCapabilities;
import SK.gnome.morena.MorenaSource;

public class Main extends JApplet implements MorenaConstants
{
    private JPanel centerPanel;
    private URL codeBase=null;
    private ImagePanel selected=null;
    private SaveImageAction saveImageAction;
    private MouseListener mouseListener=new MouseListener();
    private JComboBox<Integer> resolution;

    private class AcquireImageAction extends AbstractAction
    {
        AcquireImageAction()
        {
            super(MORENA_MESSAGES.getString("REPEAT_SCAN"));
        }

        public void actionPerformed(ActionEvent e)
        {
                // Set to the Twain or Sane source the last used capabilities (options)

            acquireImage(false);
        }
    }

    private class SelectSourceAction extends AbstractAction
    {
        SelectSourceAction()
        {
            super(MORENA_MESSAGES.getString("SELECT_SOURCE"));
        }

        public void actionPerformed(ActionEvent e)
        {
                //Pop up dialog windows to select Twain or Sane interface
            MorenaCapabilities.selectSource(centerPanel);
        }

    }

    private class CustomDialogAction extends AbstractAction
    {
        CustomDialogAction()
        {
            super(MORENA_MESSAGES.getString("CUSTOM_DIALOG_WINDOW"));
        }

        public void actionPerformed(ActionEvent e)
        {
            //Set to the Twain or Sane source the last used capabilities (options)
            acquireImage(true);
        }
    }

    private void acquireImage(boolean showDialogWindow)
    {
            //Constructor
        MorenaCapabilities morenaCapabilities;
        try
        {
            morenaCapabilities = new MorenaCapabilities(centerPanel);
            boolean acquire = true;
            if (showDialogWindow)
            {
                if (JOptionPane.YES_OPTION != morenaCapabilities.showDialogWindow())
                    acquire = false;
            }
            if (acquire)
            {
                MorenaSource ms = morenaCapabilities.getSource();
                ms.setResolution(new Integer((Integer)resolution.getSelectedItem()).doubleValue());
                System.out.println(Double.toString(ms.getResolution()));
                MorenaImage morenaImage = new MorenaImage(ms);
                ImagePanel image = new ImagePanel(Toolkit.getDefaultToolkit().createImage(morenaImage));
                centerPanel.add(image);
                select(image);
                int size = (int) Math.round(Math.sqrt(centerPanel.getComponentCount()));
                centerPanel.setLayout(new GridLayout(size, size));
                centerPanel.validate();
            }
            morenaCapabilities.close();
        } catch (Exception e)
        {
            JOptionPane.showMessageDialog(centerPanel, e.getMessage());
            e.printStackTrace();
        }
    }
 

    private class SaveImageAction extends AbstractAction
    { private class Filter extends FileFilter
    { String type;

        Filter(String type)
        { this.type=type;
        }

        public boolean accept(File file)
        { return file.getName().endsWith(type);
        }

        public String getDescription()
        { return type+" Files";
        }
    }

        SaveImageAction()
        { super(MORENA_MESSAGES.getString("SAVE_TO_FILE"));
        }

        public void actionPerformed(ActionEvent event)
        {
            try
            {
                Image image=selected.getImage();
                BufferedImage bufferedImage=new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
                bufferedImage.createGraphics().drawImage(image, 0, 0, null);
                JFileChooser chooser=new JFileChooser();
                    // Java 1.6 introduced a comfortable ImageIO.getWriterFileSuffixes() method.
                String e[] = ImageIO.getWriterFormatNames();
                for (int i=0; i<e.length; i++)
                    chooser.addChoosableFileFilter(new Filter(e[i]));
                int result=chooser.showSaveDialog(centerPanel);
                if (result==JFileChooser.APPROVE_OPTION)
                { String ext=chooser.getFileFilter().getDescription();
                    ext=ext.substring(0, ext.indexOf(' ')).toLowerCase();
                    File file=chooser.getSelectedFile();
                    String name=file.getName();
                    if (!name.endsWith(ext))
                        file=new File(file.getParentFile(), name+"."+ext);
                    ImageIO.write(bufferedImage, ext, file);
                }
            }
            catch (Throwable exception)
            { JOptionPane.showMessageDialog(centerPanel, exception.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                exception.printStackTrace();
            }
        }


        public boolean isEnabled()
        {
            return selected != null;
        }
    }

    private class MouseListener extends MouseAdapter
    { public void mouseClicked(MouseEvent event)
    { select((ImagePanel)event.getComponent());
    }
    }

    private void select(ImagePanel image)
    {
        if(selected != null)
            selected.setBorder(null);
        selected = image;
        if(selected != null)
        {
            selected.setBorder(new LineBorder(Color.blue, 1));
            saveImageAction.setEnabled(true);
        } else
        {
            saveImageAction.setEnabled(false);
        }
    }

    private class ImagePanel extends JPanel
    {
        private Image image;
        int imageWidth;
        int imageHeight;

        ImagePanel(Image image)
        {
            this.image = image;
            imageWidth = image.getWidth(null);
            imageHeight = image.getHeight(null);
            addMouseListener(mouseListener);
        }

        public Image getImage()
        {
            return image;
        }

        public void paint(Graphics g)
        {
            super.paint(g);
            int panelWidth = getWidth() - 6;
            int panelHeight = getHeight() - 6;
            double horizontalRatio = (double) panelWidth / imageWidth;
            double verticalRatio = (double) panelHeight / imageHeight;
            if (horizontalRatio > verticalRatio)
                g.drawImage(image, (int) (panelWidth - imageWidth * verticalRatio) / 2 + 3, 3,
                        (int) (imageWidth * verticalRatio), (int) (imageHeight * verticalRatio), this);
            else
                g.drawImage(image, 3, 3, (int) (imageWidth * horizontalRatio), (int) (imageHeight * horizontalRatio), this);
        }

    }


    public void init()
    {
        //First test applet's parameters, then System properties.
        String languageLocale=getParameter("user.language");
        if(null==languageLocale)
            languageLocale=System.getProperty("user.language", "en");
        String countryLocale=getParameter("user.country");
        if(null==countryLocale)
            countryLocale=System.getProperty("user.country", "US");
        Locale.setDefault(new Locale(languageLocale,countryLocale));

        codeBase=getCodeBase();
        init(getContentPane());
    }

    public void init(Container container)
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e1)
        {
            e1.printStackTrace();
        }

        JToolBar toolbar = new JToolBar();
        JButton selectSourceButton = new JButton(new SelectSourceAction());
        toolbar.add(selectSourceButton);
        toolbar.addSeparator();
        JButton customTwainDialogButton = new JButton(new CustomDialogAction());
        toolbar.add(customTwainDialogButton);
        toolbar.addSeparator();

        JButton saveButton = new JButton(saveImageAction=new SaveImageAction());
        saveImageAction.setEnabled(false);
        toolbar.add(saveButton);
        toolbar.addSeparator();

        JButton repeatScanButton = new JButton(new AcquireImageAction());
        toolbar.add(repeatScanButton);

        toolbar.addSeparator();
        JLabel lbl = new JLabel("dpi: ");
        toolbar.add(lbl);
        Integer []  resolutions = {72, 96, 200, 300};
        resolution = new JComboBox<Integer>(resolutions);
        resolution.setMaximumSize(new Dimension(30, 20));
        toolbar.add(resolution);
        toolbar.add(Box.createHorizontalGlue());

        container.add(toolbar, BorderLayout.NORTH);
        container.add(centerPanel = new JPanel(), BorderLayout.CENTER);
    }

    public static void main(String[] args)
    {
        JFrame frame = new JFrame(MORENA_MESSAGES.getString("MORENA_CAPABILITIES_EXAMPLE"));
        Main morenaCapabilitiesExample = new Main();
        morenaCapabilitiesExample.init(frame.getContentPane());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocation(100, 100);
        frame.setVisible(true);
    }

}
