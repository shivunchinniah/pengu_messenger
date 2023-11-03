package UI.Custom;

import java.awt.BorderLayout;
import java.awt.Color;
import static java.awt.Component.TOP_ALIGNMENT;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import pengu.messenger.PenguMessengerClient;
import pengu.messenger.PenguMessengerRESTClient.PublicKey;

/**
 * This class creates a contact selection panel.
 *
 * @author Shivun Chinniah
 */
public class ContactSelection extends javax.swing.JPanel {

    private JPanel list;
    private final JPanel search;
    private ModernScrollPane jsp;

    private Rectangle scroll = null;

    private boolean searchMade = false;
    private boolean loadingOnline = false;
    private String selected = "";

    private boolean useOnlineContacts = true;

    private PenguMessengerClient pmc = new PenguMessengerClient();

    private PenguMessengerClient.ContactItem[] localContacts;
    private GridBagConstraints gbc2;

    private PublicKey[] onlineContacts = new PublicKey[0];

    private void setComponentFocusable(boolean focusable) {
        this.setFocusable(focusable);
    }

    private void setComponentPreferredSize(Dimension preferredSize) {
        this.setPreferredSize(preferredSize);
    }

    private void setComponentLayout(LayoutManager mgr) {
        this.setLayout(mgr);
    }

    /**
     * Parameterized constructor method.
     *
     * @param pmc The PenguMessengerClient to use.
     */
    public ContactSelection(PenguMessengerClient pmc) {
        this.pmc = pmc;
        this.setComponentFocusable(true);
        this.setComponentPreferredSize(new Dimension(378, 527));
        //this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.setComponentLayout(new BorderLayout());

        search = new ContactSearchBox() {
            @Override
            public void search() {
                searchForTerm(getSearchTerm());
            }

        };

        loadLocalContacts();
        loadContacts("");

    }

    /**
     * Parameterized constructor method.
     *
     * @param pmc The PenguMessengerClient to use.
     * @param useOnlineContacts TRUE - Show online Contacts, FALE - Hide online
     * Contacts
     */
    public ContactSelection(PenguMessengerClient pmc, boolean useOnlineContacts) {
        this.useOnlineContacts = useOnlineContacts;
        this.pmc = pmc;
        this.setComponentFocusable(true);
        this.setComponentPreferredSize(new Dimension(378, 527));
        this.setComponentLayout(new BorderLayout());

        search = new ContactSearchBox() {
            @Override
            public void search() {
                searchForTerm(getSearchTerm());
            }

        };

        loadLocalContacts();
        loadContacts("");
    }

    // updates the localContacts array
    private void loadLocalContacts() {
        localContacts = pmc.getListOfContacts();
        loadContacts("");
    }

    // Used for checking the existance of a username in the localConstacts array. 
    private int indexOfContact(String username) {

        int i = 0;

        for (PenguMessengerClient.ContactItem x : localContacts) {
            if (x.getUsername().equalsIgnoreCase(username)) {
                return i;
            }
            i++;
        }

        return -1;

    }

    /**
     * Reloads the contact selection panel, but with the specified username selected.
     * @param selected The username to mark as selected.
     */
    public void reloadContacts(String selected) {
        PenguMessengerClient.ContactItem[] temp = new PenguMessengerClient.ContactItem[localContacts.length];
        PenguMessengerClient.ContactItem[] allcontact = pmc.getListOfContacts();

        int pos = 0;

        for (PenguMessengerClient.ContactItem allcontact1 : allcontact) {
            if (indexOfContact(allcontact1.getUsername()) > -1) {
                temp[pos] = allcontact1;
                pos++;
            }
        }
        localContacts = temp;

        loadContacts(selected);

    }

    // draw all the elements for the panel
    private void loadContacts(String selected) {
        this.removeAll();

        GridBagLayout gbl = new GridBagLayout();

        gbl.columnWidths = new int[]{0, 0, 0, 0};
        gbl.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gbl.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
        gbl.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};

        list = new JPanel(gbl);
        list.setBackground(new Color(217, 217, 217));
        list.setAlignmentX(TOP_ALIGNMENT);

        gbc2 = new GridBagConstraints();
        gbc2.gridwidth = GridBagConstraints.REMAINDER;

