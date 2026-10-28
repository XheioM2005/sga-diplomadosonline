#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>
#include <stack>
#include <queue>
#include <iomanip>
#include <algorithm>

using namespace std;

// --- CLASES (Modelado Orientado a Objetos) ---
class Persona {
protected:
    string cedula;
    string nombre;
    string correo;
public:
    Persona(string cedula, string nombre, string correo)
        : cedula(cedula), nombre(nombre), correo(correo) {}
    virtual ~Persona() {}
    string getCedula() const { return cedula; }
    string getNombre() const { return nombre; }
    string getCorreo() const { return correo; }
    void setCedula(string c) { cedula = c; }
    void setNombre(string n) { nombre = n; }
    void setCorreo(string c) { correo = c; }
};

class ProgramaAcademico {
public:
    // Metodo polimorfico virtual puro: cada clase hija define su propia regla
    virtual string evaluarAprobacion(class Alumno* alumno) = 0;
    virtual ~ProgramaAcademico() {}
};

class Alumno : public Persona {
private:
    ProgramaAcademico* programa;
    vector<double> notas; // Lista de notas (maximo 3, una a la vez)
public:
    Alumno(string cedula, string nombre, string correo, ProgramaAcademico* programa)
        : Persona(cedula, nombre, correo), programa(programa) {}
    // Destructor: libera memoria del programa asociado (evita fugas)
    ~Alumno() {
        if (programa != nullptr) {
            delete programa;
            programa = nullptr;
        }
    }
    ProgramaAcademico* getPrograma() const { return programa; }
    vector<double>& getNotas() { return notas; }
    void setPrograma(ProgramaAcademico* p) { programa = p; }
};

class Curso : public ProgramaAcademico {
public:
    string evaluarAprobacion(Alumno* alumno) override {
        if (alumno->getNotas().empty()) {
            return "REPROBADO";
        }
        double suma = 0;
        for (double n : alumno->getNotas()) suma += n;
        double prom = suma / alumno->getNotas().size();
        return prom >= 10.0 ? "APROBADO" : "REPROBADO";
    }
};

class Diplomado : public ProgramaAcademico {
public:
    string evaluarAprobacion(Alumno* alumno) override {
        if (alumno->getNotas().empty()) {
            return "REPROBADO";
        }
        double suma = 0;
        for (double n : alumno->getNotas()) suma += n;
        double prom = suma / alumno->getNotas().size();
        return prom >= 14.0 ? "APROBADO" : "REPROBADO";
    }
};

class Bootcamp : public ProgramaAcademico {
public:
    string evaluarAprobacion(Alumno* alumno) override {
        if (alumno->getNotas().empty()) {
            return "REPROBADO";
        }
        for (double n : alumno->getNotas()) {
            if (n < 14.0) {
                return "REPROBADO";
            }
        }
        return "APROBADO";
    }
};

class Profesor : public Persona {
private:
    string especialidad;
    string materia;
public:
    Profesor(string cedula, string nombre, string correo, string especialidad, string materia)
        : Persona(cedula, nombre, correo), especialidad(especialidad), materia(materia) {}
    string getEspecialidad() const { return especialidad; }
    string getMateria() const { return materia; }
    void setEspecialidad(string e) { especialidad = e; }
    void setMateria(string m) { materia = m; }
};

// --- VARIABLES GLOBALES ---
vector<Alumno*> alumnos;
vector<Profesor*> profesores;
stack<Alumno*> pilaDeshacer; // Pila LIFO

// --- UTILIDAD: nombre simple de la clase del programa ---
string tipoPrograma(ProgramaAcademico* p) {
    if (dynamic_cast<Bootcamp*>(p)) return "Bootcamp";
    if (dynamic_cast<Diplomado*>(p)) return "Diplomado";
    if (dynamic_cast<Curso*>(p)) return "Curso";
    return "Desconocido";
}

