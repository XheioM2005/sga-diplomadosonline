import java.util.*;
import java.io.*;

// --- CLASES DEL MODELO ---
abstract class Persona {
    private String cedula, nombre, correo;
    public Persona(String c, String n, String co) { cedula=c; nombre=n; correo=co; }
    public String getCedula() { return cedula; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
}

class Profesor extends Persona {
    private String especialidad, materia;
    public Profesor(String c, String n, String co, String e, String m) { super(c,n,co); especialidad=e; materia=m; }
}

abstract class ProgramaAcademico {
    // Rhadamés pidió que el método reciba al alumno como parámetro
    public abstract String evaluarAprobacion(Alumno a);
    public abstract String getNombrePrograma();
}

class Curso extends ProgramaAcademico {
    @Override public String evaluarAprobacion(Alumno a) {
        double prom = (a.getN1() + a.getN2() + a.getN3()) / 3.0;
        return prom >= 10.0 ? "APROBADO" : "REPROBADO";
    }
    @Override public String getNombrePrograma() { return "Curso"; }
}

class Diplomado extends ProgramaAcademico {
    @Override public String evaluarAprobacion(Alumno a) {
        double prom = (a.getN1() + a.getN2() + a.getN3()) / 3.0;
        return prom >= 14.0 ? "APROBADO" : "REPROBADO";
    }
    @Override public String getNombrePrograma() { return "Diplomado"; }
}

class Bootcamp extends ProgramaAcademico {
    @Override public String evaluarAprobacion(Alumno a) {
        return (a.getN1()>=14.0 && a.getN2()>=14.0 && a.getN3()>=14.0) ? "APROBADO" : "REPROBADO";
    }
    @Override public String getNombrePrograma() { return "Bootcamp"; }
}

class Alumno extends Persona {
    private double n1, n2, n3;
    private ProgramaAcademico programa;
    public Alumno(String c, String n, String co, ProgramaAcademico p) { super(c,n,co); programa=p; n1=0; n2=0; n3=0; }
    public ProgramaAcademico getPrograma() { return programa; }
    public double getN1() { return n1; }
    public double getN2() { return n2; }
    public double getN3() { return n3; }
    public void setNotas(double n1, double n2, double n3) { this.n1=n1; this.n2=n2; this.n3=n3; }
}

// --- CLASE PRINCIPAL ---
public class SGADO {
    static ArrayList<Alumno> alumnos = new ArrayList<>();
    static ArrayList<Profesor> profesores = new ArrayList<>();
    static Stack<Alumno> pilaDeshacer = new Stack<>();
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        while(true) {
            System.out.println("\n=== SGA-DO: SISTEMA DIPLOMADOSONLINE ===");
            System.out.println("1. Registrar Alumno\n2. Registrar Profesor\n3. Registrar Notas a un Alumno");
            System.out.println("4. Deshacer Último Registro de Nota\n5. Generar Cola de Certificados");
            System.out.println("6. Mostrar Reporte General\n7. Salir");
            System.out.print("Seleccione una opción (1-7): ");
            try {
                int op = Integer.parseInt(sc.nextLine());
                if(op==1) registrarAlumno();
                else if(op==2) registrarProfesor();
                else if(op==3) registrarNotas();
                else if(op==4) deshacerNota();
                else if(op==5) generarCola();
                else if(op==6) mostrarReporte();
                else if(op==7) { System.out.println("Saliendo..."); break; }
                else System.out.println("Opción no válida.");
            } catch (NumberFormatException e) {
                System.out.println("Error: Ingrese un valor numérico válido.");
            }
        }
    }
    
    static void registrarAlumno() {
        try {
            System.out.print("Cédula: "); String c = sc.nextLine();
            System.out.print("Nombre: "); String n = sc.nextLine();
            System.out.print("Correo: "); String co = sc.nextLine();
            System.out.print("Programa (1.Curso 2.Diplomado 3.Bootcamp): ");
            int t = Integer.parseInt(sc.nextLine());
            ProgramaAcademico p = (t==1)? new Curso() : (t==2)? new Diplomado() : new Bootcamp();
            alumnos.add(new Alumno(c,n,co,p));
            guardarAlumnos();
            System.out.println("Alumno registrado y guardado.");
        } catch(Exception e) { System.out.println("Error en los datos."); }
    }
    
