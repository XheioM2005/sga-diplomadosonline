import os
from collections import deque

# --- CLASES (Modelado Orientado a Objetos) ---
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
    # Metodo polimorfico: cada clase hija define su propia regla
    def evaluarAprobacion(self, alumno):
        pass

class Curso(ProgramaAcademico):
    def evaluarAprobacion(self, alumno):
        if not alumno.notas:
            return "REPROBADO"
        prom = sum(alumno.notas) / len(alumno.notas)
        return "APROBADO" if prom >= 10.0 else "REPROBADO"

class Diplomado(ProgramaAcademico):
    def evaluarAprobacion(self, alumno):
        if not alumno.notas:
            return "REPROBADO"
        prom = sum(alumno.notas) / len(alumno.notas)
        return "APROBADO" if prom >= 14.0 else "REPROBADO"

class Bootcamp(ProgramaAcademico):
    def evaluarAprobacion(self, alumno):
        if not alumno.notas:
            return "REPROBADO"
        for n in alumno.notas:
            if n < 14.0:
                return "REPROBADO"
        return "APROBADO"

class Alumno(Persona):
    def __init__(self, cedula, nombre, correo, programa):
        super().__init__(cedula, nombre, correo)
        self.programa = programa
        self.notas = []  # Lista de notas (maximo 3, una a la vez)

# --- VARIABLES GLOBALES ---
alumnos = []
profesores = []
pila_deshacer = []  # Pila LIFO: guarda (alumno) por cada nota ingresada

# --- PERSISTENCIA EN DISCO (.txt) ---
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
    """Carga los archivos .txt a memoria al iniciar el programa (persistencia)."""
    if os.path.exists("alumnos.txt"):
        with open("alumnos.txt", "r") as f:
            for line in f:
                datos = line.strip().split(",")
                if len(datos) == 7:
                    ced, nom, cor, tipo_prog, n1, n2, n3 = datos
                    if tipo_prog == "Curso":
                        prog = Curso()
                    elif tipo_prog == "Diplomado":
                        prog = Diplomado()
                    else:
                        prog = Bootcamp()
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

# --- MENSAJE DEL MENU ---
def imprimir_menu():
    print("=" * 50)
    print("SGA-DO: SISTEMA DIPLOMADOSONLINE")
    print("=" * 50)
    print("1. Registrar Alumno")
    print("2. Registrar Profesor")
    print("3. Registrar Notas a un Alumno")
    print("4. Deshacer Último Registro de Nota")
    print("5. Generar Cola de Certificados")
    print("6. Mostrar Reporte General")
    print("7. Salir")
    print("=" * 50)

# --- OPCIONES DEL MENU ---
def registrar_alumno():
    ced = input("Cédula: ").strip()
    # Correccion: validar cedula unica (no duplicados)
    if any(a.cedula == ced for a in alumnos):
        print("Error: Ya existe un alumno registrado con esa cédula.")
        return
    nom = input("Nombre Completo: ").strip()
    cor = input("Correo Electrónico: ").strip()
    # Correccion: validar programa (nunca defaultear a Bootcamp)
    tipo = input("Tipo de Programa (Curso/Diplomado/Bootcamp): ").strip().lower()
    while tipo not in ["curso", "diplomado", "bootcamp"]:
        print("Error: Programa inválido. Debe ser exactamente: Curso, Diplomado o Bootcamp.")
        tipo = input("Tipo de Programa (Curso/Diplomado/Bootcamp): ").strip().lower()
    if tipo == "curso":
        prog = Curso()
    elif tipo == "diplomado":
        prog = Diplomado()
    else:
        prog = Bootcamp()
    alumnos.append(Alumno(ced, nom, cor, prog))
    guardar_alumnos()  # Escritura inmediata en disco
    print("Alumno registrado exitosamente.")

def registrar_profesor():
    ced = input("Cédula: ").strip()
    # Correccion: validar cedula unica (no duplicados)
    if any(p.cedula == ced for p in profesores):
        print("Error: Ya existe un profesor registrado con esa cédula.")
        return
    nom = input("Nombre Completo: ").strip()
    cor = input("Correo Electrónico: ").strip()
    esp = input("Especialidad Académica: ").strip()
    mat = input("Materia Asignada: ").strip()
    profesores.append(Profesor(ced, nom, cor, esp, mat))
    guardar_profesores()  # Escritura inmediata en disco
    print("Profesor registrado exitosamente.")

