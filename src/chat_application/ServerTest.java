/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat_application;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
/**
 *
 * @author Suneth
 */
public class ServerTest{
    
    public static void main(String[] args){
        try{
            Server sally = new Server();
            sally.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            sally.startRunning();
        }catch(Exception ex){
            JOptionPane.showMessageDialog(null,"Error occured","Error",JOptionPane.ERROR_MESSAGE);
            System.out.print("Error occured: " + ex.getMessage());
        }
    }    
}
