// -----------------------------------------------
// main_webGPU.js — WebGPU初期化・テクスチャ・描画ループ
// WebGL版 main.js からの移植
// -----------------------------------------------

import { SHADER_SRC } from './shader_webGPU.js';
import { setupInteraction } from './interaction.js'; // ← WebGL版と共通でそのまま使用

const canvas = document.getElementById('gpucanvas');
const info   = document.getElementById('info');


// ================================================================
// 1. WebGPU 対応チェック & 初期化
// ================================================================

if (!navigator.gpu) {
  info.textContent = '❌ WebGPU が使えません（ブラウザ非対応）';
  throw new Error('WebGPU not supported');
}

// compatibilityMode（互換モード）を優先して取得
// Fire HD8 のように互換モードのみ対応の端末でも動作する
const adapter = await navigator.gpu.requestAdapter({ compatibilityMode: true })
             ?? await navigator.gpu.requestAdapter();  // フォールバック

if (!adapter) {
  info.textContent = '❌ WebGPU アダプターが見つかりません';
  throw new Error('No WebGPU adapter');
}

const device = await adapter.requestDevice();

// キャンバスの WebGPU コンテキスト取得
const context = canvas.getContext('webgpu');
const format  = navigator.gpu.getPreferredCanvasFormat(); // rgba8unorm など

// デバイス情報を表示
const maxTex = device.limits.maxTextureDimension2D;
const adapterInfo = await adapter.requestAdapterInfo?.() ?? {};
info.textContent = `WebGPU ✅ | MAX_TEXTURE: ${maxTex}px | vendor: ${adapterInfo.vendor ?? 'arm'} | format: ${format}`;


// ================================================================
// 2. キャンバスサイズ設定
// ================================================================

function resizeCanvas() {
  const dpr = window.devicePixelRatio || 1;
  canvas.width  = canvas.clientWidth  * dpr;
  canvas.height = canvas.clientHeight * dpr;

  // WebGPU のコンテキスト設定（リサイズのたびに再設定が必要）
  context.configure({
    device,
    format,
    alphaMode: 'opaque',
  });
}
resizeCanvas();
window.addEventListener('resize', () => { resizeCanvas(); render(); });


// ================================================================
// 3. 頂点バッファ
// WebGL版と同じ配置：xy（クリップ空間）+ uv（テクスチャ座標）
// ================================================================

//   頂点座標（クリップ空間）  UV座標
const vertices = new Float32Array([
  -1, -1,   0, 1,   // 左下
   1, -1,   1, 1,   // 右下
  -1,  1,   0, 0,   // 左上
   1, -1,   1, 1,   // 右下
   1,  1,   1, 0,   // 右上
  -1,  1,   0, 0,   // 左上
]);

const vertexBuffer = device.createBuffer({
  size:  vertices.byteLength,
  usage: GPUBufferUsage.VERTEX | GPUBufferUsage.COPY_DST,
});
device.queue.writeBuffer(vertexBuffer, 0, vertices);


// ================================================================
// 4. ユニフォームバッファ（変換行列）
// WebGL の uniformMatrix3fv に相当
// mat3x3<f32> = 9floats だが WebGPU は 16バイトアライメントのため
// mat3x3 は実際には 48バイト（各行が vec4 として扱われる）
// ================================================================

const uniformBuffer = device.createBuffer({
  size:  48,  // mat3x3<f32>: 3行 × 16バイト = 48バイト
  usage: GPUBufferUsage.UNIFORM | GPUBufferUsage.COPY_DST,
});

// mat3x3 を WebGPU のメモリレイアウト（各行16バイト）で書き込む
function writeMatrix(tx, ty, scale) {
  const data = new Float32Array(12); // 3行 × 4floats（16バイト）
  // 行0: [scale, 0, 0, _padding_]
  data[0] = scale; data[1] = 0;     data[2] = 0;  data[3] = 0;
  // 行1: [0, scale, 0, _padding_]
  data[4] = 0;     data[5] = scale; data[6] = 0;  data[7] = 0;
  // 行2: [tx, ty, 1, _padding_]
  data[8] = tx;    data[9] = ty;    data[10] = 1; data[11] = 0;
  device.queue.writeBuffer(uniformBuffer, 0, data);
}


// ================================================================
// 5. テクスチャ読み込み
// ================================================================

