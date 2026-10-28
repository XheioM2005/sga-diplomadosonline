import java.io.*;
import java.util.*;

// --- CLASES (Modelado Orientado a Objetos) ---
class Persona {
    private String cedula;
    private String nombre;
    private String correo;

    public Persona(String cedula, String nombre, String correo) {
        this.cedula = cedula;
        this.nombre = nombre;
        this.correo = correo;
    }
    public String getCedula() { return cedula; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public void setCedula(String c) { this.cedula = c; }
    public void setNombre(String n) { this.nombre = n; }
    public void setCorreo(String c) { this.correo = c; }
}

class Profesor extends Persona {
    private String especialidad;
    private String materia;

    public Profesor(String cedula, String nombre, String correo, String especialidad, String materia) {
        super(cedula, nombre, correo);
        this.especialidad = especialidad;
        this.materia = materia;
    }
    public String getEspecialidad() { return especialidad; }
    public String getMateria() { return materia; }
    public void setEspecialidad(String e) { this.especialidad = e; }
    public void setMateria(String m) { this.materia = m; }
}

abstract class ProgramaAcademico {
    // Metodo polimorfico: cada clase hija define su propia regla
    public abstract String evaluarAprobacion(Alumno alumno);
}

class Curso extends ProgramaAcademico {
    @Override
    public String evaluarAprobacion(Alumno alumno) {
        if (alumno.getNotas().isEmpty()) {
            return "REPROBADO";
        }
        double suma = 0;
        for (double n : alumno.getNotas()) suma += n;
        double prom = suma / alumno.getNotas().size();
        return prom >= 10.0 ? "APROBADO" : "REPROBADO";
    }
}

class Diplomado extends ProgramaAcademico {
    @Override
    public String evaluarAprobacion(Alumno alumno) {
        if (alumno.getNotas().isEmpty()) {
            return "REPROBADO";
        }
        double suma = 0;
        for (double n : alumno.getNotas()) suma += n;
        double prom = suma / alumno.getNotas().size();
        return prom >= 14.0 ? "APROBADO" : "REPROBADO";
    }
}

class Bootcamp extends ProgramaAcademico {
    @Override
    public String evaluarAprobacion(Alumno alumno) {
        if (alumno.getNotas().isEmpty()) {
            return "REPROBADO";
        }
        for (double n : alumno.getNotas()) {
            if (n < 14.0) {
                return "REPROBADO";
            }
        }
        return "APROBADO";
    }
}

class Alumno extends Persona {
    private ProgramaAcademico programa;
    private ArrayList<Double> notas; // Lista de notas (maximo 3, una a la vez)

    public Alumno(String cedula, String nombre, String correo, ProgramaAcademico programa) {
        super(cedula, nombre, correo);
        this.programa = programa;
        this.notas = new ArrayList<>();
    }
    public ProgramaAcademico getPrograma() { return programa; }
    public ArrayList<Double> getNotas() { return notas; }
    public void setPrograma(ProgramaAcademico p) { this.programa = p; }
}

// --- CLASE PRINCIPAL ---
public class SGADO {
    // --- VARIABLES GLOBALES ---
    static ArrayList<Alumno> alumnos = new ArrayList<>();
    static ArrayList<Profesor> profesores = new ArrayList<>();
    static Stack<Alumno> pilaDeshacer = new Stack<>(); // Pila LIFO

    static Scanner sc = new Scanner(System.in);

    // --- PERSISTENCIA EN DISCO (.txt) ---
    public static void guardarAlumnos() {
        try (PrintWriter f = new PrintWriter(new FileWriter("alumnos.txt"))) {
            for (Alumno a : alumnos) {
                // Rellenar con 0 si tiene menos de 3 notas
                double n1 = a.getNotas().size() > 0 ? a.getNotas().get(0) : 0.0;
                double n2 = a.getNotas().size() > 1 ? a.getNotas().get(1) : 0.0;
                double n3 = a.getNotas().size() > 2 ? a.getNotas().get(2) : 0.0;
                String tipoProg = a.getPrograma().getClass().getSimpleName();
                f.println(a.getCedula() + "," + a.getNombre() + "," + a.getCorreo() + "," + tipoProg + "," + n1 + "," + n2 + "," + n3);
            }
        } catch (IOException e) {
            System.out.println("Error al guardar alumnos: " + e.getMessage());
        }
    }