// --- PERSISTENCIA EN DISCO (.txt) ---
void guardarAlumnos() {
    ofstream f("alumnos.txt");
    if (!f.is_open()) {
        cout << "Error al guardar alumnos." << endl;
        return;
    }
    for (Alumno* a : alumnos) {
        // Rellenar con 0 si tiene menos de 3 notas
        double n1 = a->getNotas().size() > 0 ? a->getNotas()[0] : 0.0;
        double n2 = a->getNotas().size() > 1 ? a->getNotas()[1] : 0.0;
        double n3 = a->getNotas().size() > 2 ? a->getNotas()[2] : 0.0;
        f << a->getCedula() << "," << a->getNombre() << "," << a->getCorreo() << ","
          << tipoPrograma(a->getPrograma()) << "," << n1 << "," << n2 << "," << n3 << "\n";
    }
    f.close();
}

void guardarProfesores() {
    ofstream f("profesores.txt");
    if (!f.is_open()) {
        cout << "Error al guardar profesores." << endl;
        return;
    }
    for (Profesor* p : profesores) {
        f << p->getCedula() << "," << p->getNombre() << "," << p->getCorreo() << ","
          << p->getEspecialidad() << "," << p->getMateria() << "\n";
    }
    f.close();
}

void cargarDatos() {
    // Carga los archivos .txt a memoria al iniciar el programa (persistencia)
    ifstream fAlum("alumnos.txt");
    if (fAlum.is_open()) {
        string line;
        while (getline(fAlum, line)) {
            if (line.empty()) continue;
            vector<string> datos;
            stringstream ss(line);
            string token;
            while (getline(ss, token, ',')) datos.push_back(token);
            if (datos.size() == 7) {
                string ced = datos[0];
                string nom = datos[1];
                string cor = datos[2];
                string tipoProg = datos[3];
                ProgramaAcademico* prog;
                if (tipoProg == "Curso") {
                    prog = new Curso();
                } else if (tipoProg == "Diplomado") {
                    prog = new Diplomado();
                } else {
                    prog = new Bootcamp();
                }
                Alumno* alumno = new Alumno(ced, nom, cor, prog);
                double ns[3] = {
                    stod(datos[4]),
                    stod(datos[5]),
                    stod(datos[6])
                };
                for (double n : ns) {
                    if (n > 0) { // Solo cargar notas reales (0 = sin nota)
                        alumno->getNotas().push_back(n);
                    }
                }
                alumnos.push_back(alumno);
            }
        }
        fAlum.close();
    }

    ifstream fProf("profesores.txt");
    if (fProf.is_open()) {
        string line;
        while (getline(fProf, line)) {
            if (line.empty()) continue;
            vector<string> datos;
            stringstream ss(line);
            string token;
            while (getline(ss, token, ',')) datos.push_back(token);
            if (datos.size() == 5) {
                profesores.push_back(new Profesor(datos[0], datos[1], datos[2], datos[3], datos[4]));
            }
        }
        fProf.close();
    }
}

// --- MENSAJE DEL MENU ---
void imprimirMenu() {
    cout << string(50, '=') << "\n";
    cout << "SGA-DO: SISTEMA DIPLOMADOSONLINE\n";
    cout << string(50, '=') << "\n";
    cout << "1. Registrar Alumno\n";
    cout << "2. Registrar Profesor\n";
    cout << "3. Registrar Notas a un Alumno\n";
    cout << "4. Deshacer Último Registro de Nota\n";
    cout << "5. Generar Cola de Certificados\n";
    cout << "6. Mostrar Reporte General\n";
    cout << "7. Salir\n";
    cout << string(50, '=') << "\n";
}

// --- OPCIONES DEL MENU ---
void registrarAlumno() {
    cout << "Cédula: ";
    string ced;
    getline(cin, ced);
    // Correccion: validar cedula unica (no duplicados)
    for (Alumno* a : alumnos) {
        if (a->getCedula() == ced) {
            cout << "Error: Ya existe un alumno registrado con esa cédula." << endl;
            return;
        }
    }
    cout << "Nombre Completo: ";
    string nom;
    getline(cin, nom);
    cout << "Correo Electrónico: ";
    string cor;
    getline(cin, cor);
    // Correccion: validar programa (nunca defaultear a Bootcamp)
    cout << "Tipo de Programa (Curso/Diplomado/Bootcamp): ";
    string tipo;
    getline(cin, tipo);
    // Convertir a minusculas
    transform(tipo.begin(), tipo.end(), tipo.begin(), ::tolower);
    while (tipo != "curso" && tipo != "diplomado" && tipo != "bootcamp") {
        cout << "Error: Programa inválido. Debe ser exactamente: Curso, Diplomado o Bootcamp." << endl;
        cout << "Tipo de Programa (Curso/Diplomado/Bootcamp): ";
        getline(cin, tipo);
        transform(tipo.begin(), tipo.end(), tipo.begin(), ::tolower);
    }
    ProgramaAcademico* prog;
    if (tipo == "curso") {
        prog = new Curso();
    } else if (tipo == "diplomado") {
        prog = new Diplomado();
    } else {
        prog = new Bootcamp();
    }
    alumnos.push_back(new Alumno(ced, nom, cor, prog));
    guardarAlumnos(); // Escritura inmediata en disco
    cout << "Alumno registrado exitosamente." << endl;
}