async function loadTexture(src) {
  const img = new Image();
  img.src = src;

  await new Promise((resolve, reject) => {
    img.onload  = resolve;
    img.onerror = reject;
  });

  let source = img;

  // MAX_TEXTURE_SIZE チェック：超えていたらリサイズ
  if (img.width > maxTex || img.height > maxTex) {
    info.textContent += '  ⚠️ 画像をリサイズしました（端末の上限超過）';
    const tmp = document.createElement('canvas');
    tmp.width  = Math.min(img.width,  maxTex);
    tmp.height = Math.min(img.height, maxTex);
    tmp.getContext('2d').drawImage(img, 0, 0, tmp.width, tmp.height);
    source = tmp;
  }

  // ImageBitmap 経由でテクスチャに転送
  const bitmap = await createImageBitmap(source);

  const texture = device.createTexture({
    size:   [bitmap.width, bitmap.height, 1],
    format: 'rgba8unorm',
    usage:  GPUTextureUsage.TEXTURE_BINDING
          | GPUTextureUsage.COPY_DST
          | GPUTextureUsage.RENDER_ATTACHMENT,
  });

  device.queue.copyExternalImageToTexture(
    { source: bitmap },
    { texture },
    [bitmap.width, bitmap.height],
  );

  bitmap.close();
  return texture;
}

// サンプラー（LINEAR フィルタリング）
const sampler = device.createSampler({
  magFilter: 'linear',
  minFilter: 'linear',
});


// ================================================================
// 6. シェーダー & パイプライン
// ================================================================

const shaderModule = device.createShaderModule({ code: SHADER_SRC });

const pipeline = device.createRenderPipeline({
  layout: 'auto',
  vertex: {
    module:     shaderModule,
    entryPoint: 'vs_main',
    buffers: [{
      arrayStride: 4 * Float32Array.BYTES_PER_ELEMENT, // xy + uv = 4floats
      attributes: [
        { shaderLocation: 0, offset: 0,                              format: 'float32x2' }, // position
        { shaderLocation: 1, offset: 2 * Float32Array.BYTES_PER_ELEMENT, format: 'float32x2' }, // texCoord
      ],
    }],
  },
  fragment: {
    module:     shaderModule,
    entryPoint: 'fs_main',
    targets: [{ format }],
  },
  primitive: { topology: 'triangle-list' },
});


// ================================================================
// 7. バインドグループ（ユニフォーム + テクスチャ + サンプラー）
// ================================================================

// テクスチャ読み込み
let gpuTexture;
try {
  gpuTexture = await loadTexture('./canvas_003_4x4K.png'); // ⭐️背景画像の指定⭐️
} catch (e) {
  info.textContent = '❌ 画像を読み込めませんでした: ./canvas_003_4x4K.png';
  throw e;
}

const bindGroup = device.createBindGroup({
  layout: pipeline.getBindGroupLayout(0),
  entries: [
    { binding: 0, resource: { buffer: uniformBuffer } },
    { binding: 1, resource: gpuTexture.createView() },
    { binding: 2, resource: sampler },
  ],
});


// ================================================================
// 8. 状態（移動・スケール）& インタラクション
// interaction.js は WebGL版と共通・変更なし
// ================================================================

const state = { tx: 0, ty: 0, scale: 1 };
setupInteraction(canvas, state, render);


// ================================================================
// 9. 描画ループ
// ================================================================

function render() {
  // 行列をユニフォームバッファに書き込む
  writeMatrix(state.tx, state.ty, state.scale);

  // コマンドエンコーダー（WebGLのdrawArraysに相当する一連の命令）
  const encoder = device.createCommandEncoder();

  const pass = encoder.beginRenderPass({
    colorAttachments: [{
      view:       context.getCurrentTexture().createView(),
      clearValue: { r: 0.15, g: 0.15, b: 0.15, a: 1 }, // WebGL版と同じ背景色
      loadOp:     'clear',
      storeOp:    'store',
    }],
  });

  pass.setPipeline(pipeline);
  pass.setBindGroup(0, bindGroup);
  pass.setVertexBuffer(0, vertexBuffer);
  pass.draw(6); // 6頂点（三角形2つ）
  pass.end();

  // GPUに送信して実行
  device.queue.submit([encoder.finish()]);
}

// 初回描画
render();
