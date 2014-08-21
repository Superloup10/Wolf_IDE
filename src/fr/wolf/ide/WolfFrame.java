package fr.wolf.ide;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ActionMap;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.TaskMonitor;

import fr.wolf.ide.core.WolfCompiler;
import fr.wolf.ide.core.WolfConsole;

public class WolfFrame extends FrameView
{
    private JTextArea console;
    private final Timer messageTimer;
    private final Icon[] busyIcons = new Icon[15];
    private final Icon idleIcon;
    private int busyIconIndex = 0;
    private Timer busyIconTimer;
    private JLabel statusAnimationLabel;
    private JLabel statusMessageLabel;
    private JPanel statusPanel;
    private JProgressBar progressBar;
    private JDialog aboutBox;
    private JPanel mainPanel;
    private JSplitPane jSplitPanel;
    private JScrollPane jScrollPanel;
    private JTextArea javaSource;
    private JScrollPane jScrollPanel2;
    private JLabel jLabel;
    private JLabel jLabel2;
    private JButton jButton;
    private JMenu jMenu;
    private JMenuItem jMenuItem;
    private JMenuItem jMenuItem2;
    private JMenuBar menuBar;

    public WolfFrame(SingleFrameApplication app)
    {
        super(app);

        initComponents();
        try
        {
            System.setOut(new WolfConsole(console));
        }
        catch(FileNotFoundException ex)
        {
            Logger.getLogger(WolfFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

        ResourceMap resourceMap = getResourceMap();
        int messageTimeOut = resourceMap.getInteger("StatusBar.messageTimeOut");
        messageTimer = new Timer(messageTimeOut, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for(int i = 0; i < busyIcons.length; i++)
        {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                String propertyName = evt.getPropertyName();
                if("started".equals(propertyName))
                {
                    if(!busyIconTimer.isRunning())
                    {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                }
                else if("done".equals(propertyName))
                {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                }
                else if("message".equals(propertyName))
                {
                    String text = (String)evt.getNewValue();
                    statusMessageLabel.setText(text == null ? "" : text);
                    messageTimer.restart();
                }
                else if("progress".equals(propertyName))
                {
                    int value = (int)evt.getNewValue();
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }

    @Action
    public void showAboutBox()
    {
        if(aboutBox == null)
        {
            JFrame mainFrame = WolfIDE.getApplication().getMainFrame();
            aboutBox = new WolfAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        WolfIDE.getApplication().show(aboutBox);
    }

    private void initComponents()
    {
        mainPanel = new JPanel();
        jSplitPanel = new JSplitPane();
        jScrollPanel = new JScrollPane();
        javaSource = new JTextArea();
        jScrollPanel2 = new JScrollPane();
        console = new JTextArea();
        jLabel = new JLabel();
        jLabel2 = new JLabel();
        jButton = new JButton();
        menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu();
        JMenuItem exitMenuItem = new JMenuItem();
        jMenu = new JMenu();
        jMenuItem = new JMenuItem();
        jMenuItem2 = new JMenuItem();
        JMenu helpMenu = new JMenu();
        JMenuItem aboutMenuItem = new JMenuItem();
        statusPanel = new JPanel();
        JSeparator statusPanelSeparator = new JSeparator();
        statusMessageLabel = new JLabel();
        statusAnimationLabel = new JLabel();
        progressBar = new JProgressBar();

        mainPanel.setName("mainPanel");

        jSplitPanel.setOrientation(JSplitPane.VERTICAL_SPLIT);
        jSplitPanel.setName("jSplitPanel");

        jScrollPanel.setName("jScrolPanel");

        javaSource.setColumns(20);
        javaSource.setRows(5);
        javaSource.setMinimumSize(new Dimension(164, 164));
        javaSource.setName("javaSource");
        javaSource.setPreferredSize(new Dimension(164, 164));
        jScrollPanel.setViewportView(javaSource);

        jSplitPanel.setTopComponent(jScrollPanel);

        jScrollPanel2.setName("consoleContainer");

        console.setColumns(20);
        console.setEditable(false);
        console.setRows(5);
        console.setName("console");
        jScrollPanel2.setViewportView(console);

        jSplitPanel.setRightComponent(jScrollPanel2);

        ResourceMap resourceMap = Application.getInstance(WolfIDE.class).getContext().getResourceMap(WolfFrame.class);
        jLabel.setText(resourceMap.getString("jLabel.text"));
        jLabel.setName("jLabel");

        jLabel2.setText(resourceMap.getString("jLabel2.text"));
        jLabel2.setName("jLabel2");

        ActionMap actionMap = Application.getInstance(WolfIDE.class).getContext().getActionMap(WolfFrame.class, this);
        jButton.setAction(actionMap.get("clearConsole"));
        jButton.setIcon(resourceMap.getIcon("jButton.icon"));
        jButton.setText(resourceMap.getString("jButton.text"));
        jButton.setBorder(null);
        jButton.setBorderPainted(false);
        jButton.setName("jButton");

        GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(//
        mainPanelLayout.createParallelGroup(Alignment.LEADING)//
        .addGroup(mainPanelLayout.createSequentialGroup()//
        .addContainerGap()//
        .addGroup(mainPanelLayout.createParallelGroup(Alignment.LEADING)//
        .addComponent(jSplitPanel, GroupLayout.DEFAULT_SIZE, 703, Short.MAX_VALUE)//
        .addComponent(jLabel).addGroup(mainPanelLayout.createSequentialGroup()//
        .addComponent(jLabel2)//
        .addPreferredGap(ComponentPlacement.RELATED, 640, Short.MAX_VALUE)//
        .addComponent(jButton)))//
        .addContainerGap()));//

        mainPanelLayout.setVerticalGroup(//
        mainPanelLayout.createParallelGroup(Alignment.LEADING)//
        .addGroup(mainPanelLayout.createSequentialGroup()//
        .addContainerGap().addComponent(jLabel)//
        .addPreferredGap(ComponentPlacement.RELATED)//
        .addComponent(jSplitPanel, GroupLayout.DEFAULT_SIZE, 504, Short.MAX_VALUE)//
        .addPreferredGap(ComponentPlacement.RELATED)//
        .addGroup(mainPanelLayout.createParallelGroup(Alignment.BASELINE))//
        .addComponent(jLabel2)//
        .addComponent(jButton)//
        .addContainerGap()));//

        menuBar.setName("menuBar");

        fileMenu.setText(resourceMap.getString("fileMenu.text"));
        fileMenu.setName("fileMenu");

        exitMenuItem.setAction(actionMap.get("quit"));
        exitMenuItem.setName("exitMenu");
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        jMenu.setText(resourceMap.getString("jMenu.text"));
        jMenu.setName("jMenu");

        jMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
        jMenuItem.setIcon(resourceMap.getIcon("jMenuItem.icon"));
        jMenuItem.setText(resourceMap.getString("jMenuItem.text"));
        jMenuItem.setToolTipText(resourceMap.getString("jMenuItem.toolTipText"));
        jMenuItem.setName("jMenuItem");
        jMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                runClass(e);
            }
        });
        jMenu.add(jMenuItem);

        jMenuItem2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.SHIFT_MASK));
        jMenuItem2.setIcon(resourceMap.getIcon("jMenuItem2.icon"));
        jMenuItem2.setText(resourceMap.getString("jMenuItem2.text"));
        jMenuItem2.setToolTipText(resourceMap.getString("jMenuItem2.toolTipText"));
        jMenuItem2.setName("jMenuItem2");
        jMenuItem2.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                runClassInMemory(e);
            }
        });
        jMenu.add(jMenuItem2);

