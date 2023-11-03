package pengu.messenger;

import UI.MainScreen;

/**
 * This is the main class for the Pengu Messenger application.
 * 
 * @author Shivun Chinniah
 */
public class PenguMessenger {

    /**
     * he main method for Pengu Messenger.T
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       
        //Set the look and feel to the System look and feel
        try {
            
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
            
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        
       new UI.LoginScreen().setVisible(true);
       
    }
    
 
    
}
