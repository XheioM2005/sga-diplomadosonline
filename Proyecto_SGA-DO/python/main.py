import os
from collections import deque

# --- CLASES ---
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
    def evaluarAprobacion(self, alumno): pass

class Curso(ProgramaAcademico):
    def evaluarAprobacion(self, alumno):
        if not alumno.notas: return "REPROBADO"
        prom = sum(alumno.notas) / len(alumno.notas)
        return "APROBADO" if prom >= 10.0 else "REPROBADO"

class Diplomado(ProgramaAcademico):
    def evaluarAprobacion(self, alumno):
        if not alumno.notas: return "REPROBADO"
        prom = sum(alumno.notas) / len(alumno.notas)
        return "APROBADO" if prom >= 14.0 else "REPROBADO"

class Bootcamp(ProgramaAcademico):
    def evaluarAprobacion(self, alumno):
        if not alumno.notas: return "REPROBADO"
        for n in alumno.notas:
            if n < 14.0: return "REPROBADO"
        return "APROBADO"

class Alumno(Persona):
    def __init__(self, cedula, nombre, correo, programa):
        super().__init__(cedula, nombre, correo)
        self.programa = programa
        self.notas = []  # Lista de hasta 3 notas

# --- VARIABLES GLOBALES ---
alumnos = []
profesores = []
pila_deshacer = []  # LIFO: Guarda referencias al alumno

# --- PERSISTENCIA ---
def guardar_alumnos():
    with open("alumnos.txt", "w") as f:
        for a in alumnos:
            # Rellenar con 0 si tiene menos de 3 notas
            notas_padded = a.notas + [0.0] * (3 - len(a.notas))
            f.write(f"{a.cedula},{a.nombre},{a.correo},{a.programa.__class__.__name__},{notas_padded[0]},{notas_padded[1]},{notas_padded[2]}\n")

def guardar_profesores():
    with open("profesores.txt", "w") as f:
        for p in profesores:
            f.write(f"{p.cedula},{p.nombre},{p.correo},{p.especialidad},{p.materia}\n")

def cargar_datos():
    if os.path.exists("alumnos.txt"):
        with open("alumnos.txt", "r") as f:
            for line in f:
                datos = line.strip().split(",")
                if len(datos) == 7:
                    ced, nom, cor, tipo_prog, n1, n2, n3 = datos
                    prog = Curso() if tipo_prog == "Curso" else Diplomado() if tipo_prog == "Diplomado" else Bootcamp()
                    alumno = Alumno(ced, nom, cor, prog)
                    for n in [float(n1), float(n2), float(n3)]:
                        if n > 0:  # Solo cargar notas reales (0 = sin nota)
                            alumno.notas.append(n)
                    alumnos.append(alumno)
                    
    if os.path.exists("profesores.txt"):
        with open("profesores.txt", "r") as f:
            for line in f:
                datos = line.strip().split(",")
                if len(datos) == 5:
                    profesores.append(Profesor(datos[0], datos[1], datos[2], datos[3], datos[4]))

# --- MENÚ INTERACTIVO ---
def menu():
    cargar_datos()
    print(f"Datos cargados en memoria: {len(alumnos)} alumnos, {len(profesores)} profesores.")
    
    while True:
        print("\n=== SGA-DO: SISTEMA DIPLOMADOSONLINE ===")
        print("1. Registrar Alumno\n2. Registrar Profesor\n3. Registrar Notas a un Alumno")
        print("4. Deshacer Último Registro de Nota\n5. Generar Cola de Certificados")
        print("6. Mostrar Reporte General\n7. Salir")
        try:
            op = input("Seleccione una opción (1-7): ")
            
            if op == '1':
                ced = input("Cédula: ")
                # CORRECCIÓN 3: Validar cédula única
                if any(a.cedula == ced for a in alumnos):
                    print("Error: Ya existe un alumno registrado con esa cédula.")
                    continue
                nom = input("Nombre: ")
                cor = input("Correo: ")
                # CORRECCIÓN 1: Validar programa (no defaultear a Bootcamp)
                tipo = input("Programa (Curso/Diplomado/Bootcamp): ").lower()
                while tipo not in ["curso", "diplomado", "bootcamp"]:
                    print("Error: Programa inválido. Debe ser exactamente: Curso, Diplomado o Bootcamp.")
                    tipo = input("Programa (Curso/Diplomado/Bootcamp): ").lower()
                prog = Curso() if tipo == "curso" else Diplomado() if tipo == "diplomado" else Bootcamp()
                alumnos.append(Alumno(ced, nom, cor, prog))
                guardar_alumnos()
                print("Alumno registrado exitosamente.")
                
            elif op == '2':
                ced = input("Cédula: ")
                if any(p.cedula == ced for p in profesores):
                    print("Error: Ya existe un profesor registrado con esa cédula.")
                    continue
                nom = input("Nombre: ")
                cor = input("Correo: ")
                esp = input("Especialidad: ")
                mat = input("Materia: ")
                profesores.append(Profesor(ced, nom, cor, esp, mat))
                guardar_profesores()
                print("Profesor registrado exitosamente.")
                
            elif op == '3':
                # CORRECCIÓN 2: Registrar UNA nota a la vez
                ced = input("Cédula del Alumno a buscar: ")
                alumno = next((a for a in alumnos if a.cedula == ced), None)
                if alumno:
                    print(f"Perfil encontrado: {alumno.nombre} ({alumno.programa.__class__.__name__})")
                    print(f"Notas actuales: {alumno.notas} ({len(alumno.notas)}/3)")
                    if len(alumno.notas) >= 3:
                        print("El alumno ya tiene el máximo de 3 notas registradas.")
                        continue
                    try:
                        nota = float(input("Ingrese una calificación (0-20): "))
                        if nota < 0 or nota > 20:
                            print("Error: La calificación debe estar entre 0 y 20.")
                            continue
                        alumno.notas.append(nota)
                        pila_deshacer.append(alumno)  # Guardar referencia en la Pila LIFO
                        guardar_alumnos()
                        print(f"Nota registrada. Total notas: {len(alumno.notas)}/3")
                    except ValueError:
                        print("Error: Ingrese un valor numérico válido")
                else:
                    print("Alumno no encontrado.")
                    
            elif op == '4':
                # CORRECCIÓN 2: Deshacer UNA nota a la vez (LIFO)
                if pila_deshacer:
                    alumno = pila_deshacer.pop()
                    if alumno.notas:
                        nota_removida = alumno.notas.pop()  # Remover solo la última nota
                        guardar_alumnos()
                        print(f"Última nota ({nota_removida}) deshecha para {alumno.nombre}.")
                    else:
                        print("El alumno no tiene notas para deshacer.")
                else:
                    print("No hay notas registradas para deshacer.")
                    
            elif op == '5':
                cola = deque([a for a in alumnos if a.programa.evaluarAprobacion(a) == "APROBADO"])
                with open("certificados_pendientes.txt", "w") as f:
                    f.write(f"Total graduandos: {len(cola)}\n")
                    while cola:
                        a = cola.popleft()
                        f.write(f"{a.cedula} - {a.nombre} - APROBADO\n")
                print("Cola generada en certificados_pendientes.txt")
                
            elif op == '6':
                print("\n--- PROFESORES ---")
                for p in profesores:
                    print(f"{p.nombre} | {p.materia}")
                print("\n--- ALUMNOS ---")
                for a in alumnos:
                    print(f"{a.nombre} - {a.programa.evaluarAprobacion(a)}")
            elif op == '7':
                break
        except ValueError:
            print("Error: Ingrese un valor numérico válido")

if __name__ == "__main__":
    menu()
