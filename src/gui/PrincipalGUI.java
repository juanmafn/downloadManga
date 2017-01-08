package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JProgressBar;

import negocio.dominios.DescargaAnimeXtremist;
import negocio.dominios.DescargaMangaReader;
import negocio.dominios.DescargaSubmanga;
import InterfazDominio.IDescarga;

import java.awt.event.FocusEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComboBox;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;



public class PrincipalGUI {

	private int sizeShortY = 225; // antes 140
	private int sizeLongY = 440; // antes 350
	private int sizeX = 460;	// antes 460
	
	public Control control = new Control();
	public boolean enCurso = false;
	
	public JFrame frame;
	public JTextField textURL;
	public JTextField textRuta;
	
	public JComboBox comboBoxDominio;
	public JComboBox comboBoxManga;
	public boolean comboDominioRepe=false;
	public boolean comboMangaRepe=false;
	
	public JProgressBar progressBarTotal;
	public JProgressBar progressBarCapitulo;
	public JLabel informacionProgreso;
	public JLabel informacionImagenes;
	public JLabel informacionCapitulos;
	public JLabel informacionTiempo;
	public JLabel informacionErrores;
	
	final JButton btnDescargar = new JButton("Descargar");
	final JButton btnParar = new JButton("Parar");
	
	public String rutaFichero = null;
	
	public IDescarga descarga = null;// = new DescargaAnimeXtremist(this);
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PrincipalGUI window = new PrincipalGUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public PrincipalGUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle("Download manga - by Juanma");
		frame.setBounds(100, 100, sizeX, sizeShortY);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblSeleccionaDominio = new JLabel("Selecciona Dominio");
		lblSeleccionaDominio.setBounds(12, 12, 153, 15);
		frame.getContentPane().add(lblSeleccionaDominio);
		
		JLabel lblSeleccionaManga = new JLabel("Selecciona Manga");
		lblSeleccionaManga.setBounds(12, 51, 139, 15);
		frame.getContentPane().add(lblSeleccionaManga);
		
