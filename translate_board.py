#!/usr/bin/env python3
"""Translate Tablero.java to Board.java with proper encoding"""

import re

# Read Spanish file with correct encoding
with open('src/heuristica/Tablero.java', 'r', encoding='ISO-8859-1') as f:
    content = f.read()

# First pass - simple replacements (order matters!)
simple_replacements = [
    ('package heuristica', 'package heuristics'),
    ('import heuristica.', 'import heuristics.'),

    # Class names (before variable names)
    ('CasillaInexistente', 'NonexistentSquare'),
    ('Casilla', 'Square'),
    ('Celda', 'Cell'),
    ('Conexiones', 'Connections'),
    ('Borde', 'Border'),
    ('Tablero', 'Board'),

    # Variables
    ('CaducarOriginal', 'expireOriginal'),
    ('Mapa2', 'map2'),
    ('Mapa', 'map'),

    # Methods
    ('NuevaConexion', 'newConnection'),
    ('AgregarVecina', 'addNeighbor'),
    ('InsertarCaminoDirecto', 'insertDirectPath'),
    ('SonVecinas', 'areNeighbors'),
    ('HayConexion', 'hasConnection'),
    ('ObtenerListaCaducar', 'getExpireList'),
    ('ObtenerDimension', 'getDimension'),
    ('ObtenerCeldasLibres', 'getFreeCells'),
    ('ObtenerColor', 'getColor'),
    ('ObtenerFila', 'getRow'),
    ('ObtenerColumna', 'getColumn'),
    ('GenerarG', 'generateG'),
    ('ObtenerT', 'getConnections'),
    ('Obtener', 'get'),
    ('esVacia', 'isEmpty'),

    # Parameters
    ('columna', 'column'),
    ('fila', 'row'),
]

for spanish, english in simple_replacements:
    content = content.replace(spanish, english)

# Second pass - regex replacements for word boundaries
regex_replacements = [
    (r'\bDimension\b', 'dimension'),
    (r'\bTodas\b', 'all'),
    (r'\bc\[', 'squares['),
    (r'\bT\.', 'connections.'),
    (r'\bga\b', 'cellA'),
    (r'\bgb\b', 'cellB'),
    (r'\blista\b', 'list'),
    (r'\bf1\b', 'row1'),
    (r'\bc1\b', 'col1'),
    (r'\bf2\b', 'row2'),
    (r'\bc2\b', 'col2'),
    (r'Cell b = ', 'Cell target = '),
    (r' == b\)', ' == target)'),
]

for pattern, replacement in regex_replacements:
    content = re.sub(pattern, replacement, content)

# Third pass - fix specific single letter variables carefully
# Replace single letters n, s, e, o (west) only in specific contexts
content = re.sub(r'\bnorth, south, east, o;', 'north, south, east, west;', content)
content = re.sub(r'\bnorth = ', 'north = ', content)
content = re.sub(r'\bsouth = ', 'south = ', content)
content = re.sub(r'\bwest = new Border\(dim\*dim\+3, \'O\'\)', 'west = new Border(dim*dim+3, \'W\')', content)
content = re.sub(r'\beast = new Border\(dim\*dim\+2, \'E\'\)', 'east = new Border(dim*dim+2, \'E\')', content)
content = re.sub(r'\bnorth = new Border', 'north = new Border', content)
content = re.sub(r'\bsouth = new Border', 'south = new Border', content)

# Replace variable references to borders
content = re.sub(r'connections\.insertDirectPath\(o,', 'connections.insertDirectPath(west,', content)
content = re.sub(r'connections\.insertDirectPath\(e,', 'connections.insertDirectPath(east,', content)
content = re.sub(r'connections\.insertDirectPath\(n,', 'connections.insertDirectPath(north,', content)
content = re.sub(r'connections\.insertDirectPath\(s,', 'connections.insertDirectPath(south,', content)

content = re.sub(r'squares\[.*?\]\.addNeighbor\(o\)', lambda m: m.group(0).replace('(o)', '(west)'), content)
content = re.sub(r'squares\[.*?\]\.addNeighbor\(e\)', lambda m: m.group(0).replace('(e)', '(east)'), content)
content = re.sub(r'squares\[.*?\]\.addNeighbor\(n\)', lambda m: m.group(0).replace('(n)', '(north)'), content)
content = re.sub(r'squares\[.*?\]\.addNeighbor\(s\)', lambda m: m.group(0).replace('(s)', '(south)'), content)

content = re.sub(r'o\.addNeighbor', 'west.addNeighbor', content)
content = re.sub(r'e\.addNeighbor', 'east.addNeighbor', content)
content = re.sub(r'north\.addNeighbor', 'north.addNeighbor', content)
content = re.sub(r'south\.addNeighbor', 'south.addNeighbor', content)

# Fix G.add for borders
content = re.sub(r'G\.add\(n\);', 'G.add(north);', content)
content = re.sub(r'G\.add\(s\);', 'G.add(south);', content)
content = re.sub(r'G\.add\(e\);', 'G.add(east);', content)
content = re.sub(r'G\.add\(o\);', 'G.add(west);', content)

# Write with UTF-8 encoding
with open('src/heuristics/Board.java', 'w', encoding='UTF-8') as f:
    f.write(content)

print("Board.java translated successfully")
