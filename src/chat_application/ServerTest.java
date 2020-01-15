/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat_application;
import javax.swing.JFrame;
/**
 *
 * @author Suneth
 */
public class ServerTest{
    
    public static void main(String[] args) throws Exception{
	Server sally = new Server();
	sally.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	sally.startRunning();
    }
    
}
