// -----------------------------------------------
// main_webGL.js — WebGL初期化・テクスチャ・描画ループ
// -----------------------------------------------

import { VERT_SRC, FRAG_SRC } from './shader_webGL.js';
import { setupInteraction } from './interaction.js';

const canvas = document.getElementById('glcanvas');
const gl = canvas.getContext('webgl');
const info  = document.getElementById('info');

if (!gl) {
  alert('WebGL が使えません');
  throw new Error('WebGL not supported');
}

// ---- デバイス情報を表示 ----------------------------------------
const maxTex = gl.getParameter(gl.MAX_TEXTURE_SIZE);
info.textContent = `MAX_TEXTURE_SIZE: ${maxTex}px | DPR: ${devicePixelRatio}`;

// ---- キャンバスサイズ（高精細対応）-----------------------------
function resizeCanvas() {
  const dpr = window.devicePixelRatio || 1;
  canvas.width  = canvas.clientWidth  * dpr;
  canvas.height = canvas.clientHeight * dpr;
  gl.viewport(0, 0, canvas.width, canvas.height);
}
resizeCanvas();
window.addEventListener('resize', () => { resizeCanvas(); render(); });


// ---- シェーダーコンパイル & リンク ----------------------------
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


// ---- バッファ：画像全体を覆う四角形（2つの三角形）--------------
//   頂点座標（クリップ空間）    UV座標
const vertices = new Float32Array([
  -1, -1,   0, 1,   // 左下
   1, -1,   1, 1,   // 右下
  -1,  1,   0, 0,   // 左上
   1, -1,   1, 1,   // 右下
   1,  1,   1, 0,   // 右上
  -1,  1,   0, 0,   // 左上
]);

const buf = gl.createBuffer();
gl.bindBuffer(gl.ARRAY_BUFFER, buf);
gl.bufferData(gl.ARRAY_BUFFER, vertices, gl.STATIC_DRAW);

const stride = 4 * Float32Array.BYTES_PER_ELEMENT; // xyzw ではなく xy+uv = 4floats

const aPosLoc = gl.getAttribLocation(prog, 'a_position');
gl.enableVertexAttribArray(aPosLoc);
gl.vertexAttribPointer(aPosLoc, 2, gl.FLOAT, false, stride, 0);

const aTexLoc = gl.getAttribLocation(prog, 'a_texCoord');
gl.enableVertexAttribArray(aTexLoc);
gl.vertexAttribPointer(aTexLoc, 2, gl.FLOAT, false, stride, 2 * Float32Array.BYTES_PER_ELEMENT);


// ---- テクスチャ読み込み ----------------------------------------
const texture = gl.createTexture();

const img = new Image();
img.src = './canvas_003_4x4K.png'; // ⭐️⭐️背景画像の指定⭐️⭐️

img.onload = () => {
  let source = img;

  // MAX_TEXTURE_SIZE チェック：超えていたら Canvas でリサイズ
  if (img.width > maxTex || img.height > maxTex) {
    info.textContent += '  ⚠️ 画像をリサイズしました（端末の上限超過）';
    const tmp = document.createElement('canvas');
    tmp.width  = Math.min(img.width,  maxTex);
    tmp.height = Math.min(img.height, maxTex);
    tmp.getContext('2d').drawImage(img, 0, 0, tmp.width, tmp.height);
    source = tmp;
  }

  gl.bindTexture(gl.TEXTURE_2D, texture);
  gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, source);
  gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
  gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
  gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
  gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);

  render();
};

img.onerror = () => {
  info.textContent = '❌ 画像を読み込めませんでした：' + img.src;
};


// ---- 状態（移動・スケール）------------------------------------
const state = { tx: 0, ty: 0, scale: 1 };

// 操作イベント登録
setupInteraction(canvas, state, render);


// ---- 行列計算（移動 × スケール）-------------------------------
//  mat3 = [ sx,  0,  0 ]
//         [  0, sy,  0 ]
//         [ tx, ty,  1 ]
function buildMatrix(tx, ty, scale) {
  // WebGL は column-major なので転置して渡す
  return new Float32Array([
    scale,  0,  0,
    0,  scale,  0,
    tx,     ty, 1,
  ]);
}


// ---- 描画 -----------------------------------------------------
const uMatrix  = gl.getUniformLocation(prog, 'u_matrix');
const uTexture = gl.getUniformLocation(prog, 'u_texture');

function render() {
  gl.clearColor(0.15, 0.15, 0.15, 1);
  gl.clear(gl.COLOR_BUFFER_BIT);

  gl.uniformMatrix3fv(uMatrix, false, buildMatrix(state.tx, state.ty, state.scale));
  gl.uniform1i(uTexture, 0);
  gl.drawArrays(gl.TRIANGLES, 0, 6);
}