		String []listaComboBoxDominios = {"","Submanga","Animextremist","lala","popo"};
		comboBoxDominio = new JComboBox(listaComboBoxDominios);
		comboBoxDominio.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				String seleccionado = (String) comboBoxDominio.getSelectedItem();
				String urlSelect = "";
				if(seleccionado.equals("Submanga")){
					urlSelect = "http://submanga.com/series";
					DescargaSubmanga.obtenerMangas(comboBoxManga, urlSelect, comboDominioRepe);
				}
				else if(seleccionado.equals("Animextremist")){
					urlSelect = "http://animextremist.com/mangas.htm?ord=todos";
					DescargaAnimeXtremist.obtenerMangas(comboBoxManga, urlSelect, comboDominioRepe);
				}
				comboDominioRepe = !comboDominioRepe;
			}
		});
		comboBoxDominio.setBounds(164, 7, 272, 24);
		frame.getContentPane().add(comboBoxDominio);
		
		comboBoxManga = new JComboBox();
		comboBoxManga.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				if(comboBoxDominio.getSelectedItem().equals("Submanga")){
					textURL.setText("http://submanga.com/"+comboBoxManga.getSelectedItem()+"/completa");
				}
				else if(comboBoxDominio.getSelectedItem().equals("Animextremist")){
					textURL.setText("http://animextremist.com/"+comboBoxManga.getSelectedItem()+".htm");
				}
			}
		});
		comboBoxManga.setBounds(163, 46, 273, 24);
		frame.getContentPane().add(comboBoxManga);
		
		JLabel lblUrlManga = new JLabel("URL manga");
		lblUrlManga.setBounds(12, 96, 91, 15);
		frame.getContentPane().add(lblUrlManga);
		
		textURL = new JTextField();
		textURL.setBounds(104, 94, 332, 19);
		frame.getContentPane().add(textURL);
		textURL.setColumns(10);
		
		// botón descargar
		btnDescargar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// elegimos carpeta destino
				final JFileChooser fileChooser;
				if(rutaFichero == null){
					fileChooser = new JFileChooser() {
						private static final long serialVersionUID = 1L;
						public void approveSelection() {
							if (getSelectedFile().isFile()) {
								return;
							} else
								super.approveSelection();
						}
					};
					fileChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
					Component areaTexto = null;
					int seleccion = fileChooser.showSaveDialog(areaTexto);
					if (seleccion == JFileChooser.APPROVE_OPTION){
						rutaFichero = fileChooser.getSelectedFile().getAbsolutePath();
						textRuta.setText(rutaFichero);
					}
				}
				// iniciamos la descarga
				if(rutaFichero != null)
					new Thread(new Runnable() {
						@Override
						public void run() {
							Matcher matcher = Pattern.compile("(http://)?(www.)?([a-zA-Z]+)").matcher(textURL.getText());
							if(matcher.find()){
								if(matcher.group(3).equals("animextremist")){
									descarga = (DescargaAnimeXtremist) new DescargaAnimeXtremist(PrincipalGUI.this);
								}
								else if(matcher.group(3).equals("submanga")){
									descarga = (DescargaSubmanga) new DescargaSubmanga(PrincipalGUI.this);
								}
								else if(matcher.group(3).equals("mangareader")){
									descarga = (DescargaMangaReader) new DescargaMangaReader(PrincipalGUI.this);
								}
								if(matcher.group(1) == null){
									textURL.setText("http://"+textURL.getText());
								}
							}
							else{
								informacionErrores.setText("El link introducido es incorrecto");
								btnParar.setVisible(false);
								btnDescargar.setEnabled(true);
								return;
							}
							frame.setBounds(100, 100, sizeX, sizeLongY);
							btnParar.setText("Parar");
							btnParar.setVisible(true);
							btnDescargar.setEnabled(false);
							descarga.iniciarDescarga(textURL.getText());
							if(!control.stop){
								btnParar.setVisible(false);
								btnDescargar.setEnabled(true);
							}
						}
					}).start();
			}
		});
		btnDescargar.setBounds(319, 160, 117, 25);
		frame.getContentPane().add(btnDescargar);
		
		
		// botón parar/seguir
		btnParar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(btnParar.getText().equals("Parar")){
					control.stop=true;
					btnParar.setText("Seguir");
//					btnDescargar.setEnabled(true);
				}
				else if(btnParar.getText().equals("Seguir")){
					control.stop=false;
					btnParar.setText("Parar");
					new Thread(new Runnable() {
						@Override
						public void run() {
							btnDescargar.setEnabled(false);
							descarga.iniciarDescarga(textURL.getText());
							btnDescargar.setEnabled(true);
						}
					}).start();
				}
			}
		});
		btnParar.setBounds(190, 160, 117, 25);
		btnParar.setVisible(false);
		frame.getContentPane().add(btnParar);
		

		progressBarCapitulo = new JProgressBar();
		progressBarCapitulo.setStringPainted(true);
		progressBarCapitulo.setBounds(12, 197, 424, 15);
		frame.getContentPane().add(progressBarCapitulo);
		
		progressBarTotal = new JProgressBar(1,100);
		progressBarTotal.setValue(0);
		progressBarTotal.setStringPainted(true);
		progressBarTotal.setForeground(Color.red); 
		progressBarTotal.setBackground(Color.white);
		progressBarTotal.setBounds(12, 224, 424, 40);
		frame.getContentPane().add(progressBarTotal);
		
		informacionCapitulos = new JLabel("");
		informacionCapitulos.setBounds(12, 276, 424, 15);
		frame.getContentPane().add(informacionCapitulos);
		
		informacionImagenes = new JLabel("");
		informacionImagenes.setBounds(12, 303, 424, 15);
		frame.getContentPane().add(informacionImagenes);
		
		informacionProgreso = new JLabel("");
		informacionProgreso.setBounds(12, 330, 424, 15);
		frame.getContentPane().add(informacionProgreso);
		
		informacionTiempo = new JLabel("");
		informacionTiempo.setBounds(12, 357, 424, 15);
		frame.getContentPane().add(informacionTiempo);
		
		informacionErrores = new JLabel("");
		informacionErrores.setBounds(12, 384, 424, 15);
		frame.getContentPane().add(informacionErrores);
		
		textRuta = new JTextField();
		textRuta.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				rutaFichero = textRuta.getText();
				if(textRuta.getText().equals(""))
					rutaFichero = null;
			}
		});
		textRuta.setBounds(163, 129, 273, 19);
		frame.getContentPane().add(textRuta);
		textRuta.setColumns(10);
		
		JButton btnExaminarRuta = new JButton("Examinar ruta");
		btnExaminarRuta.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// elegimos carpeta destino
				final JFileChooser fileChooser = new JFileChooser() {
					private static final long serialVersionUID = 1L;
					public void approveSelection() {
						if (getSelectedFile().isFile()) {
							return;
						} else
							super.approveSelection();
					}
				};
				fileChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
				Component areaTexto = null;
				int seleccion = fileChooser.showSaveDialog(areaTexto);
				if (seleccion == JFileChooser.APPROVE_OPTION){
					rutaFichero = fileChooser.getSelectedFile().getAbsolutePath();
					textRuta.setText(rutaFichero);
				}
			}
		});
		btnExaminarRuta.setBounds(12, 123, 139, 25);
		frame.getContentPane().add(btnExaminarRuta);
		
		// Para salir del programa
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we){
				int eleccion = JOptionPane.showConfirmDialog(null, "Desea salir?");
				if ( eleccion == 0) {
					if(!enCurso)
						System.exit(0);
					else if(btnParar.getText().equals("Parar"))
						JOptionPane.showMessageDialog(null, "Antes debe darle a parar");
					else
						System.exit(0);
				}  
			}
		});
	}
	
	public Integer[] tiempoVisible(long tiempo) {
		Integer [] marcador = {0,0,0};	// horas, minitos, segundos
		int h,m,s;
		h = (int) (tiempo/3600);
		m = (int) ((tiempo-(h*3600))/60);
		s = (int) (tiempo-((h*3600)+(m*60)));
		marcador[0]=h;
		marcador[1]=m;
		marcador[2]=s;
		return marcador;
	}
}