package com.objogate.wl.driver.tests;

import javax.swing.*;
import java.awt.BorderLayout;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import com.objogate.wl.AWTEventQueueProber;
import com.objogate.wl.ComponentSelector;
import com.objogate.wl.driver.AbstractButtonDriver;
import com.objogate.wl.driver.JFrameDriver;
import com.objogate.wl.driver.JMenuBarDriver;
import com.objogate.wl.driver.JMenuDriver;
import static com.objogate.wl.driver.tests.JMenuBarDriverTest.Type.PLAIN;
import static com.objogate.wl.driver.tests.JMenuBarDriverTest.Type.RADIO;
import com.objogate.wl.matcher.ComponentMatchers;
import static com.objogate.wl.matcher.ComponentMatchers.withButtonText;
import static com.objogate.wl.matcher.ComponentMatchers.withMnemonicKey;
import com.objogate.wl.probe.ActionListenerProbe;

public class JMenuBarDriverTest extends AbstractComponentDriverTest<JMenuBarDriver> {
    private AWTEventQueueProber prober;
    private JMenuItem displayItem;
    private JMenuItem sheep;

    enum Type {
        PLAIN, CHECK, RADIO
    }

    @Before
    public void setUp() {

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());

        view(topPanel);
        JMenuBar menuBar = new JMenuBar();

        frame.setJMenuBar(menuBar);

        menuBar.setName("menuBar");

        menuBar.add(createFileMenu());
        menuBar.add(createEditMenu());

        frame.pack();

        driver = new JMenuBarDriver(new JFrameDriver(gesturePerformer, frame), sameInstance(menuBar));

        prober = new AWTEventQueueProber();
    }

    @Test
    public void canFindTheMenuBar() throws InterruptedException {
        driver.is("is showing on screen", ComponentMatchers.showingOnScreen());
    }

    @Test
    public void canFindAMenuByTitle() {
        String menuTexts[] = {"File", "Edit"};
        for (String menuText : menuTexts) {
            JMenuDriver menu = driver.menu(withButtonText(menuText));
            ComponentSelector<JMenu> selector = menu.component();
            prober.check("can find menu by title", selector);
            assertNotNull("Can find component", selector.component());
            menu.text(equalTo(menuText));
        }
    }

    @Test
    public void canFindAMenuByMnemonic() {
        JMenuDriver menu = driver.menu(withMnemonicKey('E'));
        ComponentSelector<JMenu> selector = menu.component();
        prober.check("can find menu by mnemonic", selector);
        assertNotNull("Can find component", selector.component());
        menu.text(equalTo("Edit"));
        menu.mnemonic(equalTo('E'));
    }

    @Test
    public void canClickOnANamedMenu() {
        JMenuDriver menu = driver.menu(withButtonText("File"));
        ComponentSelector<JMenu> selector = menu.component();
        assertTrue(prober.poll(selector));
        menu.leftClickOnComponent();
        menu.isShowing();
    }

    @Test
    public void canClickOnAMenuItemWithGivenText() throws InterruptedException {
        JMenuDriver menu = driver.menu(withButtonText("File"));
        menu.leftClickOnComponent();
        AbstractButtonDriver<JMenuItem> buttonDriver = menu.menuItem(withButtonText("New"));
        prober.check("can find", buttonDriver.component());
        buttonDriver.leftClickOnComponent();
    }

    @Test
    public void canFindSubMenusAndItems() {
        ActionListenerProbe probe = new ActionListenerProbe();
        displayItem.addActionListener(probe);

        driver.menu(withButtonText("File"))
                .subMenu(withButtonText("Properties"))
                .subItem(withButtonText("Display"))
                .leftClickOnComponent();

        prober.check("should have been clicked", probe);

    }

    @Test
    public void canFindSubSubMenusAndItems() {
        ActionListenerProbe probe = new ActionListenerProbe();
        sheep.addActionListener(probe);

        driver.menu(withButtonText("File"))
                .subMenu(withButtonText("Properties"))
                .subMenu(withButtonText("System"))
                .subItem(withButtonText("Sheep"))
                .leftClickOnComponent();

        prober.check("should have been clicked", probe);

    }

    private JMenu createEditMenu() {
        JMenu menu = new JMenu("Edit");
        menu.setMnemonic('E');

        addItem(menu, PLAIN, "Cut", null, 't', "Cut data to the clipboard");
        addItem(menu, PLAIN, "Copy", null, 'C', "Copy data to the clipboard");
        addItem(menu, PLAIN, "Paste", null, 'P', "Paste data from the clipboard");
        return menu;
    }

    private JMenu createFileMenu() {

        JMenu menu = new JMenu("File");
        menu.setMnemonic('F');

        addItem(menu, PLAIN, "New", null, 'N', null);
        addItem(menu, PLAIN, "Open...", new ImageIcon("open.gif"), 'O', "Open a new file");
        addItem(menu, PLAIN, "Save", new ImageIcon("save.gif"), 'S', " Save this file");
        addItem(menu, PLAIN, "Save As...", null, 'A', "Save this data to a new file");
        menu.addSeparator();
        menu.add(createPropertyMenu());
        menu.addSeparator();
        addItem(menu, PLAIN, "Exit", null, 'x', "Exit the program");
        return menu;
    }

    private JMenu createPropertyMenu() {
        JMenu menu = new JMenu("Properties");
        menu.setMnemonic('P');

        menu.add(createSystemMenu());
        addItem(menu, PLAIN, "Editor", null, 'E', null);
        displayItem = addItem(menu, RADIO, "Display", null, 'D', null);
        return menu;
    }

    private JMenu createSystemMenu() {
        JMenu menu = new JMenu("System");
        addItem(menu, PLAIN, "Something", null, 'E', null);
        addItem(menu, PLAIN, "Another Thing", null, 'E', null);
        sheep = addItem(menu, PLAIN, "Sheep", null, 'E', null);
        return menu;
    }

    public JMenuItem addItem(JMenu menu, Type type, String sText,
                             ImageIcon image, int mnemonic,
                             String sToolTip) {
        JMenuItem item;

        switch (type) {
            case RADIO:
                item = new JRadioButtonMenuItem();
                break;
            case CHECK:
                item = new JCheckBoxMenuItem();
                break;
            default:
                item = new JMenuItem();
                break;
        }

        item.setText(sText);

        if (image != null) {
            item.setIcon(image);
        }

        if (mnemonic > 0) {
            item.setMnemonic(mnemonic);
        }

        if (sToolTip != null) {
            item.setToolTipText(sToolTip);
        }

        menu.add(item);

        return item;
    }
}