        menuBar.add(jMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text"));
        helpMenu.setName("helpMenu");

        aboutMenuItem.setText(resourceMap.getString("aboutMenuItem.text"));
        aboutMenuItem.setName("aboutMenuItem");
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel");

        statusMessageLabel.setName("statusMessageLabel");

        statusAnimationLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel");

        progressBar.setName("progressBar");

        GroupLayout statusPanelLayout = new GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(//
        statusPanelLayout.createParallelGroup(Alignment.LEADING)//
        .addComponent(statusPanelSeparator, GroupLayout.DEFAULT_SIZE, 723, Short.MAX_VALUE)//
        .addGroup(statusPanelLayout.createSequentialGroup()//
        .addContainerGap()//
        .addComponent(statusMessageLabel)//
        .addPreferredGap(ComponentPlacement.RELATED, 553, Short.MAX_VALUE)//
        .addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)//
        .addPreferredGap(ComponentPlacement.RELATED)//
        .addComponent(statusAnimationLabel)//
        .addContainerGap()));//
        statusPanelLayout.setVerticalGroup(//
        statusPanelLayout.createParallelGroup(Alignment.LEADING)//
        .addGroup(statusPanelLayout.createSequentialGroup()//
        .addComponent(statusPanelSeparator, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)//
        .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)//
        .addGroup(statusPanelLayout.createParallelGroup(Alignment.BASELINE)//
        .addComponent(statusMessageLabel)//
        .addComponent(statusAnimationLabel)//
        .addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)//
        .addGap(3, 3, 3))));//

        this.setComponent(mainPanel);
        this.setMenuBar(menuBar);
        this.setStatusBar(statusPanel);
    }

    private void runClass(ActionEvent e)
    {
        try
        {
            console.setText("");
            WolfCompiler.doPhysicalCompilation(javaSource.getText());
        }
        catch(Exception ex)
        {
            Logger.getLogger(WolfFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void runClassInMemory(ActionEvent e)
    {
        try
        {
            console.setText("");
            WolfCompiler.doMemoryCompilation(javaSource.getText());
        }
        catch(Exception ex)
        {
            Logger.getLogger(WolfFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Action
    public void clearConsole()
    {
        console.setText("");
    }
}