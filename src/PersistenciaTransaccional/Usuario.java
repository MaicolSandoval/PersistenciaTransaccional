/**
 * 
 */
/**
 * @author User
 *
 */
package PersistenciaTransaccional;

import java.util.Scanner;

public class Usuario {
	public static void main(String[] args) {
			
		    String ip = "";
			System.out.println("Ingrese el numero de la ip");
			
			try (Scanner numeroIp = new Scanner(System.in)) {
				ip = numeroIp.nextLine();
			}
			
			System.out.println("La ip ingresada es" + ip);
			
	}
	
}