        gbc2.ipady = 1;
        gbc2.weightx = 1;
        gbc2.insets = new Insets(5, 15, 5, 15);
        gbc2.fill = GridBagConstraints.HORIZONTAL;

        if (searchMade) { // A search was made
            list.add(new ContactCategory("Local Users"), gbc2, list.getComponentCount());

            if (localContacts.length > 0) { //There are search results for local contacts
                for (PenguMessengerClient.ContactItem localContact : localContacts) {
                    if (localContact.getUsername().equalsIgnoreCase(selected)) {
                        list.add(new ContactItem(localContact.getUsername(), localContact.getDisplayName(), localContact.getContactGroupColor(), localContact.getContactGroup(), true) {
                            @Override
                            public void mouseClickedAction() {

                                scroll = jsp.getViewport().getViewRect();
                                //System.out.println(getName()+" hey");
                                markSelected(getUserName());
                                showProperties();
                            }
                        }, gbc2, list.getComponentCount());
                    } else {
                        list.add(new ContactItem(localContact.getUsername(), localContact.getDisplayName(), localContact.getContactGroupColor(), localContact.getContactGroup()) {
                            @Override
                            public void mouseClickedAction() {

                                scroll = jsp.getViewport().getViewRect();
                                //System.out.println(getName()+" hey");
                                markSelected(getUserName());
                                showProperties();
                            }
                        }, gbc2, list.getComponentCount());
                    }
                }

            } else {//No search results for local contacts
                JLabel temp = new JLabel("<html><p style='text-align: center;'>No Local Contacts Found.</p></html>", SwingConstants.CENTER);
                temp.setPreferredSize(new Dimension(300, 70));

                list.add(temp, gbc2, list.getComponentCount());

            }
            if (useOnlineContacts) {

                list.add(new ContactCategory("Public Users (Online)"), gbc2, list.getComponentCount());

                if (onlineContacts.length > 0) { // There are online Search results
                    for (PublicKey onlineContact : onlineContacts) {
                        if (onlineContact.getUsername().equalsIgnoreCase(selected)) {
                            list.add(new ContactItem(onlineContact.getUsername(), null, new Color(48, 147, 121), "Online", true) {
                                @Override
                                public void mouseClickedAction() {

                                    scroll = jsp.getViewport().getViewRect();
                                    //System.out.println(getName()+" hey");
                                    markSelected(getUserName());
                                    showProperties();
                                }
                            }, gbc2, list.getComponentCount());
                        } else {
                            list.add(new ContactItem(onlineContact.getUsername(), null, new Color(48, 147, 121), "Online") {
                                @Override
                                public void mouseClickedAction() {

                                    scroll = jsp.getViewport().getViewRect();
                                    //System.out.println(getName()+" hey");
                                    markSelected(getUserName());
                                    showProperties();
                                }
                            }, gbc2, list.getComponentCount());
                        }
                    }
                } else {// no online results
                    if (loadingOnline) {// no results because loading still
                        list.add(new ContactsLoading(), gbc2, list.getComponentCount());
                    } else {// actually no results
                        JLabel temp = new JLabel("<html><p style='text-align: center;'>No Online Contacts Found.</p></html>", SwingConstants.CENTER);
                        temp.setPreferredSize(new Dimension(300, 70));

                        list.add(temp, gbc2, list.getComponentCount());
                    }
                }
            }

        } else { // No search was made
            list.add(new ContactCategory("Local Users"), gbc2, list.getComponentCount());

            if (localContacts.length > 0) { //there are local contacts
                for (PenguMessengerClient.ContactItem localContact : localContacts) {
                    if (localContact.getUsername().equalsIgnoreCase(selected)) {
                        list.add(new ContactItem(localContact.getUsername(), localContact.getDisplayName(), localContact.getContactGroupColor(), localContact.getContactGroup(), true) {
                            @Override
                            public void mouseClickedAction() {

                                scroll = jsp.getViewport().getViewRect();
                                //System.out.println(getName()+" hey");
                                markSelected(getUserName());
                                showProperties();
                            }
                        }, gbc2, list.getComponentCount());
                    } else {
                        list.add(new ContactItem(localContact.getUsername(), localContact.getDisplayName(), localContact.getContactGroupColor(), localContact.getContactGroup()) {
                            @Override
                            public void mouseClickedAction() {

                                scroll = jsp.getViewport().getViewRect();
                                //System.out.println(getName()+" hey");
                                markSelected(getUserName());
                                showProperties();
                            }
                        }, gbc2, list.getComponentCount());
                    }
                }
            } else {//no local contacts
                JLabel temp = new JLabel("<html><p style='text-align: center;'>No contacts. Search for contacts on<br> Pengu Messenger's Public directory.<br>Enter a username in the search bar above.</p></html>", SwingConstants.CENTER);
                temp.setPreferredSize(new Dimension(300, 70));

                list.add(temp, gbc2, list.getComponentCount());
            }

        }