void registrarProfesor() {
    cout << "Cédula: ";
    string ced;
    getline(cin, ced);
    // Correccion: validar cedula unica (no duplicados)
    for (Profesor* p : profesores) {
        if (p->getCedula() == ced) {
            cout << "Error: Ya existe un profesor registrado con esa cédula." << endl;
            return;
        }
    }
    cout << "Nombre Completo: ";
    string nom;
    getline(cin, nom);
    cout << "Correo Electrónico: ";
    string cor;
    getline(cin, cor);
    cout << "Especialidad Académica: ";
    string esp;
    getline(cin, esp);
    cout << "Materia Asignada: ";
    string mat;
    getline(cin, mat);
    profesores.push_back(new Profesor(ced, nom, cor, esp, mat));
    guardarProfesores(); // Escritura inmediata en disco
    cout << "Profesor registrado exitosamente." << endl;
}

void registrarNotas() {
    cout << "Cédula del Alumno a buscar: ";
    string ced;
    getline(cin, ced);
    Alumno* alumno = nullptr;
    for (Alumno* a : alumnos) {
        if (a->getCedula() == ced) {
            alumno = a;
            break;
        }
    }
    if (alumno == nullptr) {
        cout << "Alumno no encontrado." << endl;
        return;
    }
    // Buscar y mostrar el perfil del alumno
    cout << "Perfil encontrado: " << alumno->getNombre() << " (" << tipoPrograma(alumno->getPrograma()) << ")" << endl;
    cout << "Notas actuales: [";
    for (size_t i = 0; i < alumno->getNotas().size(); ++i) {
        if (i > 0) cout << ", ";
        cout << alumno->getNotas()[i];
    }
    cout << "] (" << alumno->getNotas().size() << "/3)" << endl;
    if (alumno->getNotas().size() >= 3) {
        cout << "El alumno ya tiene el máximo de 3 notas registradas." << endl;
        return;
    }
    // Correccion: registrar UNA sola nota por vez
    cout << "Ingrese una calificación (0-20): ";
    string entrada;
    getline(cin, entrada);
    try {
        double nota = stod(entrada);
        if (nota < 0 || nota > 20) {
            cout << "Error: La calificación debe estar entre 0 y 20." << endl;
            return;
        }
        alumno->getNotas().push_back(nota);
        pilaDeshacer.push(alumno); // Guardar en la Pila (LIFO)
        guardarAlumnos(); // Escritura inmediata en disco
        cout << "Nota registrada. Total notas: " << alumno->getNotas().size() << "/3" << endl;
    } catch (...) {
        cout << "Error: Ingrese un valor numérico válido" << endl;
    }
}

void deshacerNota() {
    // Correccion: deshacer UNA sola nota (la ultima ingresada) - LIFO
    if (pilaDeshacer.empty()) {
        cout << "No hay notas registradas para deshacer." << endl;
        return;
    }
    Alumno* alumno = pilaDeshacer.top();
    pilaDeshacer.pop();
    if (alumno->getNotas().empty()) {
        cout << "El alumno no tiene notas para deshacer." << endl;
        return;
    }
    double notaRemovida = alumno->getNotas().back(); // Remover SOLO la ultima nota
    alumno->getNotas().pop_back();
    guardarAlumnos();
    cout << "Última nota (" << notaRemovida << ") deshecha para " << alumno->getNombre()
         << ". Notas restantes: " << alumno->getNotas().size() << endl;
}

