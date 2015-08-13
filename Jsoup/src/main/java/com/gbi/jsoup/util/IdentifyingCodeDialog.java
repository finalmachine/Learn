package com.gbi.jsoup.util;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class IdentifyingCodeDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = -4449075452346461148L;
	private static int width = 224;
	private static int height = 150;
	
	private ImageIcon image = null;
	private JLabel label = null;
	private JTextField text = null;
	private JButton button = null;
	
	public IdentifyingCodeDialog(ImageIcon img) {
		super();
		if (img == null) {
			throw new NullPointerException();
		}
		this.setTitle("输入验证码");
		setModal(true);
		
		Dimension dim = getToolkit().getScreenSize();
		this.setBounds(dim.width / 2 - width / 2, dim.height / 2 - height / 2, width, height);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setLayout(new FlowLayout());

		image = img;
		label = new JLabel(image, JLabel.CENTER);
		label.setSize(200, 70);
		this.add(label);

		text = new JTextField(14);
		text.setFont(new Font("Consolas", Font.PLAIN, 19));
		this.add(text);

		button = new JButton("Go");
		button.addActionListener(this);
		this.add(button);
	}
	
	public String showDialog() {
		this.setVisible(true);
		return text.getText();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == button) {
			if (text.getText().length() > 0) {
				this.dispose();
			} else {
				JOptionPane.showMessageDialog(this, "你还没有输入验证码");
			}
		}
	}
	
	public static void main(String[] args) throws MalformedURLException {
		System.out.println("1");
		String result = new IdentifyingCodeDialog(new ImageIcon(IdentifyingCodeDialog.class.getResource("wait.jpg"))).showDialog();
		System.out.println(result);
		System.out.println("2");
	}
}
