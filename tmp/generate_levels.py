import random
import json
import os

def solve(grid_size, board):
    temp = [row[:] for row in board]
    
    def path_clear(r, c, d):
        if d == 'U':
            for i in range(r):
                if temp[i][c] != '':
                    return False
        elif d == 'D':
            for i in range(r + 1, grid_size):
                if temp[i][c] != '':
                    return False
        elif d == 'L':
            for j in range(c):
                if temp[r][j] != '':
                    return False
        elif d == 'R':
            for j in range(c + 1, grid_size):
                if temp[r][j] != '':
                    return False
        return True

    steps = []
    while True:
        moved = False
        for r in range(grid_size):
            for c in range(grid_size):
                val = temp[r][c]
                if val != '' and path_clear(r, c, val):
                    steps.append((r, c, val))
                    temp[r][c] = ''
                    moved = True
                    break
            if moved:
                break
        if not moved:
            break
            
    # Check if all cleared
    for r in range(grid_size):
        for c in range(grid_size):
            if temp[r][c] != '':
                return False, []
    return True, steps

def generate_level(level_id, grid_size, density):
    attempts = 0
    while attempts < 1000:
        attempts += 1
        board = [['' for _ in range(grid_size)] for _ in range(grid_size)]
        # Populate board based on density
        arrow_count = int(grid_size * grid_size * density)
        cells = [(r, c) for r in range(grid_size) for c in range(grid_size)]
        chosen_cells = random.sample(cells, arrow_count)
        
        for r, c in chosen_cells:
            board[r][c] = random.choice(['U', 'D', 'L', 'R'])
            
        solvable, steps = solve(grid_size, board)
        if solvable and len(steps) >= arrow_count:
            # We also want to make sure it's not too trivial
            return {
                "id": level_id,
                "difficulty": "Easy" if grid_size == 5 else ("Medium" if grid_size == 6 else "Hard"),
                "gridSize": grid_size,
                "board": board
            }
    return None

def main():
    levels = []
    print("Generating levels...")
    
    # Generate 120 levels to be safe
    for level_id in range(1, 121):
        if level_id <= 40:
            grid_size = 5
            density = 0.44
        elif level_id <= 80:
            grid_size = 6
            density = 0.50
        else:
            grid_size = 7
            density = 0.55
            
        lvl = generate_level(level_id, grid_size, density)
        while lvl is None:
            lvl = generate_level(level_id, grid_size, density)
        levels.append(lvl)
        
    os.makedirs("/app/src/main/assets", exist_ok=True)
    with open("/app/src/main/assets/levels.json", "w") as f:
        json.dump(levels, f, indent=2)
    print(f"Successfully generated {len(levels)} levels in assets/levels.json")

if __name__ == "__main__":
    main()
