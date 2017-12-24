//The purpose of this class is the be a window for the user to send messages to the client or server
//Most of this class was created by the Eclipse Window Builder Pro plugin

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Window extends JFrame {
	private static final long serialVersionUID = 789436913970326654L;
	private JPanel contentPane;
	private JTextField txtSendBox;
	private JTextArea taMessages;
	
	//Create window with components on it
	public Window() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 550, 400);
		setTitle("Jakes Diffie Hellman Secure Chat");
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		txtSendBox = new JTextField();
		txtSendBox.setBounds(10, 331, 514, 20);
		txtSendBox.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				//If the key pressed was enter, send a message
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					
					try {
						//If the client is null, the server must send a message
						if (Main.client == null) {
							Main.server.send(txtSendBox.getText() + "\n");
						}
						else {
							Main.client.send(txtSendBox.getText() + "\n");
						}

						//Add our message to the messages text area
						taMessages.setText(taMessages.getText() + txtSendBox.getText() + "\n");
						
						//Clear the send box
						txtSendBox.setText("");
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					
				}
			}
		});
		txtSendBox.setColumns(10);
		contentPane.add(txtSendBox);
		
		taMessages = new JTextArea();
		taMessages.setBounds(10, 11, 514, 309);
		taMessages.setFocusable(false);
		contentPane.add(taMessages);
	}
	
	//Prints a message on the text area
	public void println(String str) {
		taMessages.setText(taMessages.getText() + str);
	}
}