    public static void guardarProfesores() {
        try (PrintWriter f = new PrintWriter(new FileWriter("profesores.txt"))) {
            for (Profesor p : profesores) {
                f.println(p.getCedula() + "," + p.getNombre() + "," + p.getCorreo() + "," + p.getEspecialidad() + "," + p.getMateria());
            }
        } catch (IOException e) {
            System.out.println("Error al guardar profesores: " + e.getMessage());
        }
    }

    public static void cargarDatos() {
        // Carga los archivos .txt a memoria al iniciar el programa (persistencia)
        File fAlum = new File("alumnos.txt");
        if (fAlum.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(fAlum))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] datos = line.split(",");
                    if (datos.length == 7) {
                        String ced = datos[0];
                        String nom = datos[1];
                        String cor = datos[2];
                        String tipoProg = datos[3];
                        ProgramaAcademico prog;
                        if (tipoProg.equals("Curso")) {
                            prog = new Curso();
                        } else if (tipoProg.equals("Diplomado")) {
                            prog = new Diplomado();
                        } else {
                            prog = new Bootcamp();
                        }
                        Alumno alumno = new Alumno(ced, nom, cor, prog);
                        double[] ns = {Double.parseDouble(datos[4]), Double.parseDouble(datos[5]), Double.parseDouble(datos[6])};
                        for (double n : ns) {
                            if (n > 0) { // Solo cargar notas reales (0 = sin nota)
                                alumno.getNotas().add(n);
                            }
                        }
                        alumnos.add(alumno);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error al cargar alumnos: " + e.getMessage());
            }
        }

