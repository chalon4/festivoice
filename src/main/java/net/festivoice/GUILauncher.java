/*
 * festivoice
 *
 * Copyright 2009 FURUHASHI Sadayuki, KASHIHARA Shuzo, SHIBATA Yasuharu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package net.festivoice;

import java.lang.*;
import java.util.*;
import java.net.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;

public class GUILauncher extends JFrame
{
	private JButton submitButton;
	private JList userList;
	private JTextField channelField;
	private JTextField userField;
	private Client client;
	private JCheckBox listenOnlyCheckBox;
	private JButton statusButton;

	private boolean memberExists = false;

	private String host;
	private int port;
	private int mode;
	private int quality;
	private boolean vbr;

	public GUILauncher(String host, int port, String channel, String user)
	{
		this.host = host;
		this.port = port;
		this.mode = 2;
		this.quality = 8;
		this.vbr = true;

		initComponents();

		userField.setText(user);
		channelField.setText(channel);

		//EventQueue.invokeLater(new Runnable() {
		//	public void run() {
		//		tryStart();
		//	}
		//});
	}

	private void initComponents()
	{
		try{
			UIManager.getInstalledLookAndFeels();
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			e.printStackTrace();
		}

		setMinimumSize(new Dimension(270, 150));

		JPanel c0 = new JPanel();
		c0.setBorder(new javax.swing.border.EmptyBorder(5, 5, 5, 5));
		getContentPane().add(c0, BorderLayout.CENTER);

		c0.setLayout(new BorderLayout(2, 2));


		// userLabel: userField   channelLabel: channelField
		JPanel c1 = new JPanel();
		c1.setLayout(new BoxLayout(c1, BoxLayout.X_AXIS));
		c0.add(c1, BorderLayout.PAGE_START);

		// userLabel: userField
		JLabel userLabel = new JLabel();
		userLabel.setText("ユーザー名");
		c1.add(userLabel);

		c1.add(Box.createRigidArea(new java.awt.Dimension(3,1)));

		userField = new JTextField(6);
		c1.add(userField);


		// gap
		c1.add(Box.createRigidArea(new java.awt.Dimension(5,1)));


		// channelLabel: channelField
		JLabel channelLabel = new JLabel();
		channelLabel.setText("チャンネル");
		c1.add(channelLabel);

		c1.add(Box.createRigidArea(new java.awt.Dimension(3,1)));

		channelField = new JTextField(6);
		c1.add(channelField);


		// userList
		userList = new JList(new DefaultListModel());
		userList.setEnabled(false);

		JScrollPane c2 = new JScrollPane();
		c2.setViewportView(userList);

		c0.add(c2, BorderLayout.CENTER);


		// statusButton  [x]listen only   [submitButton]
		JPanel c3 = new JPanel();
		c3.setLayout(new BorderLayout());
		c0.add(c3, BorderLayout.PAGE_END);


		// talk button
		statusButton = new JButton();
		statusButton.setText("未接続");
		statusButton.setEnabled(false);

		statusButton.addChangeListener( new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				talkButtonChanged();
			}
		});
		c3.add(statusButton, BorderLayout.LINE_START);



		// checkbox
		listenOnlyCheckBox = new JCheckBox();
		listenOnlyCheckBox.setText("listen only");
		listenOnlyCheckBox.setEnabled(false);

		listenOnlyCheckBox.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					changeListenOnlyCheckBox();
				}
			});

		c3.add(listenOnlyCheckBox, BorderLayout.CENTER);


		// [submitButton]
		submitButton = new JButton();
		submitButton.setText("接続");
		submitButton.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					submitPerformed();
				}
			});

		c3.add(submitButton, BorderLayout.LINE_END);


		// click submitButton on enter key event
		KeyAdapter submitOnEnterKeyAdapter = new KeyAdapter() {
				public void keyPressed(KeyEvent event) {
					if(event.getKeyCode() == KeyEvent.VK_ENTER) {
						event.consume();
						doSubmit();
					}
				}
			};

		channelField.addKeyListener(submitOnEnterKeyAdapter);
		userField.addKeyListener(submitOnEnterKeyAdapter);
		submitButton.addKeyListener(submitOnEnterKeyAdapter);

		setTitle("festivoice.net");

		pack();
	}

	private void changeListenOnlyCheckBox()
	{
		if(client == null) { return; }

		if(listenOnlyCheckBox.isSelected()) {
			statusButton.setEnabled(true);
			statusButton.requestFocusInWindow();
		} else {
			statusButton.setEnabled(false);
		}
		talkButtonChanged();
	}

	private void talkButtonChanged()
	{
		if(client == null) { return; }

		if(!isListenOnly() || statusButton.getModel().isPressed()) {
			client.setListenOnly(false);
			if(!memberExists) {
				statusButton.setText("接続待ち");
			} else {
				statusButton.setText("会話中");
			}
		} else {
			client.setListenOnly(true);
			if(!memberExists) {
				statusButton.setText("接続待ち");
			} else {
				statusButton.setText("話す");
			}
		}
	}

	private void doSubmit()
	{
		submitButton.doClick();
	}

	private void submitPerformed()
	{
		if(!isStarted()) {
			tryStart();
			listenOnlyCheckBox.requestFocusInWindow();
		} else {
			System.exit(0);
		}
	}

	private void clientInitialized()
	{
		if(client.isMicAvailable()) {
			listenOnlyCheckBox.setEnabled(true);
		} else {
			listenOnlyCheckBox.setSelected(true);
			listenOnlyCheckBox.setText("マイクが使えません");
		}
		talkButtonChanged();
	}

	private void updateUserList()
	{
		DefaultListModel model = (DefaultListModel)userList.getModel();

		model.clear();

		model.addElement(userField.getText());

		memberExists = false;
		for (IClientUserInfo userInfo : client.getClientUserInfoIterator()) {
			model.addElement(userInfo.getUserName());
			memberExists = true;
		}

		talkButtonChanged();
	}

	private void tryStart()
	{
		final String user = userField.getText();
		final String channel = channelField.getText();

		if(isStarted() || user.equals("") || channel.equals("")) {
			return;
		}

		Client tmpClient;
		try {
			tmpClient = new Client(channel, user, new InetSocketAddress(host, port),
					mode, quality, vbr);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					e.getStackTrace(),
					e.toString(),
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		statusButton.setText("初期化中");

		tmpClient.setInitCallback(new Runnable() {
				public void run() {
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							clientInitialized();
						}
					});
				}
			});

		tmpClient.setUserUpdateCallback(new Runnable() {
				public void run() {
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							updateUserList();
						}
					});
				}
			});

		userField.setEnabled(false);
		channelField.setEnabled(false);
		userList.setEnabled(true);
		submitButton.setText("終了");

		tmpClient.start();

		client = tmpClient;
		updateUserList();
	}

	private boolean isListenOnly()
	{
		return listenOnlyCheckBox.isSelected();
	}

	private boolean isStarted()
	{
		return client != null;
	}

	public static void main(String[] args)
	{
		if(args.length != 3 && args.length != 4) {
			throw new IllegalArgumentException("Arguments number is wrong.");
		}

		String host = args[0];
		int port = Integer.parseInt(args[1]);

		String channel = args[2];
		if(channel.equals("-")) {
			channel = "";
		}

		String user;
		if(args.length > 3 && !args[3].equals("") && !args[3].equals("-")) {
			user = args[3];
		} else {
			user = System.getProperty("user.name");
		}

		GUILauncher launcher = new GUILauncher(host, port, channel, user);
		launcher.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		launcher.setVisible(true);
	}
}

