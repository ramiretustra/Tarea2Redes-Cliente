import java.net.*;
import java.util.Date;
import java.util.StringTokenizer;
import java.io.*;

public class ClienteChat {
	public static void main(String argv[]) throws IOException {
		try{
			ServerSocket socket = new ServerSocket(50);

			while(true){
				Socket s = socket.accept();
				peticionWeb peticionCliente = new peticionWeb(s);
				peticionCliente.start();
			}
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
	}
}



class peticionWeb extends Thread
{
	private Socket scliente = null;
	private PrintWriter out = null;

	peticionWeb(Socket ps)
	{
		scliente = ps;
		setPriority(NORM_PRIORITY - 1);
	}

	public void run()
	{
		try{
			BufferedReader url = new BufferedReader (new InputStreamReader(scliente.getInputStream()));
			out = new PrintWriter(new OutputStreamWriter(scliente.getOutputStream(),"8859_1"),true) ;

			String cadena = "";
			cadena = url.readLine();
			if (cadena != null)
			{
				StringTokenizer st = new StringTokenizer(cadena);
				String getpost = st.nextToken(); 
				if ((st.countTokens() >= 2) && getpost.equals("GET")) 
				{
					retornaFichero(st.nextToken()) ;
				}
				else if ((st.countTokens() >= 2) && getpost.equals("POST"))
				{
					String msjOcontacto = st.nextToken();
					
					if (msjOcontacto.equals("/index.html")){
						String parametros = "", readline = null;
						int largocontenido = 0;
						StringTokenizer st2;
						
						
						while(true) {
							readline = url.readLine();
							if (!(readline.isEmpty())){
								st2 = new StringTokenizer(readline);
								if (st2.nextToken().equals("Content-Length:"))
								{
									largocontenido = Integer.parseInt(st2.nextToken());
								}
							}
							else
							{
								for (int i = 0; i < largocontenido; i++)
						        {
						            parametros += (char)url.read();
						        }
								break;
							}
						}
						
						
						String nombre=null, ip=null, puerto=null;
						String[] parametro = parametros.split("&");
					    for (int i = 0; i < parametro.length; i++) {
					    	if (parametro[i].startsWith("nombre")){
					    		nombre = parametro[i].substring(7);
					    	}
					    	else if (parametro[i].startsWith("ip")){
					    		ip = parametro[i].substring(3);
					    	}
					    	else if (parametro[i].startsWith("puerto")){
					    		puerto = parametro[i].substring(7);
					    	}
					    }
					    
						FileWriter fw = new FileWriter("Contactos.txt",true);
						fw.write("<a class='list-group-item'>" + "Nombre: " + nombre + " IP: " + ip + " Puerto: " + puerto + "</a><br>\n");
						fw.close();
						
						st.nextToken();
						retornaFichero("/index.html");
					}
					
					else if(msjOcontacto.equals("/enviarmensaje.html")){
						String readline = "", emisor = "", receptor = "", mensaje = "", ip_emisor = "", ip_receptor = "";
						int aux = 0;
						try{
							while(true){
								readline = url.readLine();

								if(readline.length() == 0)
								{
									if (aux == 1){
										emisor = url.readLine();
										System.out.println(currentThread().toString() + " - " + emisor);
									}
									else if (aux == 2){
										receptor = url.readLine();
										System.out.println(currentThread().toString() + " - " + receptor);
									}
									else if (aux == 3){
										mensaje = url.readLine();
										System.out.println(currentThread().toString() + " - " + mensaje);
										break;
									}
									aux++;
								}
							}


							Socket socketCliente = new Socket("localhost", 60);
							DataOutputStream serv = new DataOutputStream(socketCliente.getOutputStream());
							
							String lineaActual;
							String[] split;
							BufferedReader br = new BufferedReader(new FileReader("Contactos.txt"));
							
		                    while ((lineaActual = br.readLine()) != null) {
		                            split = lineaActual.split(" ");
		                            if(split[2].equals(emisor)){
		                                    ip_emisor = split[4];
		                            }
		                    }
		                    if (ip_emisor.equals("")){
		                    	System.out.println("Emisor no encontrado en Contactos.txt");
		                        ip_emisor = "0.0.0.0";
		                    }
		                    br.close();
		                    
		                    br = new BufferedReader(new FileReader("Contactos.txt"));

		                    while ((lineaActual = br.readLine()) != null) {
		                            split = lineaActual.split(" ");
		                            if(split[2].equals(receptor)){
		                                    ip_receptor = split[4];
		                            }
		                            else{
		                                   
		                            }
		                    }
		                    if (ip_receptor.equals("")){
		                    	System.out.println("Receptor no encontrado en Contactos.txt");
		                        ip_receptor = "0.0.0.0";
		                    }
		                    br.close();
							
							
							serv.writeBytes(emisor + "/" + receptor + "/" + mensaje + "/" + ip_emisor + "/" + ip_receptor);
							
							socketCliente.close();
							retornaFichero("/index.html");

						}
						catch(Exception e){
							System.out.println(currentThread().toString() + " - " + "Error al enviar mensaje\nError:" + e.toString());
						}
					}
					else 
					{
						System.out.println(currentThread().toString() + " 400 Petición Incorrecta");
						out.println("400 Petición Incorrecta") ;
					}
				}
				else 
				{
					System.out.println(currentThread().toString() + " 400 Petición Incorrecta");
					out.println("400 Petición Incorrecta") ;
				}
				while (cadena != null && cadena.length() != 0)
				{
					System.out.println(currentThread().toString() + " " + cadena);
					cadena = url.readLine();
				}
				System.out.println(currentThread().toString() + " Fin Thread\n");
			}
			else
			{
				System.out.println(currentThread().toString() + " 400 Petición Vacia");
				out.println("400 Petición Vacia");
			}
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	
	void retornaFichero(String fichero)
	{
		if (fichero.startsWith("/"))
		{
			fichero = fichero.substring(1) ;
		}


		if (fichero.endsWith("/") || fichero.equals(""))
		{
			fichero = fichero + "index.html" ;
		}

		try
		{

			File archivo = new File(fichero);

			if (archivo.exists()) 
			{
				if (fichero.endsWith("html")){
					out.println("HTTP/1.0 200 ok");
					out.println("Server: Roberto Server/1.0");
					out.println("Date: " + new Date());
					out.println("Content-Type: text/html");
					out.println("Content-Length: " + archivo.length());
					out.println("\n");
				}

				BufferedReader BufferFichero = new BufferedReader(new FileReader(archivo));


				String readline = "";

				do			
				{
					readline = BufferFichero.readLine();

					if (readline != null )
					{
						out.println(readline);
					}
				}
				while (readline != null);

				BufferFichero.close();
				out.close();

			}
			else
			{
				out.println("HTTP/1.0 400 ok");
				out.close();
			}

		}
		catch(Exception e)
		{
			System.out.println(currentThread().toString() + " - " + "Error al retornar fichero");	
		}

	}
}