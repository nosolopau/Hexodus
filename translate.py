#!/usr/bin/env python3
"""
Translation script for Hexodus project
Translates Spanish code to English
"""

import os
import re
import shutil

# Translation mappings
PACKAGE_MAPPING = {
    'heuristica': 'heuristics',
    'juego': 'game',
    'interfaz': 'ui',
    'imagenes': 'images'
}

CLASS_MAPPING = {
    # game package
    'Partida': 'Match',
    'Jugador': 'Player',
    'Movimiento': 'Move',
    'Tablero': 'Board',
    'Casilla': 'Square',
    'Borde': 'Border',
    'CasillaInexistente': 'NonexistentSquare',
    'CasillaOcupada': 'OccupiedSquare',
    'DimensionIlegal': 'IllegalDimension',
    'NivelIncorrecto': 'IncorrectLevel',

    # heuristics package
    'Ruta': 'Route',
    'Heuristica': 'Heuristic',
    'Simulacion': 'Simulation',
    'Celda': 'Cell',
    'Camino': 'Path',
    'Conexiones': 'Connections',
    'Factoria': 'Factory',
    'Matriz': 'Matrix',
    'Multihilo': 'MultiThread',
    'Monohilo': 'SingleThread',
    'Hilo': 'GameThread',

    # ui package
    'Main': 'Main',
    'Juego': 'GameWindow',
    'Opciones': 'OptionsDialog',
    'AcercaDe': 'AboutDialog',
    'Panel': 'BoardPanel',
    'GestorMenu': 'MenuHandler',
    'GestorCasilla': 'CellHandler',
    'GestorCasillaNormal': 'NormalCellHandler',
    'GestorCasillaSwap': 'SwapCellHandler',
    'GestorBotonSwap': 'SwapButtonHandler',
    'GestorBotonCerrar': 'WindowCloseHandler',
    'Aceptar': 'AcceptHandler'
}

METHOD_MAPPING = {
    'Obtener': 'get',
    'Eliminar': 'remove',
    'Agregar': 'add',
    'Establecer': 'set',
    'Restaurar': 'restore',
    'Calcular': 'calculate',
    'Mostrar': 'show',
    'Generar': 'generate',
    'Elegir': 'choose',
    'Decidir': 'decide',
    'Ofrecer': 'offer',
    'Nuevo': 'new',
    'Cambia': 'change',
    'Sugiere': 'suggest',
    'Elimina': 'remove',
    'Genera': 'generate',
    'Actualiza': 'update',
    'Aplicar': 'apply',
    'Clonar': 'clone',
    'Unir': 'unite',
    'Intercambia': 'swap',
    'Asigna': 'assign',
    'Suma': 'add',
    'Producto': 'product',
    'Finalizar': 'finish',
    'Demo': 'demo'
}

VARIABLE_MAPPING = {
    'fila': 'row',
    'columna': 'column',
    'tablero': 'board',
    'dimension': 'dimension',
    'color': 'color',
    'ocupada': 'occupied',
    'nombre': 'name',
    'tipo': 'type',
    'posicion': 'position',
    'movimiento': 'move',
    'autor': 'author',
    'numero': 'number',
    'vecinas': 'neighbors',
    'conexiones': 'connections',
    'caminos': 'paths',
    'minimo': 'minimum',
    'directo': 'direct',
    'nuevo': 'newPath',
    'resistencia': 'resistance',
    'celda': 'cell',
    'casilla': 'square',
    'borde': 'border',
    'partida': 'match',
    'jugador': 'player',
    'motor': 'engine',
    'factoria': 'factory',
    'matriz': 'matrix',
    'identificador': 'identifier',
    'ocupante': 'occupant',
    'conectada': 'connected',
    'renovar': 'renew',
    'objetivo': 'target',
    'intercambio': 'swap',
    'favoritos': 'favorites',
    'sugerido': 'suggested',
    'turno': 'turn',
    'uno': 'one',
    'dos': 'two',
    'primera': 'first',
    'ancho': 'width',
    'alto': 'height',
    'desplHoriz': 'horizontalOffset',
    'desplVert': 'verticalOffset',
    'tamH': 'widthSize',
    'tamV': 'heightSize',
    'incrH': 'horizontalIncrement',
    'incrV': 'verticalIncrement',
    'despl': 'offset',
    'barra': 'statusBar',
    'rojo': 'red',
    'azul': 'blue',
    'sugerencia': 'suggestion',
    'ic_turno': 'turnIcon',
    'texto': 'text',
    'menus': 'menus',
    'ganador': 'winner',
    'estado': 'status',
    'rutaImagen': 'imagePath',
    'bgimage': 'backgroundImage',
    'lista': 'list',
    'iteraciones': 'iterations',
    'profundidad': 'depth',
    'precisión': 'precision',
    'tolerancia': 'tolerance',
    'solución': 'solution',
    'normal': 'normal',
    'experto': 'expert'
}

