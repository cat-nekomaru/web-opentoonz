// -----------------------------------------------
// shader_webGPU.js — WGSLシェーダー（WebGPU用）
// WebGL の GLSL から WebGPU の WGSL へ移植
// -----------------------------------------------

// 頂点シェーダー + フラグメントシェーダー を1つのWGSLモジュールにまとめる
// （WebGPUはGLSLではなくWGSLを使用）

export const SHADER_SRC = /* wgsl */`

  // ---- ユニフォーム（移動・スケール行列）-----------------------
  // WebGL の uniformMatrix3fv に相当
  // WebGPU では "バインドグループ" 経由で渡す
  struct Uniforms {
    matrix : mat3x3<f32>,   // 3x3 変換行列（移動・スケール）
  };
  @group(0) @binding(0) var<uniform> uni : Uniforms;

  // ---- テクスチャ & サンプラー ---------------------------------
  @group(0) @binding(1) var uTexture : texture_2d<f32>;
  @group(0) @binding(2) var uSampler : sampler;


  // ---- 頂点シェーダー ------------------------------------------
  struct VertexIn {
    @location(0) position : vec2<f32>,  // クリップ空間座標 (-1〜+1)
    @location(1) texCoord : vec2<f32>,  // UV座標 (0〜1)
  };

  struct VertexOut {
    @builtin(position) clipPos : vec4<f32>,
    @location(0)       uv      : vec2<f32>,
  };

  @vertex
  fn vs_main(in: VertexIn) -> VertexOut {
    // mat3x3 で移動・スケール変換
    let pos = uni.matrix * vec3<f32>(in.position, 1.0);

    var out: VertexOut;
    out.clipPos = vec4<f32>(pos.xy, 0.0, 1.0);
    out.uv      = in.texCoord;
    return out;
  }


  // ---- フラグメントシェーダー ----------------------------------
  @fragment
  fn fs_main(in: VertexOut) -> @location(0) vec4<f32> {
    return textureSample(uTexture, uSampler, in.uv);
  }
`;
