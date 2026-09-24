#include <iostream>
#include <fstream>
#include <vector>
#include <stack>
#include <queue>
#include <string>
#include <limits>
using namespace std;

// Declaración anticipada para que ProgramaAcademico sepa que Alumno existe
class Alumno;

class Persona {
protected:
    string cedula, nombre, correo;
public:
    Persona(string c, string n, string co) : cedula(c), nombre(n), correo(co) {}
    string getCedula() { return cedula; }
    string getNombre() { return nombre; }
    string getCorreo() { return correo; }
};

class Profesor : public Persona {
    string especialidad, materia;
public:
    Profesor(string c, string n, string co, string e, string m) : Persona(c,n,co), especialidad(e), materia(m) {}
};

class ProgramaAcademico {
public:
    // Rhadamés pidió el método virtual puro que recibe al alumno
    virtual string evaluarAprobacion(Alumno* a) = 0;
    virtual string getNombrePrograma() = 0;
    virtual ~ProgramaAcademico() {}
};

class Alumno : public Persona {
public:
    float n1, n2, n3;
    ProgramaAcademico* programa;
    
    Alumno(string c, string n, string co, ProgramaAcademico* p) : Persona(c,n,co), programa(p), n1(0), n2(0), n3(0) {}
    ~Alumno() { delete programa; } // Liberar memoria (Prueba EVAL-04)
};

class Curso : public ProgramaAcademico {
public:
    string evaluarAprobacion(Alumno* a) override {
        float prom = (a->n1 + a->n2 + a->n3) / 3.0f;
        return (prom >= 10.0f) ? "APROBADO" : "REPROBADO";
    }
    string getNombrePrograma() override { return "Curso"; }
};

class Diplomado : public ProgramaAcademico {
public:
    string evaluarAprobacion(Alumno* a) override {
        float prom = (a->n1 + a->n2 + a->n3) / 3.0f;
        return (prom >= 14.0f) ? "APROBADO" : "REPROBADO";
    }
    string getNombrePrograma() override { return "Diplomado"; }
};

class Bootcamp : public ProgramaAcademico {
public:
    string evaluarAprobacion(Alumno* a) override {
        return (a->n1>=14.0f && a->n2>=14.0f && a->n3>=14.0f) ? "APROBADO" : "REPROBADO";
    }
    string getNombrePrograma() override { return "Bootcamp"; }
};

// Variables globales
vector<Alumno*> alumnos;
vector<Profesor*> profesores;
stack<Alumno*> pilaDeshacer;

void guardarAlumnos() {
    ofstream f("alumnos.txt");
    for(auto a : alumnos) {
        f << a->getCedula() << "," << a->getNombre() << "," << a->getCorreo() << "," 
          << a->programa->getNombrePrograma() << "," << a->n1 << "," << a->n2 << "," << a->n3 << "\n";
    }
    f.close();
}

void generarCola() {
    queue<Alumno*> cola;
    for(auto a : alumnos) if(a->programa->evaluarAprobacion(a) == "APROBADO") cola.push(a);
    
    ofstream f("certificados_pendientes.txt");
    f << "=========================================\n";
    f << "REPORTE DE CERTIFICADOS PENDIENTES\n";
    f << "=========================================\n";
    f << "Total de graduandos en cola: " << cola.size() << "\n\n";
    
    int i=1;
    while(!cola.empty()) {
        Alumno* a = cola.front();
        f << i++ << ". [" << a->getCedula() << "] " << a->getNombre() << "\n";
        f << "- Programa: " << a->programa->getNombrePrograma() << "\n";
        float prom = (a->n1 + a->n2 + a->n3) / 3.0f;
        f << "- Promedio Final: " << prom << "\n";
        f << "- Estatus: " << a->programa->evaluarAprobacion(a) << "\n\n";
        cola.pop();
    }
    f << "=========================================\n* Fin del reporte - Generado por SGA-DO *\n";
    f.close();
    cout << "Cola generada en certificados_pendientes.txt\n";
}

int main() {
    int op;
    do {
        cout << "\n=== SGA-DO: SISTEMA DIPLOMADOSONLINE ===\n";
        cout << "1. Registrar Alumno\n2. Registrar Profesor\n3. Registrar Notas a un Alumno\n";
        cout << "4. Deshacer Último Registro de Nota\n5. Generar Cola de Certificados\n";
        cout << "6. Mostrar Reporte General\n7. Salir\nSeleccione una opción (1-7): ";
        
        if(!(cin >> op)) {
            cout << "Error: Ingrese un valor numérico válido.\n";
            cin.clear();
            cin.ignore(numeric_limits<streamsize>::max(), '\n');
            continue;
        }

        if(op == 1) {
            string c, n, co; int t;
            cout << "Cédula: "; cin >> c;
            cout << "Nombre: "; cin.ignore(); getline(cin, n);
            cout << "Correo: "; cin >> co;
            cout << "Programa (1.Curso 2.Diplomado 3.Bootcamp): "; cin >> t;
            ProgramaAcademico* p = (t==1) ? (ProgramaAcademico*) new Curso() : (t==2) ? (ProgramaAcademico*) new Diplomado() : (ProgramaAcademico*) new Bootcamp();
            alumnos.push_back(new Alumno(c, n, co, p));
            guardarAlumnos();
            cout << "Alumno registrado.\n";
        } else if(op == 2) {
            string c, n, co, e, m;
            cout << "Cédula: "; cin >> c;
            cout << "Nombre: "; cin.ignore(); getline(cin, n);
            cout << "Correo: "; cin >> co;
            cout << "Especialidad: "; cin.ignore(); getline(cin, e);
            cout << "Materia: "; getline(cin, m);
            profesores.push_back(new Profesor(c, n, co, e, m));
            cout << "Profesor registrado.\n";
        } else if(op == 3) {
            string c; float n1, n2, n3;
            cout << "Cédula del Alumno a buscar: "; cin >> c;
            bool found = false;
            for(auto a : alumnos) {
                if(a->getCedula() == c) {
                    cout << "Perfil encontrado: " << a->getNombre() << " (" << a->programa->getNombrePrograma() << ")\n";
                    cout << "Nota 1: "; cin >> n1;
                    cout << "Nota 2: "; cin >> n2;
                    cout << "Nota 3: "; cin >> n3;
                    a->n1 = n1; a->n2 = n2; a->n3 = n3;
                    pilaDeshacer.push(a);
                    guardarAlumnos();
                    cout << "Notas registradas.\n";
                    found = true;
                    break;
                }
            }
            if(!found) cout << "Alumno no encontrado.\n";
        } else if(op == 4) {
            if(!pilaDeshacer.empty()) {
                Alumno* a = pilaDeshacer.top();
                pilaDeshacer.pop();
                a->n1 = 0; a->n2 = 0; a->n3 = 0;
                guardarAlumnos();
                cout << "Última nota deshecha.\n";
            } else cout << "No hay notas para deshacer.\n";
        } else if(op == 5) {
            generarCola();
        } else if(op == 6) {
            cout << "\n--- ALUMNOS ---";
            for(auto a : alumnos) cout << "\n" << a->getNombre() << " (" << a->programa->getNombrePrograma() << ") - Estatus: " << a->programa->evaluarAprobacion(a);
            cout << "\n\n--- PROFESORES ---";
            for(auto p : profesores) cout << "\n" << p->getNombre();
            cout << endl;
        }
    } while(op != 7);

    // Limpieza de memoria (Evitar fugas - Prueba EVAL-04)
    for(auto a : alumnos) delete a;
    for(auto p : profesores) delete p;

    return 0;
}