/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat_application;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
/**
 *
 * @author Suneth
 */
public class Server extends JFrame{
    private JTextField userText;
    private JTextArea chatWindow;
    private ObjectOutputStream output;
    private ObjectOutputStream keyOutput;
    private ObjectOutputStream signOutput;
    private ObjectInputStream input;
    private ObjectInputStream keyInput;
    private ObjectInputStream signInput;
    private ServerSocket server;
    private Socket connection;
    RSAencryption rsa = new RSAencryption();
        
    //constructor
    public Server(){
        super("WhatsChat Instant Messenger");
	userText = new JTextField();
	userText.setEditable(false);
	userText.addActionListener(
	new ActionListener(){
            public void actionPerformed(ActionEvent event){
                try{
                    sendMessage(event.getActionCommand());
                    userText.setText("");
                   }catch (Exception ex){
                       JOptionPane.showMessageDialog(null,"Unable to send the message","Error",JOptionPane.ERROR_MESSAGE);
                       System.out.print("The message can't be sent");
                   }
            }
	});
	add(userText, BorderLayout.NORTH);
	chatWindow = new JTextArea();
	add(new JScrollPane(chatWindow));
	setSize(300,150);
	setVisible(true);
    }
	
    //set up and run the server
    public void startRunning() throws Exception{
        try{
            server = new ServerSocket(6789, 100);
            while(true){
			try{
                            waitForConnection();
                            setupStreams();
                            setupKeyStreams();
                            setupSignStreams();
                            whileChatting();
                           }catch(EOFException eofException){
                                showMessage("\n Server ended the connection! ");
                           }finally{
				closeCrap();
                           }
                       }
           }catch(IOException ioException){
                ioException.printStackTrace();
           }
    }
	
    //wait for connection, then display connection information
    private void waitForConnection() throws IOException{
        showMessage(" Waiting for someone to connect... \n");
        connection = server.accept();
	showMessage(" Now connected to " + connection.getInetAddress().getHostName());
    }
	
    //set stream to send and receive data
    private void setupStreams() throws IOException{
	output = new ObjectOutputStream(connection.getOutputStream());
	output.flush();
	input = new ObjectInputStream(connection.getInputStream());
	showMessage("\n Streams are now setup! \n");
    }
	
    //set stream to send and receive keys
    private void setupKeyStreams() throws IOException{
        keyOutput = new ObjectOutputStream(connection.getOutputStream());
	keyOutput.flush();
	keyInput = new ObjectInputStream(connection.getInputStream());
    }
        
    //receive the public key
    private PublicKey receiveKey() throws Exception{
        PublicKey publicKey = (PublicKey)keyInput.readObject();
        return publicKey;
    }
        
    //set stream to send and receive signatures
    private void setupSignStreams() throws IOException{
        signOutput = new ObjectOutputStream(connection.getOutputStream());
	signOutput.flush();
	signInput = new ObjectInputStream(connection.getInputStream());
    }
        
    //receive the signature
    private String receiveSign() throws Exception{
        String signature = (String)signInput.readObject();
        return signature;
    }
        
    //during the chat conversation
    private void whileChatting() throws Exception{
	String message = " You are now connected! ";
	sendMessage(message);
	ableToType(true);
	do{
            try{
                String cipherText = (String)input.readObject();
                System.out.println(cipherText);
                PublicKey publicKey = receiveKey();
                // Now decrypt it
		String decipheredMessage = rsa.decrypt(cipherText, publicKey);
                System.out.println(decipheredMessage);
                showMessage("\n" + decipheredMessage);
                String signature = receiveSign();
                // Let's check the signature
		boolean isCorrect = rsa.verify("foobar", signature, publicKey);
		System.out.println("Signature correct: " + isCorrect); 
               }catch(ClassNotFoundException classNotFoundException){
                    showMessage("\n Unknown object type ");
               }
            }while(!message.equals("CLIENT - END"));
    }
	
    //close streams and sockets after you are done chatting
    private void closeCrap(){
        showMessage("\n Closing connections... \n");
        ableToType(false);
        try{
            output.close();
            keyOutput.close();
            signOutput.close();
            input.close();
            keyInput.close();
            signInput.close();
            connection.close();
           }catch(IOException ioException){
                ioException.printStackTrace();
           }
    }
	
    //input validation
    public boolean inputValidation(String m){
        String pattern= "^[a-zA-Z0-9\\t\\n ./<>?;:\"'`!@#$%^&*()\\[\\]{}_+=|\\\\-]+$";
        return m.matches(pattern);
    }
    
    //send a message to client
    private void sendMessage(String message) throws Exception{
        try{
            if(inputValidation(message) == true)
            {
                // First generate a public/private key pair
                KeyPair pair = rsa.generateKeyPair();
                // KeyPair pair = getKeyPairFromKeyStore();
                // Encrypt the message
                System.out.println("SERVER - " + message);
                String cipherText = rsa.encrypt("SERVER - " + message, pair.getPrivate());
                System.out.println(cipherText);
                output.writeObject(cipherText);
                output.flush();
                sendKey(pair.getPublic());
                showMessage("\nSERVER - " + message);
                // Let's sign our message
                String signature = rsa.sign("foobar", pair.getPrivate());
                sendSign(signature);
            }
            else
            {
                JOptionPane.showMessageDialog(null,"Unable to send the message","Error",JOptionPane.ERROR_MESSAGE);
            }
           }catch(IOException ioException){
                JOptionPane.showMessageDialog(null,"Unable to send the message","Error",JOptionPane.ERROR_MESSAGE);
                chatWindow.append("\n The message can't be sent ");
           }
    }
        
    //send the public key
    private void sendKey(PublicKey publicKey){
        try{    
            keyOutput.writeObject(publicKey);
            keyOutput.flush();
           }catch(IOException ioException){
                JOptionPane.showMessageDialog(null,"Unable to send the message","Error",JOptionPane.ERROR_MESSAGE);
                chatWindow.append("\n The message can't be sent ");
           }
    }
        
    //send the signature
    private void sendSign(String signature){
        try{    
            signOutput.writeObject(signature);
            signOutput.flush();
           }catch(IOException ioException){
                JOptionPane.showMessageDialog(null,"Unable to send the message","Error",JOptionPane.ERROR_MESSAGE);
                chatWindow.append("\n The message can't be verified ");
           }
    }
        
    //updates chat window
    private void showMessage(final String text){
        SwingUtilities.invokeLater(
            new Runnable(){
                public void run(){
                    chatWindow.append(text);
		}
            });
    }
	
    //gives user permission to type message into the text box
    private void ableToType(final boolean tof){
        SwingUtilities.invokeLater(
            new Runnable(){
                public void run(){
                    userText.setEditable(tof);
		}
            });
    }
}