void generarColaCertificados() {
    // Cola FIFO: se encolan los aprobados y se procesan en orden de llegada
    queue<Alumno*> cola;
    for (Alumno* a : alumnos) {
        if (a->getPrograma()->evaluarAprobacion(a) == "APROBADO") {
            cola.push(a);
        }
    }
    ofstream f("certificados_pendientes.txt");
    if (!f.is_open()) {
        cout << "Error al generar certificados." << endl;
        return;
    }
    // Encabezado formal (formato del proyecto.pdf)
    f << "=========================================\n";
    f << "REPORTE DE CERTIFICADOS PENDIENTES\n";
    f << "=========================================\n";
    f << "Total de graduandos en cola: " << cola.size() << "\n\n";

    int i = 1;
    while (!cola.empty()) {
        Alumno* a = cola.front(); // FIFO: el primero en entrar es el primero en salir
        cola.pop();
        double suma = 0;
        for (double n : a->getNotas()) suma += n;
        double promedio = a->getNotas().empty() ? 0.0 : suma / a->getNotas().size();
        f << i << ". [" << a->getCedula() << "] " << a->getNombre() << "\n";
        f << "- Programa: " << tipoPrograma(a->getPrograma()) << "\n";
        f << fixed << setprecision(1);
        f << "- Promedio Final: " << promedio << "\n";
        f << setprecision(6);
        if (dynamic_cast<Bootcamp*>(a->getPrograma())) {
            f << "- Estatus: APROBADO (Cumple regla de ninguna nota < 14)\n\n";
        } else {
            f << "- Estatus: APROBADO\n\n";
        }
        i++;
    }

    // Pie de pagina
    f << "=========================================\n";
    f << "* Fin del reporte - Generado por SGA-DO *\n";
    f.close();
    cout << "Cola generada en certificados_pendientes.txt" << endl;
}

void mostrarReporte() {
    cout << "\n--- PROFESORES ACTIVOS ---" << endl;
    if (profesores.empty()) {
        cout << "(No hay profesores registrados)" << endl;
    }
    for (Profesor* p : profesores) {
        cout << "[" << p->getCedula() << "] " << p->getNombre()
             << " | Especialidad: " << p->getEspecialidad()
             << " | Materia: " << p->getMateria() << endl;
    }

    cout << "\n--- ALUMNOS REGISTRADOS ---" << endl;
    if (alumnos.empty()) {
        cout << "(No hay alumnos registrados)" << endl;
    }
    for (Alumno* a : alumnos) {
        cout << "[" << a->getCedula() << "] " << a->getNombre()
             << " (" << tipoPrograma(a->getPrograma()) << ") | Notas: [";
        for (size_t i = 0; i < a->getNotas().size(); ++i) {
            if (i > 0) cout << ", ";
            cout << a->getNotas()[i];
        }
        cout << "] | Estatus: " << a->getPrograma()->evaluarAprobacion(a) << endl;
    }
}

// --- FUNCION PRINCIPAL ---
int main() {
    cargarDatos(); // Persistencia: cargar .txt al iniciar
    cout << "Datos cargados en memoria: " << alumnos.size() << " alumnos, "
         << profesores.size() << " profesores." << endl;

    while (true) {
        imprimirMenu();
        cout << "Seleccione una opción (1-7): ";
        string op;
        getline(cin, op);
        // Quitar espacios
        while (!op.empty() && (op.back() == ' ' || op.back() == '\r' || op.back() == '\n')) op.pop_back();
        try {
            int opcion = stoi(op);
            switch (opcion) {
                case 1: registrarAlumno(); break;
                case 2: registrarProfesor(); break;
                case 3: registrarNotas(); break;
                case 4: deshacerNota(); break;
                case 5: generarColaCertificados(); break;
                case 6: mostrarReporte(); break;
                case 7: {
                    cout << "Guardando cambios y saliendo del sistema... ¡Hasta pronto!" << endl;
                    // Liberar toda la memoria antes de salir (sin fugas)
                    for (Alumno* a : alumnos) delete a;
                    for (Profesor* p : profesores) delete p;
                    alumnos.clear();
                    profesores.clear();
                    return 0;
                }
                default:
                    cout << "Error: Opción no válida. Debe ser un número del 1 al 7." << endl;
            }
        } catch (...) {
            cout << "Error: Ingrese un valor numérico válido" << endl;
        }
    }
    return 0;
}
