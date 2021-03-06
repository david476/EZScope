package space.davidboles.ezscope.window;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import space.davidboles.ezscope.Start;
import space.davidboles.lib.program.Logger;
import space.davidboles.lib.program.ProgramFs;

public class ViewPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1935104195539911123L;
	
	JTextArea display;
	
	Thread updater;
	
	/**
	 * Create the panel.
	 */
	public ViewPanel() {
		setLayout(new BorderLayout(0, 0));
		
		JButton clear = new JButton("Save");
		clear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File loc = ProgramFs.getProgramFile("saves/waveform-" + System.currentTimeMillis() + ".txt");
				try {
					loc.getParentFile().mkdirs();
					loc.createNewFile();
				} catch (IOException e1) {
				}
				
				boolean success = ProgramFs.saveString(loc, display.getText());
				if(success) Start.window.logInfo("Waveform saved!");
				else Start.window.logError("Save failed :(");
			}
		});
		add(clear, BorderLayout.SOUTH);
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		
		display = new JTextArea();
		display.setFont(new Font("DejaVu Sans Mono", Font.PLAIN, 9));
		display.setText("No data.");
		scrollPane.setViewportView(display);

	}
	
	protected void reset() {
		display.setText("");
	}
	
	public void capture() {
		if(updater != null) updater.interrupt();
			
		updater = new Thread(new Runnable(){

			
			@Override
			public void run() {
				try {
					Start.window.control.capture.setEnabled(false);
					
					Start.window.logInfo("Starting capture.");
					double[] data = Start.capture.captureSamples(Start.window.control.numSampVal);
					Start.window.logInfo("Captured " + Start.window.control.numSampVal + " samples.");
					
					if(!Thread.interrupted()) Start.window.control.capture.setEnabled(true);
					
					display.setText("Rendering...");
					
					Logger.uLogger.log("Starting render.");
					int scale = Start.window.control.aFVal;
					String render = "";
					for(int i = 0; i < data.length; i++) {
						double curr = data[i];
						String newRend = "";
						int dash = (int) (Math.abs(curr) * scale);
						
						if(curr < 0) {
							newRend += "|";
							newRend = appendMul(newRend, " ", scale-dash);
							newRend = appendMul(newRend, "-", dash);
							newRend += "|";
							newRend = appendMul(newRend, " ", scale);
							newRend += "|";
						}else {
							newRend += "|";
							newRend = appendMul(newRend, " ", scale);
							newRend += "|";
							newRend = appendMul(newRend, "-", dash);
							newRend = appendMul(newRend, " ", scale-dash);
							newRend += "|";
						}
						
						render += newRend + "\n";
						//this.display.setText(render);
						//Logger.uLogger.log("Render complete of sample " + i);
					}
					Logger.uLogger.log("Render complete");
					
					if(!Thread.interrupted()) {
						display.setText(render);
					}
				}catch(Exception e) {
					if(!Thread.interrupted()) {
						Start.window.logError("Capture failed :(");
						Logger.uLogger.exception("Capturing", e);
						Start.window.control.capture.setEnabled(true);
					}
				}
			}});
		
		updater.start();
		
	}
	
	private String appendMul(String init, String append, int num) {
		for(int i = 0; i < num; i++) {
			init+=append;
		}
		return init;
	}
}
