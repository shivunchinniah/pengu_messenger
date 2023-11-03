package UI.Custom;

import java.awt.Color;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

/**
 * This class creates a contact item panel.
 * @author Shivun Chinniah
 */
public class ContactItem extends javax.swing.JPanel {

    /**
     * Creates new form ContactItem
     * @param name The name of the contact item.
     * @param displayName The display/friendly name of the contact item.
     * @param contactColor The contact group Color.
     * @param contactGroup The contact group name.
     */
    public ContactItem(String name,String displayName, Color contactColor, String contactGroup) {
        initComponents();
        lblUsername.setText(name);
        pnlContactColour.setBackground(contactColor);
        pnlContactColour.setToolTipText(contactGroup + " Contact");
        lblDisplayName.setText(displayName);

    }
    
    /**
     * Creates new form ContactItem
     * @param name The name of the contact item.
     * @param displayName The display/friendly name of the contact item.
     * @param contactColor The contact group Color.
     * @param contactGroup The contact group name.
     * @param selected Chooses if the contact should be drawn with a blue box around it.
     */
    public ContactItem(String name,String displayName, Color contactColor, String contactGroup, boolean selected) {
        initComponents();
        lblUsername.setText(name);
        lblUsername.setForeground(new Color(11, 123, 142));
        pnlContactColour.setToolTipText(contactGroup+ " Contact");
        pnlContactColour.setBackground(contactColor);
        lblDisplayName.setText(displayName);

        if (selected) {
            setTheBorder(new LineBorder(Color.blue));
        }

    }
    
    // To set the border of the panel
    private void setTheBorder(Border border){
        this.setBorder(border);
    }

    /**
     * This method must be overridden to choose what happens when a contact item is clicked 
     */
    public void mouseClickedAction() {
        System.out.println(getName() + " was clicked");
    }

  

    /**
     *
     * @return Returns the value of the username label.
     */

    public String getUserName() {
        if (lblUsername.getText() != null) {
            return this.lblUsername.getText();
        } else {
            return "";
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblUsername = new javax.swing.JLabel();
        pnlContactColour = new javax.swing.JPanel();
        lblDisplayName = new javax.swing.JLabel();

        setBackground(new java.awt.Color(248, 248, 248));
        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(248, 248, 248)));
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });

        lblUsername.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        lblUsername.setForeground(new java.awt.Color(51, 51, 51));
        lblUsername.setText("User Name");

        javax.swing.GroupLayout pnlContactColourLayout = new javax.swing.GroupLayout(pnlContactColour);
        pnlContactColour.setLayout(pnlContactColourLayout);
        pnlContactColourLayout.setHorizontalGroup(
            pnlContactColourLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );
        pnlContactColourLayout.setVerticalGroup(
            pnlContactColourLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        lblDisplayName.setFont(new java.awt.Font("Arial", 2, 12)); // NOI18N
        lblDisplayName.setText("Friendly Name");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(pnlContactColour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDisplayName))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlContactColour, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblDisplayName)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        // TODO add your handling code here:
        mouseClickedAction();
    }//GEN-LAST:event_formMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblDisplayName;
    private javax.swing.JLabel lblUsername;
    private javax.swing.JPanel pnlContactColour;
    // End of variables declaration//GEN-END:variables
}