def registrar_notas():
    ced = input("Cédula del Alumno a buscar: ").strip()
    alumno = next((a for a in alumnos if a.cedula == ced), None)
    if alumno is None:
        print("Alumno no encontrado.")
        return
    # Buscar y mostrar el perfil del alumno
    print(f"Perfil encontrado: {alumno.nombre} ({alumno.programa.__class__.__name__})")
    print(f"Notas actuales: {alumno.notas} ({len(alumno.notas)}/3)")
    if len(alumno.notas) >= 3:
        print("El alumno ya tiene el máximo de 3 notas registradas.")
        return
    # Correccion: registrar UNA sola nota por vez
    try:
        nota = float(input("Ingrese una calificación (0-20): "))
        if nota < 0 or nota > 20:
            print("Error: La calificación debe estar entre 0 y 20.")
            return
        alumno.notas.append(nota)
        pila_deshacer.append(alumno)  # Guardar en la Pila (LIFO)
        guardar_alumnos()  # Escritura inmediata en disco
        print(f"Nota registrada. Total notas: {len(alumno.notas)}/3")
    except ValueError:
        print("Error: Ingrese un valor numérico válido")

def deshacer_nota():
    # Correccion: deshacer UNA sola nota (la ultima ingresada) - LIFO
    if not pila_deshacer:
        print("No hay notas registradas para deshacer.")
        return
    alumno = pila_deshacer.pop()
    if not alumno.notas:
        print("El alumno no tiene notas para deshacer.")
        return
    nota_removida = alumno.notas.pop()  # Remover SOLO la ultima nota
    guardar_alumnos()
    print(f"Última nota ({nota_removida}) deshecha para {alumno.nombre}. Notas restantes: {len(alumno.notas)}")

def generar_cola_certificados():
    # Cola FIFO: se encolan los aprobados y se procesan en orden de llegada
    cola = deque([a for a in alumnos if a.programa.evaluarAprobacion(a) == "APROBADO"])
    with open("certificados_pendientes.txt", "w") as f:
        # Encabezado formal (formato del proyecto.pdf)
        f.write("=========================================\n")
        f.write("REPORTE DE CERTIFICADOS PENDIENTES\n")
        f.write("=========================================\n")
        f.write(f"Total de graduandos en cola: {len(cola)}\n\n")

        i = 1
        while cola:
            a = cola.popleft()  # FIFO: el primero en entrar es el primero en salir
            promedio = sum(a.notas) / len(a.notas) if a.notas else 0
            f.write(f"{i}. [{a.cedula}] {a.nombre}\n")
            f.write(f"- Programa: {a.programa.__class__.__name__}\n")
            f.write(f"- Promedio Final: {promedio:.1f}\n")
            if isinstance(a.programa, Bootcamp):
                f.write("- Estatus: APROBADO (Cumple regla de ninguna nota < 14)\n\n")
            else:
                f.write("- Estatus: APROBADO\n\n")
            i += 1

        # Pie de pagina
        f.write("=========================================\n")
        f.write("* Fin del reporte - Generado por SGA-DO *\n")
    print("Cola generada en certificados_pendientes.txt")

def mostrar_reporte():
    print("\n--- PROFESORES ACTIVOS ---")
    if not profesores:
        print("(No hay profesores registrados)")
    for p in profesores:
        print(f"[{p.cedula}] {p.nombre} | Especialidad: {p.especialidad} | Materia: {p.materia}")

    print("\n--- ALUMNOS REGISTRADOS ---")
    if not alumnos:
        print("(No hay alumnos registrados)")
    for a in alumnos:
        print(f"[{a.cedula}] {a.nombre} ({a.programa.__class__.__name__}) | Notas: {a.notas} | Estatus: {a.programa.evaluarAprobacion(a)}")

# --- FUNCION PRINCIPAL ---
def main():
    cargar_datos()  # Persistencia: cargar .txt al iniciar
    print(f"Datos cargados en memoria: {len(alumnos)} alumnos, {len(profesores)} profesores.")

    while True:
        imprimir_menu()
        try:
            op = input("Seleccione una opción (1-7): ").strip()
            if op == '1':
                registrar_alumno()
            elif op == '2':
                registrar_profesor()
            elif op == '3':
                registrar_notas()
            elif op == '4':
                deshacer_nota()
            elif op == '5':
                generar_cola_certificados()
            elif op == '6':
                mostrar_reporte()
            elif op == '7':
                print("Guardando cambios y saliendo del sistema... ¡Hasta pronto!")
                break
            else:
                print("Error: Opción no válida. Debe ser un número del 1 al 7.")
        except ValueError:
            print("Error: Ingrese un valor numérico válido")

if __name__ == "__main__":
    main()
