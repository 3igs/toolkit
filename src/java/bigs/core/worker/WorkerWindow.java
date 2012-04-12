package bigs.core.worker;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JTextPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridLayout;
import javax.swing.SwingConstants;
import java.awt.FlowLayout;

import javax.swing.JLabel;

import bigs.api.exceptions.BIGSException;
import bigs.core.BIGS;
import bigs.core.BIGSProperties;
import bigs.core.utils.Core;
import bigs.core.utils.Log;
import bigs.core.utils.Text;

import java.awt.Font;

public class WorkerWindow extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	
	private static String bigsPropertiesFromArgs = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		if (args.length>0) {
			bigsPropertiesFromArgs = Core.decrypt(args[0]);
Log.info("bigs properties are\n"+bigsPropertiesFromArgs);			
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					WorkerWindow frame = new WorkerWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	JTextPane textPane;
	Worker worker;
	JButton buttonStartWorker;
	private JPanel contentPane;
	private JPanel panel_1;
	private JLabel label;
	private JLabel labelLog;
	private JButton btnShowCurrentExploration;

	public WorkerWindow() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		buttonStartWorker = new JButton("Start BIGS Worker");
		buttonStartWorker.addActionListener(this);
		panel.add(buttonStartWorker);
		
		JButton btnExit = new JButton("Exit");
		panel.add(btnExit);
		
		btnShowCurrentExploration = new JButton("Update current exploration progress");
		panel.add(btnShowCurrentExploration);
		btnShowCurrentExploration.setVisible(false);
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		
		btnShowCurrentExploration.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (worker.currentEvaluation == null) {
					textPane.setText("no current exploration yet");
				} else {
					String r = worker.currentEvaluation.getParentExploration().getInfo();
					textPane.setText(r);
				}
			}
		});
		
		textPane = new JTextPane();
		textPane.setFont(new Font("Lucida Sans Typewriter", Font.PLAIN, 10));
		contentPane.add(textPane, BorderLayout.CENTER);
		new  bigs.core.utils.CopyPasteRightClickMenu(textPane);

		label = new JLabel("Insert configuration");
		
		if (bigsPropertiesFromArgs!=null) {
			String displayString = "";
			for (String s: Text.splitString(bigsPropertiesFromArgs, "\n")) {
				if (!Text.matchesRegExp(s, "password")) {
					displayString = displayString+s+"\n";
				} else {
					displayString = displayString+"[password]\n";
				}
			}
			textPane.setText(displayString);
			textPane.setEditable(false);
			label.setText("configuration set. click to start worker");			
		}
		
		panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.NORTH);
		panel_1.setLayout(new GridLayout(2, 0, 0, 0));
		
		label.setHorizontalAlignment(SwingConstants.CENTER);
		panel_1.add(label);
		
		labelLog = new JLabel("");
		labelLog.setFont(new Font("Lucida Sans Typewriter", Font.PLAIN, 10));
		panel_1.add(labelLog);
	}
	
	
	public void actionPerformed(ActionEvent arg0) {

		class WorkerThread extends Thread {
			String configuration = "";
			public WorkerThread(String str) { super (str); }
			public void setConfiguration(String s) { configuration = s;}					
			public void run() {
				BIGS.globalProperties = new BIGSProperties();
Log.info("----- using bigsproperties ----\n"+configuration);				
				BIGS.globalProperties = BIGSProperties.fromString(configuration);
				worker = new Worker();
				try {
					worker.start();
				} catch (BIGSException e) {
					textPane.setText(Core.getStackTrace(e));
					btnShowCurrentExploration.setVisible(false);
					contentPane.revalidate();					
				}
				
			}
		}
		
		WorkerThread wt = new WorkerThread("worker");
		// if no properties were given as arguments use the user provided ones in the textbox
		if (bigsPropertiesFromArgs!=null) {
			wt.setConfiguration(bigsPropertiesFromArgs);
			Log.info("using embedded properties");
		} else {
			wt.setConfiguration(textPane.getText());
			Log.info("using user provided properties");
		}
		wt.start();
		textPane.setEditable(false);
		contentPane.remove(buttonStartWorker);
		buttonStartWorker.setVisible(false);
		btnShowCurrentExploration.setVisible(true);
		contentPane.revalidate();
		this.repaint();
		Log.info("worker thread started");
		
		// a thread to update the label indicating what is the worker doing
		class LabelUpdateThread extends Thread {
			public LabelUpdateThread(String str) { super(str);}
			public void run() {
				String dots = ".";
				while (true) {
					dots=dots+".";
					if (worker!=null && worker.currentEvaluation!=null) {
						label.setText("Working on: "+worker.currentEvaluation.getRowKey()+" "+dots);
					} else {
						label.setText("Worker idle");
					}
					labelLog.setText(Log.getLastLogMessage());
					if (dots.length()>5) dots=".";
					Core.sleep(3000L);
				}
			}
		}
		
		new LabelUpdateThread("labelUpdate").start();
	}
}
