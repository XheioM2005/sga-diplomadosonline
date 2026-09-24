# --- CLASES (Modelado) ---
class Persona:
    def __init__(self, cedula, nombre, correo):
        self.cedula = cedula
        self.nombre = nombre
        self.correo = correo

class Profesor(Persona):
    def __init__(self, cedula, nombre, correo, especialidad, materia):
        super().__init__(cedula, nombre, correo)
        self.especialidad = especialidad
        self.materia = materia

class ProgramaAcademico:
    def evaluarAprobacion(self, alumno): pass # Polimorfismo

class Curso(ProgramaAcademico):
    def evaluarAprobacion(self, alumno):
        prom = (alumno.n1 + alumno.n2 + alumno.n3) / 3
        return "APROBADO" if prom >= 10.0 else "REPROBADO"

class Diplomado(ProgramaAcademico):
    def evaluarAprobacion(self, alumno):
        prom = (alumno.n1 + alumno.n2 + alumno.n3) / 3
        return "APROBADO" if prom >= 14.0 else "REPROBADO"

class Bootcamp(ProgramaAcademico):
    def evaluarAprobacion(self, alumno):
        return "APROBADO" if (alumno.n1>=14 and alumno.n2>=14 and alumno.n3>=14) else "REPROBADO"

class Alumno(Persona):
    def __init__(self, cedula, nombre, correo, programa):
        super().__init__(cedula, nombre, correo)
        self.programa = programa
        self.n1, self.n2, self.n3 = 0, 0, 0

# --- VARIABLES GLOBALES ---
alumnos = []
profesores = []
pila_deshacer = []

# --- FUNCIONES DE GUARDADO ---
def guardar_alumnos():
    with open("alumnos.txt", "w") as f:
        for a in alumnos:
            f.write(f"{a.cedula},{a.nombre},{a.correo},{a.programa.__class__.__name__},{a.n1},{a.n2},{a.n3}\n")

def guardar_profesores():
    with open("profesores.txt", "w") as f:
        for p in profesores:
            f.write(f"{p.cedula},{p.nombre},{p.correo},{p.especialidad},{p.materia}\n")

# --- MENÚ INTERACTIVO ---
def menu():
    while True:
        print("\n=== SGA-DO: SISTEMA DIPLOMADOSONLINE ===")
        print("1. Registrar Alumno\n2. Registrar Profesor\n3. Registrar Notas a un Alumno")
        print("4. Deshacer Último Registro de Nota\n5. Generar Cola de Certificados")
        print("6. Mostrar Reporte General\n7. Salir")
        try:
            op = input("Seleccione una opción (1-7): ")
            if op == '1':
                ced = input("Cédula: "); nom = input("Nombre: "); cor = input("Correo: ")
                tipo = input("Programa (Curso/Diplomado/Bootcamp): ").lower()
                prog = Curso() if tipo == "curso" else Diplomado() if tipo == "diplomado" else Bootcamp()
                alumnos.append(Alumno(ced, nom, cor, prog))
                guardar_alumnos()
            elif op == '2':
                ced = input("Cédula: "); nom = input("Nombre: "); cor = input("Correo: ")
                esp = input("Especialidad: "); mat = input("Materia: ")
                profesores.append(Profesor(ced, nom, cor, esp, mat))
                guardar_profesores()
            elif op == '3':
                ced = input("Cédula del Alumno a buscar: ")
                alumno = next((a for a in alumnos if a.cedula == ced), None)
                if alumno:
                    print(f"Perfil encontrado: {alumno.nombre} ({alumno.programa.__class__.__name__})")
                    alumno.n1 = float(input("Nota 1: ")); alumno.n2 = float(input("Nota 2: ")); alumno.n3 = float(input("Nota 3: "))
                    pila_deshacer.append(alumno)
                    guardar_alumnos()
                else: print("Alumno no encontrado.")
            elif op == '4':
                if pila_deshacer:
                    alumno = pila_deshacer.pop()
                    alumno.n1, alumno.n2, alumno.n3 = 0, 0, 0
                    guardar_alumnos()
                    print("Última nota deshecha.")
                else: print("No hay notas para deshacer.")
            elif op == '5':
                from collections import deque
                cola = deque([a for a in alumnos if a.programa.evaluarAprobacion(a) == "APROBADO"])
                with open("certificados_pendientes.txt", "w") as f:
                    f.write(f"Total graduandos: {len(cola)}\n")
                    while cola: 
                        a = cola.popleft()
                        f.write(f"{a.cedula} - {a.nombre} - APROBADO\n")
                print("Cola generada en certificados_pendientes.txt")
            elif op == '6':
                print("\n--- PROFESORES ---")
                for p in profesores: print(f"{p.nombre} | {p.materia}")
                print("\n--- ALUMNOS ---")
                for a in alumnos: print(f"{a.nombre} - {a.programa.evaluarAprobacion(a)}")
            elif op == '7': break
        except ValueError: print("Error: Ingrese un valor numérico válido")

if __name__ == "__main__":
    menu()