// -----------------------------------------------
// main_webGL.js — 横長画像を分割して1canvasに並べて描画
// -----------------------------------------------
import { VERT_SRC, FRAG_SRC } from './shader_webGL.js';
import { setupInteraction } from './interaction.js';

const canvas = document.getElementById('glcanvas');
const gl     = canvas.getContext('webgl');
const info   = document.getElementById('info');

if (!gl) { alert('WebGL が使えません'); throw new Error('WebGL not supported'); }

const maxTex = gl.getParameter(gl.MAX_TEXTURE_SIZE);

// ---- キャンバスサイズ -----------------------------------------
function resizeCanvas() {
  const dpr = window.devicePixelRatio || 1;
  canvas.width  = canvas.clientWidth  * dpr;
  canvas.height = canvas.clientHeight * dpr;
  gl.viewport(0, 0, canvas.width, canvas.height);
}
resizeCanvas();
window.addEventListener('resize', () => { resizeCanvas(); render(); });

// ---- シェーダーコンパイル -------------------------------------
function compileShader(type, src) {
  const s = gl.createShader(type);
  gl.shaderSource(s, src);
  gl.compileShader(s);
  if (!gl.getShaderParameter(s, gl.COMPILE_STATUS))
    throw new Error(gl.getShaderInfoLog(s));
  return s;
}

const prog = gl.createProgram();
gl.attachShader(prog, compileShader(gl.VERTEX_SHADER,   VERT_SRC));
gl.attachShader(prog, compileShader(gl.FRAGMENT_SHADER, FRAG_SRC));
gl.linkProgram(prog);
gl.useProgram(prog);

// ---- 属性ロケーション取得 -------------------------------------
const aPosLoc = gl.getAttribLocation(prog, 'a_position');
const aTexLoc = gl.getAttribLocation(prog, 'a_texCoord');
const uMatrix  = gl.getUniformLocation(prog, 'u_matrix');
const uTexture = gl.getUniformLocation(prog, 'u_texture');

// ---- バッファを作る関数（左 or 右の四角形）-------------------
// clipX0, clipX1: クリップ空間でのX範囲（左半分=-1～0、右半分=0～1）
function makeQuadBuffer(clipX0, clipX1) {
  // 頂点座標（クリップ空間）  UV座標
  const verts = new Float32Array([
    clipX0, -1,   0, 1,   // 左下
    clipX1, -1,   1, 1,   // 右下
    clipX0,  1,   0, 0,   // 左上
    clipX1, -1,   1, 1,   // 右下
    clipX1,  1,   1, 0,   // 右上
    clipX0,  1,   0, 0,   // 左上
  ]);
  const buf = gl.createBuffer();
  gl.bindBuffer(gl.ARRAY_BUFFER, buf);
  gl.bufferData(gl.ARRAY_BUFFER, verts, gl.STATIC_DRAW);
  return buf;
}

// 左半分: クリップ空間 X = -1 ～ 0
// 右半分: クリップ空間 X =  0 ～ 1
const gap = 0.02;  // ← この値を増減する ⭐️
const bufLeft  = makeQuadBuffer(-1 - gap, 0); //　⭐️ 
const bufRight = makeQuadBuffer( gap/2 , 1); //　⭐️

const stride = 4 * Float32Array.BYTES_PER_ELEMENT;

function bindBuffer(buf) {
  gl.bindBuffer(gl.ARRAY_BUFFER, buf);
  gl.enableVertexAttribArray(aPosLoc);
  gl.vertexAttribPointer(aPosLoc, 2, gl.FLOAT, false, stride, 0);
  gl.enableVertexAttribArray(aTexLoc);
  gl.vertexAttribPointer(aTexLoc, 2, gl.FLOAT, false, stride, 2 * Float32Array.BYTES_PER_ELEMENT);
}

// ---- テクスチャ作成 -------------------------------------------
function createTexture(source) {
  const tex = gl.createTexture();
  gl.bindTexture(gl.TEXTURE_2D, tex);
  gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, source);
  gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
  gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
  gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
  gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
  return tex;
}

// ---- 画像を読み込んで左右分割 ---------------------------------
function loadAndSplit(src) {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.src = src;
    img.onload = () => {
      const sliceW = Math.ceil(img.width / 2);
      const sliceH = img.height;
      const slices = [0, 1].map(i => {
        const c = document.createElement('canvas');
        c.width  = sliceW;
        c.height = sliceH;
        c.getContext('2d').drawImage(img, i * sliceW, 0, sliceW, sliceH, 0, 0, sliceW, sliceH);
        return c;
      });
      resolve(slices);
    };
    img.onerror = () => reject(new Error('画像を読み込めませんでした: ' + src));
  });
}

// ---- 行列 -----------------------------------------------------
function buildMatrix(tx, ty, scale) {
  return new Float32Array([
    scale, 0, 0,
    0, scale, 0,
    tx, ty, 1,
  ]);
}

// ---- 描画 -----------------------------------------------------
let texLeft = null, texRight = null;
const state = { tx: 0, ty: 0, scale: 1 };

function render() {
  if (!texLeft || !texRight) return;

  gl.clearColor(0.15, 0.15, 0.15, 1);
  gl.clear(gl.COLOR_BUFFER_BIT);

  const mat = buildMatrix(state.tx, state.ty, state.scale);
  gl.uniformMatrix3fv(uMatrix, false, mat);
  gl.uniform1i(uTexture, 0);

  // 左テクスチャを左半分に描画
  gl.activeTexture(gl.TEXTURE0);
  gl.bindTexture(gl.TEXTURE_2D, texLeft);
  bindBuffer(bufLeft);
  gl.drawArrays(gl.TRIANGLES, 0, 6);

  // 右テクスチャを右半分に描画
  gl.bindTexture(gl.TEXTURE_2D, texRight);
  bindBuffer(bufRight);
  gl.drawArrays(gl.TRIANGLES, 0, 6);
}

// ---- エントリーポイント ---------------------------------------
(async () => {
  try {
    info.textContent = `MAX_TEXTURE_SIZE: ${maxTex}px | DPR: ${devicePixelRatio} | 読み込み中...`;

    const slices = await loadAndSplit('./canvas_005_6x4K.png'); // ⭐️ファイル名

    texLeft  = createTexture(slices[0]);
    texRight = createTexture(slices[1]);

    info.textContent = `MAX_TEXTURE_SIZE: ${maxTex}px | DPR: ${devicePixelRatio} | ${slices[0].width}×${slices[0].height}px × 2`;

    setupInteraction(canvas, state, render);
    render();

  } catch (e) {
    info.textContent = '❌ ' + e.message;
  }
})();