package InterfazDominio;

import java.util.Vector;

public interface IDescarga {
	
	public void iniciarDescarga(String url);
	public void descargarCapitulo(int nCapitulo, Vector<String> listaImagenes, String nombreSerie);
}