        //searchMade = false;
        jsp = new ModernScrollPane(list, new Color(0, 153, 153));
        jsp.getVerticalScrollBar().setUnitIncrement(10);
        jsp.setAlignmentY(TOP_ALIGNMENT);
        jsp.setBackground(Color.white);
        jsp.getVerticalScrollBar().setUnitIncrement(10);

        if (scroll != null) {
            //System.out.println(scroll.x+" "+scroll.y+" "+scroll.width+" "+scroll.height+" ");
            jsp.getViewport().scrollRectToVisible(scroll);
        }

        this.add(search, BorderLayout.NORTH);
        this.add(jsp, BorderLayout.CENTER);

        revalidate();

    }

    /**
     * Override this to choose what happens when a contact is clicked
     */
    public void showProperties() {

    }

    /**
     * Makes a search for a given search term and displays results in panel.
     * 
     * @param searchTerm The given search term.
     */
    public void searchForTerm(String searchTerm) {
        if (searchTerm.length() == 0) { // no search term
            loadLocalContacts();
            searchMade = false;
            scroll = jsp.getViewport().getViewRect();
            selected = "";
            loadContacts(selected);

        } else if (searchTerm.trim().charAt(0) == '_' && searchTerm.trim().length() == 1) { // single underscore searches for all users.

            searchMade = true;

        } else { // make search

            loadLocalContacts();

            ArrayList<PenguMessengerClient.ContactItem> results = new ArrayList();
            ArrayList<String> resultUsernames = new ArrayList();
            
            //Serch local contacts and store in temporary array.
            for (PenguMessengerClient.ContactItem localContact : localContacts) {
                if (localContact.getUsername().toLowerCase().contains(searchTerm.toLowerCase())) {
                    results.add(localContact);
                    resultUsernames.add(localContact.getUsername());
                } else if (localContact.getDisplayName() != null && localContact.getDisplayName().toLowerCase().contains(searchTerm.toLowerCase())) {
                    results.add(localContact);
                    resultUsernames.add(localContact.getUsername());
                }
            }

            localContacts = new PenguMessengerClient.ContactItem[results.size()];

            // Load temporary array content into localContacts array.
            for (int i = 0; i < results.size(); i++) {
                localContacts[i] = results.get(i);
            }

            searchMade = true;

            if (useOnlineContacts) {// if online contacts are used

                //set to zero results for online initially 
                onlineContacts = new PublicKey[0];
                //show loading symbol
                loadingOnline = true;

                new Thread() { // make an asyncronus request to server

                    @Override
                    public void run() {
                        try {
                            onlineContacts = pmc.searchForOtherUsers(searchTerm);
                            ArrayList<PublicKey> tempContacts = new ArrayList<>();

                            for (PublicKey i : onlineContacts) {
                                if (!(resultUsernames.indexOf(i.getUsername()) > -1)) {
                                    tempContacts.add(i);

                                }
                            }

                            onlineContacts = new PublicKey[tempContacts.size()];

                            for (int i = 0; i < tempContacts.size(); i++) {
                                onlineContacts[i] = tempContacts.get(i);
                            }

                            // hide loading symbol
                            loadingOnline = false;
                            
                            searchMade = true;
                            loadContacts(selected);

                        } catch (PenguMessengerClient.ServerProblemException ex) {
                            System.out.println(ex.getDescription());
                            loadingOnline = false;
                            searchMade = true;
                            loadContacts(selected);
                        }
                    }

                }.start();

            }

            loadContacts(selected);

        }

    }

    // marks a contact as selected
    private void markSelected(String selected) {
        loadContacts(selected);
        this.selected = selected;
    }

    /**
     * Accessor method for selected field.
     * @return Returns the value of the selected field.
     */
    public String getSelected() {
        return selected;
    }

}
