package edu.mccc.cos210.fp.dice;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.imageio.ImageIO;
import javax.swing.*;

public class CroupierUI {
	public static void main(String... args) {
		EventQueue.invokeLater(CroupierUI::new);
	}
	public CroupierUI() {
		initSwing();
	}
	private static void initSwing() {
		JFrame jf = new JFrame("Croupier2019");
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel Ip = new IntroPanel();
	    jf.add(Ip);
		jf.pack();
		jf.setLocationRelativeTo(null);
		jf.setResizable(false);
		jf.setVisible(true);
	}
	static class IntroPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private JButton back;
		private JButton close;
		BufferedImage bgImg;
	public void showFileChoicePage() {
			removeAll();
			repaint();
			try {
				bgImg = ImageIO.read(new File("images/dicebg3.jpg"));
			} catch (IOException e) {
				e.printStackTrace();
			} 
			JLabel title = new JLabel("Croupier2019");
		    title.setFont(new Font("Brush Script MT",1,60));
		    title.setBounds(55, 10, 500, 80);
		    title.setForeground(Color.BLUE);
		    add(title);
		    JLabel credits  = new JLabel("<html><p style='text-align: right'>"
		    		+ "Created By:<br/>---------<br/>"
		    		+ "Rohan Joshi<br/>"
		    		+ "Anant Sheshadri<br/>"
		    		+ "Alhassane Traore<br/>"
		    		+ "Wesley Wang"
		    		);
		    credits.setFont(new Font("Arial",1,20));
		  // add(credits);
		    credits.setBounds(398, 20, 200, 200);
		    credits.setForeground(Color.BLUE);
		    JLabel desc = new JLabel("<html><p style='text-align: justify'>"
		    		+ "This program takes an image of dice and provides "
		    		+ "information about the dice</p>");
		    desc.setFont(new Font("Arial",1,20));
		  //  add(desc);
		    desc.setBounds(30, 20, 230, 200);
		    desc.setForeground(Color.BLUE);
		    back = new JButton("Back");
		    back.setBounds(370, 640, 80, 40);
		    back.setBorder(new Border() {
				@Override
				public void paintBorder(Component c, Graphics g, int x, int y,
						int width, int height) {
					g.drawRoundRect(x, y, width-1, height-1, 10, 10);
				}
				@Override
				public Insets getBorderInsets(Component c) {
					return new Insets(11, 11, 12, 10);
				}
				@Override
				public boolean isBorderOpaque() {
					return true;
				}		    	
		    });		    
		    back.addActionListener(ae->{ showFileChoicePage(); });
		    back.setForeground(Color.BLUE);
		    close = new JButton("Goodbye!");
		    close.setBounds(470, 640, 80, 40);
		    close.setBorder(new Border() {
				@Override
				public void paintBorder(Component c, Graphics g, int x, int y, 
						int width, int height) {
					g.drawRoundRect(x, y, width-1, height-1, 10, 10);
				}
				@Override
				public Insets getBorderInsets(Component c) {
					return new Insets(11, 11, 12, 10);
				}
				@Override
				public boolean isBorderOpaque() {
					return true;
				}		    	
		    });		    
		    close.addActionListener(ae->{ System.exit(0); });
		    close.setForeground(Color.BLUE);
		    JButton def = new JButton("Default Image");
		    def.setBounds(170, 640, 120, 50);
		    def.setForeground(Color.RED);
		    def.setBorder(new Border() {
				@Override
				public void paintBorder(Component c, Graphics g, int x, int y, 
						int width, int height) {
					g.drawRoundRect(x, y, width - 1, height - 1, 10, 10);
				}
				@Override
				public Insets getBorderInsets(Component c) {
					return new Insets(11, 11, 12, 10);
				}
				@Override
				public boolean isBorderOpaque() {
					return true;
				}		    	
		    });
		    add(def);
		    def.addActionListener(
					ae -> {
						try {
							showResultPage(new File("images/dice1.jpg"));
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
		    JButton b = new JButton("Choose Photo");
		    b.setBounds(320, 640, 120, 50);
		    b.setForeground(Color.GREEN);
		    b.setBorder(new Border() {
				@Override
				public void paintBorder(Component c, Graphics g, int x, int y, 
						int width, int height) {
					g.drawRoundRect(x, y, width - 1, height - 1, 10, 10);
				}
				@Override
				public Insets getBorderInsets(Component c) {
					return new Insets(11, 11, 12, 10);
				}
				@Override
				public boolean isBorderOpaque() {
					return true;
				}		    	
		    });
			add(b);
			b.addActionListener(
					ae -> {
			            JFileChooser j = new JFileChooser();
			            j.showOpenDialog(null);
			            File input = j.getSelectedFile();
			            if(input == null) {
			            	return;
			            }
			            try {
							showResultPage(input);
						} catch (IOException e) {
							e.printStackTrace();
						}
					});			
		}
	public void showResultPage(File imageFile) throws IOException {
			removeAll();
			repaint();
			BufferedImage img = ImageIO.read(imageFile);
			BufferedImage img2 = Filter.getScaled(img);
			BufferedImage imgSobel = Filter.getSobel(img2);
			Result circleResult = Filter.circleCount(imgSobel);
			BufferedImage dotImage = circleResult.theImage;
			Result squareResult = Filter.squareCount(dotImage);
			BufferedImage edgeImage = squareResult.theImage;
			
			int dot_count = circleResult.theCount;
			int dice_count = squareResult.theCount;
			
			BufferedImage mergedNeon = Filter.mergeImages(dotImage, edgeImage, 
					true);
			BufferedImage mergedWhite = Filter.mergeImages(dotImage, edgeImage,
					false);
			//alter dot_count to only count dots found on a die surface
			dot_count = 0;
			for(int i=1;i<=6;i++) {
				dot_count += i*Filter.getDiceCount(mergedNeon, i);
			}
			JLabel picLabel = new JLabel(new ImageIcon(img2));
			JLabel picLabel2 = new JLabel(new ImageIcon(mergedNeon));
			JLabel picLabel3 = new JLabel(new ImageIcon(mergedWhite));
			add(picLabel);
			add(picLabel2);
			add(picLabel3);
			picLabel.setBounds(0, 0, 276, 368);
			picLabel2.setBounds(0, 368, 276, 368);
			picLabel3.setBounds(0, 368, 276, 368);
			
			picLabel2.setVisible(false);
			picLabel2.addMouseListener(new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {
					picLabel2.setVisible(false);
					picLabel3.setVisible(true);
				}
				@Override public void mousePressed(MouseEvent e) {}
				@Override public void mouseReleased(MouseEvent e) {}
				@Override public void mouseEntered(MouseEvent e) {}
				@Override public void mouseExited(MouseEvent e) {}
			});
			picLabel3.addMouseListener(new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {
					picLabel3.setVisible(false);
					picLabel2.setVisible(true);
				}
				@Override public void mousePressed(MouseEvent e) {}
				@Override public void mouseReleased(MouseEvent e) {}
				@Override public void mouseEntered(MouseEvent e) {}
				@Override public void mouseExited(MouseEvent e) {}
			});
			JLabel ans = new JLabel("<html><p style='text-align: right; "
					+ "font-size: 110%;'>Total Dice: " + dice_count 
					+ "</p></html>");
			ans.setFont(new Font("Arial",1,18));
			ans.setBounds(286, -10, 250, 50);
			ans.setForeground(Color.RED);
			add(ans);
			JLabel ans2 = new JLabel("<html><p style='text-align: right; "
					+ "font-size: 110%;'>Total Value of Dice: " + dot_count
					+ "</p></html>");
			ans2.setFont(new Font("Arial",1,18));
			ans2.setBounds(286, 20, 250, 50);
			ans2.setForeground(Color.RED);
			add(ans2);
			JLabel ans3 = new JLabel("<html><p style='text-align: right; "
					+ "font-size: 105%;'>Types of Dice:<br/>------------"
					+"<br/>1's: "+Filter.getDiceCount(mergedNeon, 1)
					+"<br/>2's: "+Filter.getDiceCount(mergedNeon, 2)
					+"<br/>3's: "+Filter.getDiceCount(mergedNeon, 3)
					+"<br/>4's: "+Filter.getDiceCount(mergedNeon, 4)
					+"<br/>5's: "+Filter.getDiceCount(mergedNeon, 5)
					+"<br/>6's: "+Filter.getDiceCount(mergedNeon, 6) 
					+ "</p>");
			ans3.setFont(new Font("Arial",1,20));
			ans3.setBounds(395, 75, 250, 200);
			ans3.setForeground(Color.RED);
			add(ans3);
			add(back);
			add(close);
		}
	public IntroPanel() {
			super();
			setBackground(Color.WHITE);
			setPreferredSize(new Dimension(600, 736));
			setLayout(null);
			showFileChoicePage();
		}
	public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(bgImg,50,80,this);
		}
	}
}