//Name: Jake Garza
//Date started: 12/21/17
//Project made to be a secure chat secured by a diffie hellman key agreement/exchange

import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class Main {
	public static int port = 25566;
	public static String IP = "localhost";
	public static Window frame;
	
	public static DHClient client = null;
	public static DHServer server = null;
	
	public static void main(String args[]) {
		//Make widow look like the rest of the OS
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}


		String serverPrompt = JOptionPane.showInputDialog("Would you like to host? (Y/N)");

		//Add extra space to avoid exceptions
		serverPrompt = serverPrompt.toUpperCase() + " ";

		boolean server = serverPrompt.charAt(0) == 'Y';

		if (server) {
			String portStr = JOptionPane.showInputDialog("Enter the port to host on.");

			Main.port = Integer.parseInt(portStr);

			boolean started = false;
			try {
				Main.server = new DHServer(port);
				started = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			//Start a thread for receiving messages
			if (started) {
				Thread receiveThread = new Thread(new Runnable() {
					public void run() {
						while (true) {
							try {
								//Get message
								String received = Main.server.receive();
								
								//Show on the window text area
								frame.println(received);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				});
				receiveThread.start();
			}
		}
		else {
			String IPStr = JOptionPane.showInputDialog("Enter the server IP.");

			Main.IP = IPStr;

			String portStr = JOptionPane.showInputDialog("Enter the server port.");

			Main.port = Integer.parseInt(portStr);
			
			boolean started = false;
			try {
				client = new DHClient(IP, port);
				started = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			//Start a thread for receiving messages
			if (started) {
				Thread receiveThread = new Thread(new Runnable() {
					public void run() {
						while (true) {
							try {
								//Get message
								String received = Main.client.receive();
								
								//Show on the window text area
								frame.println(received);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				});
				receiveThread.start();
			}
		}
		
		frame = new Window();
		frame.setVisible(true);
	}
}