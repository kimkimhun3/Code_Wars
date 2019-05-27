const STEP = {
  '←': [-1, 0],
  '↖': [-1, -1],
  '↑': [0, -1],
  '↗': [1, -1],
  '→': [1, 0],
  '↘': [1, 1],
  '↓': [0, 1],
  '↙': [-1, 1]
}
const directions = Object.keys(STEP)
const steps = Object.values(STEP)

const charAt = (matrix, [x, y]) => {
  const row = matrix[y]
  return row && row[x]
}

const isStart = char => char === 'S'

const len = directions.length
const directionDistance = (a, b) =>
  (directions.indexOf(a) + len - directions.indexOf(b)) % len

const areCompatibleDirections = (a, b) => {
  if (isStart(a) || isStart(b)) return true
  return Math.min(directionDistance(a, b), directionDistance(b, a)) < 3
}

const step = ([x, y], [modX, modY]) => [x + modX, y + modY]

const isSamePos = (a, b) => a[0] === b[0] && a[1] === b[1]

function walk(matrix, pos, path = [], visited = []) {
  const char = charAt(matrix, pos)
  if (isStart(char) && path.length) return path
  
  return directions
    .map(direction => ({ direction, nextPos: step(pos, STEP[direction]) }))
    .filter(({ direction, nextPos }) => {
      const nextChar = charAt(matrix, nextPos)
      return nextChar
        && !visited.slice(1).find(visitedPos => isSamePos(visitedPos, nextPos))
        && areCompatibleDirections(char, direction)
        && areCompatibleDirections(nextChar, direction)
    })
    .reduce(
      (longestPath, { direction, nextPos }) => {
        const nextLongestPath = walk(matrix, nextPos, [...path, direction], [...visited, pos])
        return (!longestPath || (nextLongestPath && nextLongestPath.length > longestPath.length))
          ? nextLongestPath
          : longestPath
      },
      null
    )
}

const findStart = matrix => matrix.reduce(
  (startPos, line, y) => {
    if (startPos) return startPos
    const x = line.findIndex(isStart)
    return (x !== -1) ? [x, y] : null
  },
  null
)

function dance(map) {
  const matrix = map.split('\n').map(line => line.split(''))
  const start = findStart(matrix)
  const longestPath = walk(matrix, start) || []
  return longestPath.join('')
}
