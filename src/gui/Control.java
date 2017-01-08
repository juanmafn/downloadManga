package gui;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

public class Control{
	public boolean stop=false;
	public boolean yaExiste=false;
	public int numImagenes = 0;
	public int numImagenesDescargadas;
	public int numImagenesCapitulo;
	public int numImagenesDescargadasCapitulo;
	
	public int numCapitulos;
	public int numCapitulosEncontrados;
	
	public String mensajeInformacionImagenes;
	public String mensajeInformacionCapitulos;
	
	public JLabel informacionImagenes;
	public JLabel informacionProgreso;
	
	public JProgressBar progressBarTotal;
	public JProgressBar progressBarCapitulo;
}