    static void registrarProfesor() {
        System.out.print("Cédula: "); String c = sc.nextLine();
        System.out.print("Nombre: "); String n = sc.nextLine();
        System.out.print("Correo: "); String co = sc.nextLine();
        System.out.print("Especialidad: "); String e = sc.nextLine();
        System.out.print("Materia: "); String m = sc.nextLine();
        profesores.add(new Profesor(c,n,co,e,m));
        guardarProfesores();
        System.out.println("Profesor registrado y guardado.");
    }
    
    static void registrarNotas() {
        System.out.print("Cédula del Alumno a buscar: "); String c = sc.nextLine();
        for(Alumno a : alumnos) {
            if(a.getCedula().equals(c)) {
                System.out.println("Perfil encontrado: " + a.getNombre() + " (" + a.getPrograma().getNombrePrograma() + ")");
                try {
                    System.out.print("Nota 1: "); double n1 = Double.parseDouble(sc.nextLine());
                    System.out.print("Nota 2: "); double n2 = Double.parseDouble(sc.nextLine());
                    System.out.print("Nota 3: "); double n3 = Double.parseDouble(sc.nextLine());
                    a.setNotas(n1,n2,n3);
                    pilaDeshacer.push(a);
                    guardarAlumnos();
                    System.out.println("Notas registradas.");
                } catch(Exception e) { System.out.println("Error: Ingrese notas numéricas válidas."); }
                return;
            }
        }
        System.out.println("Alumno no encontrado.");
    }
    
    static void deshacerNota() {
        if(!pilaDeshacer.isEmpty()) {
            Alumno a = pilaDeshacer.pop();
            a.setNotas(0,0,0);
            guardarAlumnos();
            System.out.println("Última nota deshecha para " + a.getNombre());
        } else { System.out.println("No hay notas para deshacer."); }
    }
    
    static void generarCola() {
        Queue<Alumno> cola = new LinkedList<>();
        for(Alumno a : alumnos) if(a.getPrograma().evaluarAprobacion(a).equals("APROBADO")) cola.add(a);
        try (PrintWriter pw = new PrintWriter("certificados_pendientes.txt")) {
            pw.println("=========================================");
            pw.println("REPORTE DE CERTIFICADOS PENDIENTES");
            pw.println("=========================================");
            pw.println("Total de graduandos en cola: " + cola.size() + "\n");
            int i=1;
            while(!cola.isEmpty()) {
                Alumno a = cola.poll();
                pw.println(i++ + ". [" + a.getCedula() + "] " + a.getNombre());
                pw.println("- Programa: " + a.getPrograma().getNombrePrograma());
                double prom = (a.getN1()+a.getN2()+a.getN3())/3.0;
                pw.println("- Promedio Final: " + String.format("%.1f", prom));
                pw.println("- Estatus: " + a.getPrograma().evaluarAprobacion(a) + "\n");
            }
            pw.println("=========================================\n* Fin del reporte - Generado por SGA-DO *");
            System.out.println("Cola generada en certificados_pendientes.txt");
        } catch(Exception e) { System.out.println("Error al escribir archivo."); }
    }
    
    static void mostrarReporte() {
        System.out.println("\n--- PROFESORES ---");
        for(Profesor p : profesores) System.out.println(p.getNombre());
        System.out.println("\n--- ALUMNOS ---");
        for(Alumno a : alumnos) {
            System.out.println(a.getNombre() + " (" + a.getPrograma().getNombrePrograma() + ") - Estatus: " + a.getPrograma().evaluarAprobacion(a));
        }
    }
    
    static void guardarAlumnos() {
        try (PrintWriter pw = new PrintWriter("alumnos.txt")) {
            for(Alumno a : alumnos) {
                pw.println(a.getCedula()+","+a.getNombre()+","+a.getCorreo()+","+a.getPrograma().getNombrePrograma()+","+a.getN1()+","+a.getN2()+","+a.getN3());
            }
        } catch(Exception e) {}
    }
    
    static void guardarProfesores() {
        try (PrintWriter pw = new PrintWriter("profesores.txt")) {
            for(Profesor p : profesores) pw.println(p.getCedula()+","+p.getNombre()+","+p.getCorreo()+",Especialidad,Materia");
        } catch(Exception e) {}
    }
}