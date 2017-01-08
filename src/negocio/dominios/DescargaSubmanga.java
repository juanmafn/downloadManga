package negocio.dominios;

import gui.Control;
import gui.PrincipalGUI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComboBox;

import InterfazDominio.IDescarga;

public class DescargaSubmanga implements IDescarga {

	PrincipalGUI pg;
	
	public DescargaSubmanga(PrincipalGUI pg){
		this.pg = pg;
	}

	static public void obtenerMangas(JComboBox comboBoxManga, String urlSelect, boolean comboDominioRepe){
		if(!comboDominioRepe){
			comboBoxManga.removeAllItems();
			URL pagina = null;
			BufferedReader in = null;
			String contenido = null,s;
			Matcher matcher;
			Vector <String> capitulos = new Vector<String>();
			if(!urlSelect.equals("")){
				System.out.println("Seleccionada url: " + urlSelect);
				try {
					pagina = new URL(urlSelect);
					in = new BufferedReader(new InputStreamReader(pagina.openStream()));
					
					while((s = in.readLine()) != null){
						contenido += s;
					}
					
	//				matcher = Pattern.compile("<td>[ \t\n]*<a href=\"([a-zA-Z:/.]*)\"").matcher(contenido);
					matcher = Pattern.compile("<td>[ \t\n]*<a href=\"[a-zA-Z:/.]*/([^><]*)\">").matcher(contenido);
					while(matcher.find()){
						capitulos.add(matcher.group(1));
						System.out.println(matcher.group(1));
	//					comboBoxManga.addItem(matcher.group(1));
					}
					Collections.sort(capitulos);
					for(String ss:capitulos){
						comboBoxManga.addItem(ss);
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else{
				System.out.println("No se ha seleccionado ninguna url");
			}
		}
	}
	
	@Override
	public void iniciarDescarga(String url) {
		
		pg.enCurso = true;
		
		pg.informacionProgreso.setText("");
		pg.informacionImagenes.setText("");
		pg.informacionCapitulos.setText("");
		pg.informacionTiempo.setText("");
		pg.informacionErrores.setText("");
		
		pg.control.progressBarTotal = pg.progressBarTotal;
		pg.control.progressBarCapitulo = pg.progressBarCapitulo;
		
		Pattern pattern;
		Matcher matcher;
		pattern = Pattern.compile("http://submanga.com/.+/completa");
		matcher = pattern.matcher(url);
		if(!matcher.find()){
			pg.informacionErrores.setText("La URL introducida no es correcta");
			pg.enCurso = false;
			return;
		}
		
		URL pagina = null;
		BufferedReader in = null;
		String contenido = null;
		String s = null;
		String subhost = "";
		String[] sh;
		String nombreSerie;
		String capitulo;
		Vector <String> capitulos = new Vector<String>();
		int nCapitulo = 0;
		pg.control.numImagenes = 0;
		pg.control.numImagenesDescargadas = 0;
		Hashtable<Integer, Vector<String>> manga = new Hashtable<Integer, Vector<String>>();
		try {
			pg.informacionCapitulos.setText("Buscando capítulos....");
			if(url.equals("")){
				pg.informacionCapitulos.setText("No hay link para descargar");
				return;
			}
			pagina = new URL(url);
			in = new BufferedReader(new InputStreamReader(pagina.openStream()));
			while((s = in.readLine()) != null){
				contenido += s;
			}
			sh = url.split("/");
			for(int i=0;i<sh.length-1;i++){
				subhost += sh[i]+"/";
			}
			nombreSerie = subhost.split("/")[3];
			
			File dir = new File(pg.rutaFichero+"/"+nombreSerie);
			if (!dir.exists())
			  if (!dir.mkdir()){
				  pg.informacionErrores.setText("no se pudo crear la carpeta de destino");
				  return;
			  }
			
			pattern = Pattern.compile(subhost+"[0-9]*/[0-9]*");
			matcher = pattern.matcher(contenido);
			while(matcher.find()){
				capitulos.add(matcher.group(0));
			}
			pg.control.numCapitulos = capitulos.size();
			pg.control.numCapitulosEncontrados=0;
			pg.informacionCapitulos.setText("Encontrados " + pg.control.numCapitulos + " capítulos");
			Vector <EncontrarImagenesCapituloSubmanga> id_hilos = new Vector<EncontrarImagenesCapituloSubmanga>();
			int tope;
			int maxHilos=50;
			for(tope=0; tope<maxHilos;){
				if(!pg.control.stop && tope + maxHilos <= capitulos.size()){
					for(int i=tope;i<tope+maxHilos;i++){
						matcher = Pattern.compile("/[0-9]+/").matcher(capitulos.get(i));
						if(matcher.find())
							nCapitulo = new Integer(matcher.group(0).replaceAll("/", ""));
						capitulo = "http://submanga.com/c/"+capitulos.get(i).split("/")[5];
						EncontrarImagenesCapituloSubmanga h = new EncontrarImagenesCapituloSubmanga(capitulo, nCapitulo, manga, pg.control);
						h.start();
						id_hilos.add(h);
					}
					for(EncontrarImagenesCapituloSubmanga eic : id_hilos){
						try {
							eic.join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					tope+=maxHilos;
				}
				else break;
			}
			if(!pg.control.stop){
				for(int i=tope;i<capitulos.size();i++){
					matcher = Pattern.compile("/[0-9]+/").matcher(capitulos.get(i));
					if(matcher.find())
						nCapitulo = new Integer(matcher.group(0).replaceAll("/", ""));
					capitulo = "http://submanga.com/c/"+capitulos.get(i).split("/")[5];
					EncontrarImagenesCapituloSubmanga h = new EncontrarImagenesCapituloSubmanga(capitulo, nCapitulo, manga, pg.control);
					h.start();
					id_hilos.add(h);
				}
				for(EncontrarImagenesCapituloSubmanga eic : id_hilos){
					try {
						eic.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				pg.control.mensajeInformacionImagenes = "Se han recolectado " + pg.control.numImagenes + " imágenes";
				pg.control.informacionImagenes = pg.informacionImagenes;
				pg.informacionImagenes.setText(pg.control.mensajeInformacionImagenes); //TODO
			}
			else
				pg.informacionErrores.setText("Parado\n");
			
			Set<Integer> names = manga.keySet();
			ArrayList<Integer> namesList = new ArrayList<Integer>(names);
			Collections.sort(namesList);
			Iterator <Integer> i = namesList.iterator();
			long t_ini, tiempo;
			int quedan = manga.size();
			int llevo = 0;
			t_ini = System.currentTimeMillis();
			Integer[] marcador;
			pg.control.informacionProgreso = pg.informacionProgreso;
			pg.progressBarTotal.paintImmediately(0, 0, 424, 40);
			pg.progressBarCapitulo.paintImmediately(0, 0, 424, 40);
			while(!pg.control.stop && i.hasNext()){
				pg.control.yaExiste = false;
				nCapitulo = i.next();
				pg.control.mensajeInformacionCapitulos = "Descargando capítulo " + nombreSerie+"/"+nCapitulo;
				pg.informacionProgreso.setText(pg.control.mensajeInformacionCapitulos);
				pg.control.numImagenesCapitulo = manga.get(nCapitulo).size();
				pg.control.numImagenesDescargadasCapitulo = 0;
				descargarCapitulo(nCapitulo, manga.get(nCapitulo), nombreSerie);
				if(pg.control.yaExiste){
					t_ini = System.currentTimeMillis();
					quedan--;
					pg.control.yaExiste = false;
				}
				else{
					tiempo = System.currentTimeMillis() - t_ini;
					llevo++;
					quedan--;
					marcador = pg.tiempoVisible(((tiempo/llevo)*quedan)/1000);
					
					if(marcador[0] == 0 && marcador[1] == 0)
						pg.informacionTiempo.setText("Tiempo estimado: " + marcador[2]+"s");
					else if(marcador[0] == 0)
						pg.informacionTiempo.setText("Tiempo estimado: " + marcador[1]+"m " + marcador[2] + "s");
					else
						pg.informacionTiempo.setText("Tiempo estimado: " + marcador[0]+"h " + marcador[1] + "m " + marcador[2] + "s");
				}
				float porcentajeFloat = ((float)pg.control.numImagenesDescargadas*100/pg.control.numImagenes);//TODO: falta para el segundo progressbar
				String porcentaje = String.format("%.2f", porcentajeFloat); 
				
				pg.control.progressBarTotal.setValue((int)porcentajeFloat);
				pg.control.progressBarTotal.setString("Total: ("+pg.control.numImagenes+"/"+pg.control.numImagenesDescargadas+") - "+porcentaje+"%");
			}
			if(!pg.control.stop){
				pg.informacionProgreso.setText("Descarga completada");
				pg.informacionTiempo.setText("");
			}
			else
				pg.informacionProgreso.setText("Parado");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		pg.enCurso = false;
	}

	@Override
	public void descargarCapitulo(int nCapitulo, Vector<String> listaImagenes,
			String nombreSerie) {
		String folder = pg.rutaFichero+"/"+nombreSerie+"/"+nCapitulo;
		File dir = new File(folder); 
		if (!dir.exists())
		  if (!dir.mkdir()){
			  System.err.println("no se pudo crear la carpeta de destino");
			  return;
		  }
		Vector <DescargarImagenesSubmanga> id_hilos = new Vector<DescargarImagenesSubmanga>();
		for(String imagen : listaImagenes){
			DescargarImagenesSubmanga di = new DescargarImagenesSubmanga(imagen, nCapitulo, nombreSerie, pg.rutaFichero, pg.control);
			di.start();
			id_hilos.add(di);
		}
		for(DescargarImagenesSubmanga di : id_hilos){
			try {
				di.join();
			} catch (InterruptedException e) {
				System.err.println("Error al esperar a los hilos de descarga de imágenes");
				e.printStackTrace();
			}
		}
	}
	
}

class EncontrarImagenesCapituloSubmanga extends Thread{
	private String url;
	private int index;
	private Hashtable<Integer, Vector <String>> manga;
	private Control control;
	public EncontrarImagenesCapituloSubmanga(String url, int index, Hashtable<Integer, Vector <String>> manga, Control control) {
		super();
		this.url = url;
		this.index = index;
		this.manga = manga;
		this.control = control;
	}
	
	public void run(){
		URL pagina = null;
		BufferedReader in = null;
		String contenido = "", s="";
		String imagen = "";
		String [] ima;
		int numImages = 0;
		Vector <String> listaImagenes = new Vector<String>();
		try {
			pagina = new URL(url);
			in = new BufferedReader(new InputStreamReader(pagina.openStream()));
			while((s = in.readLine()) != null){
				contenido += s;
			}
			Matcher matcher = Pattern.compile("value=\"[0-9]*\"").matcher(contenido);
			while(matcher.find()) numImages++;
			control.numImagenes+=numImages;
//			System.out.println(numImages);
			matcher = Pattern.compile("http://[a-z.]*submanga.com/[a-z/0-9]*.jpg").matcher(contenido);
			if(matcher.find())
				imagen = matcher.group(0);
			ima = imagen.split("/");
			imagen="";
			for(int i=0;i<ima.length-1;i++){
				imagen += ima[i]+"/";
			}
//			System.out.println(imagen);
			for(int i=1;i<=numImages;i++){
				listaImagenes.add(imagen+i+".jpg");
			}
			synchronized (manga) {
				manga.put(index, listaImagenes);		
			}
		} catch (IOException e) {
			System.err.println("Error al encontrar las imágenes del capítulo " + index);
			e.printStackTrace();
		}
		synchronized (control) {
			control.numCapitulosEncontrados++;
			float porcentajeFloat = ((float)control.numCapitulosEncontrados*100/control.numCapitulos);
			String porcentaje = String.format("%.2f", porcentajeFloat);
			control.progressBarCapitulo.setValue((int)porcentajeFloat);	
			control.progressBarCapitulo.setString("Obteniendo imágenes - "+porcentaje+"%");
		}
	}

}

class DescargarImagenesSubmanga extends Thread{
	private String urlImagen;
	private int nCapitulo;
	private String nombreSerie;
	private String ruta;
	private Control control;

	public DescargarImagenesSubmanga(String urlImagen, int nCapitulo, String nombreSerie, String ruta, Control control) {
		super();
		this.urlImagen = urlImagen;
		this.nCapitulo = nCapitulo;
		this.nombreSerie = nombreSerie;
		this.ruta = ruta;
		this.control = control;
	}
	
	public void run(){
		String nombreImagen = "";
		String directorio = "";
		Matcher matcher;
		File file;
		
		matcher = Pattern.compile("[0-9]+.jpg").matcher(urlImagen);
		if(matcher.find())
			nombreImagen = matcher.group(0);
		
		directorio = ruta+"/"+nombreSerie+"/"+nCapitulo+"/"+nombreImagen;
		
		file = new File(directorio);
		
		if(file.exists()){
			synchronized (control) {
				control.numImagenesDescargadas++;
				control.informacionImagenes.setText(control.mensajeInformacionImagenes + " (" + control.numImagenesDescargadas + ")");
				
				control.numImagenesDescargadasCapitulo++;
				control.informacionProgreso.setText(control.mensajeInformacionCapitulos + "(" + control.numImagenesCapitulo + "/" + control.numImagenesDescargadasCapitulo + ")");
				
				control.yaExiste = true;
			}
		}
		
		boolean reintentar = true;
		if(!file.exists() && reintentar){
			reintentar = false;
			try {
				URLConnection conn = new URL(urlImagen).openConnection();
				conn.connect();
				InputStream in = conn.getInputStream();
				OutputStream out = new FileOutputStream(file);
				
				int b = 0;
				while (b != -1) {
				  b = in.read();
				  if (b != -1)
				    out.write(b);
				}
				
				out.close();
				in.close();
			} catch (MalformedURLException e) {
				reintentar = true;
				System.out.println("la url: " + urlImagen + " no es valida!");
			} catch (IOException e) {
				reintentar = true;
				System.err.println("la url: " + urlImagen + " ha fallado!");
				e.printStackTrace();
			}
			finally{
				if(reintentar){
//					try {
//						Thread.sleep(5000);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
				}
				else{
					synchronized (control) {
						control.numImagenesDescargadas++;
						control.informacionImagenes.setText(control.mensajeInformacionImagenes + " (" + control.numImagenesDescargadas + ")");
						control.numImagenesDescargadasCapitulo++;
						control.informacionProgreso.setText(control.mensajeInformacionCapitulos + " (" + control.numImagenesCapitulo + "/" + control.numImagenesDescargadasCapitulo + ")");
					}
				}
			}
		}
		float porcentajeFloat;
		String porcentaje;
		synchronized (control) {
			porcentajeFloat = ((float)control.numImagenesDescargadasCapitulo*100/control.numImagenesCapitulo);
			porcentaje = String.format("%.2f", porcentajeFloat);
			control.progressBarCapitulo.setValue((int)porcentajeFloat);	
			control.progressBarCapitulo.setString("Capítulo "+String.valueOf(nCapitulo)+": (" + control.numImagenesCapitulo + "/" + control.numImagenesDescargadasCapitulo + ") - "+porcentaje+"%");
			
			porcentajeFloat = ((float)control.numImagenesDescargadas*100/control.numImagenes);
			porcentaje = String.format("%.2f", porcentajeFloat); 
			control.progressBarTotal.setValue((int)porcentajeFloat);
			control.progressBarTotal.setString("Total: ("+control.numImagenes+"/"+control.numImagenesDescargadas+") - "+porcentaje+"%");
		}

	}

}