STRING_MAPPING = {
    'Nuevo juego': 'New Game',
    'Opciones del juego': 'Game Options',
    'Jugador 1 (vertical)': 'Player 1 (Vertical)',
    'Jugador 2 (horizontal)': 'Player 2 (Horizontal)',
    'Aceptar': 'Accept',
    'Humano': 'Human',
    'Ordenador': 'Computer',
    'Dimension: ': 'Dimension: ',
    'Activar regla de intercambio': 'Enable swap rule',
    'Acerca de Hexodus': 'About Hexodus',
    'Cerrar': 'Close',
    'Juego': 'Game',
    'Ayuda': 'Help',
    'Nuevo juego...': 'New Game...',
    'Sugerir jugada': 'Suggest Move',
    'Modo normal': 'Normal Mode',
    'Modo experto': 'Expert Mode',
    'Acerca de...': 'About...',
    'Hexodus está pensando...': 'Hexodus is thinking...',
    'Intercambiar jugada': 'Swap Move',
    'Gana el ': 'Winner: ',
    'Hexodus ha intercambiado la jugada': 'Hexodus has swapped the move',
    'Ocupada': 'Occupied',
    'Fuera de rango': 'Out of range',
    'No clonable': 'Not cloneable',
    'Azar': 'Random',
    'jugador vertical': 'vertical player',
    'jugador horizontal': 'horizontal player'
}

def translate_content(content):
    """Translate file content from Spanish to English"""

    # First translate package declarations
    for spanish, english in PACKAGE_MAPPING.items():
        content = re.sub(f'package {spanish};', f'package {english};', content)
        content = re.sub(f'import {spanish}\.', f'import {english}.', content)

    # Translate class names in declarations and usage
    for spanish, english in CLASS_MAPPING.items():
        # Class declarations
        content = re.sub(f'class {spanish}\\b', f'class {english}', content)
        content = re.sub(f'interface {spanish}\\b', f'interface {english}', content)
        # Constructor names
        content = re.sub(f'public {spanish}\\(', f'public {english}(', content)
        content = re.sub(f'private {spanish}\\(', f'private {english}(', content)
        # Type usage
        content = re.sub(f'\\b{spanish}\\b', english, content)

    # Translate UI strings
    for spanish, english in STRING_MAPPING.items():
        content = content.replace(f'"{spanish}"', f'"{english}"')

    # Translate common Spanish comments
    comment_translations = {
        'Creado el': 'Created on',
        'Representa': 'Represents',
        'Devuelve': 'Returns',
        'Evita': 'Avoids',
        'Mantiene': 'Maintains',
        'Modifica': 'Modifies',
        'Informa de': 'Informs about',
        'Elige': 'Chooses',
        'Elimina': 'Deletes',
        'Agrega': 'Adds',
        'Sobreescribe': 'Overrides',
        'Recorre': 'Traverses',
        'Crea': 'Creates',
        'Fuerza': 'Forces',
        'Crear una instancia sin mensaje': 'Create an instance without a message',
        'Crear una instancia con el mensaje que se le pase como argumento': 'Create an instance with the provided message',
        'El mensaje detallado en cuestión': 'The detailed message',
        'Excepción que se lanza cuando': 'Exception thrown when',
        'se intenta acceder a una casilla inexistente en el tablero': 'attempting to access a nonexistent square on the board',
        'se intenta colocar una ficha sobre una casilla ocupada': 'attempting to place a piece on an occupied square',
        'se utiliza una dimensión ilegal para el tablero': 'using an illegal dimension for the board',
        'se intenta proporcionar un nivel incorrecto a la heurística': 'attempting to provide an incorrect level to the heuristic',
        'desde la interfaz del sistema': 'from the system interface'
    }

    for spanish, english in comment_translations.items():
        content = content.replace(spanish, english)

    return content

def main():
    print("Starting Hexodus translation...")

    # Create target directories
    os.makedirs('src/game', exist_ok=True)
    os.makedirs('src/heuristics', exist_ok=True)
    os.makedirs('src/ui', exist_ok=True)
    os.makedirs('src/images', exist_ok=True)

    # Copy and translate exception files first (they're simple)
    exceptions = [
        ('juego/CasillaInexistente.java', 'game/NonexistentSquare.java'),
        ('juego/CasillaOcupada.java', 'game/OccupiedSquare.java'),
        ('juego/DimensionIlegal.java', 'game/IllegalDimension.java'),
        ('juego/NivelIncorrecto.java', 'game/IncorrectLevel.java')
    ]

    for spanish_path, english_path in exceptions:
        src_file = f'src/{spanish_path}'
        dst_file = f'src/{english_path}'

        with open(src_file, 'r', encoding='ISO-8859-1') as f:
            content = f.read()

        translated = translate_content(content)

        with open(dst_file, 'w', encoding='UTF-8') as f:
            f.write(translated)

        print(f"Translated: {spanish_path} -> {english_path}")

    print("\nException classes translated!")
    print("Note: Remaining files require manual translation due to complexity.")
    print("This script has created the basic structure and translated simple files.")

if __name__ == '__main__':
    main()
