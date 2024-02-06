// Note: Memory map:
// 0-63999 = 320x200 8bit palette image
// 64000-65023 = 256 RGBA8 palette entries
// 65536-321535 = 320x200 RGBA8 screen

const memory = new WebAssembly.Memory({initial:5});

// Prepare "imports" - structures we export from JS to WASM
const wasmImports = { js: { mem: memory } };

var wasmMode13hObject = null;
var wasmMainObject = null;

onmessage = function(e) {
    if(e.data != null) {
        // Let's set an initial palette
        var u32view_palette = new Uint32Array(wasmImports.js.mem.buffer, 64000, 256);
        for (var i = 0; i < 256; i++) {
            u32view_palette[i] = i + (i<<8) + (i<<16) + 0xff000000;
        }

        // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/WebAssembly
        WebAssembly.instantiate(e.data.mode13h, wasmImports).then(results => {
            wasmMode13hObject = results;

            WebAssembly.instantiate(e.data.binary, wasmImports).then(results => {
                wasmMainObject = results;
                postMessage(null);
            });
        });
    } else {
        wasmMainObject.instance.exports.wasm_main();
        wasmMode13hObject.instance.exports.palette_to_rgba8();
        // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Uint8ClampedArray/Uint8ClampedArray
        const u8view = new Uint8ClampedArray(wasmImports.js.mem.buffer, 65536, 64000*4);
        const img = new ImageData(u8view, 320, 200);
        postMessage(img);
        //postMessage(u8view, [u8view.buffer]); // tried this, but u8view is not "detachable" - which makes sense because it's a view and wasm.memory cannot be transferred between threads
    }
};