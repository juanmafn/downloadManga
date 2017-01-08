package negocio.dominios;

import gui.Control;
import gui.PrincipalGUI;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import InterfazDominio.IDescarga;

public class DescargaMangaReader implements IDescarga {

	PrincipalGUI pg;
	
	public DescargaMangaReader(PrincipalGUI pg){
		this.pg = pg;
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
		
		String nombreSerie;
		Pattern pattern;
		Matcher matcher;
		pattern = Pattern.compile("http://www.mangareader.net/(.+)");
		matcher = pattern.matcher(url);
		if(!matcher.find()){
			pg.informacionErrores.setText("La URL introducida no es correcta");
			pg.enCurso = false;
			return;
		}
		else{
			nombreSerie = matcher.group(1);
			String []ns;
			ns = nombreSerie.split("/");
			for(String nameSerie:ns){
				nombreSerie = nameSerie;
			}
		}
		
		URL pagina = null;
		BufferedReader in = null;
		String contenido = null;
		String s = null;
		String subhost = "";
		String[] sh;
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
				contenido += s+"\n";
			}
			sh = url.split("/");
			for(int i=0;i<sh.length-1;i++){
				subhost += sh[i]+"/";
			}
			
			File dir = new File(pg.rutaFichero+"/"+nombreSerie);
			if (!dir.exists())
			  if (!dir.mkdir()){
				  pg.informacionErrores.setText("no se pudo crear la carpeta de destino");
				  return;
			  }
			
			pattern = Pattern.compile("<a href=\"([^\n]+)\">[^\n]*</a>[^\n]*</td>");
			matcher = pattern.matcher(contenido);
			boolean primero = true;
			while(matcher.find()){
				if(!primero)
					capitulos.add(matcher.group(1));
				primero=false;
			}
			pg.control.numCapitulos = capitulos.size();
			pg.control.numCapitulosEncontrados=0;
			pg.informacionCapitulos.setText("Encontrados " + pg.control.numCapitulos + " capítulos");
			Vector <EncontrarImagenesCapituloMangaReader> id_hilos = new Vector<EncontrarImagenesCapituloMangaReader>();
			int tope;
			String []c;
			String cc;
			int maxHilos=50;
			for(tope=0; tope<capitulos.size();){
				if(!pg.control.stop && tope + maxHilos <= capitulos.size()){
					for(int i=tope;i<tope+maxHilos;i++){
						c = capitulos.get(i).split("/");
						for(String ccc:c){
							cc = ccc;
						}
						matcher = Pattern.compile("[0-9]+").matcher(capitulos.get(i));
						if(matcher.find())
							nCapitulo = new Integer(matcher.group(0));
						capitulo = "http://www.mangareader.net"+capitulos.get(i);
						EncontrarImagenesCapituloMangaReader h = new EncontrarImagenesCapituloMangaReader(capitulo, nCapitulo, manga, pg.control);
						h.start();
						id_hilos.add(h);
					}
					for(EncontrarImagenesCapituloMangaReader eic : id_hilos){
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
					c = capitulos.get(i).split("/");
					for(String ccc:c){
						cc = ccc;
					}
					matcher = Pattern.compile("[0-9]+").matcher(capitulos.get(i));
					if(matcher.find())
						nCapitulo = new Integer(matcher.group(0));
					capitulo = "http://www.mangareader.net"+capitulos.get(i);
					EncontrarImagenesCapituloMangaReader h = new EncontrarImagenesCapituloMangaReader(capitulo, nCapitulo, manga, pg.control);
					h.start();
					id_hilos.add(h);
				}
				for(EncontrarImagenesCapituloMangaReader eic : id_hilos){
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
			
			/*Set<Integer> names = manga.keySet();
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
			*/
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		pg.enCurso = false;
	}

	@Override
	public void descargarCapitulo(int nCapitulo, Vector<String> listaImagenes,
			String nombreSerie) {
		
	}

}



class EncontrarImagenesCapituloMangaReader extends Thread{
	private String url;
	private int index;
	private Hashtable<Integer, Vector <String>> manga;
	private Control control;
	public EncontrarImagenesCapituloMangaReader(String url, int index, Hashtable<Integer, Vector <String>> manga, Control control) {
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
		Vector <String> listaLinksImagenes = new Vector<String>();
		Vector <String> listaImagenes = new Vector<String>();
		try {
//			System.out.println(url);
			pagina = new URL(url);
			in = new BufferedReader(new InputStreamReader(pagina.openStream()));
			while((s = in.readLine()) != null){
				contenido += s;
			}
			Matcher matcher = Pattern.compile("<option value=\"([^\"]+)").matcher(contenido);
			while(matcher.find()){
				numImages++;
				listaLinksImagenes.add(matcher.group(1));
			}
			synchronized (control) {
				control.numImagenes+=numImages;				
			}
//			System.out.println(numImages);
			
			for(int i=0;i<listaLinksImagenes.size();i++){
				contenido = "";
				pagina = new URL("http://www.mangareader.net"+listaLinksImagenes.get(i));
				in = new BufferedReader(new InputStreamReader(pagina.openStream()));
				while((s = in.readLine()) != null){
					contenido += s+"\n";
				}
				matcher = Pattern.compile("src=\"([^\n]+.jpg)\"").matcher(contenido);
				if(matcher.find()){
					System.out.println(matcher.group(1));
					listaImagenes.add(matcher.group(1));
				}
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