        File fProf = new File("profesores.txt");
        if (fProf.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(fProf))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] datos = line.split(",");
                    if (datos.length == 5) {
                        profesores.add(new Profesor(datos[0], datos[1], datos[2], datos[3], datos[4]));
                    }
                }
            } catch (IOException e) {
                System.out.println("Error al cargar profesores: " + e.getMessage());
            }
        }
    }

    // --- MENSAJE DEL MENU ---
    public static void imprimirMenu() {
        System.out.println("=".repeat(50));
        System.out.println("SGA-DO: SISTEMA DIPLOMADOSONLINE");
        System.out.println("=".repeat(50));
        System.out.println("1. Registrar Alumno");
        System.out.println("2. Registrar Profesor");
        System.out.println("3. Registrar Notas a un Alumno");
        System.out.println("4. Deshacer Último Registro de Nota");
        System.out.println("5. Generar Cola de Certificados");
        System.out.println("6. Mostrar Reporte General");
        System.out.println("7. Salir");
        System.out.println("=".repeat(50));
    }

    // --- OPCIONES DEL MENU ---
    public static void registrarAlumno() {
        System.out.print("Cédula: ");
        String ced = sc.nextLine().trim();
        // Correccion: validar cedula unica (no duplicados)
        for (Alumno a : alumnos) {
            if (a.getCedula().equals(ced)) {
                System.out.println("Error: Ya existe un alumno registrado con esa cédula.");
                return;
            }
        }
        System.out.print("Nombre Completo: ");
        String nom = sc.nextLine().trim();
        System.out.print("Correo Electrónico: ");
        String cor = sc.nextLine().trim();
        // Correccion: validar programa (nunca defaultear a Bootcamp)
        System.out.print("Tipo de Programa (Curso/Diplomado/Bootcamp): ");
        String tipo = sc.nextLine().trim().toLowerCase();
        while (!tipo.equals("curso") && !tipo.equals("diplomado") && !tipo.equals("bootcamp")) {
            System.out.println("Error: Programa inválido. Debe ser exactamente: Curso, Diplomado o Bootcamp.");
            System.out.print("Tipo de Programa (Curso/Diplomado/Bootcamp): ");
            tipo = sc.nextLine().trim().toLowerCase();
        }
        ProgramaAcademico prog;
        if (tipo.equals("curso")) {
            prog = new Curso();
        } else if (tipo.equals("diplomado")) {
            prog = new Diplomado();
        } else {
            prog = new Bootcamp();
        }
        alumnos.add(new Alumno(ced, nom, cor, prog));
        guardarAlumnos(); // Escritura inmediata en disco
        System.out.println("Alumno registrado exitosamente.");
    }

    public static void registrarProfesor() {
        System.out.print("Cédula: ");
        String ced = sc.nextLine().trim();
        // Correccion: validar cedula unica (no duplicados)
        for (Profesor p : profesores) {
            if (p.getCedula().equals(ced)) {
                System.out.println("Error: Ya existe un profesor registrado con esa cédula.");
                return;
            }
        }
        System.out.print("Nombre Completo: ");
        String nom = sc.nextLine().trim();
        System.out.print("Correo Electrónico: ");
        String cor = sc.nextLine().trim();
        System.out.print("Especialidad Académica: ");
        String esp = sc.nextLine().trim();
        System.out.print("Materia Asignada: ");
        String mat = sc.nextLine().trim();
        profesores.add(new Profesor(ced, nom, cor, esp, mat));
        guardarProfesores(); // Escritura inmediata en disco
        System.out.println("Profesor registrado exitosamente.");
    }

    public static void registrarNotas() {
        System.out.print("Cédula del Alumno a buscar: ");
        String ced = sc.nextLine().trim();
        Alumno alumno = null;
        for (Alumno a : alumnos) {
            if (a.getCedula().equals(ced)) {
                alumno = a;
                break;
            }
        }
        if (alumno == null) {
            System.out.println("Alumno no encontrado.");
            return;
        }
        // Buscar y mostrar el perfil del alumno
        System.out.println("Perfil encontrado: " + alumno.getNombre() + " (" + alumno.getPrograma().getClass().getSimpleName() + ")");
        System.out.println("Notas actuales: " + alumno.getNotas() + " (" + alumno.getNotas().size() + "/3)");
        if (alumno.getNotas().size() >= 3) {
            System.out.println("El alumno ya tiene el máximo de 3 notas registradas.");
            return;
        }
        // Correccion: registrar UNA sola nota por vez
        try {
            System.out.print("Ingrese una calificación (0-20): ");
            double nota = Double.parseDouble(sc.nextLine().trim());
            if (nota < 0 || nota > 20) {
                System.out.println("Error: La calificación debe estar entre 0 y 20.");
                return;
            }
            alumno.getNotas().add(nota);
            pilaDeshacer.push(alumno); // Guardar en la Pila (LIFO)
            guardarAlumnos(); // Escritura inmediata en disco
            System.out.println("Nota registrada. Total notas: " + alumno.getNotas().size() + "/3");
        } catch (NumberFormatException e) {
            System.out.println("Error: Ingrese un valor numérico válido");
        }
    }

    public static void deshacerNota() {
        // Correccion: deshacer UNA sola nota (la ultima ingresada) - LIFO
        if (pilaDeshacer.isEmpty()) {
            System.out.println("No hay notas registradas para deshacer.");
            return;
        }
        Alumno alumno = pilaDeshacer.pop();
        if (alumno.getNotas().isEmpty()) {
            System.out.println("El alumno no tiene notas para deshacer.");
            return;
        }
        double notaRemovida = alumno.getNotas().remove(alumno.getNotas().size() - 1); // Remover SOLO la ultima nota
        guardarAlumnos();
        System.out.println("Última nota (" + notaRemovida + ") deshecha para " + alumno.getNombre() + ". Notas restantes: " + alumno.getNotas().size());
    }

    public static void generarColaCertificados() {
        // Cola FIFO: se encolan los aprobados y se procesan en orden de llegada
        LinkedList<Alumno> cola = new LinkedList<>();
        for (Alumno a : alumnos) {
            if (a.getPrograma().evaluarAprobacion(a).equals("APROBADO")) {
                cola.add(a);
            }
        }
        try (PrintWriter f = new PrintWriter(new FileWriter("certificados_pendientes.txt"))) {
            // Encabezado formal (formato del proyecto.pdf)
            f.println("=========================================");
            f.println("REPORTE DE CERTIFICADOS PENDIENTES");
            f.println("=========================================");
            f.println("Total de graduandos en cola: " + cola.size());
            f.println();

            int i = 1;
            while (!cola.isEmpty()) {
                Alumno a = cola.pollFirst(); // FIFO: el primero en entrar es el primero en salir
                double suma = 0;
                for (double n : a.getNotas()) suma += n;
                double promedio = a.getNotas().isEmpty() ? 0 : suma / a.getNotas().size();
                f.println(i + ". [" + a.getCedula() + "] " + a.getNombre());
                f.println("- Programa: " + a.getPrograma().getClass().getSimpleName());
                f.printf("- Promedio Final: %.1f%n", promedio);
                if (a.getPrograma() instanceof Bootcamp) {
                    f.println("- Estatus: APROBADO (Cumple regla de ninguna nota < 14)");
                } else {
                    f.println("- Estatus: APROBADO");
                }
                f.println();
                i++;
            }

            // Pie de pagina
            f.println("=========================================");
            f.println("* Fin del reporte - Generado por SGA-DO *");
        } catch (IOException e) {
            System.out.println("Error al generar certificados: " + e.getMessage());
            return;
        }
        System.out.println("Cola generada en certificados_pendientes.txt");
    }

    public static void mostrarReporte() {
        System.out.println("\n--- PROFESORES ACTIVOS ---");
        if (profesores.isEmpty()) {
            System.out.println("(No hay profesores registrados)");
        }
        for (Profesor p : profesores) {
            System.out.println("[" + p.getCedula() + "] " + p.getNombre() + " | Especialidad: " + p.getEspecialidad() + " | Materia: " + p.getMateria());
        }

        System.out.println("\n--- ALUMNOS REGISTRADOS ---");
        if (alumnos.isEmpty()) {
            System.out.println("(No hay alumnos registrados)");
        }
        for (Alumno a : alumnos) {
            System.out.println("[" + a.getCedula() + "] " + a.getNombre() + " (" + a.getPrograma().getClass().getSimpleName() + ") | Notas: " + a.getNotas() + " | Estatus: " + a.getPrograma().evaluarAprobacion(a));
        }
    }

    // --- FUNCION PRINCIPAL ---
    public static void main(String[] args) {
        cargarDatos(); // Persistencia: cargar .txt al iniciar
        System.out.println("Datos cargados en memoria: " + alumnos.size() + " alumnos, " + profesores.size() + " profesores.");

        while (true) {
            imprimirMenu();
            System.out.print("Seleccione una opción (1-7): ");
            String op = sc.nextLine().trim();
            try {
                int opcion = Integer.parseInt(op);
                switch (opcion) {
                    case 1: registrarAlumno(); break;
                    case 2: registrarProfesor(); break;
                    case 3: registrarNotas(); break;
                    case 4: deshacerNota(); break;
                    case 5: generarColaCertificados(); break;
                    case 6: mostrarReporte(); break;
                    case 7:
                        System.out.println("Guardando cambios y saliendo del sistema... ¡Hasta pronto!");
                        sc.close();
                        return;
                    default:
                        System.out.println("Error: Opción no válida. Debe ser un número del 1 al 7.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Ingrese un valor numérico válido");
            }
        }
    }
}
