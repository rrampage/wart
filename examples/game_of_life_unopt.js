/*
    Game of Life written in pure WASM
    From https://ashen.earth/wasm-game-of-life-1
 */
let gameState = {
    running: false,
    pixelSize: 0,
    lastReported: null,
    frameTimes: [],
    frames: 0,
    width: 0,
    height: 0,
    canvas: null,
    ctx: null,
    gameExports: null
}

const initialMessage = "Click the board above to start simulation"

export async function setup(params, wasmModule) {
    const { canvas: canvasSelector } = params;
    gameState.gameExports = wasmModule.exports

    const canvas = gameState.canvas = document.querySelector(canvasSelector)
    gameState.ctx = gameState.canvas.getContext("2d")
    gameState.ctx.fillStyle = getComputedStyle(gameState.canvas).getPropertyValue('color')

    const pixelSize = gameState.pixelSize = parseInt(canvas.getAttribute('data-pixelsize') || '2')
    gameState.width = Math.floor(parseInt(canvas.width) / pixelSize)
    gameState.height = Math.floor(parseInt(canvas.height) / pixelSize)

    initialize()
    drawBoard()

    const frameTimesElem = document.getElementById('frameTimes')
    const resetButton = document.getElementById('reset')

    frameTimesElem.innerText = initialMessage

    gameState.canvas.addEventListener('click', () => {
        if (!gameState.running) {
            if (frameTimesElem.innerText === initialMessage)
                frameTimesElem.innerText = 'Starting...'

            gameState.running = true
            frameLoop()
        } else {
            gameState.running = false;
            gameState.frameTimes = []
            gameState.lastReported = null
        }
    })

    resetButton.addEventListener('click', () => {
        gameState.running = false
        gameState.frameTimes = []
        gameState.lastReported = null
        frameTimesElem.innerText = initialMessage
        initialize()
        drawBoard()
    })
}

export async function onThemeChange() {
    gameState.ctx.fillStyle = getComputedStyle(gameState.canvas).getPropertyValue('color')
    drawBoard()
}

export async function cleanup() {
    gameState.running = false
}

function initialize() {
    const { gameExports, width, height } = gameState

    gameExports.initializeBoard(width, height)

    for (let row = 0; row < height; row++) {
        for (let column = 0; column < width; column++) {
            const filled = Math.random() > .5;
            gameExports.setValueAtPosition(row, column, filled ? 1 : 0)
        }
    }
}

function frameLoop() {
    const { gameExports, running, frameTimes } = gameState

    if (!gameState.lastReported)
        gameState.lastReported = performance.now()

    if (!running) return

    const beforeTick = performance.now()
    gameExports.tick()
    const afterTick = performance.now()
    drawBoard()
    const afterDraw = performance.now()

    // Push raw frame time
    frameTimes.push(afterDraw - beforeTick)

    if (frameTimes.length >= 10) {
        const averageFrame = (frameTimes.reduce((a, b) => a + b) / frameTimes.length).toFixed(2);

        const lastReported = gameState.lastReported
        const current = gameState.lastReported = performance.now()
        const tenFrameTime = (current - lastReported) / 1000
        const fps = Math.floor(10 / tenFrameTime).toFixed(0)
        gameState.frameTimes = []

        const pagesUsed = gameExports.getPagesUsed()

        document.getElementById('frameTimes').innerHTML =
            `Frames per second: ${fps}<br/>
       Milliseconds per frame: ${averageFrame}<br/>
       Memory allocated: ${pagesUsed} pages (${pagesUsed * 64} KiB)`
    }

    console.log(`tick took ${afterTick - beforeTick}ms`)
    console.log(`draw took ${afterDraw - afterTick}ms`)

    requestAnimationFrame(frameLoop)
}

function drawBoard() {
    const { gameExports, width, height, pixelSize, ctx, canvas } = gameState

    ctx.clearRect(0, 0, canvas.width, canvas.height)
    ctx.beginPath()
    for (let row = 0; row < height; row++) {
        for (let column = 0; column < width; column++) {
            const alive = gameExports.getValueAtPosition(row, column)

            if (!alive) continue

            const x = column * pixelSize
            const y = row * pixelSize

            ctx.moveTo(x, y)
            ctx.lineTo(x + pixelSize, y)
            ctx.lineTo(x + pixelSize, y + pixelSize)
            ctx.lineTo(x, y + pixelSize)
            ctx.lineTo(x, y)
        }
    }
    ctx.fill()
}
